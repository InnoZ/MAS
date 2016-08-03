package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

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
