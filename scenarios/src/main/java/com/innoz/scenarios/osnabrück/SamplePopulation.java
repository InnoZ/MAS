package com.innoz.scenarios.osnabrück;

import java.util.Random;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PopulationReaderMatsimV5;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class SamplePopulation {

	public static void main(String args[]) {
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		new PopulationReaderMatsimV5(scenario).readFile("/home/dhosse/scenarios/3connect/plans.xml.gz");
		
		Scenario scenario2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		double sampleSize = 0.1;
		
		Random random = MatsimRandom.getRandom();
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			if(random.nextDouble() <= sampleSize) {
				
				scenario2.getPopulation().addPerson(person);
				
			}
			
		}
		
		new PopulationWriter(scenario2.getPopulation()).write("/home/dhosse/scenarios/3connect/plans-10pSample.xml.gz");
		
		System.out.println(scenario2.getPopulation().getPersons().size());
		
	}
	
}