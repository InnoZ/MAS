package playground.dhosse.scenarioGeneration.utils;

import java.sql.Connection;
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
import org.matsim.api.core.v01.Coord;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import playground.dhosse.database.DatabaseConstants;
import playground.dhosse.scenarioGeneration.Configuration;
import playground.dhosse.utils.QuadTree;
import playground.dhosse.utils.osm.OsmKey2ActivityType;

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
class DatabaseReader {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(DatabaseReader.class);
	private final GeometryFactory gFactory = new GeometryFactory(new PrecisionModel(
			PrecisionModel.maximumPreciseValue));
	private final WKTReader wktReader = new WKTReader();
	/////////////////////////////////////////////////////////////////////////////////////////

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Geometry boundingBox;
	private CoordinateTransformation ct;
	private int counter = 0;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Std. constructor
	 */
	DatabaseReader(){}
	
	/**
	 * 
	 * This method reads administrative units from a database and puts them into the {@link Geoinformation}
	 * object. Only the administrative units with the specified ids are taken into account.
	 * 
	 * @param connection The database connection
	 * @param configuration The configuration parameters.
	 * @param ids The ids of the administrative border we want to have the geometries of.
	 * 
	 * @throws SQLException
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws ParseException
	 * @throws MismatchedDimensionException
	 * @throws TransformException
	 */
	void readAdminBorders(Connection connection, Configuration configuration, Set<String> ids)
			throws SQLException, NoSuchAuthorityCodeException, FactoryException, ParseException,
			MismatchedDimensionException, TransformException{

		log.info("Reading administrative borders from database...");

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
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.BLAND + "," + DatabaseConstants.MUN_KEY + ", "
				+ DatabaseConstants.ST_ASTEXT + "(" + DatabaseConstants.ATT_GEOM + ")" + " from " + DatabaseConstants.SCHEMA_GADM + "."
				+ DatabaseConstants.TABLE_DISTRICTS + " where" + builder.toString());

//		ResultSet set = statement.executeQuery("select vkz_nr, st_astext(geom) from gadm.berlin_vz;");
		
		// This is needed to transform the WGS84 geometries into the specified CRS
		MathTransform t = CRS.findMathTransform(CRS.decode(Geoinformation.AUTH_KEY_WGS84, true),
				CRS.decode(configuration.getCrs(), true));
		
		// A collection to temporarily store all geometries
		List<Geometry> geometryCollection = new ArrayList<Geometry>();
		
		// Go through all the results
		while(set.next()){
			
			//TODO attributes have to be added to the table
			String key = set.getString(DatabaseConstants.MUN_KEY).substring(1);
//			String key = set.getString("vkz_nr");
			String g = set.getString(DatabaseConstants.ST_ASTEXT);
//			long bland = set.getLong(BLAND);
//				int districtType = set.getInt("");
//				int municipalityType = set.getInt("");
//				int regionType = set.getInt("");
			
			// Check if the wkb string returned is neither null nor empty, otherwise this would crash
			if(g != null){
				
				if(!g.isEmpty()){
					
					// Create a new administrative unit and its geometry and add it to the
					// geoinformation
					AdministrativeUnit au = new AdministrativeUnit(key);
					Geometry geometry = wktReader.read(g);
					au.setGeometry(geometry);
//					au.setBland((int)bland);
//					au.setDistrictType(districtType);
//					au.setMunicipalityType(municipalityType);
//					au.setRegionType(regionType);
					Geoinformation.getAdminUnits().put(key, au);
					
					// Store all geometries inside a collection to get the survey area geometry in the end
					geometryCollection.add(au.getGeometry());
					
				}
				
			}
			
		}
		
