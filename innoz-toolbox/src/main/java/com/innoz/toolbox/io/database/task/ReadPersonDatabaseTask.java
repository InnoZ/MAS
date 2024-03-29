package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.DayTypes;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.PersonAgeHandler;
import com.innoz.toolbox.io.database.handler.PersonBikeAvailHandler;
import com.innoz.toolbox.io.database.handler.PersonCarAvailabilityHandler;
import com.innoz.toolbox.io.database.handler.PersonEmploymentHandler;
import com.innoz.toolbox.io.database.handler.PersonGroupHandler;
import com.innoz.toolbox.io.database.handler.PersonIdHandler;
import com.innoz.toolbox.io.database.handler.PersonIsMobileHandler;
import com.innoz.toolbox.io.database.handler.PersonLicenseHandler;
import com.innoz.toolbox.io.database.handler.PersonRegionTypeHandler;
import com.innoz.toolbox.io.database.handler.PersonSexHandler;
import com.innoz.toolbox.io.database.handler.PersonWeightHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.utils.data.Tree.Node;

/**
 * 
 * Parses through specified survey to retrieve information about persons living in a given region type. 
 * Creates maps of attributes from the ResultSet and assigns them to a person which is put into the SurveyDataContainer {@link com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer#addPerson(SurveyPerson)}.
 * @author dhosse
 *
 */

public class ReadPersonDatabaseTask extends DatabaseTask {
	
	private final DayTypes dayType;

	public ReadPersonDatabaseTask(SurveyConstants constants, Set<String> ids, DayTypes dayType) {
		
		super(constants, ids);
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
		this.handlers.add(new PersonRegionTypeHandler());
		this.handlers.add(new PersonGroupHandler());
		this.handlers.add(new PersonBikeAvailHandler());
	
	}

	@Override
	public void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException {
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = null;
		
		String table = surveyType.equalsIgnoreCase("mid") ? "mid2008.persons_raw" : "srv2013.persons";
		
		q = "select * from " + table;
		
		if(surveyType.equalsIgnoreCase("mid")){
			
			q +=  " where ";
			
			int cntOut = 0;
			Set<Integer> knownRegionTypes = new HashSet<>();

			for(Node<AdministrativeUnit> node: Geoinformation.getInstance().getAdminUnits()){
				
				AdministrativeUnit entry = node.getData();
				
				if(ids.contains(entry.getId().substring(0, 5))&&entry.getRegionType()!= null&&!knownRegionTypes.contains(entry.getRegionType())){
				
					if(cntOut > 0){

						q += " or ";
						
					}
					
					cntOut++;
					
					q += SurveyConstants.regionType(surveyType) + " = " + entry.getRegionType();
					knownRegionTypes.add(entry.getRegionType());
					
				}
				
			}
			
		} else {
			
			q += " where st_code=44"; //Osnabrück 
			
		}
			
		if(dayType.equals(DayTypes.weekday)){
			q += " and " + SurveyConstants.dayOfTheWeek(surveyType) + " < 6";
		} else if(dayType.equals(DayTypes.weekend)){
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
				attributes.put(SurveyConstants.regionType(surveyType), resultSet.getString(SurveyConstants.regionType(surveyType)));
				attributes.put(SurveyConstants.personGroup(surveyType), resultSet.getString(SurveyConstants.personGroup(surveyType)));
				attributes.put(SurveyConstants.bikeAvail(surveyType), resultSet.getString(SurveyConstants.bikeAvail(surveyType)));
				
				SurveyPerson person = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
				
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
