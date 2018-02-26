package com.innoz.scenarios.osnabrueck;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;


public class CreateTestAgentForOSRoutingModule {

	public static void main(String[] args) {
		
		Config config = ConfigUtils.createConfig();
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		Population population = scenario.getPopulation();
		
		createTestAgent(population);

		new PopulationWriter(scenario.getPopulation()).write("/home/bmoehring/3connect/TestNetworkRoutingWithAccess/plans.xml.gz");
		
	}

	private static void createTestAgent(Population population) {
		
		PopulationFactory pf = population.getFactory();
		
		Person person = pf.createPerson(Id.createPersonId("oskar1"));
		person.getAttributes().putAttribute("vehicleType", "gasoline");
		person.getAttributes().putAttribute("vehicleType", "gasoline");
		Plan plan = PopulationUtils.createPlan();
		
		Activity act = PopulationUtils.createActivityFromCoord("home", new Coord(431588.499660015, 5794983.07352271));
		act.setEndTime(7.5*3600);
		plan.addActivity(act);
		
		Leg leg = PopulationUtils.createLeg(TransportMode.car);
		plan.addLeg(leg);
		act = PopulationUtils.createActivityFromCoord("work", new Coord(434974.98393924429547042, 5792027.5684855142608285));
		act.setStartTime(8.5*3600);
		act.setEndTime(17.5*3600);
		plan.addActivity(act);
		
		leg = PopulationUtils.createLeg(TransportMode.car);
		plan.addLeg(leg);
		
		act = PopulationUtils.createActivityFromCoord("home", new Coord(431588.499660015, 5794983.07352271));
		act.setStartTime(19*3600);
		act.setEndTime(24*3600);
		plan.addActivity(act);
		
		person.addPlan(plan);
		
		population.addPerson(person);
			
		
	}

}
