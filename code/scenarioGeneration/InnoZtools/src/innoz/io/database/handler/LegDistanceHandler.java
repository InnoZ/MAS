package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class LegDistanceHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyStage stage = (SurveyStage)obj;
		
		String distance = attributes.get("wegkm_k");
		
		double d = Double.parseDouble(distance);
		
		if(d <= 950){
			
			d = d * 1000;
			stage.setDistance(Double.toString(d));
			
		} else {
			
			stage.setDistance(null);
			
		}

	}

}
