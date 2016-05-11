package playground.dhosse.scenarios;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
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
 * Test class for database connection and population generation.
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
		
		if(args.length > 0){
			
//			Logger.getLogger("org.matsim.core.controler.injector").setLevel(Level.OFF);

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
				
				Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				scenario.getConfig().scenario().setUseHouseholds(true);
				((ScenarioImpl)scenario).createHouseholdsContainer();
				
				Set<String> ids = new HashSet<>();
				for(String id : configuration.getSurveyAreaIds()){
					
					ids.add(id);
					
				}
				
				Geoinformation.readGeodataFromDatabase(configuration, ids, scenario);
				
				NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario, configuration);
//				nc.setSimplifyNetwork(true); TODO not implemented in matsim 0.7.0
				nc.setCleanNetwork(true);
				nc.setScaleMaxSpeed(true);
				nc.create();
				
				PopulationCreator.run(configuration, scenario);
				
				new NetworkWriter(scenario.getNetwork()).write(configuration.getWorkingDirectory() + "network.xml.gz");
				new PopulationWriter(scenario.getPopulation()).write(configuration.getWorkingDirectory() + "plans.xml.gz");
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration.getWorkingDirectory() + "households.xml.gz");
				
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