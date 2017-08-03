package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.postgis.PGgeometry;
import org.postgis.Point;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.FFEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.TwoWayEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.VehicleEntry;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;

/**
 * 
 * Class that builds a carsharing system in the defined survey area. On each parking space, carsharing stations / vehicles are
 * generated using OpenStreetMap landuse data.
 * 
 * @author dhosse
 *
 */
public class CarsharingStationsOnParkingSpaces {
	
	/**
	 * Example usage in 3connect context.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		CarsharingStationsOnParkingSpaces.generate("stadtteilauto", "/home/dhosse/scenarios/3connect/carsharingVehiclesParking.xml");
	}
	
	/**
	 * 
	 * Generates carsharing stations on public parking spaces inside the survey area and writes these stations / vehicles into a
	 * MATSim charsharing vehicles file.
	 * 
	 * @param companyName A string representing the name of the carsharing company one wants to generate vehicles for.
	 * @param pathToOutputFile The path to the output carsharing vehicles xml file.
	 */
	public static void generate(String companyName, String pathToOutputFile) {
		
		// The map that stores the new carsharing vehicles. The vehicle positions are mapped to the company name string.
		Map<String, Map<Coord, VehicleEntry>> vehicles = new HashMap<>();
		vehicles.put(companyName, new HashMap<>());
		
		// Internal vehicle counter
		int vehicleCounter = 0;
		
		try {
			
			// Set up the psql port for port forwarding
			Controller.configuration().psql().setPsqlPort(9999);
			
			// Create a new connection to the geodata database
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
			
			Statement statement = connection.createStatement();
			
			// Build an sql query that receives all parking amenities from the OpenStreetMap table which lie within
			// the geometry of the survey area
			String geom = Geoinformation.getInstance().getSurveyAreaBoundingBox().toText();
			String query = "SELECT st_centroid(way) as centroid FROM osm.osm_germany_polygon WHERE amenity='parking' AND st_contains("
					+ "st_geomfromtext('" + geom + "',4326), way);";
			
			ResultSet results = statement.executeQuery(query);
			
			while(results.next()) {
				
				// The geometry should only contain one point since we queried for the centroids of the geometries
				PGgeometry geometry = (PGgeometry)results.getObject("centroid");
				Point point = geometry.getGeometry().getPoint(0);
				
				// Convert the point objects into coords
				// We need two coordinates here because the map entries are each mapped to their location and would overwrite each
				// other
				Coord coord = new Coord(point.x, point.y);
				Coord ffcoord = new Coord(point.x+1, point.y+1);
				
				// The actual generation of the carsharing vehicles (twoway and freefloating)
				// This part is specifically written for 3connect and could / should be changed in other project contexts
				TwoWayEntry twEntry = new TwoWayEntry("twoway_" + vehicles.get(companyName).size());
				twEntry.c = coord;
				twEntry.vehicles.add(new StationBasedVehicle("car", twEntry.id + "_" + vehicleCounter++, twEntry.id, "twoway",
						companyName));
				twEntry.vehicles.add(new StationBasedVehicle("car", twEntry.id + "_" + vehicleCounter++, twEntry.id, "twoway",
						companyName));
				vehicles.get(companyName).put(coord, twEntry);
				
				vehicles.get(companyName).put(ffcoord, new FFEntry("freefloating_" + vehicleCounter++, coord, "car"));
				
			}
			
			results.close();
			statement.close();
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
			
		}
		
		// When everything's finished, write the vehicles to an xml file
		new CarsharingVehiclesWriter().write(pathToOutputFile, vehicles);
		
	}
	
}