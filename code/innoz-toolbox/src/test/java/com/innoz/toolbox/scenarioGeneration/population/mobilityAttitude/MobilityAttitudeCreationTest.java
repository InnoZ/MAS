package com.innoz.toolbox.scenarioGeneration.population.mobilityAttitude;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;

public class MobilityAttitudeCreationTest {

	@Ignore
	@Test
	public void testPersonCreation(){
		
		Scenario scenario = ScenarioUtils.createScenario(
				ConfigUtils.createConfig());
		
		MatsimRandom.reset(4711L);
		
		int[] ages = new int[]{14,18,25,32,38,42,45,56,61,70};
		
		String[] results = new String[10];
		
		for(int i = 0; i < 10; i++){
			
			Person person = scenario.getPopulation().getFactory().createPerson(
					Id.createPersonId(i));
			PersonUtils.setAge(person, ages[i]);
			String sex = null;
			if(MatsimRandom.getLocalInstance().nextDouble() >= 0.5){
				sex = "m";
			} else {
				sex = "f";
			}
			PersonUtils.setSex(person, sex);
			
			Random random = MatsimRandom.getLocalInstance();
			
			results[i] = MobilityAttitudeGroups.assignPersonToGroup(person,
					random, random.nextInt(5000), new ObjectAttributes());
			
		}
		
		assertTrue(results[0] == null);
		assertTrue(results[1].equals("flexCar"));
		assertTrue(results[2].equals("convBike"));
		assertTrue(results[3].equals("flexCar"));
		assertTrue(results[4].equals("envtPtBike"));
		assertTrue(results[5].equals("multiOpt"));
		assertTrue(results[6].equals("flexCar"));
		assertTrue(results[7].equals("flexCar"));
		assertTrue(results[8].equals("flexCar"));
		assertTrue(results[9].equals("convBike"));
		
	}
	
}
