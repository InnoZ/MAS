package playground.dhosse.scenarios;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.vehicles.VehicleWriterV1;
import org.opengis.referencing.FactoryException;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.network.NetworkCreatorFromPsql;
import playground.dhosse.scenarios.generic.population.PopulationCreator;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.scenarios.generic.utils.SshConnector;

import com.jcraft.jsch.JSchException;
import com.vividsolutions.jts.io.ParseException;

/**
 * 
 * Entry-point for database connection and scenario generation.
 * 
 * @author dhosse
 *
 */
public class MobilityDatabaseMain {

	private static final Logger log = Logger.getLogger(MobilityDatabaseMain.class);
	
	/**
	 * 
	 * @param args configuration file
	 */
	public static void main(String args[]){
		
		log.info("Start...");
		
		// Check, if there is a configuration file given
		if(args.length > 0){

			// create a new configuration that holds all the information and switches needed to generate a
			// MATSim scenario
			Configuration configuration = new Configuration(args[0]);

			try {
			
				// Mechanism that writes the log file into the working directory
				OutputDirectoryLogging.initLoggingWithOutputDirectory(configuration
						.getWorkingDirectory());
			
				// Create a ssh tunnel to the playground
				SshConnector.connect(configuration);
			
				// Dump scenario generation settings on the console and create the output directory
				configuration.dumpSettings();
				new File(configuration.getWorkingDirectory()).mkdirs();
				
				// Reset the random seed
				MatsimRandom.reset(configuration.getRandomSeed());
				
				// Create a MATSim scenario
				Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				// Enable the usage of households
				scenario.getConfig().scenario().setUseHouseholds(true);
				((ScenarioImpl)scenario).createHouseholdsContainer();
				
				// Generate a String set that stores the identifier(s) of the survey area regions
				Set<String> ids = new HashSet<>();
				for(String id : configuration.getSurveyAreaIds()){
					
					ids.add(id);
					
				}
				
				// Read everything concerning geodata (borders, landuse, buildings, ...)
				Geoinformation.readGeodataFromDatabase(configuration, ids, scenario);
				
				// Create a MATSim network from OpenStreetMap data
				NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(), configuration);
//				nc.setSimplifyNetwork(true); TODO not implemented in matsim 0.7.0
				nc.setCleanNetwork(true);
				nc.setScaleMaxSpeed(true);
				nc.create();
				
				// Create a MATSim population
				PopulationCreator.run(configuration, scenario);
				
				// Dump scenario elements into working directory
				new NetworkWriter(scenario.getNetwork()).write(configuration.getWorkingDirectory() +
						"network.xml.gz");
				new PopulationWriter(scenario.getPopulation()).write(configuration.getWorkingDirectory() +
						"plans.xml.gz");
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration.getWorkingDirectory() +
						"households.xml.gz");
				new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration.getWorkingDirectory() +
						"vehicles.xml.gz");
				
			} catch (JSchException | IOException | InstantiationException | IllegalAccessException |
					ClassNotFoundException | SQLException | ParseException | FactoryException e1) {
			
				e1.printStackTrace();
				
			}
			finally
			{
				
				//close the ssh tunnel and exit
				System.exit(0);
				OutputDirectoryLogging.closeOutputDirLogging();
				
			}
		
		} else {
			
			throw new RuntimeException("You must pass a configuration file as runtime argument! Execution aborts...");
			
		}
		
	}
	
}