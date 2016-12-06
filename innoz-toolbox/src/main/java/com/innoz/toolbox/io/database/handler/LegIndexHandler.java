package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class LegIndexHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyStage stage = (SurveyStage)obj;
		
		String index = attributes.get(SurveyConstants.sortedTripId(surveyType));
		stage.setIndex(index);

	}

}
