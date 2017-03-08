package com.innoz.toolbox.scenarioGeneration.population.surveys;

import org.matsim.vehicles.EngineInformation.FuelType;

public class SurveyVehicle extends SurveyObject {

	private int kbaClass;
	private FuelType fuelType;
	
	SurveyVehicle() {
		
	}

	public void setKbaClass(int clazz) {
		
		this.kbaClass = clazz;
		
	}
	
	public int getKbaClass() {
		
		return this.kbaClass;
		
	}
	
	public void setFuelType(int t) {
		
		switch(t){
		
			case 1: this.fuelType = FuelType.gasoline;
					break;
			case 2: 
			case 3: this.fuelType = FuelType.diesel;
					break;
			case 4:
			case 5: this.fuelType = FuelType.electricity;
					break;
		
		}
	
	}
	
	public FuelType getFuelType() {
		
		return this.fuelType;
		
	}

	public void setFuelType(String string) {

		this.setFuelType(Integer.parseInt(string));
		
	}
	
}