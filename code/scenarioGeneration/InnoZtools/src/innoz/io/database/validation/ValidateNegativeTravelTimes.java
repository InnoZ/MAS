package innoz.io.database.validation;

import innoz.io.database.handler.Logbook;
import innoz.io.database.handler.SurveyStage;

public class ValidateNegativeTravelTimes implements Validator {

	@Override
	public boolean validate(Logbook logbook){
		
		for(SurveyStage stage : logbook.getStages()){

			String start = stage.getStartTime();
			String end = stage.getEndTime();
			
			if(start != null && end != null){

				double startTime = Double.parseDouble(stage.getStartTime());
				double endTime = Double.parseDouble(stage.getEndTime());
				
				if(startTime > endTime){
					
					logbook.setDelete(true);
					return false;
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
}
