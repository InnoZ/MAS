package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

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
		
		String hKey = SurveyConstants.wayArrivalHour(surveyType);
		String mKey = SurveyConstants.wayArrivalMinute(surveyType);
		String dKey = SurveyConstants.wayArrivalDay(surveyType);

		if(mode) {
			hKey = SurveyConstants.wayDepartureHour(surveyType);
			mKey = SurveyConstants.wayDepartureMinute(surveyType);
			dKey = SurveyConstants.wayDepartureDay(surveyType);
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
