package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.PsqlAdapter;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.VehicleType;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.task.ConvertToPlansTask;
import com.innoz.toolbox.io.database.task.HouseholdRemovalTask;
import com.innoz.toolbox.io.database.task.PersonRemovalTask;
import com.innoz.toolbox.io.database.task.ReadHouseholdDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadPersonDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadVehicleDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadWayDatabaseTask;
import com.innoz.toolbox.io.database.task.ResolveRoundTripsTask;
import com.innoz.toolbox.io.database.task.SortStagesTask;
import com.innoz.toolbox.io.database.task.TaskRunner;
import com.innoz.toolbox.io.database.validation.ValidateMissingTravelTimes;
import com.innoz.toolbox.io.database.validation.ValidateNegativeTravelTimes;
import com.innoz.toolbox.io.database.validation.ValidateOverlappingStages;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public class SurveyDatabaseParserV2 {

	private static final Logger log = Logger.getLogger(SurveyDatabaseParserV2.class);
	
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
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation, Set<String> ids){
		
		// Initialize the survey constants
		this.constants = new SurveyConstants();
		
		try {
			
			log.info("Parsing surveys database to create a synthetic population");
			
			// Instantiate a new postgreSQL driver and establish a connection to the mobility database
			Connection connection = PsqlAdapter.createConnection(configuration, DatabaseConstants.SURVEYS_DB);
		
			SurveyPopulationConfigurationGroup group = configuration.surveyPopulation();
			
			if(connection != null){
				
				boolean isUsingHouseholds = group.isUsingHouseholds();
				
				if(isUsingHouseholds){
					
					log.info("Creating survey households...");
					
					new ReadHouseholdDatabaseTask(constants, geoinformation, ids).parse(connection, container, group.getSurveyType().name());
						
					log.info("Read " + container.getHouseholds().size() + " households...");
					
				}
				
				log.info("Creating survey persons...");
				
				new ReadPersonDatabaseTask(constants, geoinformation, ids, configuration.surveyPopulation().getDayTypes()).parse(connection, container, group.getSurveyType().name());
				
				log.info("Read " + container.getPersons().size() + " persons...");
				
				log.info("Creating survey ways...");

				new ReadWayDatabaseTask(constants, geoinformation, ids, group.getDayTypes()).parse(connection, container, group.getSurveyType().name());
				
				if(group.getVehicleType().equals(VehicleType.SURVEY) && group.getSurveyType().equals(SurveyType.MiD)){
				
					log.info("Creating survey cars...");
					
					new ReadVehicleDatabaseTask(constants, geoinformation, ids).parse(connection, container, group.getSurveyType().name());
					
				}
	
				process(container);
				
				log.info("Conversion statistics:");
				
				if(container.getHouseholds() != null){
					
					log.info("#Households in survey: " + container.getHouseholds().size());
					
				}
				
				log.info("#Persons in survey   : " + container.getPersons().size());
				
				if(container.getVehicles() != null){
				
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
		
		if(container.getHouseholds() != null){
			
			TaskRunner.exec(new HouseholdRemovalTask(), container);
			
		}
		
		new ConvertToPlansTask().run(container);
		new ResolveRoundTripsTask().run(container);
		
	}
	
}