package playground.dhosse.scenarios.generic.utils;

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
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.utils.QuadTree;
import playground.dhosse.utils.osm.OsmKey2ActivityType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

class OsmDbConnection {

	private static final Logger log = Logger.getLogger(OsmDbConnection.class);
	
	private final GeometryFactory gFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.maximumPreciseValue));
	private final WKTReader wktReader = new WKTReader();
	
	//FIELD NAMES (for mobility database version)
	private final String SCHEMA_GADM = "gadm";
	private final String TABLE_DISTRICTS = "districts";
	
	private final String SCHEMA_OSM = "osm";
	private final String TABLE_RELATIONS = "planet_osm_rels";
	private final String TABLE_WAYS = "osm_ways";
	private final String TABLE_NODES = "osm_nodes";
	private final String TABLE_POLYGONS = "osm_polygon";
	private final String TABLE_POINTS = "osm_point";
	
	private final String BLAND = "cca_4";
	private final String MUN_KEY = "cca_4";
	
	private Geometry boundingBox;
	private CoordinateTransformation ct;
	
	OsmDbConnection(final Scenario scenario){}
	
	public void readAdminBorders(Connection connection, Configuration configuration, Set<String> ids) throws SQLException,
		NoSuchAuthorityCodeException, FactoryException, ParseException, MismatchedDimensionException, TransformException{

		log.info("Reading administrative borders from database...");
		
		log.info("Successfully connected with geoinformation database");

		Statement statement = connection.createStatement();
		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		
		for(String id : ids){

			if(i < ids.size() - 1){
				
				builder.append(" " + MUN_KEY + " like '" + id + "%' OR");
				
			} else {
				
				builder.append(" " + MUN_KEY + " like '" + id + "%'");
				
			}
			
			i++;
			
		}
		
		ResultSet set = statement.executeQuery("select " + BLAND + "," + MUN_KEY + ", st_astext(geom)"
				+ " from " + SCHEMA_GADM + "." + TABLE_DISTRICTS + " where" + builder.toString());

//		ResultSet set = statement.executeQuery("select vkz_nr, st_astext(geom) from gadm.berlin_vz;");
		
		MathTransform t = CRS.findMathTransform(CRS.decode("EPSG:4326",true), CRS.decode(configuration.getCrs(),true));
		
		while(set.next()){
			
			//TODO attributes have to be added to the table
			String key = set.getString(MUN_KEY).substring(1);
//			String key = set.getString("vkz_nr");
			String g = set.getString("st_astext");
//			long bland = set.getLong(BLAND);
//				int districtType = set.getInt("");
//				int municipalityType = set.getInt("");
//				int regionType = set.getInt("");
			
			if(g != null){
				
				if(!g.isEmpty()){
					
					AdministrativeUnit au = new AdministrativeUnit(key);
					Geometry geometry = wktReader.read(g);
					au.setGeometry(geometry);
//					au.setBland((int)bland);
//					au.setDistrictType(districtType);
//					au.setMunicipalityType(municipalityType);
//					au.setRegionType(regionType);
					Geoinformation.getAdminUnits().put(key, au);
					
					if(Geoinformation.getCompleteGeometry() == null){
						Geoinformation.setCompleteGeometry(geometry);
					} else{
						Geoinformation.getCompleteGeometry().union(geometry);
					}
					
				}
				
			}
			
		}
		
		List<Geometry> geometryCollection = new ArrayList<>();
		for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
			geometryCollection.add(au.getGeometry());
		}
		
		Geoinformation.setCompleteGeometry(gFactory.buildGeometry(geometryCollection).getEnvelope());
		Geometry copy = (Geometry) Geoinformation.getCompleteGeometry().clone();
