package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonSexHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String sex = attributes.get("hp_sex");
		
		if(sex.equals("1")){
			person.setSex("m");
		} else {
			person.setSex("f");
		}
		
	}

}
