package com.innoz.scenarios.osnabrück;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

/*
 * This class can be used to change the distribution of the population's mobilityAttitudes
 * Adjust the doubles in line 63-69
 * @author bsmoehring
 */

public class RunAdjustMobilityAttitude {

	
	static final String FILEBASE = "/home/bmoehring/3connect/";
	
	static final String MOBILITYATTITUDE = "mobilityAttitude";
	static final String AGE  = "age";
	
	static final String TRADCAR = "tradCar";
	static final String FLEXCAR = "flexCar";
	static final String URBANPT = "urbanPt";
	static final String CONVBIKE = "convBike";
	static final String ENVTPTBIKE = "envtPtBike";
	static final String MULTIOPT = "multiOpt";
	static final String NONE = "none";
	
	public static void main(String[] args) {
				
		Config config = ConfigUtils.createConfig();
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		PopulationReader pr = new PopulationReader(scenario);
		pr.readFile(FILEBASE + "plans_2025.xml.gz");
		ObjectAttributesXmlReader oa= new ObjectAttributesXmlReader(scenario.getPopulation().getPersonAttributes());
		oa.readFile(FILEBASE + "personAttributes_2025.xml.gz");
		
		ObjectAttributes attributes = scenario.getPopulation().getPersonAttributes();
			
		{
			Map<String, Integer> distribution = countMobilityAttitudes(scenario.getPopulation().getPersons().keySet(), attributes);
			
			printCount(distribution);
			
			Map<String, Double> distributionNew = new HashMap<String, Double>();
	//		You can specify the distribution for people older 18 years here:
			distributionNew.put(TRADCAR, 		0.152);
			distributionNew.put(FLEXCAR, 		0.209);
			distributionNew.put(URBANPT, 		0.082);
			distributionNew.put(CONVBIKE, 		0.189);
			distributionNew.put(ENVTPTBIKE, 	0.170);
			distributionNew.put(MULTIOPT, 		0.198);
			distributionNew.put("null", 		0.000);
			
			Map<String, Integer> distributionDiff = calcDistribution(distribution, distributionNew);
			
			attributes = adjustAttitudes(scenario.getPopulation(), attributes, distributionDiff);
			
			distribution = countMobilityAttitudes(scenario.getPopulation().getPersons().keySet(), attributes);
			printCount(distribution);
			
			ObjectAttributesXmlWriter writer = new ObjectAttributesXmlWriter(attributes);
			writer.writeFile(FILEBASE + "personAttributes_2025.xml.gz");
			System.out.println("Attitudes adjusted and writen to "+ FILEBASE + "personAttributes.xml.gz");
		}
	}

	private static ObjectAttributes adjustAttitudes(Population population, ObjectAttributes attributes, Map<String, Integer> distributionDiff) {
			
			for (Person p : population.getPersons().values()){
				
				String mobilityAttitude; 
				int age;
				
				try {
					
					mobilityAttitude = attributes.getAttribute(p.getId().toString(), MOBILITYATTITUDE).toString();
					
				} catch(Exception e) { 
					
					mobilityAttitude = "null";
					
				} 
				
				try {
					
					if (mobilityAttitude.equalsIgnoreCase(NONE) || (Integer)(p.getAttributes().getAttribute("age")) < 18){
						
						attributes.putAttribute(p.getId().toString(), MOBILITYATTITUDE, NONE);
						
					} else if (distributionDiff.get(mobilityAttitude) < 0){
						
						for (Entry<String, Integer> entry : distributionDiff.entrySet()){
							
							if (entry.getValue()>0){
								
								attributes.putAttribute(p.getId().toString(), MOBILITYATTITUDE, entry.getKey());
								
								entry.setValue(entry.getValue()-1);
								
								distributionDiff.put(mobilityAttitude, distributionDiff.get(mobilityAttitude)+1);	
								
								//System.out.println(p.getId().toString() + " changed from " + mobilityAttitude + " to " + entry.getKey());
								
								break;
								
							}
							
						}
						
					} 
				} catch (Exception e){
					
				}
				
			}
			
			return attributes;
			
		}

	private static Map<String, Integer> calcDistribution(Map<String, Integer> distributionOld,	Map<String, Double> distributionNew) {
		
		Integer sumPersonsWithAttitude = 0;
		Map<String, Integer> distributionDiff = new HashMap<String, Integer>();
		
		for (Entry<String, Integer> entry : distributionOld.entrySet()){
			
			sumPersonsWithAttitude = sumPersonsWithAttitude + entry.getValue();
			
		}
		
		double sum = 0;
		for (Entry<String, Double> entry : distributionNew.entrySet()){
			
			sum += entry.getValue();
			
		}
		
		System.out.println("sum of weights:  " + sum);
		System.out.println(sumPersonsWithAttitude);
		
		for (Entry<String, Double> entry : distributionNew.entrySet()){
			
			String mobilityAttitude = entry.getKey().toString(); 
			int oldcount = 0;
			try {
				
				oldcount = distributionOld.get(mobilityAttitude).intValue();
				
			} catch (Exception e) {
				
				oldcount = 0;
				
			} 
			Integer attitudePersons = (int)(Math.round((distributionNew.get(mobilityAttitude)/sum) * sumPersonsWithAttitude)-oldcount);
			distributionDiff.put(mobilityAttitude, attitudePersons);
		}
		
		System.out.println(distributionDiff.toString());
		
		return distributionDiff;
	}

	private static Map<String, Integer> countMobilityAttitudes(Set<Id<Person>> personIds, ObjectAttributes attributes){
		
		int persons = 0;
		int none = 0;
		
		Map<String, Integer> mobilityAttitudesCount = new HashMap<String, Integer>();
		
//		iterate through personalAttributes by 
		
		for (Id<Person> id  : personIds){
			
			String mobilityAttitude;
			
			try {	
				
				mobilityAttitude = attributes.getAttribute(id.toString(), MOBILITYATTITUDE).toString();
				
			} catch (Exception e) {
				
				mobilityAttitude = "null";
			} 
				
			if (!mobilityAttitudesCount.containsKey(mobilityAttitude)){
				
				mobilityAttitudesCount.put(mobilityAttitude, 1);
				
			} else {
				
				mobilityAttitudesCount.put(mobilityAttitude, mobilityAttitudesCount.get(mobilityAttitude)+1);
			} 

			persons++;
			
		}
		
		return mobilityAttitudesCount;
		
	}
	
	private static void printCount(Map<String, Integer> mobilityAttitudesCount){
		
		int persons = 0;
		
		for (Integer entry : mobilityAttitudesCount.values()){
			persons += entry;
		}
		
		for (Entry<String, Integer> entry : mobilityAttitudesCount.entrySet()){
			Double percentage = (double)entry.getValue() / (double)persons * 100;
			System.out.println(entry.getKey() + " : " + String.format( "%.2f", percentage) + " %");
		}
		
		System.out.println("in total: " + persons + " persons");
		System.out.println(mobilityAttitudesCount);
		
	}

}