//		for(Coordinate c : copy.getCoordinates()){
//			double temp = c.y;
//			c.y = c.x;
//			c.x = temp;
//		}
		this.boundingBox = JTS.transform(copy,t).getEnvelope();
		
		set.close();
		statement.close();
				
	}
	
	public void readOsmData(Connection connection, Configuration configuration){

		ct = TransformationFactory.getCoordinateTransformation("EPSG:4326", configuration.getCrs());
		
		log.info("Reading osm data...");
		
		try {
			
			readLanduseData(connection);
			readAmenities(connection);
			
			if(configuration.isUsingBuildings()){
				
				readBuildings(connection);
				
			}
				
//			readPtStops(connection);
			
			log.info("...done.");
			
		} catch (SQLException | ParseException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	void readLanduseData(Connection connection){
		
		log.info("Reading in landuse data...");
		
		try {

//			getRelationBasedLanduseData(connection);
//			getWayBasedLanduseData(connection);
			//parse polygons for landuse data
			getPolygonBasedLanduseData(connection);
		
		} catch (SQLException | ParseException e) {

			e.printStackTrace();
			
		}
		
		log.info("...done");
		
	}

	void getPolygonBasedLanduseData(Connection connection) throws ParseException, SQLException {
		
		log.info("Processing polygon landuse data...");
		
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select landuse, amenity, leisure, shop, st_astext(way) from "
				+ SCHEMA_OSM + "." + TABLE_POLYGONS + " where st_within(way,st_geomfromtext('" + //planet_osm_polygon
				Geoinformation.getCompleteGeometry().toString() + "',4326))"
						+ " and (landuse is not null or amenity is not null or "
						+ "leisure is not null or shop is not null);");
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString("st_astext"));
			String landuse = set.getString("landuse");
			String amenity = set.getString("amenity");
			String leisure = set.getString("leisure");
			String shop = set.getString("shop");
			
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
	
	void getWayBasedLanduseData(Connection connection) throws SQLException{
		
		log.info("Processing way landuse data...");
		
		Statement statement = connection.createStatement();
		
		ResultSet set = statement.executeQuery("select id,nodes, tags from "
				+ SCHEMA_OSM + "." + TABLE_WAYS + " where tags @> ARRAY['landuse'];");
		
		while(set.next()){
			
			String[] tags = (String[])set.getArray("tags").getArray();
			
			String landuse = null;
			int k = 0;
			
			do{
				
				landuse = getLanduseType(tags[k]);
				k++;
				
			} while(landuse == null && k < tags.length);
			
			if(landuse != null){

				StringBuilder sb = new StringBuilder();
				
				Long[] nodes = (Long[])set.getArray("nodes").getArray();
				
				for(int i = 0; i < nodes.length; i++){
					
					if(i < nodes.length - 1){
						
						sb.append(" id = '" + nodes[i] + "' or");
						
					} else{
						
						sb.append(" id = '" + nodes[i] + "'");
						
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
	
	void getRelationBasedLanduseData(Connection connection) throws SQLException {
		
		log.info("Processing relation landuse data...");
		
		//parse relations for landuse tags to get multipolygon geometries
		Statement statement = connection.createStatement();
			
		ResultSet set = statement.executeQuery("select parts, members, tags from "
				+ SCHEMA_OSM + "." + TABLE_RELATIONS + " where tags @> ARRAY['landuse'];");
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long[] parts = (Long[])set.getArray("parts").getArray();
			String[] tags = (String[])set.getArray("tags").getArray();
			String[] members = (String[])set.getArray("members").getArray();
			
			for(int i = 0; i < parts.length; i++){
				
				if(i < parts.length - 1){
					sb.append(" id = '" + parts[i] + "' or");
				} else {
					sb.append(" id = '" + parts[i] + "'");
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
		ResultSet set = statement.executeQuery("select * from " + SCHEMA_OSM + "." + TABLE_WAYS + " where" + arguments);
		
		Map<Long, LinearRing> linearRingSet = new HashMap<>();
		
		while(set.next()){
			
			StringBuilder sb = new StringBuilder();
			
			Long id = set.getLong("id");
			
			Long[] nodes = (Long[])set.getArray("nodes").getArray();
			
			for(int i = 0; i < nodes.length; i++){
				
				if(i < nodes.length - 1){
					
					sb.append(" id = '" + nodes[i] + "' or");
					
				} else{
					
					sb.append(" id = '" + nodes[i] + "'");
					
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
		ResultSet set = statement.executeQuery("select * from " + SCHEMA_OSM + "." + TABLE_NODES + " where" + arguments);
		
		Coordinate[] coordinates = new Coordinate[nodes.length];
		
		//in case one or more coordinates are contained several times,
		//we need to store them in order to access them again
		Map<Long, Coordinate> coordMap = new HashMap<>();
		
		while(set.next()){
			
			Long id = set.getLong("id");
			
			int idx = 0;
			for(int i = 0; i < nodes.length; i++){
				Long curr = nodes[i];
				if(curr.equals(id)){
					
					idx = i;
					break;
					
				}
				
			}
			
			double lon = set.getDouble("lon")/Math.pow(10, 7);
			double lat = set.getDouble("lat")/Math.pow(10, 7);
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
	
	void readAmenities(Connection connection) throws SQLException, ParseException{
		
		log.info("Reading in amenities...");

		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select st_astext(way), amenity, leisure, shop"
				+ " from " + SCHEMA_OSM + "." + TABLE_POINTS + " where"
				+ " st_within(way,st_geomfromtext('" +
				Geoinformation.getCompleteGeometry().toString() + "',4326)) and ("
						+ "amenity is not null or leisure is not null or shop is not null)");
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString("st_astext"));
			
			String amenity = set.getString("amenity");
			String leisure = set.getString("leisure");
			String shop = set.getString("shop");
			
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
	
	private int counter = 0;
	
	private void addGeometry(String landuse, Geometry g){
		
		if(g != null){
			
			if(g.isValid()){

				for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
					
					if(au.getGeometry().contains(g) || au.getGeometry().touches(g) || au.getGeometry().intersects(g)){
						
						au.addLanduseGeometry(landuse, g);
//						if(!landuse.equals(ActivityTypes.LEISURE)&&!landuse.equals("residential")){
//							au.addLanduseGeometry(ActivityTypes.WORK, g);
//						}
//						if(!landuse.equals(ActivityTypes.LEISURE)&&!landuse.equals("residential")&&
//								!landuse.equals(ActivityTypes.SHOPPING)){
//							au.addLanduseGeometry(ActivityTypes.OTHER, g);
//						}
						
						if(!Geoinformation.actType2QT.containsKey(landuse)){
							
							Geoinformation.actType2QT.put(landuse, new QuadTree<>(boundingBox.getCoordinates()[0].x,
									boundingBox.getCoordinates()[0].y, boundingBox.getCoordinates()[2].x, boundingBox.getCoordinates()[2].y));
							
						} 
//						if(!Geoinformation.actType2QT.containsKey(ActivityTypes.WORK)){
//							Geoinformation.actType2QT.put(ActivityTypes.WORK, new QuadTree<>(boundingBox.getCoordinates()[0].x,
//									boundingBox.getCoordinates()[0].y, boundingBox.getCoordinates()[2].x, boundingBox.getCoordinates()[2].y));
//						}
//						if(!Geoinformation.actType2QT.containsKey(ActivityTypes.OTHER)){
//							Geoinformation.actType2QT.put(ActivityTypes.OTHER, new QuadTree<>(boundingBox.getCoordinates()[0].x,
//									boundingBox.getCoordinates()[0].y, boundingBox.getCoordinates()[2].x, boundingBox.getCoordinates()[2].y));
//						}
						
						Coord c = ct.transform(MGC.point2Coord(g.getCentroid()));
						
						if(this.boundingBox.contains(MGC.coord2Point(c))){
							
							Geoinformation.actType2QT.get(landuse).put(c.getX(), c.getY(), g);
//							if(!landuse.equals(ActivityTypes.LEISURE)&&!landuse.equals("residential")){
//								Geoinformation.actType2QT.get(ActivityTypes.OTHER).put(c.getX(), c.getY(), g);
//							}
//							if(!landuse.equals(ActivityTypes.LEISURE)&&!landuse.equals("residential")){
//								Geoinformation.actType2QT.get(ActivityTypes.WORK).put(c.getX(), c.getY(), g);
//							}
							
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
		String s = "select building, st_astext(way) from "
				+ "planet_osm_polygon where st_within(way,st_geomfromtext('" +
				Geoinformation.getCompleteGeometry().toString() + "',4326))"
						+ " and building is not null";
		ResultSet set = statement.executeQuery(s);
		
		while(set.next()){
			
			Geometry geometry = wktReader.read(set.getString("st_astext"));
			String building = set.getString("building");
			
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
	
	public void readPtStops(Connection connection) throws SQLException, ParseException{
		
		Statement statement = connection.createStatement();
		ResultSet set = statement.executeQuery("select st_astext(way) from planet_osm_point where"
				+ " st_within(way,st_geomfromtext('" + Geoinformation.getCompleteGeometry().toString() + "',4326));");
		
		Set<Geometry> ptStops = new HashSet<>();
		
		while(set.next()){
			
			Geometry g = wktReader.read(set.getString("st_astext"));
			
			for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
				
				if(au.getGeometry().contains(g)){
					
					//TODO set the buffer radius to whatever the search radius of the transit router is...
					//default is 1000, so we will leave it like this for the time being
//					Point point = MGC.coord2Point(this.ct.transform(MGC.point2Coord((Point)g)));
					
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
		
		if(buildingTag.equals("apartments") || buildingTag.equals("hut") || buildingTag.equals("detached") || buildingTag.equals("house") || buildingTag.equals("semi") || buildingTag.equals("terrace")){
			
			return "residential";
			
		} else if(buildingTag.equals("barn") || buildingTag.equals("brewery") || buildingTag.equals("factory") || buildingTag.equals("office") || buildingTag.equals("warehouse")){
			
			return ActivityTypes.WORK;
			
		} else if(buildingTag.equals("castle") || buildingTag.equals("monument") || buildingTag.equals("palace")){
			
			//TODO
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
