package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonCarAvailabilityHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyPerson person = (SurveyPerson)obj;
		
		String carAvail = attributes.get(SurveyConstants.personCarAvailability(surveyType));
		
		if(carAvail.equals("1") || carAvail.equals("2")){
			person.setCarAvailable(true);
		} else {
			person.setCarAvailable(false);
		}

	}

}
