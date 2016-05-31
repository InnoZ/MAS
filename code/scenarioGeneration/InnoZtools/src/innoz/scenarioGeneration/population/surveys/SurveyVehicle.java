package innoz.scenarioGeneration.population.surveys;

import org.matsim.vehicles.EngineInformation.FuelType;

public class SurveyVehicle {

	private String id;
	private int kbaClass;
	private FuelType fuelType;
	
	public SurveyVehicle(String id){
		
		this.id = id;
		
	}
	
	public String getId(){
		return this.id;
	}
	
	public void setKbaClass(int clazz){
		this.kbaClass = clazz;
	}
	
	public int getKbaClass(){
		return this.kbaClass;
	}
	
	public void setFuelType(int t){
		
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
	
	public FuelType getFuelType(){
		return this.fuelType;
	}
	
}
