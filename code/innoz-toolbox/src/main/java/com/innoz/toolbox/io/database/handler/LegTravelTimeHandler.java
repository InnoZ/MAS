package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class LegTravelTimeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyStage stage = (SurveyStage)obj;
		
		int time = calcSeconds(attributes, true);
		stage.setStartTime(Integer.toString(time));
		
		time = calcSeconds(attributes, false);
		stage.setEndTime(Integer.toString(time));

	}
	
	private int calcSeconds(Map<String, String> attributes, boolean mode) {
		
		String hKey = "en_std";
		String mKey = "en_min";
		String dKey = "en_dat";

		if(mode) {
			hKey = "st_std";
			mKey = "st_min";
			dKey = "st_dat";
		}

		String hour = attributes.get(hKey);
		String min = attributes.get(mKey);
		String nextDay = attributes.get(dKey);

		double time = Double.parseDouble(min) * 60 + Double.parseDouble(hour) * 60 * 60;

		if(nextDay != null && nextDay.equalsIgnoreCase("1")) {
			time += 86400;
		}

		return (int)time;
		
	}

}
