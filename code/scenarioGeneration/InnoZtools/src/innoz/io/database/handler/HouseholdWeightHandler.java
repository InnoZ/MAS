package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdWeightHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyHousehold hh = (SurveyHousehold)obj;
		
		double w = Double.parseDouble(attributes.get("hh_gew"));
		
		hh.setWeight(w);
		
	}

}
