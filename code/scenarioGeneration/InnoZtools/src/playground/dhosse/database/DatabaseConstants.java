package playground.dhosse.database;

public final class DatabaseConstants {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	public static final String PSQL_DRIVER = "org.postgresql.Driver";
	public static final String PSQL_PREFIX = "jdbc:postgresql://localhost:";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//DATABASES//////////////////////////////////////////////////////////////////////////////
	public static final String GEODATA_DB = "geodata";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//DATABASE-SPECIFIC CONSTANTS////////////////////////////////////////////////////////////
	public enum schemata { gadm, osm };
	public enum tables { districts, osm_line, osm_nodes, osm_point, osm_polygon, osm_rels, osm_ways };

	public enum functions { st_astext, st_geomfromtext, st_within };
	
	public static final String BLAND = "cca_4";
	public static final String MUN_KEY = "cca_4";
	public static final String ATT_LANDUSE = "landuse";
	public static final String ATT_AMENITY = "amenity";
	public static final String ATT_LEISURE = "leisure";
	public static final String ATT_SHOP = "shop";
	public static final String ATT_WAY = "way";
	public static final String ATT_ID = "id";
	public static final String ATT_NODES = "nodes";
	public static final String ATT_TAGS = "tags";
	public static final String ATT_PARTS = "parts";
	public static final String ATT_MEMBERS = "members";
	public static final String ATT_LON = "lon";
	public static final String ATT_LAT = "lat";
	public static final String ATT_BUILDING = "building";
	public static final String ATT_GEOM = "geom";
	
	public static final String ATT_ACCESS = "access";
	public static final String ATT_GEOMETRY = "st_astext";
	public static final String ATT_HIGHWAY = "highway";
	public static final String ATT_OSM_ID = "osm_id";
	public static final String ATT_JUNCTION = "junction";
//	public static final String TAG_LANES = "lanes";
//	public static final String TAG_MAXSPEED = "maxspeed";
	public static final String ATT_ONEWAY = "oneway";
	/////////////////////////////////////////////////////////////////////////////////////////
	
}
