package innoz.io.database.task;

import java.util.HashSet;
import java.util.Set;

import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;

public class PersonRemovalTask implements Task {

	public void apply(SurveyDataContainer container){
		
		Set<String> toRemove = new HashSet<>();
		
		for(SurveyPerson person : container.getPersons().values()){
			
			if(person.getLogbook().isEmpty()){
				
				toRemove.add(person.getId());
				
			}
			
		}
		
		for(String id : toRemove){
			container.removePerson(id);
		}
		
	}
	
}
