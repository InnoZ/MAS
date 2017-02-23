package com.innoz.toolbox.scenarioGeneration.population.surveys;

public abstract class SurveyObject {

	//just to hide the constructor
	SurveyObject(){};
	
	public static SurveyObject newInstance(Class<? extends SurveyObject> clazz) {
		
		try {
		
			return clazz.newInstance();
		
		} catch (InstantiationException | IllegalAccessException e) {

			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
}
