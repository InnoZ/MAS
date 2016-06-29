package innoz.scenarioGeneration.population.mobilityAttitude;

import static org.junit.Assert.*;

import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class MobilityAttitudeCreationTest {

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
			results[i] = MobilityAttitudeGroups.assignPersonToGroup(person, MatsimRandom.getLocalInstance().nextInt(5000));
			
		}
		
		assertTrue(results[0] == null);
		assertTrue(results[1].equals("multiOpt"));
		assertTrue(results[2].equals("flexCar"));
		assertTrue(results[3].equals("multiOpt"));
		assertTrue(results[4].equals("envtPtBike"));
		assertTrue(results[5].equals("tradCar"));
		assertTrue(results[6].equals("multiOpt"));
		assertTrue(results[7].equals("flexCar"));
		assertTrue(results[8].equals("urbanPt"));
		assertTrue(results[9].equals("urbanPt"));
		
	}
	
}
