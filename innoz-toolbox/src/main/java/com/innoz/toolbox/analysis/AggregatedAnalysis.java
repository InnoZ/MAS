package com.innoz.toolbox.analysis;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.utils.geometry.CoordUtils;

public class AggregatedAnalysis {

	private AggregatedAnalysis(){};

	static Map<String, Integer> modeCounts = new HashMap<>();
	static Map<String, Double> modeDistanceStats = new HashMap<>();
	static Map<String, Double> modeEmissionStats = new HashMap<>();
	
	public static void generate(Scenario scenario) {

		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			Plan plan = person.getSelectedPlan();
			
			for(PlanElement planElement : plan.getPlanElements()) {
				
				if(planElement instanceof Leg) {
					
					Leg leg = (Leg) planElement;
	
					String mode = interpretLegMode(leg.getMode());
					
					if(!modeCounts.containsKey(mode)) {
						
						modeCounts.put(mode, 0);
						modeDistanceStats.put(mode, 0.0);
						modeEmissionStats.put(mode, 0.0);
						
					}
					
					int count = modeCounts.get(mode);
					modeCounts.put(mode, count+1);
					
					double distance = modeDistanceStats.get(mode);
					
					Coord from = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) - 1)).getCoord();
					Coord to = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) + 1)).getCoord();
					double additionalDistance = CoordUtils.calcEuclideanDistance(from, to) * 1.3 / 1000;
					modeDistanceStats.put(mode, distance + additionalDistance);
					
					double emissions = modeEmissionStats.get(mode);
					double additionalEmissions = additionalDistance * getEmissionsFactorForMode(mode);
					modeEmissionStats.put(mode, emissions + additionalEmissions);
					
				}
				
			}
			
		}
		
	}
	
	private static double getEmissionsFactorForMode(String mode) {
		
		if(TransportMode.car.equals(mode)) return 150d / 1000000;
		else if(TransportMode.pt.equals(mode)) return 75d / 1000000;
		else return 0;
		
	}
	
	public static Map<String, String> getModeCounts() {
		
		Map<String, String> map = new HashMap<>();
		
		modeCounts.forEach((mode,count) ->  {
			map.put(mode, Integer.toString(count));
		});
		
		return map;
		
	}
	
	public static Map<String, String> getModeDistanceStats() {
		
		Map<String, String> map = new HashMap<>();
		
		modeDistanceStats.forEach((mode, d) -> {
			map.put(mode, Double.toString(d));
		});
		
		return map;
		
	}
	
	public static Map<String, String> getModeEmissionStats() {

		Map<String, String> map = new HashMap<>();
		
		modeEmissionStats.forEach((mode, d) -> {
			map.put(mode, Double.toString(d));
		});
		
		return map;
		
	}
	
	private static String interpretLegMode(String mode) {
		
		if(mode.contains("oneway") || mode.contains("twoway") || mode.contains("freefloat")) {
			return "carsharing";
		} else {
			return mode;
		}
		
	}
	
	
}