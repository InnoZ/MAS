package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyStage;

public class LegTravelTimeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyStage stage = (SurveyStage)obj;
		
		double time = calcSeconds(attributes, true, surveyType);
		stage.setStartTime(Double.toString(time));
		
		time = calcSeconds(attributes, false, surveyType);
		stage.setEndTime(Double.toString(time));

	}
	
	private double calcSeconds(Map<String, String> attributes, boolean mode, String surveyType) {
		
		String hKey = SurveyConstants.tripArrivalHour(surveyType);
		String mKey = SurveyConstants.tripArrivalMinute(surveyType);
		String dKey = SurveyConstants.tripArrivalDay(surveyType);

		if(mode) {
			hKey = SurveyConstants.tripDepartureHour(surveyType);
			mKey = SurveyConstants.tripDepartureMinute(surveyType);
			dKey = SurveyConstants.tripDepartureDay(surveyType);
		}

		String hour = attributes.get(hKey);
		String min = attributes.get(mKey);
		String nextDay = attributes.get(dKey);

		double time = Double.parseDouble(min) * 60 + Double.parseDouble(hour) * 60 * 60;

		if(nextDay != null && nextDay.equalsIgnoreCase("1")) {
			time += 86400;
		}

		return time;
		
	}

}
