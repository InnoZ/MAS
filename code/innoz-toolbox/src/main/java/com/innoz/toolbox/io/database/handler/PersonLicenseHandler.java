package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonLicenseHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String license = attributes.get(SurveyConstants.personDrivingLicense(surveyType));
		
		if(license.equals("1")){
			person.setHasLicense(true);
		} else {
			person.setHasLicense(false);
		}
		
	}

}
