package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.sql.Connection;
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
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.vehicles.VehicleType;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.ConfigurationUtils;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;

public class CreateCarsharingVehicles {
	
	public static void main(String args[]) {
		
		Configuration configuration = ConfigurationUtils.createConfiguration();
		configuration.scenario().setSurveyAreaId("03404");
		CreateCarsharingVehicles.run(configuration, ScenarioUtils.createScenario(ConfigUtils.createConfig()));
		
	}
	
	public static void run(Configuration configuration, final Scenario scenario){

		Map<String, Map<Coord, VehicleEntry>> vehicles = new HashMap<>();
		int stationCount = 0;
		
		Map<Id<VehicleType>, VehicleType> types = OsVehicleTypes.getAll();
		for(VehicleType t : types.values()){
			scenario.getVehicles().addVehicleType(t);
		}
		
		CoordinateTransformation trafo = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:32632");
		
		try {
		
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.SHARED_DB);
			
			if(connection != null){
				
				String statkQuery = "select distinct on(key) * from vehicle_sightings_merged where city='koeln' and remote_id is not null"
						+ " and last_seen_at between '2016-08-01 00:00' and '2016-09-01 00:00';";
				
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(statkQuery);
				
				while(resultSet.next()){
				
					String key = resultSet.getString("key");
					double x = resultSet.getDouble("longitude");
					double y = resultSet.getDouble("latitude");
					Coord c = trafo.transform(new Coord(x, y));
					String provider = resultSet.getString("provider");
					String vehicleType = resultSet.getString("vehicle_type");
					
					if(!vehicles.containsKey(provider)) {
						
						vehicles.put(provider, new HashMap<>());
						
					}
					
					boolean stationary = resultSet.getBoolean("stationary");
					
					if(stationary) {
						
						TwoWayEntry current = null;
						
						Map<Coord, VehicleEntry> map = vehicles.get(provider);
						
						if(!map.containsKey(c)){
							
							current = new TwoWayEntry(Integer.toString(stationCount));
							stationCount++;
							current.c = c;
							map.put(c, current);
							
						}
						
						current = (TwoWayEntry) map.get(c);
						
						StationBasedVehicle vehicle = new StationBasedVehicle(vehicleType, key, current.id, "twoway", provider);
						current.vehicles.add(vehicle);
						
					} else {
						
						FFEntry vehicle = new FFEntry(key, c, vehicleType);
						vehicles.get(provider).put(c, vehicle);
						
					}
					
				}
				
				resultSet.close();
				
				statement.close();
				
			}
			
			connection.close();
		
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
		new CarsharingVehiclesWriter().write("/home/dhosse/osGtfs/csVehicles.xml", vehicles);

	}
	
	static class VehicleEntry {
		
	}
	
	static class TwoWayEntry extends VehicleEntry {
		String id;
		Coord c;
		List<CSVehicle> vehicles = new ArrayList<CSVehicle>();
		TwoWayEntry(String id){
			this.id = id;
		}
	}
	
	static class OnewayEntry extends VehicleEntry {
		String id;
		Coord c;
		int freeparking;
		List<CSVehicle> vehicles = new ArrayList<CSVehicle>();
		OnewayEntry(String id) {
			this.id = id;
		}
	}
	
	static class FFEntry extends VehicleEntry {
		String id;
		Coord c;
		String vehicleType;
		FFEntry(String id, Coord c, String vehicleType) {
			this.id = id;
			this.c = c;
			this.vehicleType = vehicleType;
		}
	}
	
}