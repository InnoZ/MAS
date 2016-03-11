package playground.dhosse.scenarios.generic.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * Class for reading and storing geoinformation.
 * 
 * @author dhosse
 *
 */
public class Geoinformation {
	
	private static Map<String, Geometry> geometries = new HashMap<String, Geometry>();
	
	//no instance!
	private Geoinformation(){};
	
	public static void readGeodataFromShapefile(String filename, Set<String> ids){
		
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			if(ids.contains(kennzahl)){
				
				geometries.put(kennzahl, (Geometry)feature.getDefaultGeometry());
				
			}
			
		}
		
	}
	
	public static void readGeodataFromDatabase(String filename, Set<String> ids) throws Exception{
		
		WKTReader wktReader = new WKTReader();
		
		Properties properties = new Properties();
		
		try {
			
			properties.load(new FileInputStream(new File("")));
			Class.forName(properties.getProperty("")).newInstance();
			Connection connection = DriverManager.getConnection(properties.getProperty(""),
					properties.getProperty(""), properties.getProperty(""));
			
			if(connection != null){

				Statement statement = connection.createStatement();
				ResultSet set = statement.executeQuery("select id,geometry from [table] where [condition]"
						+ "and ...");
				
				while(set.next()){
					
					String key = set.getString("id");
					String g = set.getString("geometry");
					
					if(g != null){
						
						if(!g.isEmpty()){
							
							geometries.put(key, wktReader.read(g));
							
						}
						
					}
					
				}
				
				connection.close();
				
			} else{
				
				throw new Exception("Database connection could not be established! Aborting...");
				
			}
			
		} catch (IOException | InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | ParseException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void readGeodataFromShapefileWithFilter(String filename, Set<String> filterIds){
		
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			for(String id : filterIds){
				
				if(kennzahl.startsWith(id)){
					
					geometries.put(kennzahl, (Geometry)feature.getDefaultGeometry());
					break;
					
				}
				
			}
			
		}
		
	}
	
	public static Map<String, Geometry> getGeometries(){
		
		return geometries;
		
	}
	
}
