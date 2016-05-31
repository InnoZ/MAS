package innoz.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

public class SurveyHousehold {
	
	private String id;
	
	private double nCars;
	private double hhIncome;
	private double weight;
	
	private final List<String> memberIds;
	private final List<String> vehicleIds;
	
	public SurveyHousehold(String id){
		
		this.id = id;
		this.memberIds = new ArrayList<String>();
		this.vehicleIds = new ArrayList<String>();

	}

	public String getId(){
		
		return this.id;
		
	}
	
	public int getNPersons(){
		
		return this.memberIds.size();
		
	}
	
	public double getNCars(){
		
		return this.vehicleIds.size();
		
	}
	
	public double getIncome(){
		
		return this.hhIncome;
		
	}
	
	public void setIncome(double income){
		
		this.hhIncome = income;
		
	}

	public List<String> getMemberIds() {
		
		return this.memberIds;
		
	}
	
	public List<String> getVehicleIds(){
		return this.vehicleIds;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
}
