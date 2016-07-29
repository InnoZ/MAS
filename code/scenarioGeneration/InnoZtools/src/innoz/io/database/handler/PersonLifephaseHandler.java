package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonLifephaseHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String lifephase = attributes.get("lebensph");
		
		if(lifephase != null){
			
			person.setLifePhase(Integer.parseInt(lifephase));
			
		}
		
	}

}
