package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonSexHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String sex = attributes.get(SurveyConstants.personSex(surveyType));
		
		if(sex.equals("1")){
			person.setSex("m");
		} else {
			person.setSex("f");
		}
		
	}

}
