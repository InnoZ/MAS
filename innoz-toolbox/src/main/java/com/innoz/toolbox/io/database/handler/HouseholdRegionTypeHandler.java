package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdRegionTypeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String type = attributes.get(SurveyConstants.regionType(surveyType));
		
		if(type != null) {
			
			hh.setRegionType(Integer.parseInt(type));
			
		}
		
	}

}
