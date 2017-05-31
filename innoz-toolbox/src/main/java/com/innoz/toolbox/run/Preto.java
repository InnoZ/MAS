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

import com.innoz.toolbox.analysis.AggregatedAnalysis;
import com.innoz.toolbox.io.pgsql.MatsimPsqlAdapter;
import com.innoz.toolbox.utils.GlobalNames;
import com.innoz.toolbox.utils.misc.PlansToJson;

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
			Activity act = factory.createActivityFromCoord("home", new Coord(-0.144194, 51.431663));
			act.setStartTime(0);
			act.setEndTime(8 * 3600);
			plan.addActivity(act);
		}

		plan.addLeg(factory.createLeg(TransportMode.pt));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(-0.085854, 51.512692));
			act.setStartTime(9 * 3600);
			act.setEndTime(14 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("eating_out", new Coord(-0.083639, 51.511175));
			act.setStartTime(14.1 * 3600);
			act.setEndTime(14.5 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg(TransportMode.walk));
		
		{
			Activity act = factory.createActivityFromCoord("work", new Coord(-0.085854, 51.512692));
			act.setStartTime(14.75 * 3600);
			act.setEndTime(18 * 3600);
			plan.addActivity(act);
		}
		
		plan.addLeg(factory.createLeg("carsharing"));
		
		{
			Activity act = factory.createActivityFromCoord("home", new Coord(-0.144194, 51.431663));
			act.setStartTime(19 * 3600);
			act.setEndTime(24 * 3600);
			plan.addActivity(act);
		}		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		population.addPerson(person);
		
		PlansToJson.run(scenario, outputDirectory + "features.json", GlobalNames.WGS84);
		MatsimPsqlAdapter.writeScenarioToPsql(scenario, args[0] + "_" + args[1]);
		AggregatedAnalysis.generate(scenario, outputDirectory + "aggregatedAnalysis.json");
		
	}
	
}