package innoz.io.database.task;

import java.util.HashSet;
import java.util.Set;

import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;

public class HouseholdRemovalTask implements Task {

	public void apply(SurveyDataContainer container){
		
		Set<String> hhToRemove = new HashSet<>();
		
		for(SurveyHousehold hh : container.getHouseholds().values()){
			if(hh.getMemberIds().isEmpty()){
				hhToRemove.add(hh.getId());
				continue;
			}
			
			Set<String> idsToRemove = new HashSet<>();
			
			for(String id : hh.getMemberIds()){
				if(!container.getPersons().containsKey(id)){
					idsToRemove.add(id);
				}
			}
			
			hh.getMemberIds().removeAll(idsToRemove);
			
			if(hh.getMemberIds().size() <= 0){
				hhToRemove.add(hh.getId());
			}
			
			if(hh.getWeight() == null)
				hhToRemove.add(hh.getId());
			
		}
		
		for(String id : hhToRemove){
			
			container.removeHousehold(id);
			
		}
		
	}
	
}