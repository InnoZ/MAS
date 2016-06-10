package innoz.run;

import innoz.config.Configuration;
import innoz.config.Configuration.PopulationType;
import innoz.io.BbsrDataReader;
import innoz.io.database.DatabaseReader;
import innoz.io.database.DatabaseUpdater;
import innoz.scenarioGeneration.config.InitialConfigCreator;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.NetworkCreatorFromPsql;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.utils.PersonUtils;

import java.io.File;
import java.sql.SQLException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.vehicles.VehicleWriterV1;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.io.ParseException;

public class ScenarioGenerationController implements DefaultController {

	private final Configuration configuration;
	
	public ScenarioGenerationController(final Configuration configuration){
		this.configuration = configuration;
	}
	
	@Override
	public void run() {

		try {
		
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
			if(configuration.isUsingVehicles()){
				scenario.getConfig().scenario().setUseVehicles(true);
				((ScenarioImpl)scenario).createVehicleContainer();
			}
			
			// Container for geoinformation (admin borders, landuse)
			Geoinformation geoinformation = new Geoinformation();
	
			// A class that reads data from database tables into local containers
			DatabaseReader dbReader = new DatabaseReader(geoinformation);
			dbReader.readGeodataFromDatabase(configuration, scenario);
			new BbsrDataReader().read(geoinformation);
			
			// Create a MATSim network from OpenStreetMap data
			NetworkCreatorFromPsql nc;
			nc = new NetworkCreatorFromPsql(scenario.getNetwork(),
						geoinformation,	configuration);
			nc.setSimplifyNetwork(true);
			nc.setCleanNetwork(true);
			nc.setScaleMaxSpeed(true);
			nc.create(dbReader);
			
			// Create a MATSim population
			new PopulationCreator(geoinformation).run(configuration, scenario);
			
			// Create an initial MATSim config file and write it into the output directory
			Config config = InitialConfigCreator.create(configuration);
			new ConfigWriter(config).write(configuration.getOutputDirectory() + "config.xml.gz");
			
			// Dump scenario elements into the output directory
			new NetworkWriter(scenario.getNetwork()).write(configuration
					.getOutputDirectory() + "network.xml.gz");
	
			if(!configuration.getPopulationType().equals(PopulationType.none)){
	
				new PopulationWriter(scenario.getPopulation()).write(configuration
						.getOutputDirectory() + "plans.xml.gz");
				new ObjectAttributesXmlWriter((ObjectAttributes) scenario.getScenarioElement(
						PersonUtils.PERSON_ATTRIBUTES)).writeFile(configuration.getOutputDirectory()
								+ "personAttributes.xml.gz");
				
				if(configuration.isUsingHouseholds()){
					
					new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration
							.getOutputDirectory() + "households.xml.gz");
					
				}
				
				if(configuration.isUsingVehicles()){
	
					new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration
							.getOutputDirectory() + "vehicles.xml.gz");
					
				}
				
			}
			
			if(configuration.isWritingDatabaseOutput()){
				
				new DatabaseUpdater().update(configuration, scenario,
						configuration.getDatabaseSchemaName(),
						configuration.isWritingIntoMobilityDatahub());
				
			}
		
		} catch (FactoryException | InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | ParseException e) {
			e.printStackTrace();
		}
		
	}
	
}
