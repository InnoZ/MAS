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
	public static final String SCHEMA_GADM = "gadm";
	public static final String TABLE_DISTRICTS = "districts";
	public static final String SCHEMA_OSM = "osm";
	public static final String TABLE_RELATIONS = "planet_osm_rels";
	public static final String TABLE_WAYS = "osm_ways";
	public static final String TABLE_NODES = "osm_nodes";
	public static final String TABLE_POLYGONS = "osm_polygon";
	public static final String TABLE_POINTS = "osm_point";
	public static final String BLAND = "cca_4";
	public static final String MUN_KEY = "cca_4";
	public static final String ST_ASTEXT = "st_astext";
	public static final String ST_GEOMFROMTEXT = "st_geomfromtext";
	public static final String ST_WITHIN = "st_within";
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
	/////////////////////////////////////////////////////////////////////////////////////////
	
}
