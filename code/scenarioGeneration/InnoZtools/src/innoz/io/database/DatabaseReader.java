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
import innoz.io.database.datasets.OsmPointDataset;
import innoz.io.database.datasets.OsmPolygonDataset;
import innoz.run.parallelization.MultithreadedModule;
import innoz.run.parallelization.BuildingThread;
import innoz.run.parallelization.DataProcessingAlgoThread;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Building;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.WayEntry;
import innoz.scenarioGeneration.utils.ActivityTypes;

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
	private List<Building> buildingList = Collections.synchronizedList(new ArrayList<>());
	private QuadTree<Building> buildingsQuadTree;
	private final Configuration configuration;
	private Map<String, List<OsmPolygonDataset>> polygonData = new HashMap<>();
	private List<OsmPointDataset> pointData = Collections.synchronizedList(new ArrayList<>());
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	double maxY = Double.MIN_VALUE;

	boolean resultSet = false;
	
	private List<Geometry> bufferedAreasForNetworkGeneration = new ArrayList<>();
	private Geometry buffer;
	/////////////////////////////////////////////////////////////////////////////////////////

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
	
	public Configuration getConfiguration(){
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
				
				if(!configuration.getPopulationType().equals(PopulationType.none)
						|| !configuration.getVicinityPopulationType().equals(PopulationType.none)){

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

		// This is needed to transform the WGS84 geometries into the specified CRS
		MathTransform t = CRS.findMathTransform(CRS.decode(Geoinformation.AUTH_KEY_WGS84, true),
				CRS.decode(configuration.getCrs(), true));
		
		// A collection to temporarily store all geometries
		List<Geometry> geometryCollection = new ArrayList<Geometry>();
		
		getAndAddGeodataFromIdSet(connection, configuration, geometryCollection, true);
		this.geoinformation.setSurveyAreaBoundingBox(gFactory.buildGeometry(geometryCollection)
				.convexHull());
		
		if(configuration.getVicinityIds() != null){
			getAndAddGeodataFromIdSet(connection, configuration, geometryCollection, false);
			this.geoinformation.setVicinityBoundingBox(gFactory.buildGeometry(geometryCollection)
					.convexHull());
		}
		
		for(District d : this.geoinformation.getAdminUnits().values()){
			d.setnHouseholds(configuration.getAdminUnitEntries().get(d.getId()).getNumberOfHouseholds());
		}
		
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
				+ DatabaseConstants.functions.st_astext.name() + "(geom), "
				+ DatabaseConstants.functions.st_astext.name() + "(st_transform(st_buffer(st_transform("
				+ DatabaseConstants.ATT_GEOM + ",32632),5000),4326)) as buffer" + " from " + DatabaseConstants.schemata.gadm.name() + "." +
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
					
					if(surveyArea){
						bufferedAreasForNetworkGeneration.add(wktReader.read(set.getString("buffer")));
					}

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

			for(Coordinate coord : this.geoinformation.getCompleteGeometry().getCoordinates()){
				if(coord.x < minX) minX = coord.x;
				if(coord.x > maxX) maxX = coord.x;
				if(coord.y < minY) minY = coord.y;
				if(coord.y > maxY) maxY = coord.y;
			}
			
			this.buildingsQuadTree = new QuadTree<Building>(minX, minY, maxX, maxY);
			
			// Read polygon geometries
			readPolygonData(connection, configuration);

			// Read point geometries
			readPointData(connection);
			
			if(configuration.isUsingBuildings()){
				
				for(AdministrativeUnit au : this.geoinformation.getSubUnits().values()){
					
					au.getLanduseGeometries().clear();
					
				}
				
				MultithreadedModule module = new MultithreadedModule(configuration.getNumberOfThreads());
				module.initThreads(BuildingThread.class.getName(), this);
				for(Building b : this.buildingList){
					module.handle(b);
				}
				module.execute();
				
			}
			
			log.info("Done.");
			
		} catch (SQLException | ParseException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	private void readPolygonData(Connection connection, Configuration configuration) throws SQLException, ParseException{
		
		log.info("Processing osm polygon data...");
		
		this.polygonData.put("landuse", new ArrayList<>());
		this.polygonData.put("buildings", new ArrayList<>());
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(1000);
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
		MultithreadedModule module = new MultithreadedModule(configuration.getNumberOfThreads());
		module.initThreads(DataProcessingAlgoThread.class.getName(), this, "buildings");
		for(OsmPolygonDataset dataset : this.polygonData.get("buildings")){
			module.handle(dataset);
		}
		module.execute();
		
		module.initThreads(DataProcessingAlgoThread.class.getName(), this, "landuse");
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
	private void readPointData(Connection connection) throws SQLException, ParseException{
		
		log.info("Processing osm point data...");

		// Create a statement and execute an SQL query to retrieve all amenities that have a tag
		// containing a shopping, leisure or any other activity.
		Statement statement = connection.createStatement();
		statement.setFetchSize(1000);
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
			
			this.pointData.add(new OsmPointDataset(geometry, amenity, shop, leisure));
			
		}
		
		// Close everything in the end
		set.close();
		statement.close();
		
		//post process
		MultithreadedModule module = new MultithreadedModule(configuration.getNumberOfThreads());
		module.initThreads(DataProcessingAlgoThread.class.getName(), this, "amenities");
		for(OsmPointDataset dataset : this.pointData){
			module.handle(dataset);
		}
		module.execute();
		
	}
	
	/**
	 * 
	 * Adds a landuse geometry to the geoinformationo container.
	 * 
	 * @param landuse The MATSim activity option that can be performed at this location.
	 * @param g The geometry of the activity location.
	 */
	public void addGeometry(String landuse, Geometry g){
		
		synchronized(this.geoinformation){
		
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

			buffer = gFactory.buildGeometry(bufferedAreasForNetworkGeneration).buffer(0);
			
			// Create a new statement and execute an SQL query to retrieve OSM road data
			Statement statement = connection.createStatement();
			statement.setFetchSize(1000);
			ResultSet result = statement.executeQuery("select " + DatabaseConstants.ATT_OSM_ID + ", " + DatabaseConstants.ATT_ACCESS + ", "
					+ DatabaseConstants.ATT_HIGHWAY + ", " + DatabaseConstants.ATT_JUNCTION + ", " + DatabaseConstants.ATT_ONEWAY + ", "
					+ DatabaseConstants.TAG_LANES + " ," + DatabaseConstants.TAG_MAXSPEED + ", "
					+ DatabaseConstants.functions.st_astext.name() + "(" + DatabaseConstants.ATT_WAY + ") from "
					+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_line.name() + " where "
					+ DatabaseConstants.ATT_HIGHWAY + " is not null and " + DatabaseConstants.functions.st_within.name() + "("
					+ DatabaseConstants.ATT_WAY + "," + DatabaseConstants.functions.st_geomfromtext.name() + "('"
					+ this.geoinformation.getCompleteGeometry().toString() + "',4326)) order by " + DatabaseConstants.ATT_OSM_ID + ";");
			
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
	
	public List<Building> getBuildingList(){
		return buildingList;
	}
	
	public QuadTree<Building> getBuildingsQuadTree(){
		return this.buildingsQuadTree;
	}
	
	public Geometry getBufferedArea(){
		return this.buffer;
	}
	
	public Map<String,List<OsmPolygonDataset>> getPolygonData(){
		return this.polygonData;
	}
	
}