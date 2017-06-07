package com.innoz.toolbox.io.database.validation;

import com.innoz.toolbox.io.database.handler.Logbook;

public class ValidateNegativeTravelTimes implements Validator {

	@Override
	public boolean validate(Logbook logbook){
		
		logbook.getStages().stream().filter(stage -> stage.getStartTime() != null).filter(stage -> stage.getEndTime() != null).forEach(stage ->{
			
			double startTime = Double.parseDouble(stage.getStartTime());
			double endTime = Double.parseDouble(stage.getEndTime());
			
			if(startTime > endTime) {
				
				logbook.setDelete(true);
				return;
				
			}
			
		});
		
		return !logbook.isDelete();
		
	}
	
}