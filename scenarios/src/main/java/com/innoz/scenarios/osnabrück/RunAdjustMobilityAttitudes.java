package com.innoz.scenarios.osnabrück;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

/*
 * This class can be used to change the distribution of the population's mobilityAttitudes
 * Adjust the doubles in line 47-52
 * @bsmoehring
 */

public class RunAdjustMobilityAttitudes {

	
	static String filebase = "/home/bmoehring/scenarios/osnabrueck/2017_03404_base/";
	
	static final String MOBILITYATTITUDE = "mobilityAttitude";
	
	static final String TRADCAR = "tradCar";
	static final String FLEXCAR = "flexCar";
	static final String URBANPT = "urbanPt";
	static final String CONVBIKE = "convBike";
	static final String ENVTPTBIKE = "envtPtBike";
	static final String MULTIOPT = "multiOpt";
	static final String NONE = "none";

	public static void main(String[] args) {
				
		Config config = ConfigUtils.loadConfig(filebase + "config.xml.gz");
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		ObjectAttributes attributes = scenario.getPopulation().getPersonAttributes();
		Set<Id<Person>> personIds = scenario.getPopulation().getPersons().keySet();
		
		Map<String, Integer> distributionOld = countMobilityAttitudes(personIds, attributes);
		
		Map<String, Double> distributionNew = new HashMap<String, Double>();
//		distributionNew.put(TRADCAR, 		0.152);
//		distributionNew.put(FLEXCAR, 		0.209);
//		distributionNew.put(URBANPT, 		0.082);
//		distributionNew.put(CONVBIKE, 		0.189);
//		distributionNew.put(ENVTPTBIKE, 	0.170);
//		distributionNew.put(MULTIOPT, 		0.198);
		
//		You can specify the distribution for people older 18 years here:
		distributionNew.put(TRADCAR, 		0.152);
		distributionNew.put(FLEXCAR, 		0.209);
		distributionNew.put(URBANPT, 		0.082);
		distributionNew.put(CONVBIKE, 		1.189);
		distributionNew.put(ENVTPTBIKE, 	0.170);
		distributionNew.put(MULTIOPT, 		0.198);
		
		Map<String, Integer> distributionDiff = calcDistribution(distributionOld, distributionNew);
		
		adjustAttitudes(personIds, attributes, distributionDiff);
		
		ObjectAttributesXmlWriter writer = new ObjectAttributesXmlWriter(attributes);
		writer.writeFile(filebase + "personAttributes.xml.gz");

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
		
		for (Entry<String, Double> entry : distributionNew.entrySet()){
			
			String mobilityAttitude = entry.getKey().toString(); 
			Integer attitudePersons = (int)(Math.round((distributionNew.get(mobilityAttitude)/sum) * sumPersonsWithAttitude)-distributionOld.get(mobilityAttitude));
			distributionDiff.put(mobilityAttitude, attitudePersons);
			
		}
		
		System.out.println(distributionDiff.toString());
		
		return distributionDiff;
	}

	private static void adjustAttitudes(Set<Id<Person>> personIds, ObjectAttributes attributes, Map<String, Integer> distributionDiff) {
		
		for (Id<Person> id : personIds){
			
			String mobilityAttitude = attributes.getAttribute(id.toString(), MOBILITYATTITUDE).toString();
			
			if (mobilityAttitude.equals(NONE)){
				
				
				
			} else if (Integer.parseInt((String) attributes.getAttribute(id.toString(), "age")) < 18){
				
				attributes.putAttribute(id.toString(), MOBILITYATTITUDE, NONE);
				
			} else if (distributionDiff.get(mobilityAttitude) < 0){
				
				for (Entry<String, Integer> entry : distributionDiff.entrySet()){
					
					if (entry.getValue()>0){
						
						attributes.putAttribute(id.toString(), MOBILITYATTITUDE, entry.getKey());
						
						entry.setValue(entry.getValue()-1);
						
						distributionDiff.put(mobilityAttitude, distributionDiff.get(mobilityAttitude)+1);	
						
						System.out.println(id.toString() + " changed from " + mobilityAttitude + " to " + entry.getKey());
						
						break;
						
					}
					
				}
				
			} 
			
		}
		
	}

	private static Map<String, Integer> countMobilityAttitudes(Set<Id<Person>> personIds, ObjectAttributes attributes){
		
		int persons = 0;
		int none = 0;
		
		Map<String, Integer> mobilityAttitudesCount = new HashMap<String, Integer>();
		
//		iterate through personalAttributes by 
		
		for (Id<Person> id  : personIds){
			
			String mobilityAttitude = attributes.getAttribute(id.toString(), MOBILITYATTITUDE).toString();
			
			if ( mobilityAttitude.equals(NONE) || Integer.parseInt((String) attributes.getAttribute(id.toString(), "age")) < 18){
				
				
				none ++;
				
			
			} else if (!mobilityAttitudesCount.containsKey(mobilityAttitude)){
							
				mobilityAttitudesCount.put(mobilityAttitude, 1);
				
			} else {
				
				mobilityAttitudesCount.put(mobilityAttitude, mobilityAttitudesCount.get(mobilityAttitude)+1);
			} 
			
			persons++;
			
		}
		
		System.out.println("in total: " + persons + " persons");
		System.out.println(mobilityAttitudesCount);
		System.out.println("none=" + none);
		
		return mobilityAttitudesCount;
		
	}

}
