package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

public class SurveyHousehold implements SurveyObject {
	
	private String id;
	
	private double hhIncome;
	private Double weight;
	
	private final List<String> memberIds;
	private final List<String> vehicleIds;
	
	public SurveyHousehold(){
		
		this.memberIds = new ArrayList<String>();
		this.vehicleIds = new ArrayList<String>();

	}

	public String getId(){
		
		return this.id;
		
	}
	
	public void setId(String id){
		
		this.id = id;
		
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

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
}
