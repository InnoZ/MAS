package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

public class SurveyHousehold extends SurveyObject implements Comparable<Double> {
	
	private double hhIncome;
	private Double weight;
	
	private final List<String> memberIds;
	private final List<String> vehicleIds;
	
	SurveyHousehold() {
		
		this.memberIds = new ArrayList<String>();
		this.vehicleIds = new ArrayList<String>();

	}

	public int getNPersons() {
		
		return this.memberIds.size();
		
	}
	
	public double getNCars() {
		
		return this.vehicleIds.size();
		
	}
	
	public double getIncome() {
		
		return this.hhIncome;
		
	}
	
	public void setIncome(double income) {
		
		this.hhIncome = income;
		
	}

	public List<String> getMemberIds() {
		
		return this.memberIds;
		
	}
	
	public List<String> getVehicleIds() {
		
		return this.vehicleIds;
		
	}

	public Double getWeight() {
		
		return weight;
		
	}

	public void setWeight(Double weight) {
		
		this.weight = weight;
	
	}

	@Override
	public String toString() {
	
		return "[hhid='" + this.id + "'],[weight='" + this.weight + "'],[income='" + this.hhIncome + "']";
		
	}

	@Override
	public int compareTo(Double w) {

		return Double.compare(this.weight, w);
		
	}
	
}