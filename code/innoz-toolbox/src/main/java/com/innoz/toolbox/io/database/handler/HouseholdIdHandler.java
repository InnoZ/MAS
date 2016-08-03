package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdIdHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String hhId = attributes.get("hhid");
		
		hh.setId(hhId);
		

	}

}
