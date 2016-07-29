package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonGroupHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String persongroup = attributes.get("pg12");
		
		if(persongroup != null){
			
			person.setPersonGroup(Integer.parseInt(persongroup));
			
		}
		
	}

}