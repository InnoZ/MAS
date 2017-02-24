package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

import com.innoz.toolbox.scenarioGeneration.utils.Weighted;

public class SurveyHousehold extends SurveyObject implements Weighted, Comparable<Double> {
	
	private double hhIncome;
	private Double weight;
	
	int rtyp;
	
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
	
	public int getRegionType() {
		return this.rtyp;
	}
	
	public void setRegionType(int t) {
		this.rtyp = t;
	}
	
	@Override
	public double getWeight() {
		
		return this.weight;
		
	}

	@Override
	public void setWeight(double w) {

		this.weight = w;
		
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