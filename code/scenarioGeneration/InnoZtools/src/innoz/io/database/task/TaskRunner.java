package innoz.io.database.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import innoz.io.database.handler.Logbook;
import innoz.io.database.validation.Validator;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class TaskRunner {

	public static void exec(Validator validator, Collection<? extends SurveyPerson> collection){
		
		Set<Logbook> toRemove;
		
		for(SurveyPerson person : collection){
			
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
				
				boolean b = validator.validate(logbook);
				if(!b){
					toRemove.add(logbook);
				}
				
			}
			
			for(Logbook logbook : toRemove){
				
				person.getLogbook().values().remove(logbook);
				
			}
			
		}
		
	}
	
}
