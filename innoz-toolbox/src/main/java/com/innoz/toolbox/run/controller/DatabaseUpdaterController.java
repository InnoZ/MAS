package com.innoz.toolbox.run.controller;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.DatabaseUpdater;
import com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils;

public class DatabaseUpdaterController extends DefaultController {

	private final Scenario scenario;
	private final String vehiclesFile;
	
	public DatabaseUpdaterController(final Configuration configuration, String plansFile, String networkFile,
			String vehiclesFile, String attributesFile){
		
		super(configuration);
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		if(plansFile != null){
		
			new PopulationReader(this.scenario).readFile(plansFile);
		
		}
		
		if(networkFile != null){
			
			new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
			
		}
		
		this.vehiclesFile = vehiclesFile;
		
		if(attributesFile != null){

			ObjectAttributes atts = new ObjectAttributes();
			new ObjectAttributesXmlReader(atts).readFile(attributesFile);
			scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, atts);
			
		}
		
	}
	
	@Override
	public void run() {

		new DatabaseUpdater().update(this.configuration, this.scenario, this.vehiclesFile);
		
	}

}
