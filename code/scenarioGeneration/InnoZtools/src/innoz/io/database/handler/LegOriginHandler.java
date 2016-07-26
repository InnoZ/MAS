package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class LegOriginHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyStage stage = (SurveyStage)obj;
		
		String origin = attributes.get("w01");
		
		if(origin.equals("1")){
			stage.setOrigin("HOME");
		} else if(origin.equals("2")){
			stage.setOrigin("WORK");
		} else if(origin.equals("3")){
			stage.setOrigin("IN_TOWN");
		} else if(origin.equals("4")){
			stage.setOrigin("OUT_OF_TOWN");
		}

	}

}
