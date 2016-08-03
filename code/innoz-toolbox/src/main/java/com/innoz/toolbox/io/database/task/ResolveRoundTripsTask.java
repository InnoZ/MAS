package com.innoz.toolbox.io.database.task;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.handler.SurveyStage;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class ResolveRoundTripsTask implements SurveyDataTask {
	
	@Override
	public void run(SurveyDataContainer container){
		
		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
				
				for(SurveyStage stage : logbook.getStages()){
					
					if(stage.getDestination() != null){

						if(stage.getDestination().equals("5")){
							
							int start = Integer.parseInt(stage.getStartTime());
							int end = Integer.parseInt(stage.getEndTime());
							int duration = end - start;
							double distance = Double.parseDouble(stage.getDistance());
							
							SurveyStage st2 = new SurveyStage();
							st2.setDestination("HOME");
							st2.setPurpose(ActivityTypes.HOME);
							st2.setDistance(Double.toString(distance / 2));
							st2.setEndTime(Integer.toString(end));
							st2.setMode(stage.getMode());
							st2.setStartTime(Integer.toString(start + duration / 2));
							
							logbook.getStages().add(logbook.getStages().indexOf(stage)+1, st2);
							
							stage.setDistance(Double.toString(distance / 2));
							stage.setEndTime(Integer.toString(start + duration / 2 - 1));
							stage.setPurpose(ActivityTypes.OTHER);
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}

}
