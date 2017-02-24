package com.innoz.toolbox.scenarioGeneration.population.utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MapX;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonUtilsTest {

	ListX<SurveyPerson> persons;
	
	@Before
	public void setup() {
		
		SurveyPerson p1 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p1.setAge(20);
		p1.setWeight(1);
		SurveyPerson p2 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p2.setAge(15);
		p2.setWeight(0.5);
		SurveyPerson p3 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p3.setAge(41);
		p3.setWeight(1);
		SurveyPerson p4 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p4.setAge(12);
		p4.setWeight(0.5);
		
		persons = ListX.of(p1, p2, p3, p4);
		
	}
	
	@Test
	public void testAgeGroupsClassification() {
		
		MapX<Integer, List<SurveyPerson>> map = persons.groupBy(SurveyPerson::getAgeGroup);
		
		assertEquals("Unexpected number of persons for age group!", 2, map.get(new Integer(18)).size());
		assertEquals("Unexpected number of persons for age group!", 1, map.get(new Integer(25)).size());
		assertEquals("Unexpected number of persons for age group!", 1, map.get(new Integer(45)).size());
		
	}
	
	public void testTotalWeightComputation() {
		
		assertEquals("Wrong total weight for person collection!", 3.0, PersonUtils.getTotalWeight(persons), 0.0);
		
	}
	
}