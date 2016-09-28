package com.innoz.toolbox.io.database.validation;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.handler.SurveyStage;

public class ValidateMissingTravelTimes implements Validator {

	@Override
	public boolean validate(Logbook logbook) {

		for(SurveyStage stage : logbook.getStages()){

			String start = stage.getStartTime();
			String end = stage.getEndTime();
			
			if(start.equals("NaN") || end.equals("NaN") ||
					start == null || end == null){
				logbook.setDelete(true);
				return false;
			}
			
			//TODO this seems a little messy. Better create a separate validator for distances.
			String distance = stage.getDistance();
			if(distance == null){
				return false;
			}
			
		}
		
		return true;
		
	}

}
