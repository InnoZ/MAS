package playground.dhosse.scenarioGeneration.population.utils;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.population.Person;

import playground.dhosse.scenarioGeneration.population.surveys.SurveyPerson;
import playground.dhosse.scenarioGeneration.utils.ActivityTypes;

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

	public static String getEducationalActTypeForPerson(Person person){
		
		int age = (int) person.getCustomAttributes().get(ATT_AGE);
		
		if(age < 6){
			
			return ActivityTypes.KINDERGARTEN;
			
		} else if(age >= 6 && age < 13){
			
			return ActivityTypes.PRIMARY_SCHOOL;
			
		} else if(age >= 13 && age < 18){
			
			return ActivityTypes.SECONDARY_SCHOOL;
			
		} else{
			
			return ActivityTypes.UNIVERSITY;
			
		}
		
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
		
		double weight = 0;
		
		for(SurveyPerson p : collection){
		
			weight += p.getWeight();
			
		}
		
		return weight;
		
	}
	
}
