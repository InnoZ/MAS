package com.innoz.toolbox.io.database.task;

import java.util.HashSet;
import java.util.Set;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonRemovalTask implements DataContainerTask {

	@Override
	public void apply(SurveyDataContainer container){
		
		Set<String> toRemove = new HashSet<>();
		
		for(SurveyPerson person : container.getPersons().values()){
			
			if(person.getLogbook().isEmpty() && person.isMobile()){
				
				toRemove.add(person.getId());
				
			}
			
		}
		
		for(String id : toRemove){
			container.removePerson(id);
		}
		
	}
	
}