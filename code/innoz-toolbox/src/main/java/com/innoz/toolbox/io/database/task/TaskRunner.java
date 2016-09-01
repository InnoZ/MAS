package com.innoz.toolbox.io.database.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.validation.Validator;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;

public class TaskRunner {

	public static void exec(Task task, SurveyDataContainer container){

		if(task instanceof DataContainerTask){
			
			((DataContainerTask)task).apply(container);
			
		}
		
	}
	
	public static void exec(Validator validator, Collection<? extends SurveyPerson> collection){
		
		Set<Logbook> toRemove;
		
		Set<SurveyPerson> removePersons = new HashSet<>();
		
		int nEmpty = 0;
		
		for(SurveyPerson person : collection){
			
			int size = new Integer(person.getLogbook().size()).intValue();
			
			toRemove = new HashSet<>();
			
			if(person.getLogbook().values().isEmpty()) nEmpty++;
			
			for(Logbook logbook : person.getLogbook().values()){
				
				boolean b = validator.validate(logbook);
				if(!b){
					toRemove.add(logbook);
				}
				
			}
			
			for(Logbook logbook : toRemove){
				
				person.getLogbook().values().remove(logbook);
				
			}
			
			if(person.getLogbook().size() < size){
				
				removePersons.add(person);
				
			}
			
		}
		
		System.out.println(nEmpty);
		
		for(SurveyPerson person : removePersons){
			
			collection.remove(person);
			
		}
		
	}
	
}
