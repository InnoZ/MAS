package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import org.matsim.api.core.v01.TransportMode;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyStage;

public class LegModeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyStage stage = (SurveyStage)obj;
		
		String mode = attributes.get(SurveyConstants.tripMode(surveyType));
		if(mode.equals("1")){
			stage.setMode(TransportMode.walk);
		} else if(mode.equals("2")){
			stage.setMode(TransportMode.bike);
		} else if(mode.equals("3")){
			stage.setMode(TransportMode.ride);
		} else if(mode.equals("4")){
			stage.setMode(TransportMode.car);
		} else if(mode.equals("5")){
			stage.setMode(TransportMode.pt);
		} else {
			stage.setMode(TransportMode.other);
		}

	}

}
