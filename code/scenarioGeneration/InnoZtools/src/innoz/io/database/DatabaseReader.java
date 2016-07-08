package innoz.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
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
import org.matsim.core.utils.collections.QuadTree;
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
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import innoz.config.Configuration;
import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.Configuration.PopulationType;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Building;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.WayEntry;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.utils.osm.OsmKey2ActivityType;

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
	private List<Building> buildingList = Collections.synchronizedList(new ArrayList<>());
	private QuadTree<Building> buildingsQuadTree;
	private final Configuration configuration;
	/////////////////////////////////////////////////////////////////////////////////////////

	public List<Building> getBuildingList(){
		return buildingList;
	}
	
	public QuadTree<Building> getBuildingsQuadTree(){
		return this.buildingsQuadTree;
	}
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param geoinformation The geoinformation container.
	 */
	public DatabaseReader(final Configuration configuration, final Geoinformation geoinformation){
		
		// Initialize all final fields
		this.gFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue));
		this.wktReader = new WKTReader();
		this.geoinformation = geoinformation;
		this.configuration = configuration;
		
	}
	
	Configuration getConfiguration(){
		return this.configuration;
	}
	
	Geoinformation getGeoinformation(){
		return this.geoinformation;
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
	public void readGeodataFromDatabase(Configuration configuration, Scenario scenario) {
		
		try {
			
			// Create a postgresql database connection
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL +
					configuration.getLocalPort() + "/" + DatabaseConstants.GEODATA_DB, configuration.getDatabaseUsername(),
					configuration.getDatabasePassword());
			
			if(connection != null){

				Log.info("Successfully connected with geodata database...");
				
				// Read the administrative borders that have one of the specified ids
				// TODO Vicinity... /dhosse 05/16
				this.readAdminBorders(connection, configuration);
				
				// If no administrative units were created, we are unable to proceed
				// The process would probably finish, but no network or population would be created
				if(this.geoinformation.getAdminUnits().size() < 1){
				
					Log.error("No administrative boundaries were created!");
					Log.error("Maybe the ids you specified don't exist in the database.");
					throw new RuntimeException("Execution aborts...");
					
				}
				
				for(AdminUnitEntry entry : configuration.getAdminUnitEntries().values()){

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
				
				if(!configuration.getPopulationType().equals(PopulationType.none)){

					// Otherwise, read in the OSM data
					this.readOsmData(connection, configuration);
					
				}
				
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
	private void readAdminBorders(Connection connection, Configuration configuration) throws SQLException,
			NoSuchAuthorityCodeException, FactoryException, ParseException,
			MismatchedDimensionException, TransformException {

		log.info("Reading administrative borders from database...");

		// A collection to temporarily store all geometries
		List<Geometry> geometryCollection = new ArrayList<Geometry>();
		
		getAndAddGeodataFromIdSet(connection, configuration, geometryCollection, true);
		this.geoinformation.setSurveyAreaBoundingBox(gFactory.buildGeometry(geometryCollection)
				.convexHull().buffer(5000));
		if(configuration.getVicinityIds() != null){
			getAndAddGeodataFromIdSet(connection, configuration, geometryCollection, false);
			this.geoinformation.setVicinityBoundingBox(gFactory.buildGeometry(geometryCollection)
					.convexHull());
		}
		
		for(District d : this.geoinformation.getAdminUnits().values()){
			d.setnHouseholds(configuration.getAdminUnitEntries().get(d.getId()).getNumberOfHouseholds());
		}
		
		// This is needed to transform the WGS84 geometries into the specified CRS
		MathTransform t = CRS.findMathTransform(CRS.decode(Geoinformation.AUTH_KEY_WGS84, true),
				CRS.decode(configuration.getCrs(), true));
		
		// Get the survey area by building the bounding box of all geometries 
		this.geoinformation.setCompleteGeometry(gFactory.buildGeometry(geometryCollection)
				.convexHull());
		
		this.boundingBox = JTS.transform((Geometry) this.geoinformation.getCompleteGeometry()
				.clone(), t).convexHull();
		
	}
	
	private void getAndAddGeodataFromIdSet(Connection connection, Configuration configuration, List<Geometry> geometryCollection,
			boolean surveyArea) throws SQLException, NoSuchAuthorityCodeException, FactoryException, ParseException,
			MismatchedDimensionException, TransformException{
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		StringBuilder builder = new StringBuilder();
		
		String[] ids = surveyArea ? configuration.getSurveyAreaIds().split(",") : configuration.getVicinityIds().split(",");
		
		int i = 0;
		
		// Append all ids inside the given collection to a string
		for(String id : ids){

			if(i < ids.length - 1){
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%' OR");
				
			} else {
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%'");
				
			}
			
			i++;
			
		}
		
		// Execute the query and store the returned valued inside a set.
		String q = "select " + DatabaseConstants.BLAND + "," + DatabaseConstants.MUN_KEY + ", cca_2, ccn_3, "
				+ DatabaseConstants.functions.st_astext.name() + "(" + DatabaseConstants.ATT_GEOM +
				")" + " from " + DatabaseConstants.schemata.gadm.name() + "." +
				DatabaseConstants.tables.districts.name() + " where" + builder.toString();
		ResultSet set = statement.executeQuery(q);

		// Go through all the results
		while(set.next()){
			
			//TODO attributes have to be added to the table
			String key = set.getString(DatabaseConstants.MUN_KEY);
			String g = set.getString(DatabaseConstants.functions.st_astext.name());
			int bland = set.getInt(DatabaseConstants.BLAND);
			String district = set.getString("cca_2") != null ? set.getString("cca_2") : set.getString("ccn_3").substring(0, 3);
			
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
						
						au.setNetworkDetail(configuration.getAdminUnitEntries().get(district).getNetworkDetail());

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
			
			for(Coordinate coord : this.geoinformation.getCompleteGeometry().getCoordinates()){
				if(coord.x < minX) minX = coord.x;
				if(coord.x > maxX) maxX = coord.x;
				if(coord.y < minY) minY = coord.y;
				if(coord.y > maxY) maxY = coord.y;
			}
			
			this.buildingsQuadTree = new QuadTree<Building>(minX, minY, maxX, maxY);
			
			getPolygonData(connection, configuration);

			// If buildings should be considered, also read their geometries
//			if(configuration.isUsingBuildings()){
//				
//				readBuildings(connection);
//				
//			}

			// Read amenity geometries
			readAmenities(connection);
			
			// Read landuse geometries
//			readLanduseData(connection, configuration);
			
			if(configuration.isUsingBuildings()){
				
				for(AdministrativeUnit au : this.geoinformation.getSubUnits().values()){
					
					au.getLanduseGeometries().clear();
					
				}
				
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.SUPPLY).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.KINDERGARTEN).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.HOME).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.WORK).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.SHOPPING).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.OTHER).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.LEISURE).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.EDUCATION).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.EATING).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.CULTURE).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.SPORTS).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.FURTHER).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.EVENT).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.HEALTH).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.SERVICE).clear();
//				this.geoinformation.getQuadTreeForActType(ActivityTypes.ERRAND).clear();
			
