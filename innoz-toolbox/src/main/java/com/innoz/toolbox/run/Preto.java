package com.innoz.toolbox.run;

import java.io.IOException;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.utils.GlobalNames;
import com.innoz.toolbox.utils.misc.PlansToJson;

public class Preto {

	public static void main(String args[]) throws IOException {
		
		String outputDirectory = args[2] + "/" + args[0] + "_" + args[1] + "/";
		IOUtils.createDirectory(outputDirectory);

		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		Population population = scenario.getPopulation();
		PopulationFactory factory = population.getFactory();
		
		Person person = factory.createPerson(Id.createPersonId("nigel"));
		
		Plan plan = factory.createPlan();
		
		{
			Activity act = factory.createActivityFromCoord("home", new Coord(0.0, 0.0));
			act.setStartTime(0);
			act.setEndTime(8 * 3600);
			plan.addActivity(act);
		}

		plan.addLeg(factory.createLeg(TransportMode.pt));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(100.0, 100.0));
			act.setStartTime(9 * 3600);
			act.setEndTime(14 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("eating_out", new Coord(105.0, 100.0));
			act.setStartTime(14.1 * 3600);
			act.setEndTime(14.5 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(100.0, 100.0));
			act.setStartTime(14.75 * 3600);
			act.setEndTime(18 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg("carsharing"));
		
		{
			Activity act = factory.createActivityFromCoord("home", new Coord(0.0, 0.0));
			act.setStartTime(19 * 3600);
			act.setEndTime(24 * 3600);
			plan.addActivity(act);
		}		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		population.addPerson(person);
		
		new PopulationWriter(population).write(outputDirectory + "plans.xml.gz");
		
		PlansToJson.run(scenario, outputDirectory + "features.json", GlobalNames.WGS84);
		
	}
	
}