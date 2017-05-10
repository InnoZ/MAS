package com.innoz.toolbox.analysis;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.utils.io.IOUtils;

public class AggregatedAnalysis {

	private AggregatedAnalysis(){};
	
	public static void generate(Scenario scenario, String outputPath) {

		Map<String, Integer> modeCounts = new HashMap<>();
		int numberOfLegs = 0;
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			for(PlanElement planElement : person.getSelectedPlan().getPlanElements()) {
				
				if(planElement instanceof Leg) {
					
					numberOfLegs++;
					
					Leg leg = (Leg) planElement;
					
					if(!modeCounts.containsKey(leg.getMode())) {
						
						modeCounts.put(leg.getMode(), 0);
						
					}
					
					int count = modeCounts.get(leg.getMode());
					modeCounts.put(leg.getMode(), count+1);
					
				}
				
			}
			
		}
		
		try {
		
			generateJsonOutput(modeCounts, numberOfLegs, outputPath);
		
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private static void generateJsonOutput(final Map<String, Integer> modeCounts, int n, String path) throws IOException {
		
		BufferedWriter writer = IOUtils.getBufferedWriter(path);
		
		writer.write("{");
		writer.newLine();
		writer.write("\t\"modal_split\" : {");
		
		for(Entry<String, Integer> entry : modeCounts.entrySet()) {
			
			writer.newLine();
			writer.write("\t\t{\"mode\": \"" + entry.getKey() + "\",");
			writer.write("\"share\" : \"" + ((double)entry.getValue()/n) + "\"},");
			writer.newLine();
			writer.flush();
			
		}
		
		writer.newLine();
		writer.write("\t}");
		writer.newLine();
		writer.write("}");
		
		writer.close();
		
	}
	
}