package playground.dhosse.scenarios.generic.population.io.mid;

import java.util.ArrayList;
import java.util.List;

public class SurveyHousehold {
	
	private String id;
	
	private double nCars;
	private double hhIncome;
	private double weight;
	
	private final List<String> memberIds;
	
	public SurveyHousehold(String id){
		
		this.id = id;
		this.memberIds = new ArrayList<>();

	}

	public String getId(){
		
		return this.id;
		
	}
	
	public int getNPersons(){
		
		return this.memberIds.size();
		
	}
	
	public double getNCars(){
		
		return this.nCars;
		
	}
	
	public void setNCars(double n){
		
		this.nCars = n;
		
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

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
}
