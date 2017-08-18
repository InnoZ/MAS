package com.innoz.toolbox.io.database.population;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.opengis.geometry.MismatchedDimensionException;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.utils.data.Tree.Node;

public class PopulationDatabaseReader {

	//CONSTANTS////////////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(DatabaseReader.class);
	private static final PopulationDatabaseReader instance = new PopulationDatabaseReader();
	///////////////////////////////////////////////////////////////////////////////////////////////	
	
	// Private!
	private PopulationDatabaseReader() {};
	
	public static PopulationDatabaseReader getInstance() {
		
		return PopulationDatabaseReader.instance;
		
	}
	
	/**
	 * Imports detailed forecast of the population subdivided by sex and age groups
	 * 
	 * 
	 * @param configuration The configuration for the scenario generation process.
	 * @param surveyAreaIdsString The survey area id(s).
	 * @param vicinityIdsString The vicinity area id(s).
	 * @param scenario The MATSim scenario.
	 */
	public void readPopulationFromDatabase() {
		
		try {
			
			// Create a postgresql database connection
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.POPULATIONFORECAST_DB);
			
			if(connection != null){

				log.info("Successfully connected with population database...");
				
				// If no administrative units were created, we are unable to proceed
				// The process would probably finish, but no network or population would be created
				// Size = 1 means, only the root element (basically the top level container) has been initialized
				if(Controller.configuration().scenario().getSurveyAreaId() == null ||
						Controller.configuration().scenario().getSurveyAreaId().isEmpty()){
				
					log.error("No ids found");
					throw new RuntimeException("Execution aborts...");
					
				}

				String id = Controller.configuration().scenario().getSurveyAreaId();
				int year = Controller.configuration().scenario().getYear();
					
				Node<AdministrativeUnit> d = Geoinformation.getInstance().getAdminUnit(id);
				
				if(d != null){
					
					AdministrativeUnit unit = d.getData();
					
					// Execute the query and store the returned valued inside a set.
					String q = "SELECT agegroup, year" + year
							+ " FROM bbsrprognose.populationdata "
							+ " WHERE gkz=" + id + " AND agegroup NOT LIKE '%z%' ";

					Statement statement = connection.createStatement();
					statement.setFetchSize(100);
					ResultSet rs = statement.executeQuery(q);

					// Create a map and put all the results
					HashMap<String, Integer> populationByAgeGroup = new HashMap<String, Integer>();
					while (rs.next()){
						
						populationByAgeGroup.put(rs.getString("agegroup") , rs.getInt("year" + year));
						
					}
				
					rs.close();
					
					unit.setPopulationMap(populationByAgeGroup);;
					
				}
						
			// Close the connection when everything's done.
			connection.close();
			
			}
			
			log.info("Done.");

		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException | 
				MismatchedDimensionException e) {

			e.printStackTrace();
			
		}
		
	}
	
}