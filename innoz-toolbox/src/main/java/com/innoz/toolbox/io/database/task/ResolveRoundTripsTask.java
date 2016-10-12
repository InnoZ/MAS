package com.innoz.toolbox.io.database.task;

import java.util.ArrayList;
import java.util.List;

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

				String prevAct = null;
				List<SurveyStage> stages = new ArrayList<>();
				
				for(SurveyStage stage : logbook.getStages()){
					
					if(prevAct == null){
						if(stage.getOrigin() == null){
							prevAct = ActivityTypes.HOME;
						} else {
							prevAct = stage.getOrigin().toLowerCase();
						}
					}
					
					if(stage.getDestination() != null){

						if(stage.getDestination().equals("ROUND_TRIP")){
							
							double start = Double.parseDouble(stage.getStartTime());
							double end = Double.parseDouble(stage.getEndTime());
							int duration = (int)(end - start);
							double distance = Double.parseDouble(stage.getDistance());
							
							SurveyStage st2 = new SurveyStage();
							st2.setDestination("HOME");
							st2.setPurpose(stage.getPurpose());
							st2.setDistance(Double.toString(distance / 2));
							st2.setEndTime(Double.toString(start + duration / 2 - 1));
							st2.setMode(stage.getMode());
							st2.setStartTime(Double.toString(start));
							
							stages.add(st2);
							
							stage.setDistance(Double.toString(distance / 2));
							stage.setStartTime(Double.toString(start + duration / 2));
							stage.setEndTime(Double.toString(end));
							stage.setPurpose(prevAct);
							
						}
						
					}
					
					prevAct = stage.getPurpose();
					stages.add(stage);
					
				}
				
				logbook.getStages().clear();
				logbook.getStages().addAll(stages);
				
			}
			
		}
		
	}

}
