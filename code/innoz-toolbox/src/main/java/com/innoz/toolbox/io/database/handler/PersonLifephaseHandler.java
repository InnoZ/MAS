package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonLifephaseHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyPerson person = (SurveyPerson)obj;
		
		String lifephase = attributes.get("lebensph");
		
		if(lifephase != null){
			
			person.setLifePhase(Integer.parseInt(lifephase));
			
		}
		
	}

}
