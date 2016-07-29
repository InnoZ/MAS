package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdWeightHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String w = attributes.get("hh_gew");
		
		if(w != null){
			
			hh.setWeight(Double.parseDouble(w));
			
		} else {
			
			hh.setWeight(null);
			
		}
		
	}

}
