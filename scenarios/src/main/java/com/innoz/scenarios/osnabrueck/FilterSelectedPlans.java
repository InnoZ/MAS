package com.innoz.scenarios.osnabrueck;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class FilterSelectedPlans {

	public static void main(String[] args) {
		
		String inputPath = "/home/bmoehring/3connect/1_negativSzenario/"; 
		
		Config config = ConfigUtils.createConfig();
		config.plans().setInputFile(inputPath + "output_plans.xml.gz");
		
		Scenario scenario = ScenarioUtils.loadScenario(config); 
		scenario.getPopulation().getPersons().values().stream().forEach(person -> PersonUtils.removeUnselectedPlans(person));
		
		System.out.println(scenario.getPopulation().getPersons().size());
		
		new PopulationWriter(scenario.getPopulation()).write("/home/bmoehring/3connect/1_negativSzenario/plans_selected.xml.gz");

	}

}
