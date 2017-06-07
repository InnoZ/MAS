package com.innoz.toolbox.io.database.task;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public class HouseholdRemovalTask implements DataContainerTask {

	@Override
	public void apply(SurveyDataContainer container){

		container.getHouseholds().values().forEach(hh -> {
			hh.getMemberIds().removeIf(id -> !container.getPersons().containsKey(id));
		});
		
		container.getHouseholds().values().removeIf(hh -> hh.getMemberIds().isEmpty() || hh.getWeight() < 0);
		
		
	}
	
}