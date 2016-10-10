package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.VehicleFuelTypeHandler;
import com.innoz.toolbox.io.database.handler.VehicleIdHandler;
import com.innoz.toolbox.io.database.handler.VehicleKbaSegmentHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyVehicle;

public class ReadVehicleDatabaseTask extends DatabaseTask {

	public ReadVehicleDatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids) {
		
		super(constants, geoinformation, ids);
		
		this.handlers = new HashSet<>();
		this.handlers.add(new VehicleIdHandler());
		this.handlers.add(new VehicleFuelTypeHandler());
		this.handlers.add(new VehicleKbaSegmentHandler());
		
	}

	@Override
	public void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException {

		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = "select * from mid2008.cars_raw";
		
		resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			boolean contained = true;
			String hhId = resultSet.getString(SurveyConstants.householdId(surveyType));
			
			if(container.getHouseholds() != null){
				
				contained = container.getHouseholds().containsKey(hhId);
			
			}
			
			if(contained){

				Map<String, String> attributes = new HashMap<>();
				attributes.put(SurveyConstants.vehicleId(surveyType), hhId + resultSet.getString(SurveyConstants.vehicleId(surveyType)));
				attributes.put(SurveyConstants.vehicleFuelType(surveyType), Integer.toString(resultSet.getInt(
						SurveyConstants.vehicleFuelType(surveyType))));
				attributes.put(SurveyConstants.vehicleSegmentKBA(surveyType), Integer.toString(resultSet.getInt(
						SurveyConstants.vehicleSegmentKBA(surveyType))));
				
				SurveyVehicle vehicle = new SurveyVehicle();
				
				for(DefaultHandler handler : this.handlers){
					
					handler.handle(vehicle, attributes, surveyType);
					
				}
				
				container.getHouseholds().get(hhId).getVehicleIds().add(vehicle.getId());
				container.getVehicles().put(vehicle.getId(), vehicle);
				
			}
			
		}
		
	}

}