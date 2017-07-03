package com.innoz.toolbox.scenarioGeneration.population.utils;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.matsim.core.gbl.MatsimRandom;

public class PersonUtils {

	static final String CHILD = "child";
	static final String ADULT = "adult";
	static final String PENSIONER = "pensioner";
	
	public static final String PERSON_ATTRIBUTES = "personAttributes";
	
	public static final String ATT_AGE = "age";
	public static final String ATT_SEX = "sex";
	public static final String ATT_EMPLOYED = "isEmployed";
	public static final String ATT_CAR_AVAIL = "carAvail";
	public static final String ATT_LICENSE = "hasLicense";
	public static final String ATT_MOBILITYATTITUDE = "mobilityAttitude";
	
	static final String[] mobilityAttitudes = {"none", "tradCar", "flexCar", "urbanPt",
			"convBike", "envtPtBike", "multiOpt",null};
	
	public static String[] getMobilityAttitudes() {
		
		return mobilityAttitudes;
		
	}
	
	static final Integer[] ageGroups = new Integer[]{0,5,10,18,25,35,45,55,65,75,85};
	
	public static int getAgeGroup(int age) {
		
		return Arrays.asList(ageGroups).stream().filter(p -> p.intValue() >= age).findFirst().get().intValue();
		
	}

	public static String getEducationalActTypeForPerson(SurveyPerson person){
		
		int personGroup12 = person.getPersonGroup();
		
		if(personGroup12 == 5 || personGroup12 == 6){

			return ActivityTypes.UNIVERSITY;
			
		} else if(personGroup12 == 7 || personGroup12 == 8){
			
			return ActivityTypes.PROFESSIONAL_SCHOOL;
			
		} else if(personGroup12 == 9){
			
			return ActivityTypes.KINDERGARTEN;
			
		} else if(personGroup12 == 10){
			
			return ActivityTypes.PRIMARY_SCHOOL;
			
		} else if(personGroup12 == 11 | personGroup12 == 12){
			
			return ActivityTypes.SECONDARY_SCHOOL;
			
		} else return null;
		
	}
	
	public static int setPersonAge(){
		
		return 0;
		
	}
	
	public static SurveyPerson getTemplate(Map<String, SurveyPerson> templatePersons, double personalRandom){
		
		double accumulatedWeight = 0.;
		
		for(SurveyPerson person : templatePersons.values()){
			
			accumulatedWeight += person.getWeight();
			
			if(personalRandom <= accumulatedWeight){
				
				return person;
				
			}
			
		}
		
		return null;
		
	}
	
	public static double getTotalWeight(Collection<SurveyPerson> collection){
		
		return collection.stream().collect(Collectors.summarizingDouble(SurveyPerson::getWeight)).getSum();
		
	}
	
	public static double createRandomEndTime() {
		
		Random random = MatsimRandom.getLocalInstance();
		double r1 = random.nextDouble();
		double r2 = random.nextDouble();
		// Box-Muller-Method in order to get a normally distributed variable
		double normal = Math.cos(2 * Math.PI * r1) * Math.sqrt(-2 * Math.log(r2));
		double endTime = 20 * 60 * normal;
		
		return endTime;
		
	}
	
}