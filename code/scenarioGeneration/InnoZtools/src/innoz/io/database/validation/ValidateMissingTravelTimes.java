package innoz.io.database.validation;

import innoz.io.database.handler.Logbook;
import innoz.io.database.handler.SurveyStage;

public class ValidateMissingTravelTimes implements Validator {

	@Override
	public void validate(Logbook logbook) {

		for(SurveyStage stage : logbook.getStages()){

			String start = stage.getStartTime();
			String end = stage.getEndTime();
			
			if(start == null || end == null){
				logbook.setDelete(true);
				return;
			}
			
		}
		
	}

}
