package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class LegDestinationHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes){
		
		SurveyStage stage = (SurveyStage)obj;
		
		String value = attributes.get("w13");
		
		if(value.equals("1")) stage.setDestination("HOME");
		if(value.equals("2")) stage.setDestination("WORK");
		if(value.equals("3")) stage.setDestination("IN_TOWN");
		if(value.equals("4")) stage.setDestination("OUT_OF_TOWN");
		if(value.equals("5")) stage.setDestination("ROUND_TRIP");
		
	}
	
}