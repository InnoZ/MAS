package com.innoz.toolbox.io.database;

import java.sql.Connection;
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
import org.postgis.PGgeometry;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.datasets.OsmPointDataset;
import com.innoz.toolbox.io.database.datasets.OsmPolygonDataset;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.run.parallelization.BuildingThread;
import com.innoz.toolbox.run.parallelization.DataProcessingAlgoThread;
import com.innoz.toolbox.run.parallelization.MultithreadedModule;
import com.innoz.toolbox.scenarioGeneration.facilities.FacilitiesCreator;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Building;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.innoz.toolbox.scenarioGeneration.network.OsmNodeEntry;
import com.innoz.toolbox.scenarioGeneration.network.WayEntry;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GlobalNames;
import com.innoz.toolbox.utils.data.Tree.Node;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
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
	
	private static DatabaseReader instance = new DatabaseReader(Controller.configuration());

	public static DatabaseReader getInstance() {
		
		return instance;
		
	}
	
	private DatabaseReader(final Configuration configuration) {
		
		// Initialize all final fields
		this.gFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue));
		this.wktReader = new WKTReader();
		this.configuration = configuration;
		
	}
	
	public Configuration getConfiguration(){
		return this.configuration;
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
	public void readGeodataFromDatabase(Scenario scenario) {
		
		try {
			
			// Create a postgresql database connection
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
			
			if(connection != null){

				Log.info("Successfully connected with geodata database...");
				
				// Read the administrative borders that have one of the specified ids
				this.readAdminBorders(connection, configuration);
				
				// If no administrative units were created, we are unable to proceed
				// The process would probably finish, but no network or population would be created
				// Size = 1 means, only the root element (basically the top level container) has been initialized
				if(Geoinformation.getInstance().getNumberOfAdminUnits() < 2 && this.configuration.scenario().getAreaSets() != null){
				
					Log.error("No administrative boundaries were created!");
					Log.error("Maybe the ids you specified don't exist in the database.");
					throw new RuntimeException("Execution aborts...");
					
				}
				
				readBbsrData(connection, this.configuration);
				
				if(this.configuration.scenario().getAreaSets() != null) {

					Set<PopulationSource> populationAlgorithms = new HashSet<PopulationSource>(2);
					
					for(ConfigurationGroup cg : configuration.scenario().getAreaSets().values()){
						
						AreaSet entry = (AreaSet)cg;
						
						populationAlgorithms.add(entry.getPopulationSource());
						
						for(String id : entry.getIds().split(",")){

							Node<AdministrativeUnit> d = Geoinformation.getInstance().getAdminUnit(id);
							
							if(d != null){
								
								AdministrativeUnit unit = d.getData();
								
								unit.setNumberOfHouseholds(entry.getNumberOfHouseholds());
								
							}
							
						}

					}
					
					// We don't need landuse data if there is no population to be created or
					// if the source is tracks.
					if(populationAlgorithms.contains(PopulationSource.COMMUTER) ||
							populationAlgorithms.contains(PopulationSource.SURVEY)){
						
						this.readOsmData(connection, configuration, scenario);
						
					}
					
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
		MathTransform t = CRS.findMathTransform(CRS.decode(GlobalNames.WGS84, true),
				CRS.decode(configuration.misc().getCoordinateSystem(), true));
		
		// A collection to temporarily store all geometries
		List<Geometry> geometryCollection = new ArrayList<Geometry>();
		
		if(configuration.scenario().getAreaSets() != null) {

			for(ConfigurationGroup cg : configuration.scenario().getAreaSets().values()){
				
				AreaSet set = (AreaSet)cg;
				getAndAddGeodataFromIdSet(connection, configuration, geometryCollection, set);
			
				if(set.isSurveyArea()){
					
					Geoinformation.getInstance().setSurveyAreaBoundingBox(gFactory.buildGeometry(geometryCollection)
							.convexHull());
					
				} else{
					
					Geoinformation.getInstance().setVicinityBoundingBox(gFactory.buildGeometry(geometryCollection)
							.convexHull());
					
				}
				
			}
			
		} else {
			
			geometryCollection.add(setSurveyAreaByBoundingBox(connection, configuration));
			
		}
		
		// Get the survey area by building the bounding box of all geometries 
		Geoinformation.getInstance().setCompleteGeometry(gFactory.buildGeometry(geometryCollection)
				.convexHull());
		
		this.boundingBox = JTS.transform((Geometry) Geoinformation.getInstance().getCompleteGeometry()
				.clone(), t).convexHull();
		
	}
	
	private Geometry setSurveyAreaByBoundingBox(Connection connection, Configuration configuration) {
		
		Coordinate[] coordinates = new Coordinate[]{
				new Coordinate(11.9236,51.3621),
				new Coordinate(12.0949,51.3621),
				new Coordinate(12.0949,51.2273),
				new Coordinate(11.9236,51.2273),
				new Coordinate(11.9236,51.3621)
				};

		Geometry g = gFactory.createPolygon(coordinates);
		
		Geoinformation.getInstance().setSurveyAreaBoundingBox(g.getEnvelope());
		Geoinformation.getInstance().setVicinityBoundingBox(g.getEnvelope());
		
		return g;
		
	}
	
	private void getAndAddGeodataFromIdSet(Connection connection, Configuration configuration, List<Geometry> geometryCollection,
			AreaSet areaSet) throws SQLException, NoSuchAuthorityCodeException, FactoryException, ParseException,
			MismatchedDimensionException, TransformException{
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		
		String[] splitIds = areaSet.getIds().split(",");
		
		// Append all ids inside the given collection to a string
		for(String id : splitIds){

			if(i < splitIds.length - 1){
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%' OR");
				
			} else {
				
				builder.append(" " + DatabaseConstants.MUN_KEY + " like '" + id + "%'");
				
			}
			
			i++;
			
		}
		
		// Execute the query and store the returned valued inside a set.
		String q = "select " + DatabaseConstants.BLAND + "," + DatabaseConstants.MUN_KEY + ", cca_2, ccn_3, "
				+ DatabaseConstants.functions.st_astext.name() + "(geom), "
				+ DatabaseConstants.functions.st_astext.name() + "(st_transform("
				+ DatabaseConstants.ATT_GEOM + ",4326)) as buffer from " + DatabaseConstants.schemata.gadm.name() + "." +
				DatabaseConstants.tables.districts.name() + " where" + builder.toString();
		ResultSet set = statement.executeQuery(q);

		// Go through all the results
		while(set.next()){
			
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
					
					if(areaSet.isSurveyArea()){
						bufferedAreasForNetworkGeneration.add(wktReader.read(set.getString("buffer")));
					}

					if(district != null){
						
						au.setNetworkDetail(areaSet.getNetworkLevel());

						Geoinformation.getInstance().addAdministrativeUnit(new AdministrativeUnit(district));
						
					}
					
					Geoinformation.getInstance().addAdministrativeUnit(au);
					
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
	
	private void readBbsrData(Connection connection, Configuration configuration) throws SQLException {
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		
		StringBuilder builder = new StringBuilder();
		for(ConfigurationGroup cg : Controller.configuration().scenario().getAreaSets().values()){
			AreaSet set = (AreaSet) cg;
			String[] ids = set.getIds().split(",");
			for(String i : ids)
				builder.append("'" + i + "%' OR gkz LIKE ");
		}
		String ids = builder.toString();
		ids = ids.substring(0,ids.length()-13);
		
		String sql = "SELECT gkz, rtype7 from bbsr.regiontypes WHERE gkz LIKE " + ids + ";";
		
		ResultSet result = statement.executeQuery(sql);
		
		while(result.next()) {
			
			String gkz = result.getString("gkz").substring(0, 5);
			int typ = result.getInt("rtype7");
			
			if(Geoinformation.getInstance().getAdminUnit(gkz) != null) {
			
				Geoinformation.getInstance().getAdminUnit(gkz).getData().setRegionType(typ);
				
			}
			
		}
		
		result.close();
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
	private void readOsmData(Connection connection, Configuration configuration, Scenario scenario)
			throws NoSuchAuthorityCodeException, FactoryException{

		final CoordinateReferenceSystem fromCRS = CRS.decode(GlobalNames.WGS84, true);
		final CoordinateReferenceSystem toCRS = CRS.decode(configuration.misc().getCoordinateSystem(), true);
		
		this.ct = TransformationFactory.getCoordinateTransformation(fromCRS.toString(),
				toCRS.toString());
		
		log.info("Reading osm data...");
		
		try {

			for(Coordinate coord : Geoinformation.getInstance().getCompleteGeometry().getCoordinates()){
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
			
			if(!configuration.scenario().getActivityLocationsType().equals(ActivityLocationsType.LANDUSE)){
				
				if(configuration.scenario().getActivityLocationsType().equals(ActivityLocationsType.FACILITIES)){
					
					new FacilitiesCreator().create(this, scenario, buildingList, minX, minY, maxX, maxY);

				} else {
					
					MultithreadedModule module = new MultithreadedModule(configuration.misc().getNumberOfThreads());
					module.initThreads(BuildingThread.class.getName(), this);
					for(Building b : this.buildingList){
						module.handle(b);
					}
					module.execute();
					
				}
				
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
				.name() + "." + DatabaseConstants.tables.osm_germany_polygon.name() + " where "
				+ DatabaseConstants.functions.st_within.name() + "(" + DatabaseConstants.ATT_WAY
				+ ", " + DatabaseConstants.functions.st_geomfromtext.name() + "('"
				+ Geoinformation.getInstance().getCompleteGeometry().toString() + "', 4326)) and ("
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
		MultithreadedModule module = new MultithreadedModule(configuration.misc().getNumberOfThreads());
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
				+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_germany_point.name() + " where "
				+ DatabaseConstants.functions.st_within + "(" + DatabaseConstants.ATT_WAY + "," + DatabaseConstants.functions.st_geomfromtext.name()
				+ "('" + Geoinformation.getInstance().getCompleteGeometry().toString() + "',4326)) and (" + DatabaseConstants.ATT_AMENITY
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
		MultithreadedModule module = new MultithreadedModule(configuration.misc().getNumberOfThreads());
		module.initThreads(DataProcessingAlgoThread.class.getName(), this, "amenities");
		for(OsmPointDataset dataset : this.pointData){
			module.handle(dataset);
		}
		module.execute();
		
	}
	
	/**
	 * 
	 * Adds a landuse geometry to the geoinformation container.
	 * 
	 * @param landuse The MATSim activity option that can be performed at this location.
	 * @param g The geometry of the activity location.
	 */
	public void addGeometry(String landuse, Landuse g){
		
		synchronized(Geoinformation.getInstance()){
		
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
		
		Geometry geometry = g.getGeometry();
		
		// Check if the geometry is not null
		if(geometry != null){
			
			// Check if the geometry is valid (e.g. not intersecting itself)
			if(geometry.isValid()){

				for(AdministrativeUnit au : Geoinformation.getInstance().getAdminUnitsWithGeometry()){

					// Add the landuse geometry to the administrative unit containing it or skip it if it's outside of the survey area
					if(au.getGeometry().contains(geometry) || au.getGeometry().touches(geometry) || au.getGeometry().intersects(geometry)){
						
						au.addLanduse(landuse, g);
						if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals(ActivityTypes.HOME)){
							au.addLanduse(ActivityTypes.WORK, g);
						}
						
						// If we don't have a quad tree for this activity type already, create a new one
						if(Geoinformation.getInstance().getLanduseOfType(landuse) == null){
							
							Geoinformation.getInstance().createQuadTreeForActType(landuse, new double[]{minX,minY,maxX,maxY});
							
						}
						
						if(Geoinformation.getInstance().getLanduseOfType(ActivityTypes.WORK) == null){
							
							Geoinformation.getInstance().createQuadTreeForActType(ActivityTypes.WORK, new double[]{minX,minY,maxX,maxY});
							
						}
						
						// Add the landuse geometry's centroid as new quad tree entry
						Coord c = ct.transform(MGC.point2Coord(geometry.getCentroid()));
						
						// Add the landuse geometry's centroid as new quad tree entry
						if(this.boundingBox.contains(MGC.coord2Point(c))){
							
							Geoinformation.getInstance().getLanduseOfType(landuse).put(c.getX(), c.getY(), g);
							if(!landuse.equals(ActivityTypes.LEISURE) && !landuse.equals(ActivityTypes.HOME)){
								Geoinformation.getInstance().getLanduseOfType(ActivityTypes.WORK).put(c.getX(), c.getY(), g);
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
	public /*Set<WayEntry>*/void readOsmRoads(final Configuration configuration, final Map<String, WayEntry> wayEntries,
			final Map<String, OsmNodeEntry> nodeEntries) throws SQLException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, ParseException{
		
		log.info("Reading osm ways from database...");
		
		Map<Coordinate, OsmNodeEntry> coordinates2Nodes = new HashMap<>();
		
		// Connect to the geodata database
		Connection connection = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
	
		if(connection != null){

			buffer = gFactory.buildGeometry(bufferedAreasForNetworkGeneration).buffer(0);
			
			// Create a new statement and execute an SQL query to retrieve OSM road data
			Statement statement = connection.createStatement();
			statement.setFetchSize(1000);
			ResultSet result = statement.executeQuery("SELECT * from "
					+ DatabaseConstants.schemata.osm.name() + "." + DatabaseConstants.tables.osm_germany_line.name() + " where ("
					+ DatabaseConstants.ATT_RAILWAY + " is not null or "+ DatabaseConstants.ATT_HIGHWAY + " is not null) and "
					+ DatabaseConstants.functions.st_within.name() + "(" + DatabaseConstants.ATT_WAY + ","
					+ DatabaseConstants.functions.st_geomfromtext.name() + "('"	+ Geoinformation.getInstance().getCompleteGeometry().toString()
					+ "',4326)) order by " + DatabaseConstants.ATT_OSM_ID + ";");
			
			while(result.next()){
				
				// Create a new way entry for each result and set its attributes according to the table entries
				WayEntry entry = new WayEntry();
				entry.setOsmId(result.getString(DatabaseConstants.ATT_OSM_ID));
				entry.setAccessTag(result.getString(DatabaseConstants.ATT_ACCESS));
				String highway = result.getString(DatabaseConstants.ATT_HIGHWAY);
				String railway = result.getString(DatabaseConstants.ATT_RAILWAY);
				String type = highway != null ? highway : railway;
				entry.setHighwayTag(type);
				entry.setJunctionTag(result.getString(DatabaseConstants.ATT_JUNCTION));
				entry.setLanesTag(result.getString(DatabaseConstants.TAG_LANES));
				entry.setMaxspeedTag(result.getString(DatabaseConstants.TAG_MAXSPEED));
				entry.setOnewayTag(result.getString(DatabaseConstants.ATT_ONEWAY));
//				entry.setForwardLanesTag(result.getString("lanes:forward"));
//				entry.setBackwardLanesTag(result.getString("lanes:backward"));
//				entry.setConditionalMaxspeedTag(result.getString("maxspeed:conditional"));
				
				PGgeometry geometry = (PGgeometry)result.getObject("way");
				Geometry geom = this.wktReader.read(geometry.toString().split(";")[1]);
				
				if(geom.getCoordinates().length > 1){

					for(Coordinate coordinate : geom.getCoordinates()){
						
						OsmNodeEntry nEntry = null;
						
						if(!coordinates2Nodes.containsKey(coordinate)){
							
							nEntry = new OsmNodeEntry(Integer.toString(coordinates2Nodes.size()), coordinate.x, coordinate.y);
							coordinates2Nodes.put(coordinate, nEntry);
							nodeEntries.put(nEntry.getId(), nEntry);
							
						} else {

							nEntry = coordinates2Nodes.get(coordinate);
							
						}
						
						entry.getNodes().add(nEntry.getId());
						
					}
					
					entry.setGeometry(geom);
					wayEntries.put(entry.getOsmId(), entry);
					
				}
				
			}
			
			// After all road data is retrieved, close the statement and the connection.
			result.close();
			statement.close();
			
		}
		
		connection.close();
		
		log.info("Done.");
		
	}
	
	/**
	 * Imports detailed forecast of the population subdivided by sex and age groups
	 * 
	 * 
	 * @param configuration The configuration for the scenario generation process.
	 * @param surveyAreaIdsString The survey area id(s).
	 * @param vicinityIdsString The vicinity area id(s).
	 * @param scenario The MATSim scenario.
	 */
	public void readPopulationFromDatabase(Scenario scenario) {
		
		try {
			
			// Create a postgresql database connection
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.POPULATIONFORECAST_DB);
			
			if(connection != null){

				Log.info("Successfully connected with population database...");
				
				// If no administrative units were created, we are unable to proceed
				// The process would probably finish, but no network or population would be created
				// Size = 1 means, only the root element (basically the top level container) has been initialized
				if(this.configuration.scenario().getAreaSets().isEmpty()){
				
					Log.error("No ids found");
					throw new RuntimeException("Execution aborts...");
					
				}
				
				for(ConfigurationGroup cg : configuration.scenario().getAreaSets().values()){
					
					AreaSet entry = (AreaSet)cg;
					
					int year = configuration.scenario().getYear();
					
					for(String id : entry.getIds().split(",")){

//						String id = uid.startsWith("0") ? uid.substring(1) : uid;
						
						Node<AdministrativeUnit> d = Geoinformation.getInstance().getAdminUnit(id);
						
						if(d != null){
							
							AdministrativeUnit unit = d.getData();
							
							// Execute the query and store the returned valued inside a set.
							String q = "SELECT agegroup, year" + year
									+ " FROM bbsrprognose.populationdata "
									+ " WHERE gkz=" + id + " AND agegroup NOT LIKE '%z%' ";

							Statement statement = connection.createStatement();
							statement.setFetchSize(100);
							ResultSet rs = statement.executeQuery(q);

							// Create a map and put all the results
							HashMap<String, Integer> populationByAgeGroup = new HashMap<String, Integer>();
							while (rs.next()){
								populationByAgeGroup.put(rs.getString("agegroup") , rs.getInt("year" + year));
							}
							rs.close();
							
							unit.setPopulationMap(populationByAgeGroup);;
							
						}
						
					}

				}
					
			}
			
			// Close the connection when everything's done.
			connection.close();
			
			Log.info("Done.");

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException | 
				MismatchedDimensionException e) {

			e.printStackTrace();
			
		}
		
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
	
}