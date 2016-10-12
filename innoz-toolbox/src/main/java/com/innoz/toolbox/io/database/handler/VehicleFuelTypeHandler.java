package com.innoz.toolbox.io.database.handler;

import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyVehicle;

public class VehicleFuelTypeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes, String surveyType) {

		String fuelType = attributes.get(SurveyConstants.vehicleFuelType(surveyType));
		
		if(fuelType != null){
			
			((SurveyVehicle)obj).setFuelType(fuelType);
			
		}
		
	}

}
