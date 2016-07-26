package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class LegIndexHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyStage stage = (SurveyStage)obj;
		
		String index = attributes.get("wid");
		stage.setIndex(index);

	}

}
