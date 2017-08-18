package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyVehicleType;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.task.ConvertToPlansTask;
import com.innoz.toolbox.io.database.task.HouseholdRemovalTask;
import com.innoz.toolbox.io.database.task.PersonRemovalTask;
import com.innoz.toolbox.io.database.task.ReadHouseholdDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadPersonDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadTripsDatabaseTask;
import com.innoz.toolbox.io.database.task.ReadVehicleDatabaseTask;
import com.innoz.toolbox.io.database.task.ResolveRoundTripsTask;
import com.innoz.toolbox.io.database.task.SortStagesTask;
import com.innoz.toolbox.io.database.task.TaskRunner;
import com.innoz.toolbox.io.database.validation.ValidateDistances;
import com.innoz.toolbox.io.database.validation.ValidateMissingTravelTimes;
import com.innoz.toolbox.io.database.validation.ValidateNegativeTravelTimes;
import com.innoz.toolbox.io.database.validation.ValidateOverlappingStages;
import com.innoz.toolbox.run.controller.Controller;
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
	public void run(Set<String> ids){
		
		// Initialize the survey constants
		this.constants = SurveyConstants.getInstance();
		
		try {
			
			log.info("Parsing surveys database to create a synthetic population");
			
			// Instantiate a new postgreSQL driver and establish a connection to the mobility database
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.SURVEYS_DB);
		
			SurveyPopulationConfigurationGroup group = Controller.configuration().surveyPopulation();
			
			if(connection != null){
				
				boolean isUsingHouseholds = group.isUsingHouseholds();
				
				if(isUsingHouseholds) {
					
					log.info("Creating survey households...");
					
					new ReadHouseholdDatabaseTask(constants, ids).parse(connection, SurveyDataContainer.getInstance(), group.getSurveyType().name());
						
					log.info("Read " + SurveyDataContainer.getInstance().getHouseholds().size() + " households...");
					
				}
				
				log.info("Creating survey persons...");
				
				new ReadPersonDatabaseTask(constants, ids, Controller.configuration().surveyPopulation().getDayTypes()).parse(connection, SurveyDataContainer.getInstance(), group.getSurveyType().name());
				
				log.info("Read " + SurveyDataContainer.getInstance().getPersons().size() + " persons...");
				
				log.info("Creating survey trips...");

				new ReadTripsDatabaseTask(constants, ids, Controller.configuration().surveyPopulation().getDayTypes()).parse(connection, SurveyDataContainer.getInstance(), Controller.configuration().surveyPopulation().getSurveyType().name());
				
				if(group.getVehicleType().equals(SurveyVehicleType.SURVEY) && group.getSurveyType().equals(SurveyType.MiD)){
				
					log.info("Creating survey cars...");
					
					new ReadVehicleDatabaseTask(constants, ids).parse(connection, SurveyDataContainer.getInstance(), group.getSurveyType().name());
					
				}
	
				process();
				
				log.info("Conversion statistics:");
				
				if(SurveyDataContainer.getInstance().getHouseholds() != null){
					
					log.info("#Households in survey: " + SurveyDataContainer.getInstance().getHouseholds().size());
					
				}
				
				log.info("#Persons in survey   : " + SurveyDataContainer.getInstance().getPersons().size());
				
				if(SurveyDataContainer.getInstance().getVehicles() != null){
				
					log.info("#Vehicles in survey  : " + SurveyDataContainer.getInstance().getVehicles().size());
					
				}
				
				connection.close();
				
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private void process(){
		
		TaskRunner.exec(new SortStagesTask(), SurveyDataContainer.getInstance());
		TaskRunner.exec(new ValidateMissingTravelTimes(), SurveyDataContainer.getInstance().getPersons().values());
		TaskRunner.exec(new ValidateNegativeTravelTimes(), SurveyDataContainer.getInstance().getPersons().values());
		TaskRunner.exec(new ValidateOverlappingStages(), SurveyDataContainer.getInstance().getPersons().values());
		TaskRunner.exec(new ValidateDistances(), SurveyDataContainer.getInstance().getPersons().values());
		TaskRunner.exec(new PersonRemovalTask(), SurveyDataContainer.getInstance());
		
		if(SurveyDataContainer.getInstance().getHouseholds() != null){
			
			TaskRunner.exec(new HouseholdRemovalTask(), SurveyDataContainer.getInstance());
			
		}
		
		new ConvertToPlansTask().run(SurveyDataContainer.getInstance());
		new ResolveRoundTripsTask().run(SurveyDataContainer.getInstance());
		
	}
	
}