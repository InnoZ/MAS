package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonLifephaseHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String lifephase = attributes.get(SurveyConstants.personLifephase(surveyType));
		
		if(lifephase != null){
			
			person.setLifePhase(Integer.parseInt(lifephase));
			
		}
		
	}

}
