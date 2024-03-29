package com.innoz.toolbox.io.database.task;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyStage;

public class SortStagesTask implements DataContainerTask {

	private Comparator<String> intComparator = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			
			int i1 = Integer.parseInt(o1);
			int i2 = Integer.parseInt(o2);
			
			return i1 - i2;
			
		}
	};
	
	@Override
	public void apply(SurveyDataContainer container) {

		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
				
				SortedMap<String, SurveyStage> stages = new TreeMap<>(intComparator);
				
				for(SurveyStage stage : logbook.getStages()){
					stages.put(stage.getIndex(), stage);
				}
				
				for(SurveyStage stage : stages.values()){
					logbook.getStages().remove(stage);
				}
				
				for(SurveyStage stage : stages.values()){
					logbook.getStages().add(stage);
				}
				
			}
			
		}
		
	}

}
