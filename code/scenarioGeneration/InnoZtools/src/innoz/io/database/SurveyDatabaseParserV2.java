package innoz.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import innoz.config.Configuration;
import innoz.io.SurveyConstants;
import innoz.io.database.task.ConvertToPlansTask;
import innoz.io.database.task.HouseholdRemovalTask;
import innoz.io.database.task.PersonRemovalTask;
import innoz.io.database.task.ReadHouseholdDatabaseTask;
import innoz.io.database.task.ReadPersonDatabaseTask;
import innoz.io.database.task.ReadWayDatabaseTask;
import innoz.io.database.task.ResolveRoundTripsTask;
import innoz.io.database.task.SortStagesTask;
import innoz.io.database.task.TaskRunner;
import innoz.io.database.validation.ValidateMissingTravelTimes;
import innoz.io.database.validation.ValidateNegativeTravelTimes;
import innoz.io.database.validation.ValidateOverlappingStages;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

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
		
		TaskRunner.exec(new SortStagesTask(), container);
		TaskRunner.exec(new ValidateMissingTravelTimes(), container.getPersons().values());
		TaskRunner.exec(new ValidateNegativeTravelTimes(), container.getPersons().values());
		TaskRunner.exec(new ValidateOverlappingStages(), container.getPersons().values());
		TaskRunner.exec(new PersonRemovalTask(), container);
		TaskRunner.exec(new HouseholdRemovalTask(), container);
		new ResolveRoundTripsTask().run(container);
		new ConvertToPlansTask().run(container);
		
	}
	
}