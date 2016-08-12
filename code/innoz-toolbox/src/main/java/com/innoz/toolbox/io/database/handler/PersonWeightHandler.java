package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonWeightHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyPerson person = (SurveyPerson)obj;
		
		double w = Double.parseDouble(attributes.get(SurveyConstants.personWeight(surveyType)));
		
		person.setWeight(w);

	}

}
