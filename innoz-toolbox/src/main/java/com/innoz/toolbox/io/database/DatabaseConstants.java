package com.innoz.toolbox.io.database;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * Some constant values that are used in all InnoZ databases.
 * 
 * @author dhosse
 *
 */
public final class DatabaseConstants {

	//DATABASES//////////////////////////////////////////////////////////////////////////////
	public static final String DEFAULT_USER = "postgres";
	public static final String DEFAULT_PASSWORD = "postgres";
	public static final String GEODATA_DB = "geodata";
	public static final String SHARED_DB = "shared_mobility";
	public static final String SIMULATIONS_DB = "simulated_mobility";
	public static final String SIMULATIONS_DB_LOCAL = "innoz_simulation_development";
	public static final String SURVEYS_DB = "surveyed_mobility";
	public static final String TRACKS_DB = "modalyzer";
	public static final String POPULATIONFORECAST_DB = "population";
	public static final String INTERFACE_DEVEL = "mas_interface_development";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param name String identifier for the database table, normally 'schemaname'.'tablename', must not be null!
	 */
	public static DatabaseTable getDatabaseTable(final String name) {
		
		switch(name) {
			case "persons": return DatabaseTable.personsTable;
			case "plans": return DatabaseTable.plansTable;
			default: return null;
		}
		
	}
	
	//DATABASE-SPECIFIC CONSTANTS////////////////////////////////////////////////////////////
	
	public static final String PLANS_TABLE = "plans";
	
	public enum schemata { gadm, osm };
	public enum tables { districts, osm_germany_line, osm_germany_nodes, osm_germany_point, osm_germany_polygon,
		osm_germany_rels, osm_germany_ways };

	public enum functions { st_astext, st_geomfromtext, st_within };
	
	public static final String BLAND = "cca_1";
	public static final String MUN_KEY = "cca_4";
	
	public static final String ATT_ACCESS = "access";
	public static final String ATT_AMENITY = "amenity";
	public static final String ATT_BUILDING = "building";
	public static final String ATT_GEOM = "geom";
	public static final String ATT_HIGHWAY = "highway";
	public static final String ATT_ID = "id";
	public static final String ATT_JUNCTION = "junction";
	public static final String ATT_LANDUSE = "landuse";
	public static final String TAG_LANES = "lanes";
	public static final String ATT_LAT = "lat";
	public static final String ATT_LEISURE = "leisure";
	public static final String ATT_LON = "lon";
	public static final String TAG_MAXSPEED = "maxspeed";
	public static final String ATT_MEMBERS = "members";
	public static final String ATT_NODES = "nodes";
	public static final String ATT_ONEWAY = "oneway";
	public static final String ATT_OSM_ID = "osm_id";
	public static final String ATT_PARTS = "parts";
	public static final String ATT_RAILWAY = "railway";
	public static final String ATT_SHOP = "shop";
	public static final String ATT_TAGS = "tags";
	public static final String ATT_WAY = "way";
	
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public static class DatabaseTable {
		
		// final instances
		private static final DatabaseTable personsTable = new DatabaseTable("persons",
				new String[]{"id", "age", "sex", "license", "car_avail", "employed"});
		
		private static final DatabaseTable plansTable = new DatabaseTable("plans", new String[]{"person_id", "element_index", "selected",
				"act_type", "act_coord", "act_start", "act_end", "act_duration", "leg_mode"});
		
		// members
		private final String tablename;
		private final List<String> columnNames;
		
		private DatabaseTable(String tablename, String[] columnNames) {
			
			this.tablename = tablename;
			this.columnNames = Arrays.asList(columnNames);
			
		}
		
		public String getTableName(){
			return this.tablename;
		}
		
		public List<String> getColumnNames(){
			return this.columnNames;
		}
	}
	
}