//				List<Building> b1 = this.buildingList.subList(0, this.buildingList.size()/n);
//				List<Building> b2 = this.buildingList.subList(this.buildingList.size()/n, this.buildingList.size());
//				
//				new BuildingsThread(b1).start();
//				new BuildingsThread(b2).start();
				
				for(Building b : this.buildingList){
					
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
	
	class BuildingsThread extends Thread{
		
		List<Building> buildings;
		
		public BuildingsThread(List<Building> buildings){
			this.buildings = buildings;
		}
		
		public void run(){
			
			for(Building b : this.buildings){
				
				for(String actType : b.getActivityOptions()){
					
					if(actType != null){
				
						addGeometry(actType, b.getGeometry());
					
					}
				
				}
				
			}
			
		}
		
	}
	
	private Map<String, List<OsmPolygonDataset>> polygonData = new HashMap<>();
	
	private void getPolygonData(Connection connection, Configuration configuration) throws SQLException, ParseException{
		
		log.info("Processing osm polygon data...");
		
		this.polygonData.put("landuse", new ArrayList<>());
		this.polygonData.put("buildings", new ArrayList<>());
		
		Statement statement = connection.createStatement();
		
		String query = "select " + DatabaseConstants.ATT_LANDUSE + ", " + DatabaseConstants.ATT_BUILDING + ", "
				+ DatabaseConstants.ATT_AMENITY +  ", " + DatabaseConstants.ATT_LEISURE + ", "
				+ DatabaseConstants.ATT_SHOP + ", " + DatabaseConstants.functions.st_astext.name()
				+ "(" + DatabaseConstants.ATT_WAY + ") from " + DatabaseConstants.schemata.osm
				.name() + "." + DatabaseConstants.tables.osm_polygon.name() + " where "
				+ DatabaseConstants.functions.st_within.name() + "(" + DatabaseConstants.ATT_WAY
				+ ", " + DatabaseConstants.functions.st_geomfromtext.name() + "('"
				+ this.geoinformation.getCompleteGeometry().toString() + "', 4326)) and ("
				+ DatabaseConstants.ATT_LANDUSE + " is not null" + " or " + DatabaseConstants
				.ATT_AMENITY + " is not null or " + DatabaseConstants.ATT_LEISURE + " is not null"
				+ " or " + DatabaseConstants.ATT_SHOP + " is not null or " 
				+ DatabaseConstants.ATT_BUILDING + " is not null);";
		
		ResultSet resultSet = statement.executeQuery(query);
		
		while(resultSet.next()){
			
			Geometry geometry = wktReader.read(resultSet.getString(DatabaseConstants.functions.st_astext.name()));
			String landuse = resultSet.getString(DatabaseConstants.ATT_LANDUSE);
			String amenity = resultSet.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = resultSet.getString(DatabaseConstants.ATT_LEISURE);
			String shop = resultSet.getString(DatabaseConstants.ATT_SHOP);
			String building = resultSet.getString(DatabaseConstants.ATT_BUILDING);
			
			String type = null;
			
			if(building != null){
			
				type = "buildings";
				
			} else {
				
				type = "landuse";
				
			}
			
			this.polygonData.get(type).add(new OsmPolygonDataset(geometry, landuse, amenity, shop, leisure, building));
			
		}
		
		resultSet.close();
		statement.close();
		
		//post process
		MultithreadedDataModule module = new MultithreadedDataModule(this);
		module.initThreads("buildings");
		for(OsmPolygonDataset dataset : this.polygonData.get("buildings")){
			module.handle(dataset);
		}
		module.execute();
		
//		module = new MultithreadedDataModule(this);
		module.initThreads("landuse");
		for(OsmPolygonDataset dataset : this.polygonData.get("landuse")){
			module.handle(dataset);
		}
		module.execute();
		//
		
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
	
	boolean resultSet = false;
	
	/**
	 * 
	 * Adds a landuse geometry to the geoinformationo container.
	 * 
	 * @param landuse The MATSim activity option that can be performed at this location.
	 * @param g The geometry of the activity location.
	 */
	/*private */void addGeometry(String landuse, Geometry g){
		
		if(!resultSet){
			
			resultSet = true;
			minX = Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			maxX = Double.MIN_VALUE;
			maxY = Double.MIN_VALUE;
			
			for(Coordinate coord : this.boundingBox.getCoordinates()){
				if(coord.x < minX) minX = coord.x;
				if(coord.x > maxX) maxX = coord.x;
				if(coord.y < minY) minY = coord.y;
				if(coord.y > maxY) maxY = coord.y;
			}
			
		}
		
		// Check if the geometry is not null
		if(g != null){
			
			// Check if the geometry is valid (e.g. not intersecting itself)
			if(g.isValid()){

				for(AdministrativeUnit au : this.geoinformation.getSubUnits().values()){

					// Add the landuse geometry to the administrative unit containing it or skip it if it's outside of the survey area
					if(au.getGeometry().contains(g) || au.getGeometry().touches(g) || au.getGeometry().intersects(g)){
						
						au.addLanduseGeometry(landuse, g);
						if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals(ActivityTypes.HOME)){
							au.addLanduseGeometry(ActivityTypes.WORK, g);
						}
						
						// If we don't have a quad tree for this activity type already, create a new one
						if(this.geoinformation.getQuadTreeForActType(landuse) == null){
							
							this.geoinformation.createQuadTreeForActType(landuse, new double[]{minX,minY,maxX,maxY});
							
						}
						if(this.geoinformation.getQuadTreeForActType(ActivityTypes.WORK) == null){
							this.geoinformation.createQuadTreeForActType(ActivityTypes.WORK, new double[]{minX,minY,maxX,maxY});
						}
						
						// Add the landuse geometry's centroid as new quad tree entry
						Coord c = ct.transform(MGC.point2Coord(g.getCentroid()));
						
						if(this.boundingBox.contains(MGC.coord2Point(c))){
							
							this.geoinformation.getQuadTreeForActType(landuse).put(c.getX(), c.getY(), g);
							if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals(ActivityTypes.HOME)){
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
	
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	double maxY = Double.MIN_VALUE; 
	
	void readBuildings(Connection connection) throws SQLException, ParseException{
	
		//TODO this method implies we are eventually using facilities in MATSim...
		//alternatively: use buildings to "bound" activities
		log.info("Reading in buildings...");
		
		for(Coordinate coord : this.geoinformation.getCompleteGeometry().getCoordinates()){
			if(coord.x < minX) minX = coord.x;
			if(coord.x > maxX) maxX = coord.x;
			if(coord.y < minY) minY = coord.y;
			if(coord.y > maxY) maxY = coord.y;
		}
		
		this.buildingsQuadTree = new QuadTree<Building>(minX, minY, maxX, maxY);
		
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
			
			return ActivityTypes.HOME;
			
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
		Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL + configuration.getLocalPort() + "/"
				+ DatabaseConstants.GEODATA_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
	
		if(connection != null){

			// Create a new statement and execute an SQL query to retrieve OSM road data
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select " + DatabaseConstants.ATT_OSM_ID + ", " + DatabaseConstants.ATT_ACCESS + ", "
					+ DatabaseConstants.ATT_HIGHWAY + ", " + DatabaseConstants.ATT_JUNCTION + ", " + DatabaseConstants.ATT_ONEWAY + ", "
					+ DatabaseConstants.TAG_LANES + " ," + DatabaseConstants.TAG_MAXSPEED + ", "
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
				entry.setLanesTag(result.getString(DatabaseConstants.TAG_LANES));
				entry.setMaxspeedTag(result.getString(DatabaseConstants.TAG_MAXSPEED));
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