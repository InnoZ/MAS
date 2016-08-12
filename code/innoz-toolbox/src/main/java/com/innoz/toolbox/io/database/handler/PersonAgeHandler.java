package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonAgeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyPerson person = (SurveyPerson)obj;
		
		String age = attributes.get(SurveyConstants.personAge(surveyType));
		
		if(age != null && !age.equals("NaN")){
			
			Integer a = Integer.parseInt(age);
			
			if(a > 0 && a < 102){
				
				person.setAge(a);
				
			}
			
		}

	}

}
