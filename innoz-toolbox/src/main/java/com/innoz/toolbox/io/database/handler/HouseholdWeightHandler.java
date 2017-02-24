package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdWeightHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String w = attributes.get(SurveyConstants.householdWeight(surveyType));
		
		if(w != null){
			
			hh.setWeight(Double.parseDouble(w));
			
		} else {
			
			hh.setWeight(-1);
			
		}
		
	}

}