		// Get the survey area by building the bounding box of all geometries 
		Geoinformation.setCompleteGeometry(gFactory.buildGeometry(geometryCollection).getEnvelope());
		this.boundingBox = JTS.transform((Geometry) Geoinformation.getCompleteGeometry().clone(), t)
				.getEnvelope();
		
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
	void readOsmData(Connection connection, Configuration configuration) throws NoSuchAuthorityCodeException,
		FactoryException{

		final CoordinateReferenceSystem fromCRS = CRS.decode(Geoinformation.AUTH_KEY_WGS84, true);
		final CoordinateReferenceSystem toCRS = CRS.decode(configuration.getCrs(), true);
		
		this.ct = TransformationFactory.getCoordinateTransformation(fromCRS.toString(), toCRS.toString());
		
		log.info("Reading osm data...");
		
		try {
			
			// Read landuse geometries
			readLanduseData(connection);
			
			// Read amenity geometries
			readAmenities(connection);
			
			// If buildings should be considered, also read their geometries
			if(configuration.isUsingBuildings()){
				
				readBuildings(connection);
				
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
	private void getPolygonBasedLanduseData(Connection connection) throws ParseException, SQLException {
		
		log.info("Processing polygon landuse data...");
		
		// Create a new statement to execute the sql query
		Statement statement = connection.createStatement();
		// Execute the query and store the returned valued inside a set.
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_LANDUSE + ", " + DatabaseConstants.ATT_AMENITY +  ", "
				+ DatabaseConstants.ATT_LEISURE + ", " + DatabaseConstants.ATT_SHOP + ", " + DatabaseConstants.ST_ASTEXT + "("
				+ DatabaseConstants.ATT_WAY + ") from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_POLYGONS + " where "
				+ DatabaseConstants.ST_WITHIN + "(" + DatabaseConstants.ATT_WAY + ", " + DatabaseConstants.ST_GEOMFROMTEXT + "('"
				+ Geoinformation.getCompleteGeometry().toString() + "', 4326)) and (" + DatabaseConstants.ATT_LANDUSE + " is not null"
				+ " or " + DatabaseConstants.ATT_AMENITY + " is not null or " + DatabaseConstants.ATT_LEISURE + " is not null or "
				+ DatabaseConstants.ATT_SHOP + " is not null);");
		
		// Go through all results
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.ST_ASTEXT));
			String landuse = set.getString(DatabaseConstants.ATT_LANDUSE);
			String amenity = set.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = set.getString(DatabaseConstants.ATT_LEISURE);
			String shop = set.getString(DatabaseConstants.ATT_SHOP);
			
			if(amenity != null){
				
				landuse = amenity;
				
			} else if(leisure != null){
				
				landuse = leisure;
				
			} else if(shop != null){
				
				landuse = shop;
				
			}
			
			landuse = getLanduseType(landuse);
			
			if(landuse != null){
				
				addGeometry(landuse, geometry);
				
			}
			
		}
		
