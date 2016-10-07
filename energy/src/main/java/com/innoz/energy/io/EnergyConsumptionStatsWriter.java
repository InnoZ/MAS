package com.innoz.energy.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.io.IOUtils;

public class EnergyConsumptionStatsWriter {

	public void writePersonBasedStats(String path, Map<Id<Person>, Double> map){
		
		BufferedWriter writer = IOUtils.getBufferedWriter(path);
		
		try {
		
			writer.write("person_id\tenergy_consumption_kWh");
			
			for(Entry<Id<Person>, Double> entry : map.entrySet()){
				
				writer.newLine();
				writer.write(entry.getKey().toString() + "\t" + entry.getValue());
				
			}

			writer.flush();
			writer.close();
		
		} catch (IOException e) {

			e.printStackTrace();
		
		}
		
	}
	
	public void writeAggregatedStatsPerHour(String path, double[] values){
		
		BufferedWriter writer = IOUtils.getBufferedWriter(path);
		
		try {
		
			writer.write("hour\tenergy_consumption_kWh");
			
			for(int i = 0; i < values.length; i++){
				
				writer.newLine();
				writer.write(i + "\t" + values[i]);
				
			}

			writer.flush();
			writer.close();
		
		} catch (IOException e) {

			e.printStackTrace();
		
		}
		
	}
	
}