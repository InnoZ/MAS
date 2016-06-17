package innoz.run.controller;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import innoz.config.Configuration;
import innoz.io.database.DatabaseUpdater;
import innoz.scenarioGeneration.population.utils.PersonUtils;

public class DatabaseUpdaterControler implements DefaultController {

	private final Configuration configuration;
	private final Scenario scenario;
	private final String vehiclesFile;
	
	public DatabaseUpdaterControler(final Configuration configuration, String plansFile, String networkFile, String vehiclesFile, String attributesFile){
		
		this.configuration = configuration;
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		if(plansFile != null){
		
			new MatsimPopulationReader(this.scenario).readFile(plansFile);
		
		}
		
		if(networkFile != null){
			
			new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
			
		}
		
		this.vehiclesFile = vehiclesFile;
		
		if(attributesFile != null){

			ObjectAttributes atts = new ObjectAttributes();
			new ObjectAttributesXmlReader(atts).parse(attributesFile);
			scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, atts);
			
		}
		
	}
	
	@Override
	public void run() {

		new DatabaseUpdater().update(this.configuration, this.scenario, this.vehiclesFile);
		
	}

}
