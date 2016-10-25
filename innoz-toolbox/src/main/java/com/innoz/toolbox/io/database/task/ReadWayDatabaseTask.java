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

import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.DayTypes;
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

public class ReadWayDatabaseTask extends DatabaseTask {

	private final DayTypes dayType;
	
	public ReadWayDatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids, DayTypes dayType){
		
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
		
		String table = surveyType.equals("mid") ? "mid2008.ways_raw" : "srv2013.ways";
		
		q = "select * from " + table;
		
		if(dayType.equals(DayTypes.weekday)){
		
			q += " where " + SurveyConstants.dayOfTheWeek(surveyType) + " < 6";

		} else if(dayType.equals(DayTypes.weekend)){
			
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
				
				attributes.put(SurveyConstants.sortedWayId(surveyType), resultSet.getString(SurveyConstants.sortedWayId(surveyType)));
				attributes.put(SurveyConstants.waySource(surveyType), resultSet.getString(SurveyConstants.waySource(surveyType)));
				attributes.put(SurveyConstants.waySink(surveyType), resultSet.getString(SurveyConstants.waySink(surveyType)));
				attributes.put(SurveyConstants.wayPurpose(surveyType), resultSet.getString(SurveyConstants.wayPurpose(surveyType)));
				attributes.put(SurveyConstants.wayDetailedPurpose(surveyType), resultSet.getString(SurveyConstants.wayDetailedPurpose(surveyType)));
				attributes.put(SurveyConstants.wayTravelDistance(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayTravelDistance(surveyType))));
				attributes.put(SurveyConstants.wayMode(surveyType), resultSet.getString(SurveyConstants.wayMode(surveyType)));
				attributes.put(SurveyConstants.wayTravelTime(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayTravelTime(surveyType))));
				attributes.put(SurveyConstants.wayDepartureHour(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayDepartureHour(surveyType))));
				attributes.put(SurveyConstants.wayDepartureMinute(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayDepartureMinute(surveyType))));
				attributes.put(SurveyConstants.wayDepartureDay(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayDepartureDay(surveyType))));
				attributes.put(SurveyConstants.wayArrivalHour(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayArrivalHour(surveyType))));
				attributes.put(SurveyConstants.wayArrivalMinute(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayArrivalMinute(surveyType))));
				attributes.put(SurveyConstants.wayArrivalDay(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.wayArrivalDay(surveyType))));
				
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