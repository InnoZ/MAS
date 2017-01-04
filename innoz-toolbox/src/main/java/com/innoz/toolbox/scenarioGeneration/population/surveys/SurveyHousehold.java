package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

import com.innoz.toolbox.scenarioGeneration.utils.Weighted;

public class SurveyHousehold implements SurveyObject, Weighted {
	
	private String id;
	
	private double hhIncome;
	private Double weight;
	
	int rtyp;
	
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
	
	public int getRegionType() {
		return this.rtyp;
	}
	
	public void setRegionType(int t) {
		this.rtyp = t;
	}

	@Override
	public double getWeight() {
		return weight;
	}

	@Override
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
}
