package innoz.io.database.validation;

import innoz.io.database.handler.Logbook;

public class ValidateOverlappingStages implements Validator {

	@Override
	public boolean validate(Logbook logbook){
		
		for(int i = 1; i < logbook.getStages().size(); i++){

			String start = logbook.getStages().get(i).getStartTime();
			String end = logbook.getStages().get(i - 1).getEndTime();
			
			if(start != null && end != null){

				double startTime = Double.parseDouble(start);
				double endTime = Double.parseDouble(end);
				
				if(startTime < endTime){
					
					logbook.setDelete(true);
					return false;
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
}
