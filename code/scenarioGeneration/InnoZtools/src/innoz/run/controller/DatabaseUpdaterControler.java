package innoz.run.controller;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import innoz.config.Configuration;
import innoz.io.database.DatabaseUpdater;

public class DatabaseUpdaterControler implements DefaultController {

	private final Configuration configuration;
	private final Scenario scenario;
	private final boolean writePersons;
	
	public DatabaseUpdaterControler(final Configuration configuration, String plansFile, boolean writePersons){
		
		this.configuration = configuration;
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimPopulationReader(this.scenario).readFile(plansFile);
		this.writePersons = writePersons;
		
	}
	
	@Override
	public void run() {

		new DatabaseUpdater().update(this.configuration, this.scenario, this.writePersons);
		
	}

}
