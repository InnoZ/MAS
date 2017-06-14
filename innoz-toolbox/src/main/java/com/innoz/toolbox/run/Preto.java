package com.innoz.toolbox.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.io.pgsql.MatsimPsqlAdapter;

/**
 * 
 * java -cp <path-to-jar> com.innoz.toolbox.run.Main args1 args2 args3 args4
 * 
 * args0: survey area id (AGKZ)
 * args1: year
 * args2: output path
 * args3: rails environment
 * 
 * @author dhosse
 *
 */
public class Preto {

	public static void main(String args[]) throws IOException {
		
		String outputDirectory = args[2] + "/" + args[0] + "_" + args[1] + "/";

		Files.createDirectories(Paths.get(outputDirectory));
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		Population population = scenario.getPopulation();
		PopulationFactory factory = population.getFactory();
		
		Person person = factory.createPerson(Id.createPersonId("nigel"));
		
		Plan plan = factory.createPlan();
		
		{
			Activity act = factory.createActivityFromCoord("home", new Coord(9.732166, 52.418675));
			act.setStartTime(0);
			act.setEndTime(8 * 3600);
			plan.addActivity(act);
		}

		plan.addLeg(factory.createLeg(TransportMode.pt));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(9.713913, 52.417740));
			act.setStartTime(9 * 3600);
			act.setEndTime(14 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("eating_out", new Coord(9.715572, 52.418324));
			act.setStartTime(14.1 * 3600);
			act.setEndTime(14.5 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(9.713913, 52.417740));
			act.setStartTime(14.75 * 3600);
			act.setEndTime(18 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg("carsharing"));
		
		{
			Activity act = factory.createActivityFromCoord("home", new Coord(9.732166, 52.418675));
			act.setStartTime(19 * 3600);
			act.setEndTime(24 * 3600);
			plan.addActivity(act);
		}		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		population.addPerson(person);
		
		MatsimPsqlAdapter.writeScenarioToPsql(scenario, args[0] + "_" + args[1], args[3]);
		
	}
	
}