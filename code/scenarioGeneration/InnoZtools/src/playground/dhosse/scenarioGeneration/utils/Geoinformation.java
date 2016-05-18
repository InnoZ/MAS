package playground.dhosse.scenarioGeneration.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jfree.util.Log;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import playground.dhosse.scenarioGeneration.Configuration;
import playground.dhosse.utils.QuadTree;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * Class for reading, storing and accessing geoinformation, such as
 * <ul>
 * <li>administrative borders
 * <li>osm data
 * <ul>
 * <li>landuse
 * <li>buildings
 * <li>amenities (e.g. shops, schools)
 * </ul>
 * </ul>
 * 
 * There are standard procedures for reading shapefiles or geometry tables of the MobilityDatahub.
 * 
 * @author dhosse
 *
 */
public class Geoinformation {
	
	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	static final String AUTH_KEY_WGS84 = "EPSG:4326";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private static Map<String, AdministrativeUnit> adminUnits = new HashMap<>();
	private static Geometry completeGeometry;
	protected static Map<String,QuadTree<Geometry>> actType2QT = new HashMap<>();
	protected static Geometry catchmentAreaPt;
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	
	//no instance!
	private Geoinformation(){};
	
	/**
	 * Reads the geometries of the specified id(s) from an ESRI shapefile into the
	 * administrative unit collection.
	 * 
	 * @param filename Input shapefile path.
	 * @param ids The id's of the geometries we want to read in.
	 */
	public static void readGeodataFromShapefile(String filename, Set<String> ids){
		
		// Read in the shapefile
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		// Go through all features. If the GKZ (Gemeindekennzahl) is one of the specified ids,
		// add a new administrative unit with the feature's geometry.
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			if(ids.contains(kennzahl)){
				
				AdministrativeUnit au = new AdministrativeUnit(kennzahl);
				au.setGeometry((Geometry)feature.getDefaultGeometry());
				adminUnits.put(kennzahl, au);
				
			}
			
		}
		
	}
	
	/**
	 * Imports administrative borders and OpenStreetMap data from the mobility database.
	 * 
	 * 
	 * @param configuration The configuration for the scenario generation process.
	 * @param ids The survey area id(s).
	 * @param scenario The MATSim scenario.
	 */
	public static void readGeodataFromDatabase(Configuration configuration, Set<String> ids,
			Scenario scenario) {
		
		// Create a new database connection
		DatabaseReader dbConnection = new DatabaseReader();
		
		try {
			
			// Create a postgresql database connection
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" +
					configuration.getLocalPort() + "/geodata", configuration.getDatabaseUsername(),
					configuration.getPassword());
			
			if(connection != null){

				Log.info("Successfully connected with geodata database...");
				
				// Read the administrative borders that have one of the specified ids
				dbConnection.readAdminBorders(connection, configuration, ids);
				
				// If no administrative units were created, we are unable to proceed
				if(adminUnits.size() < 1){
				
					Log.error("No administrative boundaries were created!");
					Log.error("Maybe the ids you specified don't exist in the database.");
					throw new RuntimeException("Execution aborts...");
					
				}
				
				// Otherwise, read in the OSM data
				dbConnection.readOsmData(connection, configuration);
				
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
	 * Same functionality as {@link #readGeodataFromShapefile(String, Set)}. The difference is that
	 * the ids of the shapefile features are tested wheter they start with any of the id strings inside
	 * the filter ids set. By that, it's possible to read in e.g. all municipalities of a district or region.
	 * 
	 * @param filename Input shapefile path.
	 * @param filterIds
	 */
	public static void readGeodataFromShapefileWithFilter(String filename, Set<String> filterIds){
		
		// Read in the shapefile
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		// Go through all features. If the GKZ (Gemeindekennzahl) starts with one of the specified ids,
		// add a new administrative unit with the feature's geometry.
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			for(String id : filterIds){
				
				if(kennzahl.startsWith(id)){
	
					AdministrativeUnit au = new AdministrativeUnit(kennzahl);
					au.setGeometry((Geometry)feature.getDefaultGeometry());
					adminUnits.put(kennzahl, au);
					break;
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Sums up the weights for all landuse geometries that are mapped to the specified key.
	 * 
	 * @param key A string representing the landuse type of interest.
	 * @return The total weight of the landuse geometries inside the survey area.
	 */
	public static double getTotalWeightForLanduseKey(String key){
		
		double weight = 0.;
		
		for(AdministrativeUnit au : adminUnits.values()){
			
			weight += au.getWeightForKey(key);
			
		}
		
		return weight;
		
	}
	
	public static Map<String, AdministrativeUnit> getAdminUnits(){
		
		return adminUnits;
		
	}
	
	public static Geometry getCompleteGeometry(){
		
		return completeGeometry;
		
	}
	
	public static void setCompleteGeometry(Geometry g){
		
		completeGeometry = g;
		
	}
	
	public static QuadTree<Geometry> getQuadTreeForActType(String actType){
		
		return actType2QT.get(actType);
		
	}
	
	public static Geometry getCatchmentAreaPt(){
		
		return catchmentAreaPt;
		
	}
	
}
