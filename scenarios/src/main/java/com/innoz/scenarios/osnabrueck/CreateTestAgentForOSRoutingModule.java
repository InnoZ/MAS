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

		new PopulationWriter(scenario.getPopulation()).write("/home/bmoehring/3connect/TestNetworkRoutingWithAccess/input_positiv/plans.xml.gz");
		
	}

	private static Population createTestAgent(Population population) {
		
		PopulationFactory pf = population.getFactory();
		
		population.addPerson(createAgent(TransportMode.car, "verbrenner", pf, population.getPersons().size()));
		population.addPerson(createAgent(TransportMode.car, "electric", pf, population.getPersons().size()));
		population.addPerson(createAgent(TransportMode.bike, null, pf, population.getPersons().size()));
		population.addPerson(createAgent(TransportMode.pt, null, pf, population.getPersons().size()));
		population.addPerson(createAgent("pedelec", null, pf, population.getPersons().size()));
		
		return population;
		
	}
	
	private static Person createAgent(String mode, String vehicleType, PopulationFactory pf, int i){
		
		Person person = pf.createPerson(Id.createPersonId(Integer.toString(i)));
		if (vehicleType != null){
			person.getAttributes().putAttribute("vehicleType", vehicleType);
		}
		person.getAttributes().putAttribute("carAvail", "always");
		person.getAttributes().putAttribute("hasLicense", "yes");
		Plan plan = PopulationUtils.createPlan();
		
		Activity act = PopulationUtils.createActivityFromCoord("home", new Coord(465925.77056044666, 5779162.656347102));
		act.setEndTime(7.5*3600);
		plan.addActivity(act);
		
		Leg leg = PopulationUtils.createLeg(mode);
		plan.addLeg(leg);
		act = PopulationUtils.createActivityFromCoord("work", new Coord(434991.88942246226, 5792153.973491052));
		act.setStartTime(8.5*3600);
		act.setEndTime(17.5*3600);
		plan.addActivity(act);
		
		leg = PopulationUtils.createLeg(mode);
		plan.addLeg(leg);
		
		act = PopulationUtils.createActivityFromCoord("home", new Coord(465925.77056044666, 5779162.656347102));
		act.setStartTime(19*3600);
		act.setEndTime(24*3600);
		plan.addActivity(act);
		
		person.addPlan(plan);
		return person;
	}

}
