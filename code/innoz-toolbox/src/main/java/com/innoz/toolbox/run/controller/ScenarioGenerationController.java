package com.innoz.toolbox.run.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

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

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.Configuration.PopulationSource;
import com.innoz.toolbox.config.Configuration.PopulationType;
import com.innoz.toolbox.config.Configuration.VehicleSource;
import com.innoz.toolbox.io.BbsrDataReader;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.io.database.DatabaseUpdater;
import com.innoz.toolbox.scenarioGeneration.config.InitialConfigCreator;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.network.NetworkCreatorFromPsql;
import com.innoz.toolbox.scenarioGeneration.population.PopulationCreator;
import com.vividsolutions.jts.io.ParseException;

public class ScenarioGenerationController extends DefaultController {

	public ScenarioGenerationController(final Configuration configuration){
		
		super(configuration);
		
	}
	
	@Override
	public void run() {

		try {
			
			double t0 = System.currentTimeMillis();
			
			// Dump scenario generation settings on the console and create the output directory
			new File(configuration.getOutputDirectory()).mkdirs();
			
			OutputDirectoryLogging.initLoggingWithOutputDirectory(configuration.getOutputDirectory());
			configuration.dumpSettings();
			
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
	
			if(!configuration.getPopulationSource().equals(PopulationSource.none) ||
					!configuration.getVicinityPopulationSource().equals(PopulationSource.none)){
	
				new PopulationWriter(scenario.getPopulation()).write(configuration
						.getOutputDirectory() + "plans.xml.gz");
				
				new ObjectAttributesXmlWriter(scenario.getPopulation().
						getPersonAttributes()).writeFile(configuration.getOutputDirectory()
								+ "personAttributes.xml.gz");
					
				if(configuration.getPopulationType().equals(PopulationType.households)){
					
					new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration
							.getOutputDirectory() + "households.xml.gz");
					
				}
				
				if(configuration.getVehicleSource().equals(VehicleSource.survey)){
	
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