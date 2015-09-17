package simulationWithFacilitiesForMID_Data;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;


public class MyTest<TestCreateDemandMethods> {

	CreateDemandWithMID_Data demandCreator = new CreateDemandWithMID_Data();
	ObjectAttributes personsCS_CardExistence = new ObjectAttributes();

	@Test
	public void testCS_Card(){
		Id id1 = new IdImpl("12");
		Id id2 = new IdImpl("22");
		Person person1 = new PersonImpl(id1);
		Person person2 = new PersonImpl(id2);
		((PersonImpl) person1).setLicence("yes");
		((PersonImpl) person2).setLicence("no");
		
		Assert.assertEquals(2,  demandCreator.hasCS_Card(person2, 2));
		Assert.assertEquals(3,  demandCreator.hasCS_Card(person1, 2));
		Assert.assertEquals(0,  demandCreator.hasCS_Card(person1, 3));


	}
}
