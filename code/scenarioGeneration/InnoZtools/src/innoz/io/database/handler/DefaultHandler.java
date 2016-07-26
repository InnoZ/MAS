package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;

public interface DefaultHandler {

	void handle(SurveyObject obj, Map<String, String> attributes);
	
}