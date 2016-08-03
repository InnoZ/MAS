package com.innoz.toolbox.io.database.validation;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.handler.SurveyStage;

public class ValidateMissingTravelTimes implements Validator {

	@Override
	public boolean validate(Logbook logbook) {

		for(SurveyStage stage : logbook.getStages()){

			String start = stage.getStartTime();
			String end = stage.getEndTime();
			
			if(start == null || end == null){
				logbook.setDelete(true);
				return false;
			}
			
		}
		
		return true;
		
	}

}
