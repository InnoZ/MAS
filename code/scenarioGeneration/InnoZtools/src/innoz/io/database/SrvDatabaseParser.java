package innoz.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

import org.apache.log4j.Logger;

public class SrvDatabaseParser {

	private static final Logger log = Logger.getLogger(MidDatabaseParser.class);
	
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation){
	
		log.info("Parsing MiD database to create a synthetic population");
		
		try {
		
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_PREFIX + configuration.getLocalPort() +
					DatabaseConstants.SIMULATIONS_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating SrV households...");
					//TODO
					
				}
				
				log.info("Creating SrV persons...");
				//TODO
				
				log.info("Creating SrV ways...");
				//TODO
				
				connection.close();
			
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
}
