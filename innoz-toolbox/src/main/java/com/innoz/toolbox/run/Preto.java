package com.innoz.toolbox.run;

import java.io.IOException;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;

public class Preto {

	public static void main(String args[]) throws IOException {
		
		String outputDirectory = args[0] + "_" + args[1] + "/";

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		Population population = scenario.getPopulation();
		PopulationFactory factory = population.getFactory();
		
		Person person = factory.createPerson(Id.createPersonId("nigel"));
		
		Plan plan = factory.createPlan();
		
		plan.addActivity(factory.createActivityFromCoord("home", new Coord(0.0, 0.0)));
		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		population.addPerson(person);
		
		new PopulationWriter(population).write(outputDirectory + "plans.xml.gz");
		
	}
	
}