package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.config.Configuration.DayType;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.PersonAgeHandler;
import com.innoz.toolbox.io.database.handler.PersonCarAvailabilityHandler;
import com.innoz.toolbox.io.database.handler.PersonEmploymentHandler;
import com.innoz.toolbox.io.database.handler.PersonIdHandler;
import com.innoz.toolbox.io.database.handler.PersonIsMobileHandler;
import com.innoz.toolbox.io.database.handler.PersonLicenseHandler;
import com.innoz.toolbox.io.database.handler.PersonSexHandler;
import com.innoz.toolbox.io.database.handler.PersonWeightHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.utils.data.Tree.Node;

public class ReadPersonDatabaseTask extends DatabaseTask {
	
	private final DayType dayType;

	public ReadPersonDatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids, DayType dayType) {
		
		super(constants, geoinformation, ids);
		this.dayType = dayType;
		
		this.handlers = new HashSet<>();
		this.handlers.add(new PersonAgeHandler());
		this.handlers.add(new PersonCarAvailabilityHandler());
		this.handlers.add(new PersonEmploymentHandler());
		this.handlers.add(new PersonIdHandler());
		this.handlers.add(new PersonLicenseHandler());
		this.handlers.add(new PersonSexHandler());
		this.handlers.add(new PersonWeightHandler());
		this.handlers.add(new PersonIsMobileHandler());
	
	}

	@Override
	public void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException {
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = null;
		
		String table = surveyType.equals("mid") ? "mid2008.persons_raw" : "srv2013.persons";
		
		q = "select * from " + table;
		
//		if(container.getHouseholds() == null){
			
			if(surveyType.equals("mid")){
				
				q +=  " where ";
				
				int cntOut = 0;
				Set<Integer> knownRegionTypes = new HashSet<>();

				for(Node<AdministrativeUnit> node: geoinformation.getAdminUnits()){
					
					AdministrativeUnit entry = node.getData();
					
					if(ids.contains(entry.getId().substring(0, 5))&&!knownRegionTypes.contains(entry.getRegionType())){
					
						cntOut++;
						
						q += SurveyConstants.regionType(surveyType) + " = " + entry.getRegionType();
						knownRegionTypes.add(entry.getRegionType());

						if(cntOut < ids.size()){

							q += " or ";
							
						}
						
					}
					
				}
				
			} else {
				
				q += " where st_code=44"; //Osnabrück 
				
			}
			
//		}
		
		if(dayType.equals(DayType.weekday)){
			q += " and " + SurveyConstants.dayOfTheWeek(surveyType) + " < 6";
		} else if(dayType.equals(DayType.weekend)){
			q += " and " + SurveyConstants.dayOfTheWeek(surveyType) + " > 5";
		}
		
		q += ";";
		
		resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){

			boolean contained = true;
			String hhId = resultSet.getString(SurveyConstants.householdId(surveyType));
			
			if(container.getHouseholds() != null){
				contained = container.getHouseholds().containsKey(hhId);
			}
			
			if(contained){

				Map<String, String> attributes = new HashMap<>();
				attributes.put(SurveyConstants.personId(surveyType), hhId + resultSet.getString(SurveyConstants.personId(surveyType)));
				attributes.put(SurveyConstants.personWeight(surveyType), Double.toString(resultSet.getDouble(SurveyConstants.personWeight(surveyType))));
				attributes.put(SurveyConstants.personCarAvailability(surveyType), resultSet.getString(SurveyConstants.personCarAvailability(surveyType)));
				attributes.put(SurveyConstants.personDrivingLicense(surveyType), resultSet.getString(SurveyConstants.personDrivingLicense(surveyType)));
				attributes.put(SurveyConstants.personSex(surveyType), resultSet.getString(SurveyConstants.personSex(surveyType)));
				attributes.put(SurveyConstants.personAge(surveyType), resultSet.getString(SurveyConstants.personAge(surveyType)));
				attributes.put(SurveyConstants.personEmployment(surveyType), resultSet.getString(SurveyConstants.personEmployment(surveyType)));
				attributes.put(SurveyConstants.mobile(surveyType), resultSet.getString(SurveyConstants.mobile(surveyType)));
				
				SurveyPerson person = new SurveyPerson();
				
				for(DefaultHandler handler : this.handlers){
					
					handler.handle(person, attributes, surveyType);
					
				}
				
				container.addPerson(person);
				
				if(container.getHouseholds() != null){
					
					container.getHouseholds().get(hhId).getMemberIds().add(person.getId());
					
				}
				
			}
			
		}

	}

}
