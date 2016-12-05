package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.config.Configuration.DayType;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.LegDestinationHandler;
import com.innoz.toolbox.io.database.handler.LegDistanceHandler;
import com.innoz.toolbox.io.database.handler.LegIndexHandler;
import com.innoz.toolbox.io.database.handler.LegModeHandler;
import com.innoz.toolbox.io.database.handler.LegOriginHandler;
import com.innoz.toolbox.io.database.handler.LegPurposeHandler;
import com.innoz.toolbox.io.database.handler.LegTravelTimeHandler;
import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.handler.SurveyStage;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class ReadTripsDatabaseTask extends DatabaseTask {

	private final DayType dayType;
	
	public ReadTripsDatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids,
			DayType dayType){
		
		super(constants, geoinformation, ids);
		this.dayType = dayType;
		
		this.handlers = new HashSet<>();
		this.handlers.add(new LegDestinationHandler());
		this.handlers.add(new LegDistanceHandler());
		this.handlers.add(new LegIndexHandler());
		this.handlers.add(new LegModeHandler());
		this.handlers.add(new LegOriginHandler());
		this.handlers.add(new LegPurposeHandler());
		this.handlers.add(new LegTravelTimeHandler());
		
	}
	
	@Override
	public void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException{
		
		Map<String, List<SurveyStage>> personId2Stages = new HashMap<>();
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = null;
		
		String table = surveyType.equals("mid") ? "mid2008.trips_raw" : "srv2013.trips";
		
		q = "select * from " + table;
		
		if(dayType.equals(DayType.weekday)){
		
			q += " where " + SurveyConstants.dayOfTheWeek(surveyType) + " < 6";

		} else if(dayType.equals(DayType.weekend)){
			
			q += " where " + SurveyConstants.dayOfTheWeek(surveyType) + " > 5";
			
		}
		
		resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			String householdId = resultSet.getString(SurveyConstants.householdId(surveyType));
			boolean contained = true;
			
			if(container.getHouseholds() != null){
				
				contained = container.getHouseholds().containsKey(householdId);
				
			}
			
			String personId = resultSet.getString(SurveyConstants.personId(surveyType));
			
			if(contained && container.getPersons().containsKey(householdId + personId)){
				
				Map<String, String> attributes = new HashMap<>();
				
				attributes.put(SurveyConstants.sortedTripId(surveyType), resultSet.getString(SurveyConstants.sortedTripId(surveyType)));
				attributes.put(SurveyConstants.tripSource(surveyType), resultSet.getString(SurveyConstants.tripSource(surveyType)));
				attributes.put(SurveyConstants.tripSink(surveyType), resultSet.getString(SurveyConstants.tripSink(surveyType)));
				attributes.put(SurveyConstants.tripPurpose(surveyType), resultSet.getString(SurveyConstants.tripPurpose(surveyType)));
				attributes.put(SurveyConstants.tripDetailedPurpose(surveyType), resultSet.getString(SurveyConstants.tripDetailedPurpose(surveyType)));
				attributes.put(SurveyConstants.tripTravelDistance(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripTravelDistance(surveyType))));
				attributes.put(SurveyConstants.tripMode(surveyType), resultSet.getString(SurveyConstants.tripMode(surveyType)));
				attributes.put(SurveyConstants.tripTravelTime(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripTravelTime(surveyType))));
				attributes.put(SurveyConstants.tripDepartureHour(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripDepartureHour(surveyType))));
				attributes.put(SurveyConstants.tripDepartureMinute(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripDepartureMinute(surveyType))));
				attributes.put(SurveyConstants.tripDepartureDay(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripDepartureDay(surveyType))));
				attributes.put(SurveyConstants.tripArrivalHour(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripArrivalHour(surveyType))));
				attributes.put(SurveyConstants.tripArrivalMinute(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripArrivalMinute(surveyType))));
				attributes.put(SurveyConstants.tripArrivalDay(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.tripArrivalDay(surveyType))));
				
				int stichtag = resultSet.getInt(SurveyConstants.dayOfTheWeek(surveyType));
				
				SurveyStage stage = new SurveyStage();
				
				for(DefaultHandler handler : this.handlers){
					
					handler.handle(stage, attributes, surveyType);
					
				}
				
				SurveyPerson person = container.getPersons().get(householdId + personId);
				
				if(!person.getLogbook().containsKey(stichtag)){
					person.getLogbook().put(stichtag, new Logbook());
				}
				person.getLogbook().get(stichtag).getStages().add(stage);
				
				if(!personId2Stages.containsKey(personId)){
					
					personId2Stages.put(householdId + personId, new ArrayList<>());
					
				}
				
				personId2Stages.get(householdId + personId).add(stage);
				
			}
			
		}
		
		resultSet.close();
		statement.close();
		
	}
	
}