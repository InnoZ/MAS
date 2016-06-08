package innoz.io.database;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.io.BbsrDataReader;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Building;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.WayEntry;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.utils.matsim.QuadTree;
import innoz.utils.osm.OsmKey2ActivityType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.jfree.util.Log;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * This class uses a {@link java.sql.Connection} to retrieve data from a database.
 * There are methods for getting administrative borders data and OpenStreetMap data.
 * 
 * @author dhosse
 *
 */
public class DatabaseReader {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(DatabaseReader.class);
	private final GeometryFactory gFactory;
	private final WKTReader wktReader;
	private final Geoinformation geoinformation;
	/////////////////////////////////////////////////////////////////////////////////////////

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Geometry boundingBox;
	private CoordinateTransformation ct;
	private int counter = 0;
	private List<Building> buildings;
	private QuadTree<Building> buildingsQuadTree;
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 
	 * Constructor.
	 * 
	 * @param geoinformation The geoinformation container.
	 */
	public DatabaseReader(final Geoinformation geoinformation){
		
		// Initialize all final fields
		this.gFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue));
		this.wktReader = new WKTReader();
		this.geoinformation = geoinformation;
		
	}
	
	/**
	 * Imports administrative borders and OpenStreetMap data from the mobility database.
	 * 
	 * 
	 * @param configuration The configuration for the scenario generation process.
	 * @param surveyAreaIdsString The survey area id(s).
	 * @param vicinityIdsString The vicinity area id(s).
	 * @param scenario The MATSim scenario.
	 */
	public void readGeodataFromDatabase(Configuration configuration, String surveyAreaIdsString, 
			String vicinityIdsString, Scenario scenario) {
		
		try {
			
			// Create a postgresql database connection
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" +
					configuration.getLocalPort() + "/geodata", configuration.getDatabaseUsername(),
					configuration.getDatabasePassword());
			
			if(connection != null){

				Log.info("Successfully connected with geodata database...");
				
				// Read the administrative borders that have one of the specified ids
				// TODO Vicinity... /dhosse 05/16
				this.readAdminBorders(connection, configuration, surveyAreaIdsString, vicinityIdsString);
				
				// If no administrative units were created, we are unable to proceed
				// The process would probably finish, but no network or population would be created
				if(this.geoinformation.getAdminUnits().size() < 1){
				
					Log.error("No administrative boundaries were created!");
					Log.error("Maybe the ids you specified don't exist in the database.");
					throw new RuntimeException("Execution aborts...");
					
				}
				
				new BbsrDataReader().read(this.geoinformation);
				
				for(AdminUnitEntry entry : configuration.getAdminUnitEntries()){

					String id = entry.getId().startsWith("0") ? entry.getId().substring(1) : entry.getId();
					
					District d = this.geoinformation.getAdminUnits().get(id);
					
					if(d != null){
						
						d.setnHouseholds(entry.getNumberOfHouseholds());
						
						for(AdministrativeUnit au : this.geoinformation.getAdminUnits().get(id).getAdminUnits().values()){
							
							if(au.getId().startsWith(id)){
								
								au.setNumberOfHouseholds(entry.getNumberOfHouseholds());
								
							}
							
						}
						
					}
					
				}
				
				// Otherwise, read in the OSM data
				this.readOsmData(connection, configuration);
				
			}
			
			// Close the connection when everything's done.
			connection.close();
			
			Log.info("Done.");

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException | 
				MismatchedDimensionException | FactoryException | ParseException | TransformException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * This method reads administrative units from a database and puts them into the {@link Geoinformation}
	 * object. Only the administrative units with the specified ids are taken into account.
	 * 
	 * @param connection The database connection
	 * @param configuration The configuration parameters.
	 * @param surveyAreaIds The ids of the administrative border we want to have the geometries of.
	 * 
	 * @throws SQLException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws ParseException
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	private void readAdminBorders(Connection connection, Configuration configuration,
			String surveyAreaIdsString, String vicinityIdsString) throws SQLException,
			NoSuchAuthorityCodeException, FactoryException, ParseException,
			MismatchedDimensionException, TransformException {

		log.info("Reading administrative borders from database...");

		// A collection to temporarily store all geometries
		List<Geometry> geometryCollection = new ArrayList<Geometry>();
		
		getAndAddGeodataFromIdSet(connection, configuration, CollectionUtils.stringToSet(
				surveyAreaIdsString), geometryCollection, true);
		this.geoinformation.setSurveyAreaBoundingBox(gFactory.buildGeometry(geometryCollection)
				.getEnvelope());
		if(vicinityIdsString != null){
			getAndAddGeodataFromIdSet(connection, configuration, CollectionUtils.stringToSet(
					vicinityIdsString), geometryCollection, false);
			this.geoinformation.setVicinityBoundingBox(gFactory.buildGeometry(geometryCollection)
					.getEnvelope());
		}
		
		// This is needed to transform the WGS84 geometries into the specified CRS
		MathTransform t = CRS.findMathTransform(CRS.decode(Geoinformation.AUTH_KEY_WGS84, true),
				CRS.decode(configuration.getCrs(), true));
		
		// Get the survey area by building the bounding box of all geometries 
		this.geoinformation.setCompleteGeometry(gFactory.buildGeometry(geometryCollection)
				.getEnvelope());
		
		this.boundingBox = JTS.transform((Geometry) this.geoinformation.getCompleteGeometry()
				.clone(), t).getEnvelope();
		
	}
	
	private void getAndAddGeodataFromIdSet(Connection connection, Configuration configuration,
			Set<String> ids, List<Geometry> geometryCollection, boolean surveyArea)
					throws SQLException, NoSuchAuthorityCodeException, FactoryException,
					ParseException, MismatchedDimensionException, TransformException{
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		
		// Append all ids inside the given collection to a string
		for(String id : ids){

			if(i < ids.size() - 1){
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%' OR");
				
			} else {
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%'");
				
			}
			
			i++;
			
		}
		
		// Execute the query and store the returned valued inside a set.
		String q = "select " + DatabaseConstants.BLAND + "," + DatabaseConstants.MUN_KEY + ", cca_2,"
				+ DatabaseConstants.functions.st_astext.name() + "(" + DatabaseConstants.ATT_GEOM +
				")" + " from " + DatabaseConstants.schemata.gadm.name() + "." +
				DatabaseConstants.tables.districts.name() + " where" + builder.toString();
		ResultSet set = statement.executeQuery(q);

		// Go through all the results
		while(set.next()){
			
			//TODO attributes have to be added to the table
			String key = set.getString(DatabaseConstants.MUN_KEY);
			if(key.startsWith("0")) key = key.substring(1);
			String g = set.getString(DatabaseConstants.functions.st_astext.name());
			int bland = set.getInt(DatabaseConstants.BLAND);
			String district = set.getString("cca_2");
			if(district != null){
				if(district.startsWith("0")) district = district.substring(1);
			}
			
			// Check if the wkb string returned is neither null nor empty, otherwise this would
			// crash
			if(g != null){
				
				if(!g.isEmpty()){
					
					// Create a new administrative unit and its geometry and add it to the
					// geoinformation
					AdministrativeUnit au = new AdministrativeUnit(key);
					Geometry geometry = wktReader.read(g);
					au.setGeometry(geometry);
					au.setBland((int)bland);

					if(district != null){

						if(!this.geoinformation.getAdminUnits().containsKey(district)){
							this.geoinformation.getAdminUnits().put(district,
									new District(district));
						}
						this.geoinformation.getAdminUnits().get(district).getAdminUnits()
							.put(key, au);
						
					}
					
					this.geoinformation.addSubUnit(au);
					
					// Store all geometries inside a collection to get the survey area geometry in
					// the end
					geometryCollection.add(au.getGeometry());
					
				}
				
			}
			
		}
		
		// Close the result set and the statement
		set.close();
		statement.close();
		
	}
	
	/**
	 * 
	 * This method reads OpenStreetMap landuse data from a database and puts it into the 
	 * {@link Geoinformation} object.
	 * 
	 * @param connection The database connection
	 * @param configuration The configuration parameters.
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 */
	private void readOsmData(Connection connection, Configuration configuration)
			throws NoSuchAuthorityCodeException, FactoryException{

		final CoordinateReferenceSystem fromCRS = CRS.decode(Geoinformation.AUTH_KEY_WGS84, true);
		final CoordinateReferenceSystem toCRS = CRS.decode(configuration.getCrs(), true);
		
		this.ct = TransformationFactory.getCoordinateTransformation(fromCRS.toString(),
				toCRS.toString());
		
		log.info("Reading osm data...");
		
		try {
			
			this.buildings = new ArrayList<Building>();

			// If buildings should be considered, also read their geometries
			if(configuration.isUsingBuildings()){
				
				readBuildings(connection);
				
			}

			// Read amenity geometries
			readAmenities(connection);
			
			// Read landuse geometries
			readLanduseData(connection);
			
			if(configuration.isUsingBuildings()){
				
				for(AdministrativeUnit au : this.geoinformation.getSubUnits().values()){
					
					au.getLanduseGeometries().clear();
					
				}
				
				this.geoinformation.getQuadTreeForActType(ActivityTypes.SUPPLY).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.KINDERGARTEN).clear();
				this.geoinformation.getQuadTreeForActType("residential").clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.WORK).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.SHOPPING).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.OTHER).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.LEISURE).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.EDUCATION).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.EATING).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.CULTURE).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.SPORTS).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.FURTHER).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.EVENT).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.HEALTH).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.SERVICE).clear();
				this.geoinformation.getQuadTreeForActType(ActivityTypes.ERRAND).clear();
			
				for(Building b : this.buildings){
					
					for(String actType : b.getActivityOptions()){
						if(actType != null){
							addGeometry(actType, b.getGeometry());
						}
					}
					
				}
				
			}
			
			log.info("Done.");
			
		} catch (SQLException | ParseException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * Reads OpenStreetMap landuse data from a database.
	 * 
	 * @param connection The database connection
	 */
	private void readLanduseData(Connection connection){
		
		log.info("Reading in landuse data...");
		
		try {

			// Parse polygons for landuse data
			getPolygonBasedLanduseData(connection);
		
		} catch (SQLException | ParseException e) {

			e.printStackTrace();
			
		}
		
		log.info("...done");
		
	}

	/**
	 * 
	 * Retrieves polygon OpenStreetMap data containing a landuse key.
	 * 
	 * @param connection The database connection
	 * @throws ParseException
	 * @throws SQLException
	 */
	private void getPolygonBasedLanduseData(Connection connection) throws ParseException,
		SQLException {
		
		log.info("Processing polygon landuse data...");
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		// Execute the query and store the returned valued inside a set.
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_LANDUSE + ", "
				+ DatabaseConstants.ATT_AMENITY +  ", " + DatabaseConstants.ATT_LEISURE + ", "
				+ DatabaseConstants.ATT_SHOP + ", " + DatabaseConstants.functions.st_astext.name()
				+ "(" + DatabaseConstants.ATT_WAY + ") from " + DatabaseConstants.schemata.osm
				.name() + "." + DatabaseConstants.tables.osm_polygon.name() + " where "
				+ DatabaseConstants.functions.st_within.name() + "(" + DatabaseConstants.ATT_WAY
				+ ", " + DatabaseConstants.functions.st_geomfromtext.name() + "('"
				+ this.geoinformation.getCompleteGeometry().toString() + "', 4326)) and ("
				+ DatabaseConstants.ATT_LANDUSE + " is not null" + " or " + DatabaseConstants
				.ATT_AMENITY + " is not null or " + DatabaseConstants.ATT_LEISURE + " is not null"
				+ " or " + DatabaseConstants.ATT_SHOP + " is not null) and " 
				+ DatabaseConstants.ATT_BUILDING + " is null;");
		
		// Go through all results
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.functions.st_astext
					.name()));
			String landuse = set.getString(DatabaseConstants.ATT_LANDUSE);
			String amenity = set.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = set.getString(DatabaseConstants.ATT_LEISURE);
			String shop = set.getString(DatabaseConstants.ATT_SHOP);
			
			// Set the landuse type by checking the amenity, leisure and shop tags
			if(amenity != null){
				
				landuse = amenity;
				
			} else if(leisure != null){
				
				landuse = leisure;
				
			} else if(shop != null){
				
				landuse = shop;
				
			}

			// Convert the osm landuse tag into a MATSim activity type
			landuse = getLanduseType(landuse);
			
			if(landuse != null){
				
				// Add the landuse geometry to the geoinformation if we have a valid activity option for it
				addGeometry(landuse, geometry);
				
				for(Building b : this.buildings){

					if(b.getActivityOptions().isEmpty()){

						if(geometry.contains(b.getGeometry())){
							
							b.addActivityOption(landuse);
							
							if(!landuse.startsWith(ActivityTypes.LEISURE) && !landuse.equals("residential")){
							
								b.addActivityOption(ActivityTypes.WORK);
							
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		// Close everything in the end
		set.close();
		statement.close();
		
	}
	
	/**
	 * 
	 * Retrieves landuse data stored in OSM way elements.
	 * 
	 * @param connection The database connection
	 * @throws SQLException
	 */
	@SuppressWarnings("unused")
	private void getWayBasedLanduseData(Connection connection) throws SQLException{
		
		log.info("Processing way landuse data...");
		
		Statement statement = connection.createStatement();
		
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_OSM_ID + ", "
				+ DatabaseConstants.ATT_NODES + ", " + DatabaseConstants.ATT_TAGS + " from " +
				DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_ways
				.name() + " where " + DatabaseConstants.ATT_TAGS + " @> ARRAY['" +
				DatabaseConstants.ATT_LANDUSE + "'];");
		
		while(set.next()){
			
			String[] tags = (String[])set.getArray(DatabaseConstants.ATT_TAGS).getArray();
			
			String landuse = null;
			int k = 0;
			
			do{
				
				landuse = getLanduseType(tags[k]);
				k++;
				
			} while(landuse == null && k < tags.length);
			
			if(landuse != null){

				StringBuilder sb = new StringBuilder();
				
				Long[] nodes = (Long[])set.getArray(DatabaseConstants.ATT_NODES).getArray();
				
				for(int i = 0; i < nodes.length; i++){
					
					if(i < nodes.length - 1){
						
						sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + nodes[i] + "' or");
						
					} else{
						
						sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + nodes[i] + "'");
						
					}
					
				}
				
				LinearRing p = createLinearRing(connection, sb.toString(), nodes);
				
				if(p != null){
					
					Geometry polygon = gFactory.createPolygon(p);
					
					if(polygon != null){
						
						addGeometry(landuse, polygon);
						
					}
					
				}
				
			}
			
		}
		
		set.close();
		statement.close();
			
	}
	
	@SuppressWarnings("unused")
	private void getRelationBasedLanduseData(Connection connection) throws SQLException {
		
		log.info("Processing relation landuse data...");
		
		// Parse relations for landuse tags to get multipolygon geometries
		Statement statement = connection.createStatement();
			
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_PARTS + ", "
				+ DatabaseConstants.ATT_MEMBERS + ", " + DatabaseConstants.ATT_TAGS + " from "
				+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_rels
				.name() + " where " + DatabaseConstants.ATT_TAGS + " @> ARRAY['"
				+ DatabaseConstants.ATT_LANDUSE + "'];");
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long[] parts = (Long[])set.getArray(DatabaseConstants.ATT_PARTS).getArray();
			String[] tags = (String[])set.getArray(DatabaseConstants.ATT_TAGS).getArray();
			String[] members = (String[])set.getArray(DatabaseConstants.ATT_MEMBERS).getArray();
			
			for(int i = 0; i < parts.length; i++){
				
				if(i < parts.length - 1){
					sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + parts[i] + "' or");
				} else {
					sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + parts[i] + "'");
				}
				
			}
			
			String landuse = null;
			int k = 0;
			
			do{
				
				landuse = getLanduseType(tags[k]);
				k++;
				
			} while(landuse == null && k < tags.length);
			
			if(landuse != null){

				Map<Long, LinearRing> linearRings = getAffiliatedWays(connection, sb.toString());

				Geometry current = null;
				
				for(int i = 1; i < members.length; i += 2){
					
					int idx = (int)Math.floor(i/2);
					Long id = parts[idx];
					
					current = gFactory.createPolygon(linearRings.get(id));

					addGeometry(landuse, current);
					
				}
				
			}
			
		}
		
		set.close();
		statement.close();
			
	}
	
	private Map<Long,LinearRing> getAffiliatedWays(Connection connection, String arguments)
			throws SQLException{

		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select * from " + DatabaseConstants.schemata.osm
				.name() + "." + DatabaseConstants.tables.osm_ways.name() + " where" + arguments);
		
		Map<Long, LinearRing> linearRingSet = new HashMap<>();
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long id = set.getLong(DatabaseConstants.ATT_OSM_ID);
			
			Long[] nodes = (Long[])set.getArray(DatabaseConstants.ATT_NODES).getArray();
			
			for(int i = 0; i < nodes.length; i++){
				
				if(i < nodes.length - 1){
					
					sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + nodes[i] + "' or");
					
				} else{
					
					sb.append(" " + DatabaseConstants.ATT_OSM_ID + " = '" + nodes[i] + "'");
					
				}
				
			}

			LinearRing p = createLinearRing(connection, sb.toString(),nodes);
			if(p != null){
				linearRingSet.put(id, p);
			}
			
		}

		return linearRingSet;
		
	}
	
	private LinearRing createLinearRing(Connection connection, String arguments, Long[] nodes)
			throws SQLException{
		
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select * from " + DatabaseConstants.schemata.osm
				.name() + "." + DatabaseConstants.tables.osm_nodes.name() + " where" + arguments);
		
		Coordinate[] coordinates = new Coordinate[nodes.length];
		
		//in case one or more coordinates are contained several times,
		//we need to store them in order to access them again
		Map<Long, Coordinate> coordMap = new HashMap<>();
		
		while(set.next()){
			
			Long id = set.getLong(DatabaseConstants.ATT_OSM_ID);
			
			int idx = 0;
			for(int i = 0; i < nodes.length; i++){
				Long curr = nodes[i];
				if(curr.equals(id)){
					
					idx = i;
					break;
					
				}
				
			}
			
			double lon = set.getDouble(DatabaseConstants.ATT_LON)/Math.pow(10, 7);
			double lat = set.getDouble(DatabaseConstants.ATT_LAT)/Math.pow(10, 7);
			coordinates[idx] = new Coordinate(lon, lat);
			coordMap.put(id, coordinates[idx]);
			
		}
		
		coordinates[coordinates.length-1] = coordinates[0];
		
		for(int i = 0; i < coordinates.length; i++){
			if(coordinates[i] == null){
				coordinates[i] = coordMap.get(nodes[i]);
			}
		}
		
		return coordinates.length >= 4 ? gFactory.createLinearRing(coordinates) : null;
		
	}
	
	/**
	 * 
	 * Creates a MATSim activity type from a given OSM landuse tag.
	 * 
	 * @param landuseTag The tag for the landuse.
	 * @return An activity type that can be used in MATSim.
	 */
	private static String getLanduseType(String landuseTag){
		
		if(landuseTag.equals("college") || landuseTag.equals("school") || landuseTag.equals(
				"university")){
			
			return ActivityTypes.EDUCATION;
			
		} else if(landuseTag.equals("commercial") || landuseTag.equals("industrial")){
			
			return ActivityTypes.WORK;
			
		} else if(landuseTag.equals("hospital")){
			
			return ActivityTypes.OTHER;
			
		} else if(landuseTag.equals("recreation_ground") || landuseTag.equals("park")
				|| landuseTag.equals("village_green")){
			
			return ActivityTypes.LEISURE;
			
		} else if(landuseTag.equals("residential")){
			
			return "residential";
			
		} else if(landuseTag.equals("retail")){
			
			return ActivityTypes.SHOPPING;
			
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * Retrieves amenities and adds them to the geoinformation.
	 * 
	 * @param connection The database connection
	 * @throws SQLException
	 * @throws ParseException
	 */
	private void readAmenities(Connection connection) throws SQLException, ParseException{
		
		log.info("Reading in amenities...");

		// Create a statement and execute an SQL query to retrieve all amenities that have a tag
		// containing a shopping, leisure or any other activity.
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.functions.st_astext.name() + "(" + DatabaseConstants.ATT_WAY
				+ "), "	+ DatabaseConstants.ATT_AMENITY + ", " + DatabaseConstants.ATT_LEISURE + ", " + DatabaseConstants.ATT_SHOP + " from "
				+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_point.name() + " where "
				+ DatabaseConstants.functions.st_within + "(" + DatabaseConstants.ATT_WAY + "," + DatabaseConstants.functions.st_geomfromtext.name()
				+ "('" + this.geoinformation.getCompleteGeometry().toString() + "',4326)) and (" + DatabaseConstants.ATT_AMENITY
				+ " is not null or " + DatabaseConstants.ATT_LEISURE + " is not null or " + DatabaseConstants.ATT_SHOP + " is not null)");
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.functions.st_astext.name()));
			
			String amenity = set.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = set.getString(DatabaseConstants.ATT_LEISURE);
			String shop = set.getString(DatabaseConstants.ATT_SHOP);
			
			String landuse = null;
			
			// Set the landuse type by checking the amenity, leisure and shop tags
			if(amenity != null){
				
				landuse = amenity;
				
			} else if(leisure != null){
				
				landuse = leisure;
				
			} else if(shop != null){
				
				landuse = shop;
				
			}

			// Convert the OSM landuse tag into a MATSim activity type
			String actType = getAmenityType(landuse);
			
			if(actType != null){

				// Add the landuse geometry to the geoinformation if we have a valid activity option for it
				addGeometry(actType, geometry);
				
				Building closest = this.buildingsQuadTree.getClosest(geometry.getCentroid().getX(), geometry.getCentroid().getY());
				if(closest != null){
					closest.addActivityOption(actType);
				}
				
			}
			
		}
		
		// Close everything in the end
		set.close();
		statement.close();
		
	}
	
	/**
	 * 
	 * Adds a landuse geometry to the geoinformationo container.
	 * 
	 * @param landuse The MATSim activity option that can be performed at this location.
	 * @param g The geometry of the activity location.
	 */
	private void addGeometry(String landuse, Geometry g){
		
		// Check if the geometry is not null
		if(g != null){
			
			// Check if the geometry is valid (e.g. not intersecting itself)
			if(g.isValid()){

				for(AdministrativeUnit au : this.geoinformation.getSubUnits().values()){

					// Add the landuse geometry to the administrative unit containing it or skip it if it's outside of the survey area
					if(au.getGeometry().contains(g) || au.getGeometry().touches(g) || au.getGeometry().intersects(g)){
						
						au.addLanduseGeometry(landuse, g);
						if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals("residential")){
							au.addLanduseGeometry(ActivityTypes.WORK, g);
						}
						
						// If we don't have a quad tree for this activity type already, create a new one
						if(this.geoinformation.getQuadTreeForActType(landuse) == null){
							
							Coordinate[] coordinates = boundingBox.getCoordinates();
							this.geoinformation.createQuadTreeForActType(landuse, new double[]{coordinates[0].x, coordinates[0].y,
									coordinates[2].x, coordinates[2].y});
							
						}
						if(this.geoinformation.getQuadTreeForActType(ActivityTypes.WORK) == null){
							Coordinate[] coordinates = boundingBox.getCoordinates();
							this.geoinformation.createQuadTreeForActType(ActivityTypes.WORK, new double[]{coordinates[0].x, coordinates[0].y,
									coordinates[2].x, coordinates[2].y});
						}
						
						// Add the landuse geometry's centroid as new quad tree entry
						Coord c = ct.transform(MGC.point2Coord(g.getCentroid()));
						
						if(this.boundingBox.contains(MGC.coord2Point(c))){
							
							this.geoinformation.getQuadTreeForActType(landuse).put(c.getX(), c.getY(), g);
							if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals("residential")){
								this.geoinformation.getQuadTreeForActType(ActivityTypes.WORK).put(c.getX(), c.getY(), g);
							}
							
						}
						
					}
					
				}
					
			} else {
				
				// Warnings counter for invalid geometries
				if(counter <= 5){
					
					log.warn("Invalid geometry! Skipping this entry...");
					
				}
				
				if(counter == 5){
					log.warn(Gbl.FUTURE_SUPPRESSED);
				
				}
				
				counter++;
				
			}
			
		}
		
	}
	
	void readBuildings(Connection connection) throws SQLException, ParseException{
	
		//TODO this method implies we are eventually using facilities in MATSim...
		//alternatively: use buildings to "bound" activities
		log.info("Reading in buildings...");
		
		Coordinate[] coords = this.geoinformation.getCompleteGeometry().getCoordinates();
		
		this.buildingsQuadTree = new QuadTree<Building>(coords[0].x, coords[0].y, coords[2].x, coords[2].y);
		
		WKTReader wktReader = new WKTReader();
		
		Statement statement = connection.createStatement();
		String s = "select name," + DatabaseConstants.ATT_BUILDING + "," + DatabaseConstants.ATT_AMENITY +  ", "
				+ DatabaseConstants.ATT_LEISURE + ", " + DatabaseConstants.ATT_SHOP + ", " + DatabaseConstants.functions.st_astext.name() + "("
				+ DatabaseConstants.ATT_WAY	+ ") from " + DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_polygon
				+ " where " + DatabaseConstants.functions.st_within + "(" + DatabaseConstants.ATT_WAY + ","
				+ DatabaseConstants.functions.st_geomfromtext.name() + "('" + this.geoinformation.getCompleteGeometry().toString() + "',4326))"
				+ " and " + DatabaseConstants.ATT_BUILDING + " is not null";
		ResultSet set = statement.executeQuery(s);
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.functions.st_astext.name()));
			String building = set.getString(DatabaseConstants.ATT_BUILDING);
			
			String type = getTypeOfBuilding(building);
			String amenity = set.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = set.getString(DatabaseConstants.ATT_LEISURE);
			String shop = set.getString(DatabaseConstants.ATT_SHOP);
			
			Building b = new Building(geometry);
			if(type != null){
				b.addActivityOption(type);
			}
			if(amenity != null){
				
				b.addActivityOption(getAmenityType(amenity));
				
			}
			if(leisure != null){
				b.addActivityOption(getAmenityType(leisure));
			}
			if(shop != null){
				b.addActivityOption(getAmenityType(shop));
			}
			
			this.buildings.add(b);
			this.buildingsQuadTree.put(geometry.getCentroid().getX(), geometry.getCentroid().getY(), b);
				
		}
		
	}
	
	@SuppressWarnings("unused")
	private void readPtStops(Connection connection) throws SQLException, ParseException{
		
//		Statement statement = connection.createStatement();
//		ResultSet set = statement.executeQuery("select " + DatabaseConstants.functions.st_astext + "(" + DatabaseConstants.ATT_WAY
//				+ ") from "	+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_point.name() + " where "
//				+ DatabaseConstants.functions.st_within + " (" + DatabaseConstants.ATT_WAY + "," + DatabaseConstants.functions.st_geomfromtext.name()
//				+ "('" + this.geoinformation.getCompleteGeometry().toString() + "',4326));");
//		
//		Set<Geometry> ptStops = new HashSet<>();
//		
//		while(set.next()){
//			
//			Geometry g = wktReader.read(set.getString(DatabaseConstants.functions.st_astext.name()));
//			
//			for(AdministrativeUnit au : this.geoinformation.getSurveyArea().values()){
//				
//				if(au.getGeometry().contains(g)){
//					
//					//TODO set the buffer radius to whatever the search radius of the transit router is...
//					//default is 1000, so we will leave it like this for the time being
//					ptStops.add(g.buffer(1000));
//					
//				}
//				
//			}
//			
//			this.geoinformation.setCatchmentAreaPt(gFactory.buildGeometry(ptStops));
//			
//		}
		
	}
	
	/**
	 * 
	 * Creates a MATSim activity type from a given OSM amenity tag.
	 * 
	 * @param tag The OSM amenity tag.
	 * @return A MATsim activity type.
	 */
	private static String getAmenityType(String tag){
		
		if(OsmKey2ActivityType.education.contains(tag)){
			
			return ActivityTypes.EDUCATION;
			
		} else if(OsmKey2ActivityType.groceryShops.contains(tag) || OsmKey2ActivityType.miscShops.contains(tag) || OsmKey2ActivityType.serviceShops.contains(tag)){
			
			if(OsmKey2ActivityType.groceryShops.contains(tag)){
				
				return ActivityTypes.SUPPLY;
				
			} else if(OsmKey2ActivityType.serviceShops.contains(tag)){
				
				return ActivityTypes.SERVICE;
				
			} else {
				
				return ActivityTypes.SHOPPING;
				
			}
			
		} else if(OsmKey2ActivityType.leisure.contains(tag) || OsmKey2ActivityType.eating.contains(tag) || OsmKey2ActivityType.culture.contains(tag) || OsmKey2ActivityType.sports.contains(tag)
				|| OsmKey2ActivityType.furtherEducation.contains(tag) || OsmKey2ActivityType.events.contains(tag)){
			
			if(OsmKey2ActivityType.eating.contains(tag)){
				
				return ActivityTypes.EATING;
				
			} else if(OsmKey2ActivityType.culture.contains(tag)){
				
				return ActivityTypes.CULTURE;
				
			} else if(OsmKey2ActivityType.sports.contains(tag)){
				
				return ActivityTypes.SPORTS;
				
			} else if(OsmKey2ActivityType.furtherEducation.contains(tag)){
				
				return ActivityTypes.FURTHER;
				
			} else if(OsmKey2ActivityType.events.contains(tag)){
				
				return ActivityTypes.EVENT;
				
			} else {
				
				return ActivityTypes.LEISURE;
				
			}
			
		} else if(OsmKey2ActivityType.otherPlaces.contains(tag) || OsmKey2ActivityType.healthcare.contains(tag) || OsmKey2ActivityType.errand.contains(tag)) {
		
			if(OsmKey2ActivityType.healthcare.contains(tag)){
				
				return ActivityTypes.HEALTH;
				
			} else if(OsmKey2ActivityType.errand.contains(tag)){
				
				return ActivityTypes.ERRAND;
						
			} else {
				
				return ActivityTypes.OTHER;
				
			}
			
		} else if(ActivityTypes.KINDERGARTEN.equals(tag)){
			
			return ActivityTypes.KINDERGARTEN;
			
		} else{
			
			return null;
			
		}
		
	}

	private String getTypeOfBuilding(String buildingTag){
		
		if(buildingTag.equals("apartments") || buildingTag.equals("detached") || buildingTag.equals("house") || buildingTag.equals("semi")
				|| buildingTag.equals("terrace")){
			
			return "residential";
			
		} else if(buildingTag.equals("barn") || buildingTag.equals("brewery") || buildingTag.equals("factory") || buildingTag.equals("office")
				|| buildingTag.equals("warehouse")){
			
			return ActivityTypes.WORK;
			
		} else if(buildingTag.equals("castle") || buildingTag.equals("monument") || buildingTag.equals("palace")){
			
			//TODO tourism
			return "tourism";
			
		} else if(buildingTag.equals("church") || buildingTag.equals("city_hall") || buildingTag.equals("hall")){
			
			return ActivityTypes.OTHER;
			
		} else if(buildingTag.equals("stadium")){
			
			return ActivityTypes.LEISURE;
			
		} else if(buildingTag.equals("store")){
			
			return ActivityTypes.SHOPPING;
			
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * Parses the OSM database for road objects and return them as a set for generating a MATSim network. 
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @return A set of OSM way entries.
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws ParseException
	 */
	public Set<WayEntry> readOsmRoads(final Configuration configuration) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, ParseException{
		
		log.info("Reading osm ways from database...");
		
		// Initialize an empty set
		Set<WayEntry> wayEntries = new HashSet<>();

		// COnnect to the geodata database
		Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
		Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_PREFIX + configuration.getLocalPort() + "/"
				+ DatabaseConstants.GEODATA_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
	
		if(connection != null){

			// Create a new statement and execute an SQL query to retrieve OSM road data
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select " + DatabaseConstants.ATT_OSM_ID + ", " + DatabaseConstants.ATT_ACCESS + ", "
					+ DatabaseConstants.ATT_HIGHWAY + ", " + DatabaseConstants.ATT_JUNCTION + ", " + DatabaseConstants.ATT_ONEWAY + ", "
					+ DatabaseConstants.functions.st_astext.name() + "(" + DatabaseConstants.ATT_WAY + ") from "
					+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_line.name() + " where "
					+ DatabaseConstants.ATT_HIGHWAY + " is not null and " + DatabaseConstants.functions.st_within.name() + "("
					+ DatabaseConstants.ATT_WAY + "," + DatabaseConstants.functions.st_geomfromtext.name() + "('"
					+ this.geoinformation.getCompleteGeometry().toString() + "',4326));");
			
			while(result.next()){
				
				// Create a new way entry for each result and set its attributes according to the table entries
				WayEntry entry = new WayEntry();
				entry.setOsmId(result.getString(DatabaseConstants.ATT_OSM_ID));
				entry.setAccessTag(result.getString(DatabaseConstants.ATT_ACCESS));
				entry.setHighwayTag(result.getString(DatabaseConstants.ATT_HIGHWAY));
				entry.setJunctionTag(result.getString(DatabaseConstants.ATT_JUNCTION));
				//TODO add lanes and maxspeed as attributes into db table
//				entry.lanesTag = result.getString(TAG_LANES);
//				entry.maxspeedTag = result.getString(TAG_MAXSPEED);
				entry.setOnewayTag(result.getString(DatabaseConstants.ATT_ONEWAY));
				entry.setGeometry(this.wktReader.read(result.getString(DatabaseConstants.functions.st_astext.name())));
				wayEntries.add(entry);
				
			}
			
			// After all road data is retrieved, close the statement and the connection.
			result.close();
			statement.close();
			
		}
		
		connection.close();
		
		log.info("Done.");
		
		return wayEntries;
		
	}
	
}