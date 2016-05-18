package playground.dhosse.scenarios.generic.population;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.matsim.api.core.v01.population.Person;

import playground.dhosse.scenarios.generic.population.io.mid.SurveyPerson;
import playground.dhosse.scenarios.generic.utils.ActivityTypes;

public class PersonUtils {

	static final String CHILD = "child";
	static final String ADULT = "adult";
	static final String PENSIONER = "pensioner";
	
	static final String PERSON_ATTRIBUTES = "personAttributes";
	
	static final String ATT_AGE = "age";
	static final String ATT_SEX = "sex";

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
//		return templatePersons.values().get(new Random().nextInt(templatePersons.size()));
		
	}
	
	public static double getTotalWeight(Collection<SurveyPerson> collection){
		
		double weight = 0;
		
		for(SurveyPerson p : collection){
		
			weight += p.getWeight();
			
		}
		
		return weight;
		
	}
	
}
