package innoz.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import innoz.io.SurveyConstants;
import innoz.io.database.handler.DefaultHandler;
import innoz.io.database.handler.PersonAgeHandler;
import innoz.io.database.handler.PersonCarAvailabilityHandler;
import innoz.io.database.handler.PersonEmploymentHandler;
import innoz.io.database.handler.PersonIdHandler;
import innoz.io.database.handler.PersonLicenseHandler;
import innoz.io.database.handler.PersonSexHandler;
import innoz.io.database.handler.PersonWeightHandler;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class ReadPersonDatabaseTask extends DatabaseTask {

	public ReadPersonDatabaseTask(SurveyConstants constants) {
		super(constants);
		this.handlers = new HashSet<>();
		this.handlers.add(new PersonAgeHandler());
		this.handlers.add(new PersonCarAvailabilityHandler());
		this.handlers.add(new PersonEmploymentHandler());
		this.handlers.add(new PersonIdHandler());
		this.handlers.add(new PersonLicenseHandler());
		this.handlers.add(new PersonSexHandler());
		this.handlers.add(new PersonWeightHandler());
	}

	@Override
	public void parse(Connection connection, boolean isUsingHouseholds, boolean onlyWorkingDays, SurveyDataContainer container)
			throws SQLException {
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
		
		ResultSet resultSet = null;
		String q = null;
		
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.persons_raw" : "srv2013.persons";
		
		q = "select * from " + table;
		
		if(onlyWorkingDays){
			q += " where " + this.constants.dayOfTheWeek() + " < 6";
		}
		
		q += ";";
		
		resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			String hhId = resultSet.getString(this.constants.householdId());
			
			if(container.getHouseholds().containsKey(hhId)){

				Map<String, String> attributes = new HashMap<>();
				attributes.put(this.constants.personId(), hhId + resultSet.getString(this.constants.personId()));
				attributes.put(this.constants.personWeight(), Double.toString(resultSet.getDouble(this.constants.personWeight())));
				attributes.put(this.constants.personCarAvailability(), resultSet.getString(this.constants.personCarAvailability()));
				attributes.put(this.constants.personDrivingLicense(), resultSet.getString(this.constants.personDrivingLicense()));
				attributes.put(this.constants.personSex(), resultSet.getString(this.constants.personSex()));
				attributes.put(this.constants.personAge(), resultSet.getString(this.constants.personAge()));
				attributes.put(this.constants.personEmployment(), resultSet.getString(this.constants.personEmployment()));
				
				SurveyPerson person = new SurveyPerson();
				
				for(DefaultHandler handler : this.handlers){
					
					handler.handle(person, attributes);
					
				}
				
				container.addPerson(person);
				container.getHouseholds().get(hhId).getMemberIds().add(person.getId());
				
			}
			
		}

	}

}
