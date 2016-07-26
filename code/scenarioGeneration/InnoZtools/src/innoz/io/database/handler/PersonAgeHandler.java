package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonAgeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyPerson person = (SurveyPerson)obj;
		
		String age = attributes.get("hp_alter");
		
		if(age != null && !age.equals("NaN")){
			
			Integer a = Integer.parseInt(age);
			
			if(a > 0 && a < 102){
				
				person.setAge(a);
				
			}
			
		}

	}

}
