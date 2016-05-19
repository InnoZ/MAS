package playground.dhosse.scenarioGeneration;

import java.io.File;
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
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.vehicles.VehicleWriterV1;

import playground.dhosse.config.Configuration;
import playground.dhosse.config.SshConnector;
import playground.dhosse.database.DatabaseReader;
import playground.dhosse.database.DatabaseUpdater;
import playground.dhosse.scenarioGeneration.geoinformation.Geoinformation;
import playground.dhosse.scenarioGeneration.network.NetworkCreatorFromPsql;
import playground.dhosse.scenarioGeneration.population.PopulationCreator;
import playground.dhosse.scenarioGeneration.population.utils.PersonUtils;

/**
 * 
 * Entry-point for database connection and scenario generation.
 * 
 * @author dhosse
 *
 */
public class MobilityDatabaseMain {

	private static final Logger log = Logger.getLogger(MobilityDatabaseMain.class);
	
	// No instance!
	private MobilityDatabaseMain(){};
	
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
						.getOutputDirectory());
			
				// Create a ssh tunnel to the playground
				SshConnector.connect(configuration);
			
				// Dump scenario generation settings on the console and create the output directory
				configuration.dumpSettings();
				new File(configuration.getOutputDirectory()).mkdirs();
				
				// Reset the random seed
				MatsimRandom.reset(configuration.getRandomSeed());
				
				// Create a MATSim scenario
				Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
				// Enable the usage of households
				scenario.getConfig().scenario().setUseHouseholds(true);
				((ScenarioImpl)scenario).createHouseholdsContainer();
				
				// If we want to explicitly model household's cars, enable it
				if(configuration.isUsingCars()){
					scenario.getConfig().scenario().setUseVehicles(true);
					((ScenarioImpl)scenario).createVehicleContainer();
				}
				
				// Generate a String set that stores the identifier(s) of the survey area regions
				Set<String> ids = new HashSet<>();
				for(String id : configuration.getSurveyAreaIds()){
					
					ids.add(id);
					
				}
				
				// Container for geoinformation (admin borders, landuse)
				Geoinformation geoinformation = new Geoinformation();

				// A class that reads data from database tables into local containers
				DatabaseReader dbReader = new DatabaseReader(geoinformation);
				dbReader.readGeodataFromDatabase(configuration, ids, scenario);
				
				// Create a MATSim network from OpenStreetMap data
				NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(), geoinformation,
						configuration);
				nc.setSimplifyNetwork(true);
				nc.setCleanNetwork(true);
				nc.setScaleMaxSpeed(true);
				nc.create(dbReader);
				
				// Create a MATSim population
				new PopulationCreator(geoinformation).run(configuration, scenario);
				
				// Dump scenario elements into working directory
				new NetworkWriter(scenario.getNetwork()).write(configuration
						.getOutputDirectory() + "network.xml.gz");
				new PopulationWriter(scenario.getPopulation()).write(configuration
						.getOutputDirectory() + "plans.xml.gz");
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration
						.getOutputDirectory() + "households.xml.gz");
				new ObjectAttributesXmlWriter((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES))
					.writeFile(configuration.getOutputDirectory() + "personAttributes.xml.gz");
				if(configuration.isUsingCars()){
					new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration
							.getOutputDirectory() + "vehicles.xml.gz");
				}
				
				if(configuration.isWritingDatabaseOutput()){
					
					new DatabaseUpdater().update(configuration, scenario,
							configuration.getDatabaseSchemaName(),
							configuration.isWritingIntoMobilityDatahub());
					
				}
				
			} catch (Exception e1) {
			
				e1.printStackTrace();
				
			}
			finally
			{
				
				//close the ssh tunnel and exit
				OutputDirectoryLogging.closeOutputDirLogging();
				System.exit(0);
				
			}
		
		} else {
			
			throw new RuntimeException("You must pass a configuration file as runtime argument!"
					+ " Execution aborts...");
			
		}
		
	}
	
}