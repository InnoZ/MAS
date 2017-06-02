package com.innoz.toolbox.run.controller;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.vehicles.VehicleWriterV1;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.config.validation.ConfigurationValidator;
import com.innoz.toolbox.io.BbsrDataReader;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.scenarioGeneration.config.InitialConfigCreator;
import com.innoz.toolbox.scenarioGeneration.network.NetworkCreatorFromPsql;
import com.innoz.toolbox.scenarioGeneration.population.PopulationCreator;
import com.innoz.toolbox.utils.GlobalNames;

public class ScenarioGenerationController extends DefaultController {

	public ScenarioGenerationController(final Configuration configuration){
		
		super(configuration);
		
	}
	
	@Override
	public void run() {

		try {
			
			double t0 = System.currentTimeMillis();
			Logger.getLogger(org.matsim.matrices.Matrix.class).setLevel(Level.OFF);
			
			ConfigurationValidator.validate(this.configuration);
			
			// Dump scenario generation settings on the console and create the output directory
			new File(configuration.misc().getOutputDirectory()).mkdirs();
			
			OutputDirectoryLogging.initLoggingWithOutputDirectory(configuration.misc().getOutputDirectory());
			
			// Reset the random seed
			MatsimRandom.reset(configuration.scenario().getRandomSeed());
			
			// Create a MATSim scenario
			Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
			
			DatabaseReader.getInstance().readGeodataFromDatabase(scenario);
			DatabaseReader.getInstance().readPopulationFromDatabase(scenario);
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("regionstypen.csv");
			new BbsrDataReader().read(new InputStreamReader(in));
			
			// Create a MATSim network from OpenStreetMap data
			NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(), configuration);
			nc.create();

//			CreateCarsharingVehicles.run(configuration, scenario);

			if(configuration.scenario().getAreaSets() != null) {
			
				// Create a MATSim population
				PopulationCreator.run(configuration, scenario);
				
			}
			
			// Create an initial MATSim config file and write it into the output directory
			Config config = InitialConfigCreator.create(configuration);
			new ConfigWriter(config).write(configuration.misc().getOutputDirectory() + "config.xml.gz");
			
			// Dump scenario elements into the output directory
			new NetworkWriter(scenario.getNetwork()).write(configuration.misc()
					.getOutputDirectory() + "network.xml.gz");
	
			new PopulationWriter(scenario.getPopulation()).write(configuration.misc()
					.getOutputDirectory() + "plans.xml.gz");
				
			new ObjectAttributesXmlWriter(scenario.getPopulation().
					getPersonAttributes()).writeFile(configuration.misc().getOutputDirectory()
							+ "personAttributes.xml.gz");
					
			new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration.misc()
					.getOutputDirectory() + "households.xml.gz");
					
			new VehicleWriterV1(scenario.getVehicles()).writeFile(configuration.misc()
					.getOutputDirectory() + "vehicles.xml.gz");
				
			if(configuration.scenario().getActivityLocationsType().equals(ActivityLocationsType.FACILITIES)){

				CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(GlobalNames.WGS84, GlobalNames.UTM32N);
				
				for(ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()){
					
					((ActivityFacilityImpl)facility).setCoord(transformation.transform(facility.getCoord()));
					((ActivityFacilityImpl)facility).setLinkId(NetworkUtils.getNearestLink(scenario.getNetwork(),
							facility.getCoord()).getId());
					
				}
				
				new FacilitiesWriter(scenario.getActivityFacilities()).write(configuration.misc().getOutputDirectory() + "facilities.xml.gz");
				
			}
			
			double t1 = System.currentTimeMillis();
			
			log.info("Total execution time: " + Time.writeTime((t1 - t0) / 1000));
		
		} catch (Exception e) {
			
			e.printStackTrace();
			log.error("There were exceptions during scenario generation. No output was created!");
			return;
		
		}

		log.info("> Scenario generation complete. All files have been written to "
				+ configuration.misc().getOutputDirectory());
		
		OutputDirectoryLogging.closeOutputDirLogging();
		
		return;
		
	}
	
}