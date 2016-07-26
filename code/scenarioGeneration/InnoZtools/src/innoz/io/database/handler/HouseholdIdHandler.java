package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdIdHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String hhId = attributes.get("hhid");
		
		hh.setId(hhId);
		

	}

}
