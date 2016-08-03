package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public interface DefaultHandler {

	void handle(SurveyObject obj, Map<String, String> attributes);
	
}