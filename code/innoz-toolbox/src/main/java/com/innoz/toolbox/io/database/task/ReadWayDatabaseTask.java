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
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class ReadWayDatabaseTask extends DatabaseTask {

	private final DayType dayType;
	
	public ReadWayDatabaseTask(SurveyConstants constants, DayType dayType){
		
		super(constants);
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
	public void parse(Connection connection, SurveyDataContainer container) throws SQLException{
		
		Map<String, List<SurveyStage>> personId2Stages = new HashMap<>();
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = null;
		
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.ways_raw" : "srv2013.ways";
		
		q = "select * from " + table;
		
		if(dayType.equals(DayType.weekday)){
		
			q += " where " + this.constants.dayOfTheWeek() + " < 6";

		} else if(dayType.equals(DayType.weekend)){
			
			q += " where " + this.constants.dayOfTheWeek() + " > 5";
			
		}
		
		resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			String householdId = resultSet.getString(this.constants.householdId());
			String personId = resultSet.getString(this.constants.personId());
			
			if(container.getHouseholds().containsKey(householdId) && container.getPersons().containsKey(householdId + personId)){

				Map<String, String> attributes = new HashMap<>();
				
				attributes.put(this.constants.wayId(), resultSet.getString(this.constants.wayId()));
				attributes.put(this.constants.waySource(), resultSet.getString(this.constants.waySource()));
				attributes.put(this.constants.waySink(), resultSet.getString(this.constants.waySink()));
				attributes.put(this.constants.wayPurpose(), resultSet.getString(this.constants.wayPurpose()));
				attributes.put(this.constants.wayDetailedPurpose(), resultSet.getString(this.constants.wayDetailedPurpose()));
				attributes.put(this.constants.wayTravelDistance(), Double.toString(resultSet.getDouble(this.constants.wayTravelDistance())));
				attributes.put(this.constants.wayMode(), resultSet.getString(this.constants.wayMode()));
				attributes.put(this.constants.wayTravelTime(), Double.toString(resultSet.getDouble(this.constants.wayTravelTime())));
				attributes.put(this.constants.wayDepartureHour(), Double.toString(resultSet.getDouble(this.constants.wayDepartureHour())));
				attributes.put(this.constants.wayDepartureMinute(), Double.toString(resultSet.getDouble(this.constants.wayDepartureMinute())));
				attributes.put(this.constants.wayDepartureDay(), Double.toString(resultSet.getDouble(this.constants.wayDepartureDay())));
				attributes.put(this.constants.wayArrivalHour(), Double.toString(resultSet.getDouble(this.constants.wayArrivalHour())));
				attributes.put(this.constants.wayArrivalMinute(), Double.toString(resultSet.getDouble(this.constants.wayArrivalMinute())));
				attributes.put(this.constants.wayArrivalDay(), Double.toString(resultSet.getDouble(this.constants.wayArrivalDay())));
				
				int stichtag = resultSet.getInt(this.constants.dayOfTheWeek());
				
				SurveyStage stage = new SurveyStage();
				
				for(DefaultHandler handler : this.handlers){
					
					handler.handle(stage, attributes);
					
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