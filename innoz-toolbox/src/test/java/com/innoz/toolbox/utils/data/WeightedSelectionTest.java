package com.innoz.toolbox.utils.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class WeightedSelectionTest {

	ListX<SurveyPerson> persons;
	
	@Before
	public void setup() {
		
		SurveyPerson p1 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p1.setId("1");
		p1.setWeight(1);
		SurveyPerson p2 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p2.setId("2");
		p2.setWeight(0.5);
		SurveyPerson p3 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p3.setId("3");
		p3.setWeight(1);
		SurveyPerson p4 = (SurveyPerson) SurveyObject.newInstance(SurveyPerson.class);
		p4.setId("4");
		p4.setWeight(0.5);
		
		persons = ListX.of(p1, p2, p3, p4);
		
	}
	
	@Test
	public void testWeightedSelection() {
		
		SurveyPerson p = (SurveyPerson) WeightedSelection.choose(persons, 0.5);
		
		assertEquals("The weighted selection method returned the wrong person!", "2", p.getId());
		
	}
	
}