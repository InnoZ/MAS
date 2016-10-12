package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * @author dhosse
 *
 */
public class SurveyPlanTrip implements SurveyPlanElement {

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private int id;
	
	private double weight;
	private double startTime;
	private double endTime;
	private double travelTime;
	private double travelDistance;
	
	private boolean roundTrip;
	
	private String personId;
	private String mainMode;
	
	private Set<String> modes;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Creates a new trip from data that has been reported in a traffic survey.
	 * 
	 * @param id The identifier for this trip (normally the current number of the trip in the list of reported trips).
	 */
	public SurveyPlanTrip(int id){
		
		this.id = id;
		this.modes = new HashSet<>();
		
	}
	
	@Override
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	public String getPersonId() {
		return this.personId;
	}
	
	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getMainMode() {
		return mainMode;
	}

	public void setMainMode(String mainMode) {
		this.mainMode = mainMode;
	}

	public Set<String> getModes() {
		return modes;
	}

	public void setModes(Set<String> modes) {
		this.modes = modes;
	}

	public double getTravelTime() {
		return this.travelTime;
	}

	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}

	public double getTravelDistance() {
		return this.travelDistance;
	}

	public void setTravelDistance(double travelDistance) {
		this.travelDistance = travelDistance;
	}

	public boolean isRoundTrip() {
		return this.roundTrip;
	}

	public void setRoundTrip(boolean roundTrip) {
		this.roundTrip = roundTrip;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	
}
