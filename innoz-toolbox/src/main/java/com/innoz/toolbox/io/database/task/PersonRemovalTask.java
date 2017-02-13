package com.innoz.toolbox.io.database.task;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonRemovalTask implements DataContainerTask {

	@Override
	public void apply(SurveyDataContainer container){
		
		Logger.getLogger(org.matsim.matrices.Matrix.class).setLevel(Level.OFF);
		
		Set<String> toRemove = new HashSet<>();
		
		int personsToBeRemoved = 0;
		
		for(SurveyPerson person : container.getPersons().values()){
			
			if(person.getLogbook().isEmpty() && person.isMobile()){
				
				toRemove.add(person.getId());
				
				personsToBeRemoved++;
				
			}
			
		}
		
		for(String id : toRemove){
			container.removePerson(id);
		}
		
		Log.info(personsToBeRemoved + " mobile persons without logbook. " + toRemove.size() + " persons removed.");
		
	}
	
}
