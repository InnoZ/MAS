package com.innoz.toolbox.io.database.task;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public class PersonRemovalTask implements DataContainerTask {

	@Override
	public void apply(SurveyDataContainer container){

		container.getPersons().values().removeIf(person -> person.isMobile() && person.getLogbook().isEmpty());
		
	}
	
}
