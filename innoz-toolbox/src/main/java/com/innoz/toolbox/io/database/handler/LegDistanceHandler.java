package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class LegDistanceHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyStage stage = (SurveyStage)obj;
		
		String distance = attributes.get(SurveyConstants.wayTravelDistance(surveyType));
		
		double d = Double.parseDouble(distance);
		
		if(d <= 950){
			
			d = d * 1000;
			stage.setDistance(Double.toString(d));
			
		} else {
			
			stage.setDistance(null);
			
		}

	}

}
