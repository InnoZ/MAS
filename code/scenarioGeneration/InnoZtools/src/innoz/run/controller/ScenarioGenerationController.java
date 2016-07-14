package innoz.run.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.vehicles.VehicleWriterV1;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.io.ParseException;

import innoz.config.Configuration;
import innoz.config.Configuration.PopulationType;
import innoz.io.BbsrDataReader;
import innoz.io.database.DatabaseReader;
import innoz.io.database.DatabaseUpdater;
import innoz.scenarioGeneration.config.InitialConfigCreator;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.network.NetworkCreatorFromPsql;
import innoz.scenarioGeneration.population.PopulationCreator;

public class ScenarioGenerationController implements DefaultController {

	private static final Logger log = Logger.getLogger(ScenarioGenerationController.class);
	
	private final Configuration configuration;
	
	public ScenarioGenerationController(final Configuration configuration){
		this.configuration = configuration;
	}
	
	@Override
	public void run() {

		try {
			
			double t0 = System.currentTimeMillis();
		
			// Dump scenario generation settings on the console and create the output directory
			configuration.dumpSettings();
			new File(configuration.getOutputDirectory()).mkdirs();
			
			OutputDirectoryLogging.initLoggingWithOutputDirectory(configuration.getOutputDirectory());
			
			// Reset the random seed
			MatsimRandom.reset(configuration.getRandomSeed());
			
			// Create a MATSim scenario
			Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
			
			// Container for geoinformation (admin borders, landuse)
			Geoinformation geoinformation = new Geoinformation();
	
			// A class that reads data from database tables into local containers
			DatabaseReader dbReader = new DatabaseReader(configuration, geoinformation);
			dbReader.readGeodataFromDatabase(configuration, scenario);
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("regionstypen.csv");
			new BbsrDataReader().read(geoinformation, new InputStreamReader(in));
			
			// Create a MATSim network from OpenStreetMap data
			NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(),
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
	
			if(!configuration.getPopulationType().equals(PopulationType.none) ||
					!configuration.getVicinityPopulationType().equals(PopulationType.none)){
	
				new PopulationWriter(scenario.getPopulation()).write(configuration
						.getOutputDirectory() + "plans.xml.gz");
				
				new ObjectAttributesXmlWriter(scenario.getPopulation().
						getPersonAttributes()).writeFile(configuration.getOutputDirectory()
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
				
				new DatabaseUpdater().update(configuration, scenario, null);
				
			}
			
			OutputDirectoryLogging.closeOutputDirLogging();
			
			double t1 = System.currentTimeMillis();
			
			log.info("Total execution time: " + Time.writeTime((t1 - t0) / 1000));
		
		} catch (FactoryException | InstantiationException | IllegalAccessException | ClassNotFoundException |
				SQLException | ParseException | IOException e) {
			e.printStackTrace();
			return;
		}

		log.info("> Scenario generation complete. All files have been written to "
				+ configuration.getOutputDirectory());
		return;
		
	}
	
}