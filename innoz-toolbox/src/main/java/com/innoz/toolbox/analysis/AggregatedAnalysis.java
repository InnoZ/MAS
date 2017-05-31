package com.innoz.toolbox.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.io.IOUtils;

public class AggregatedAnalysis {

	private AggregatedAnalysis(){};
	
	public static void generate(Scenario scenario, String outputPath) {

		Map<String, Integer> modeCounts = new HashMap<>();
		Map<String, Double> modeDistanceStats = new HashMap<>();
		
		int numberOfLegs = 0;
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			Plan plan = person.getSelectedPlan();
			
			for(PlanElement planElement : plan.getPlanElements()) {
				
				if(planElement instanceof Leg) {
					
					numberOfLegs++;
					
					Leg leg = (Leg) planElement;
					
					if(!modeCounts.containsKey(leg.getMode())) {
						
						modeCounts.put(leg.getMode(), 0);
						modeDistanceStats.put(leg.getMode(), 0.0);
						
					}
					
					int count = modeCounts.get(leg.getMode());
					modeCounts.put(leg.getMode(), count+1);
					
					double distance = modeDistanceStats.get(leg.getMode());
					
					Coord from = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) - 1)).getCoord();
					Coord to = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) + 1)).getCoord();
					modeDistanceStats.put(leg.getMode(), distance + CoordUtils.calcEuclideanDistance(from, to) * 1.3 );
					
				}
				
			}
			
		}
		
		try {
		
			generateJsonOutput(modeCounts, modeDistanceStats, numberOfLegs, outputPath);
		
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private static void generateJsonOutput(Map<String, Integer> modeCounts, Map<String, Double> modeDistanceStats, int n,
			String path) throws IOException {
		
		BufferedWriter writer = IOUtils.getBufferedWriter(path);
		
		writer.write("{");
		writer.write("\"modal_split\": [");
	
		int i = 0;
		
		for(Entry<String, Integer> entry : modeCounts.entrySet()) {

			i++;
			
			writer.write("{\"mode\": \"" + entry.getKey() + "\",");
			
			if(i >= modeCounts.entrySet().size()) {
				writer.write("\"share\" : \"" + ((double)entry.getValue()/n) + "\"}");
			} else {
				writer.write("\"share\" : \"" + ((double)entry.getValue()/n) + "\"},");
			}
			writer.flush();
			writer.newLine();
			
		}
		
		writer.write("],")
		;
		writer.newLine();
		
		writer.write("\"mode_distance_stats\": [");
		
		i = 0;
		
		for(Entry<String, Double> entry : modeDistanceStats.entrySet()) {
			
			i++;
			
			writer.write("{\"mode\": \"" + entry.getKey() + "\",");
			
			if(i >= modeDistanceStats.entrySet().size()) {
				writer.write("\"distance\" : \"" + entry.getValue() + "\"}");
			} else {
				writer.write("\"distance\" : \"" + entry.getValue() + "\"},");
			}
			
			writer.flush();
			writer.newLine();
			
		}
		
		writer.write("],");
		
		writer.newLine();
		
		writer.write("\"mode_emission_stats\": [");
		
		i = 0;
		
		for(Entry<String, Double> entry : modeDistanceStats.entrySet()) {

			double factor = 0.0;
			
			switch(entry.getKey()) {
				case TransportMode.car: factor = 150.0 / 1000000;
										break;
				case TransportMode.pt: factor = 75.0 / 1000000;
										break;
				default: break;
			}
			
			i++;
			
			writer.write("{\"mode\": \"" + entry.getKey() + "\",");
			
			double d = factor * entry.getValue();
			
			if(i >= modeDistanceStats.entrySet().size()) {
				writer.write("\"co2eq\" : \"" + d + "\"}");
			} else {
				writer.write("\"co2eq\" : \"" + d + "\"},");
			}
			
			writer.flush();
			writer.newLine();
			
		}
		
		writer.write("]");
		
		writer.write("}");
		
		writer.close();
		
	}
	
}