package playground.dhosse.scenarios.generic.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import playground.dhosse.scenarios.generic.Configuration;
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
 * @author dhosse
 *
 */
public class Geoinformation {
	
	private static Map<String, AdministrativeUnit> adminUnits = new HashMap<>();
	private static Geometry completeGeometry;
	
	protected static Map<String,QuadTree<Geometry>> actType2QT = new HashMap<>();
	
	protected static Geometry catchmentAreaPt;
	
	//no instance!
	private Geoinformation(){};
	
	public static void readGeodataFromShapefile(String filename, Set<String> ids){
		
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
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
	 * Imports administrative borders and mobility survey data from the mobility database.
	 * 
	 * @param configuration The configuration for the scenario generation process.
	 * @param ids The survey area id(s).
	 * @param scenario The MATSim scenario.
	 */
	public static void readGeodataFromDatabase(Configuration configuration, Set<String> ids, Scenario scenario) {
		
		OsmDbConnection osmDb = new OsmDbConnection(scenario);
		
		try {
			
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() + "/geodata",
					configuration.getDatabaseUsername(), configuration.getPassword());
			
			if(connection != null){

				osmDb.readAdminBorders(connection, configuration, ids);
				
				if(adminUnits.size() < 1){
				
					throw new RuntimeException("No administrative boundaries were created! Execution aborts...");
					
				}
				
				osmDb.readOsmData(connection, configuration);
				
			}
			
			connection.close();

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException | 
				MismatchedDimensionException | FactoryException | ParseException | TransformException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static void readGeodataFromShapefileWithFilter(String filename, Set<String> filterIds){
		
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
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
	
	public static double getTotalWeightForLanduseKey(String key){
		
		double weight = 0.;
		
		for(AdministrativeUnit au : adminUnits.values()){
			
			weight += au.getWeightForKey(key);
			
		}
		
		return weight;
		
	}
	
}
