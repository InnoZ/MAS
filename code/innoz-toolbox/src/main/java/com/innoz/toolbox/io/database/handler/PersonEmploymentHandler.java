package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonEmploymentHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {
		
		SurveyPerson person = (SurveyPerson) obj;
		
		String emp = attributes.get("hp_beruf");
		
		if(emp != null){
			
			if(emp.equals("1")){
				
				person.setEmployed(true);
				
			} else {
				
				person.setEmployed(false);
				
			}
			
		}
		
	}
	
}
