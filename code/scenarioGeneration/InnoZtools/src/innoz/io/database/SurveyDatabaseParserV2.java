package innoz.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import innoz.config.Configuration;
import innoz.io.SurveyConstants;
import innoz.io.database.handler.Logbook;
import innoz.io.database.task.ConvertToPlansTask;
import innoz.io.database.task.ReadHouseholdDatabaseTask;
import innoz.io.database.task.ReadPersonDatabaseTask;
import innoz.io.database.task.ReadWayDatabaseTask;
import innoz.io.database.task.ResolveRoundTripsTask;
import innoz.io.database.task.SortStagesTask;
import innoz.io.database.validation.ValidateMissingTravelTimes;
import innoz.io.database.validation.ValidateNegativeTravelTimes;
import innoz.io.database.validation.ValidateOverlappingStages;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class SurveyDatabaseParserV2 {

	private static final Logger log = Logger.getLogger(SurveyDatabaseParser.class);
	
	private SurveyConstants constants;
	
	/**
	 * 
	 * Executes the data retrieval process. Depending on the specifications in the configuration household, person,
	 * travel and vehicle data is read from the survey database tables.
	 * 
	 * @param configuration The scenario generation configuration.
	 * @param container The class containing all survey information needed for demand generation.
	 * @param geoinformation
	 */
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation){
		
		// Initialize the survey constants according to what datasource was specified.
		this.constants = new SurveyConstants(configuration.getDatasource());
		
		try {
			
			log.info("Parsing surveys database to create a synthetic population");
			
			// Instantiate a new postgreSQL driver and establish a connection to the mobility database
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL +
					configuration.getLocalPort() + "/" + DatabaseConstants.SURVEYS_DB, 
					configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating survey households...");
					
					new ReadHouseholdDatabaseTask(constants, geoinformation).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
						
					log.info("Read " + container.getHouseholds().size() + " households...");
					
				}
				
				log.info("Creating survey persons...");
				
				new ReadPersonDatabaseTask(constants).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				log.info("Read " + container.getPersons().size() + " persons...");
				
				log.info("Creating survey ways...");

				new ReadWayDatabaseTask(constants).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				if(configuration.isUsingVehicles() && configuration.getDatasource().equals("mid")){
				
					log.info("Creating survey cars...");
					
//					parseVehiclesDatabase(connection, container);
					
				}
	
				process(container);
				
				log.info("Conversion statistics:");
				log.info("#Households in survey: " + container.getHouseholds().size());
				log.info("#Persons in survey   : " + container.getPersons().size());
				if(configuration.isUsingVehicles()){
					log.info("#Vehicles in survey  : " + container.getVehicles().size());
				}
				
				connection.close();
				
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private void process(SurveyDataContainer container){
		
		Set<Logbook> toRemove;
		
		SortStagesTask task1 = new SortStagesTask();
		
		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
			
				task1.apply(logbook);
			
			}
			
		}
		
		ValidateMissingTravelTimes vmtt = new ValidateMissingTravelTimes();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vmtt.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
			
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
		
		}
		
		ValidateNegativeTravelTimes vntt = new ValidateNegativeTravelTimes();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vntt.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
			
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
		
		}
		
		ValidateOverlappingStages vos = new ValidateOverlappingStages();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vos.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
		
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
			
		}
		
		Set<String> personsToRemove = new HashSet<>();
		for(SurveyPerson person : container.getPersons().values()){
			if(person.getLogbook().size() < 1){
				personsToRemove.add(person.getId());
			}
			if(person.getId() == null){
				personsToRemove.add(person.getId());
			}
		}
		
		for(String id : personsToRemove){
			container.removePerson(id);
		}
		
		Set<String> hhToRemove = new HashSet<>();
		for(SurveyHousehold hh : container.getHouseholds().values()){
			if(hh.getMemberIds().isEmpty()){
				hhToRemove.add(hh.getId());
				continue;
			}
			int size = hh.getMemberIds().size();
			Set<String> idsToRemove = new HashSet<>();
			for(String id : hh.getMemberIds()){
				if(container.getPersons().get(id) == null){
					idsToRemove.add(id);
					size--;
				}
			}
			hh.getMemberIds().removeAll(idsToRemove);
			if(size <= 0){
				hhToRemove.add(hh.getId());
			}
			
			if(hh.getWeight() == null)
				hhToRemove.add(hh.getId());
			
		}
		for(String id : hhToRemove){
			container.removeHousehold(id);
		}
		
		new ResolveRoundTripsTask().run(container);
		
		new ConvertToPlansTask().run(container);
		
	}
	
}