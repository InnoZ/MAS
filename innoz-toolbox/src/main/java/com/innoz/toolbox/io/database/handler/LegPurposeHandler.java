package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class LegPurposeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {
		
		SurveyStage stage = (SurveyStage)obj;
		
		String actType = null;
		String purpose = attributes.get(SurveyConstants.wayPurpose(surveyType));
		String subtype = attributes.get(SurveyConstants.wayDetailedPurpose(surveyType));
		
		if(purpose.equals("1")){
			
			actType = ActivityTypes.WORK;
			
		} else if(purpose.equals("3")){
			
			actType = ActivityTypes.EDUCATION;
			
		} else if(purpose.equals("4")){
			
			actType = ActivityTypes.SHOPPING;
			
			if(subtype != null){

				if(subtype.equals("501")){
					
					actType = ActivityTypes.SUPPLY;
					
				} else if(subtype.equals("504")){
					
					actType = ActivityTypes.SERVICE;
					
				}
				
			}
			
		} else if(purpose.equals("5")){
			
			actType = ActivityTypes.ERRAND;
			
		} else if(purpose.equals("7")){
			
			actType = ActivityTypes.LEISURE;
			
			if(subtype != null){
				
				if(subtype.equals("702")){
					
					actType = ActivityTypes.CULTURE;
					
				} else if(subtype.equals("703")){
					
					actType = ActivityTypes.EVENT;
					
				} else if(subtype.equals("704")){
					
					actType = ActivityTypes.SPORTS;
					
				} else if(subtype.equals("705")){
					
					actType = ActivityTypes.FURTHER;
					
				} else if(subtype.equals("706")){
					
					actType = ActivityTypes.EATING;
					
				} else if(subtype.equals("707")){
					
					actType = ActivityTypes.ALLOTMENT;
					
				}
				
			}
			
		} else if(purpose.equals("8")){
			
			actType = ActivityTypes.HOME;
			
		} else if(purpose.equals("9")){
			
			actType = "return";
			
		} else if(purpose.equals("31")){
			
			actType = ActivityTypes.EDUCATION;
			
		} else if(purpose.equals("32")){
			
			actType = ActivityTypes.KINDERGARTEN;
			
		} else {
			
			actType = ActivityTypes.OTHER;
			
			if(subtype.equals("601")){
				
				actType = ActivityTypes.HEALTH;
				
			}
			
		}
		
		stage.setPurpose(actType);

	}

}
