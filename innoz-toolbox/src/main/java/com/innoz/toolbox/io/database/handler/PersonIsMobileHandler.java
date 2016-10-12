package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonIsMobileHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyPerson person = (SurveyPerson)obj;
		
		String isMobile = attributes.get(SurveyConstants.mobile(surveyType));
		
		if(isMobile.equals("1")){
			person.setMobile(true);
		} else {
			person.setMobile(false);
		}

	}

}