		set.close();
		statement.close();
		
	}
	
	@SuppressWarnings("unused")
	private void getWayBasedLanduseData(Connection connection) throws SQLException{
		
		log.info("Processing way landuse data...");
		
		Statement statement = connection.createStatement();
		
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_ID + ", " + DatabaseConstants.ATT_NODES + ", "
				+ DatabaseConstants.ATT_TAGS + " from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_WAYS + " where "
				+ DatabaseConstants.ATT_TAGS + " @> ARRAY['" + DatabaseConstants.ATT_LANDUSE + "'];");
		
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
						
						sb.append(" " + DatabaseConstants.ATT_ID + " = '" + nodes[i] + "' or");
						
					} else{
						
						sb.append(" " + DatabaseConstants.ATT_ID + " = '" + nodes[i] + "'");
						
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
			
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ATT_PARTS + ", " + DatabaseConstants.ATT_MEMBERS + ", "
				+ DatabaseConstants.ATT_TAGS + " from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_RELATIONS +
				" where " + DatabaseConstants.ATT_TAGS + " @> ARRAY['" + DatabaseConstants.ATT_LANDUSE + "'];");
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long[] parts = (Long[])set.getArray(DatabaseConstants.ATT_PARTS).getArray();
			String[] tags = (String[])set.getArray(DatabaseConstants.ATT_TAGS).getArray();
			String[] members = (String[])set.getArray(DatabaseConstants.ATT_MEMBERS).getArray();
			
			for(int i = 0; i < parts.length; i++){
				
				if(i < parts.length - 1){
					sb.append(" " + DatabaseConstants.ATT_ID + " = '" + parts[i] + "' or");
				} else {
					sb.append(" " + DatabaseConstants.ATT_ID + " = '" + parts[i] + "'");
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
	
	private Map<Long,LinearRing> getAffiliatedWays(Connection connection, String arguments) throws SQLException{

		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select * from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_WAYS
				+ " where" + arguments);
		
		Map<Long, LinearRing> linearRingSet = new HashMap<>();
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long id = set.getLong(DatabaseConstants.ATT_ID);
			
			Long[] nodes = (Long[])set.getArray(DatabaseConstants.ATT_NODES).getArray();
			
			for(int i = 0; i < nodes.length; i++){
				
				if(i < nodes.length - 1){
					
					sb.append(" " + DatabaseConstants.ATT_ID + " = '" + nodes[i] + "' or");
					
				} else{
					
					sb.append(" " + DatabaseConstants.ATT_ID + " = '" + nodes[i] + "'");
					
				}
				
			}

			LinearRing p = createLinearRing(connection, sb.toString(),nodes);
			if(p != null){
				linearRingSet.put(id, p);
			}
			
		}

		return linearRingSet;
		
	}
	
	private LinearRing createLinearRing(Connection connection, String arguments, Long[] nodes) throws SQLException{
		
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select * from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_NODES
				+ " where" + arguments);
		
		Coordinate[] coordinates = new Coordinate[nodes.length];
		
		//in case one or more coordinates are contained several times,
		//we need to store them in order to access them again
		Map<Long, Coordinate> coordMap = new HashMap<>();
		
		while(set.next()){
			
			Long id = set.getLong(DatabaseConstants.ATT_ID);
			
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
	
	private static String getLanduseType(String landuseTag){
		
		if(landuseTag.equals("college") || landuseTag.equals("school") || landuseTag.equals("university")){
			
			return ActivityTypes.EDUCATION;
			
		} else if(landuseTag.equals("commercial") || landuseTag.equals("industrial")){
			
			return ActivityTypes.WORK;
			
		} else if(landuseTag.equals("hospital")){
			
			return ActivityTypes.OTHER;
			
		} else if(landuseTag.equals("recreation_ground") || landuseTag.equals("park") || landuseTag.equals("village_green")){
			
			return ActivityTypes.LEISURE;
			
		} else if(landuseTag.equals("residential")){
			
			return "residential";
			
		} else if(landuseTag.equals("retail")){
			
			return ActivityTypes.SHOPPING;
			
		}
		
		return null;
		
	}
	
	private void readAmenities(Connection connection) throws SQLException, ParseException{
		
		log.info("Reading in amenities...");

		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ST_ASTEXT + "(" + DatabaseConstants.ATT_WAY + "), " +
				DatabaseConstants.ATT_AMENITY + ", " + DatabaseConstants.ATT_LEISURE + ", " + DatabaseConstants.ATT_SHOP + " from "
				+ DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_POINTS + " where " + DatabaseConstants.ST_WITHIN + "("
				+ DatabaseConstants.ATT_WAY + "," + DatabaseConstants.ST_GEOMFROMTEXT + "('" +
				Geoinformation.getCompleteGeometry().toString() + "',4326)) and (" + DatabaseConstants.ATT_AMENITY + " is not null or " 
				+ DatabaseConstants.ATT_LEISURE + " is not null or " + DatabaseConstants.ATT_SHOP + " is not null)");
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.ST_ASTEXT));
			
			String amenity = set.getString(DatabaseConstants.ATT_AMENITY);
			String leisure = set.getString(DatabaseConstants.ATT_LEISURE);
			String shop = set.getString(DatabaseConstants.ATT_SHOP);
			
			String landuse = null;
			
			if(amenity != null){
				
				landuse = amenity;
				
			} else if(leisure != null){
				
				landuse = leisure;
				
			} else if(shop != null){
				
				landuse = shop;
				
			}
			
			String actType = getAmenityType(landuse);
			
			if(actType != null){

				addGeometry(actType, geometry);
				
			}
			
		}
		
		set.close();
		statement.close();
		
	}
	
	private void addGeometry(String landuse, Geometry g){
		
		if(g != null){
			
			if(g.isValid()){

				for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
					
					if(au.getGeometry().contains(g) || au.getGeometry().touches(g) || au.getGeometry().intersects(g)){
						
						au.addLanduseGeometry(landuse, g);
						
						if(!Geoinformation.actType2QT.containsKey(landuse)){
							
							Geoinformation.actType2QT.put(landuse, new QuadTree<>(boundingBox.getCoordinates()[0].x,
									boundingBox.getCoordinates()[0].y, boundingBox.getCoordinates()[2].x, boundingBox.getCoordinates()[2].y));
							
						} 
						
						Coord c = ct.transform(MGC.point2Coord(g.getCentroid()));
						
						if(this.boundingBox.contains(MGC.coord2Point(c))){
							
							Geoinformation.actType2QT.get(landuse).put(c.getX(), c.getY(), g);
							
						}
						
					}
				
				}
				
			} else {
				
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
		
		WKTReader wktReader = new WKTReader();
		
		Statement statement = connection.createStatement();
		String s = "select " + DatabaseConstants.ATT_BUILDING + ", " + DatabaseConstants.ST_ASTEXT + "(" + DatabaseConstants.ATT_WAY
				+ ") from " + DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_POLYGONS + " where "	+
				DatabaseConstants.ST_WITHIN + "(" + DatabaseConstants.ATT_WAY + "," + DatabaseConstants.ST_GEOMFROMTEXT +
				"('" + Geoinformation.getCompleteGeometry().toString() + "',4326))"	+ " and " + DatabaseConstants.ATT_BUILDING + " is not null";
		ResultSet set = statement.executeQuery(s);
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString(DatabaseConstants.ST_ASTEXT));
			String building = set.getString(DatabaseConstants.ATT_BUILDING);
			
			for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
				
				String landuse = getTypeOfBuilding(building);
				
				if(au.getGeometry().contains(geometry)){
					
					if(landuse != null){
						
						if(!au.getBuildingsGeometries().containsKey(landuse)){
							
							au.getBuildingsGeometries().put(landuse, new ArrayList<>());
							
						}
							
						au.getBuildingsGeometries().get(landuse).add(geometry);
							
						continue;
						
					}
					
					for(String use : au.getLanduseGeometries().keySet()){
						
						if(au.getLanduseGeometries().get(use).contains(geometry)){

							if(!au.getBuildingsGeometries().containsKey(use)){
								
								au.getBuildingsGeometries().put(use, new ArrayList<>());
								
							} else {
								
								au.getBuildingsGeometries().get(use).add(geometry);
								
							}
							
						}
						
					}
					
				}
				
			}
				
		}
		
	}
	
	@SuppressWarnings("unused")
	private void readPtStops(Connection connection) throws SQLException, ParseException{
		
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select " + DatabaseConstants.ST_ASTEXT + "(" + DatabaseConstants.ATT_WAY + ") from "
				+ DatabaseConstants.SCHEMA_OSM + "." + DatabaseConstants.TABLE_POINTS + " where " + DatabaseConstants.ST_WITHIN + " ("
				+ DatabaseConstants.ATT_WAY + "," + DatabaseConstants.ST_GEOMFROMTEXT + "('" + Geoinformation.getCompleteGeometry().toString()
				+ "',4326));");
		
		Set<Geometry> ptStops = new HashSet<>();
		
		while(set.next()){
			
			Geometry g = wktReader.read(set.getString(DatabaseConstants.ST_ASTEXT));
			
			for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
				
				if(au.getGeometry().contains(g)){
					
					//TODO set the buffer radius to whatever the search radius of the transit router is...
					//default is 1000, so we will leave it like this for the time being
					ptStops.add(g.buffer(1000));
					
				}
				
			}
			
			Geoinformation.catchmentAreaPt = gFactory.buildGeometry(ptStops);
			
		}
		
	}
	
	private static String getAmenityType(String tag){
		
		if(OsmKey2ActivityType.education.contains(tag)){
			
			return ActivityTypes.EDUCATION;
			
		} else if(OsmKey2ActivityType.groceryShops.contains(tag) || OsmKey2ActivityType.miscShops.contains(tag)){
			
			return ActivityTypes.SHOPPING;
			
		} else if(OsmKey2ActivityType.leisure.contains(tag)){
			
			return ActivityTypes.LEISURE;
			
		} else if(OsmKey2ActivityType.otherPlaces.contains(tag)) {
			
			return ActivityTypes.OTHER;
			
		} else{
			
			return null;
			
		}
		
	}

	private static String getTypeOfBuilding(String buildingTag){
		
		if(buildingTag.equals("apartments") || buildingTag.equals("hut") || buildingTag.equals("detached") || buildingTag.equals("house") || buildingTag.equals("semi")
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
	
}
