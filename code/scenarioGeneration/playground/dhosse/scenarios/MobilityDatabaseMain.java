package playground.dhosse.scenarios;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.households.HouseholdsWriterV10;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.network.NetworkCreatorFromPsql;
import playground.dhosse.scenarios.generic.population.PopulationCreator;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.scenarios.generic.utils.SshConnector;

import com.jcraft.jsch.JSchException;

/**
 * 
 * Test class for database connection and population generation.
 * 
 * @author dhosse
 *
 */
public class MobilityDatabaseMain {

	/**
	 * 
	 * @param args configuration file
	 */
	public static void main(String args[]){
		
		if(args.length > 0){
			
			//TODO this method actually needs to call playground.dhosse.scenarios.generic.ScenarioBuilder
			//we need to call the MATSim code from here because of an issue w/ a maven module that uses
			//psql 8.x. Since we need version 9.4, I created a separate project w/ the required includes //dhosse 04/16 
			Configuration configuration = new Configuration(args[0]);
			
			try {
				
				//create a ssh tunnel to the playground
				SshConnector.connect(configuration);
				
				configuration.dumpSettings();
				
				MatsimRandom.reset(4711);
				
				Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

				Set<String> ids = new HashSet<>();
				for(String id : configuration.getSurveyAreaIds()){
					
					ids.add(id);	
					
				}
				
				Geoinformation.readGeodataFromDatabase(configuration, ids, scenario);
				
				NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario, configuration);
				nc.setSimplifyNetwork(true);
				nc.setCleanNetwork(true);
				nc.setScaleMaxSpeed(true);
				nc.create();
				
				PopulationCreator.run(configuration, scenario);
				
				new NetworkWriter(scenario.getNetwork()).write(configuration.getWorkingDirectory() + "network.xml.gz");
				new PopulationWriter(scenario.getPopulation()).write(configuration.getWorkingDirectory() + "plans.xml.gz");
				new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration.getWorkingDirectory() + "households.xml.gz");
				
			} catch (JSchException | IOException e1) {
			
				e1.printStackTrace();
				
			}
			finally
			{
				
				//close the ssh tunnel and exit
				System.exit(0);
				
			}
		
		} else {
			
			throw new RuntimeException("You must pass a configuration file as runtime argument! Execution aborts...");
			
		}
		
	}
	
}