package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.contrib.carsharing.vehicles.FFVehicleImpl;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.vehicles.VehicleType;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.DatabaseConstants;

public class CreateCarsharingVehicles {
	
	public static void run(Configuration configuration, final Scenario scenario){

		Map<Coord, FFVehicleImpl> ffVehicles = new HashMap<>();
		Map<Coord, TwoWayEntry> stations = new HashMap<>();
		
		OsVehicleTypes vTypes = new OsVehicleTypes();
		Map<Id<VehicleType>, VehicleType> types = vTypes.getAll();
		for(VehicleType t : types.values()){
			scenario.getVehicles().addVehicleType(t);
		}
		
		CoordinateTransformation trafo = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:32632");
		
		try {
		
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL +
					configuration.getLocalPort() + "/" + DatabaseConstants.SHARED_DB, configuration.getDatabaseUsername(),
					configuration.getDatabasePassword());
			
			if(connection != null){
				
				String statkQuery = "select distinct on(key) * from vehicle_sightings_merged where city='osnabrueck' and stationary='t' and remote_id is not null"
						+ " and last_seen_at between '2016-08-01 00:00' and '2016-09-01 00:00';";
				
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(statkQuery);
				
				while(resultSet.next()){
				
					String key = resultSet.getString("key");
					double x = resultSet.getDouble("longitude");
					double y = resultSet.getDouble("latitude");
					Coord c = trafo.transform(new Coord(x, y));
					
					TwoWayEntry current = null;
					
					if(!stations.containsKey(c)){
						
						current = new TwoWayEntry(Integer.toString(stations.size()));
						current.c = c;
						stations.put(c, current);
						
					}
					
					current = stations.get(c);
					
					StationBasedVehicle vehicle = new StationBasedVehicle("kompakt", key, current.id, "twoway", "stadtteilauto");
					current.vehicles.add(vehicle);
					
				}
				
				resultSet.close();
				
				String flowkQuery = "select distinct on(key) * from vehicle_sightings_merged where city='osnabrueck' and stationary='f' and remote_id is not null"
						+ " and last_seen_at between '2016-09-01 00:00' and '2016-09-01 23:59';";
				resultSet = statement.executeQuery(flowkQuery);
				
				while(resultSet.next()){
					
					String key = resultSet.getString("key");
					double x = resultSet.getDouble("longitude");
					double y = resultSet.getDouble("latitude");
					Coord c = trafo.transform(new Coord(x, y));
					FFVehicleImpl vehicle = new FFVehicleImpl("flowk", key, "stadtteilauto");
					ffVehicles.put(c, vehicle);
					
				}
				
				resultSet.close();
				statement.close();
				
			}
			
			connection.close();
		
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
		new CarsharingVehiclesWriter().write("/home/dhosse/osGtfs/csVehicles.xml", stations, ffVehicles, "stadtteilauto");

	}
	
	static class TwoWayEntry{
		String id;
		Coord c;
		List<CSVehicle> vehicles = new ArrayList<>();
		TwoWayEntry(String id){
			this.id = id;
		}
	}
	
}