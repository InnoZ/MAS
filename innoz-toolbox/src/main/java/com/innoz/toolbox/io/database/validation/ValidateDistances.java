package com.innoz.toolbox.io.database.validation;

import org.matsim.api.core.v01.TransportMode;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyStage;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class ValidateDistances implements Validator{

	@Override
	public boolean validate(Logbook logbook) {
		
		for(SurveyStage stage : logbook.getStages()){
			
//			TODO: write distance boundaries in specified class. sSomething like ValidationConstants.java
			if(stage.getMode().equals(TransportMode.walk) &&
					Double.parseDouble(stage.getDistance()) > 5000){
				logbook.setDelete(true);	
				return false;
			}
			if(stage.getMode().equals(TransportMode.transit_walk) &&
					Double.parseDouble(stage.getDistance()) > 5000){
				logbook.setDelete(true);
				return false;
			}
			if(stage.getMode().equals(TransportMode.bike) &&
					Double.parseDouble(stage.getDistance()) > 25000 &&
					!stage.getPurpose().startsWith(ActivityTypes.LEISURE)){
				logbook.setDelete(true);
				return false;
			}
			
		}
		
		return true;
	}

}
