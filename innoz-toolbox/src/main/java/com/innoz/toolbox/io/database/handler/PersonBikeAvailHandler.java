package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonBikeAvailHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes,
	        String surveyType) {
		
		SurveyPerson person = (SurveyPerson) obj;
		
		String bikeAvail = attributes.get(SurveyConstants.bikeAvail(surveyType));
		
		boolean hasBike = false;
		
		if(!bikeAvail.equals("NaN")) {
			
			int bike = Integer.parseInt(bikeAvail);
			
			if(bike < 2) hasBike = true;
			
		}
		
		person.setBikeAvailable(hasBike);

	}

}
