package com.innoz.toolbox.utils.population;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class PlanAnalysis {

	public static void main(String[] args) {
		
		Config config = new Config();
		
		ConfigUtils.loadConfig( config, "/home/bmoehring/scenarios/osnabrueck/test/config.xml" );
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Map<Integer, Integer> planElementsCount = new HashMap<Integer, Integer>();
		
		for (Person person : scenario.getPopulation().getPersons().values()){
			
			Integer planElements = person.getPlans().get(0).getPlanElements().size();
			
			if (planElementsCount.containsKey(planElements)){
				planElementsCount.put(planElements, (planElementsCount.get(planElements).intValue()+1));
			} else {
				planElementsCount.put(planElements, 1);
			}
			
			if(planElements > 18){
				System.out.println(person.getId().toString() + " " + planElements);
			}
//			System.out.println(person.getId().toString() + " " 
//					+ person.getPlans().size() + " " 
//					+ person.getPlans().get(0).getPlanElements().size() + " " 
//					+ person.getPlans().get(0).getPlanElements());
			
		}
		
		int total = 0;
		for (Entry<Integer, Integer> entry : planElementsCount.entrySet()){
			total += entry.getValue();
			System.out.println(entry.getKey() + " " + entry.getValue());
			
		}
		
		System.out.println(total);
		
		
	}

}
