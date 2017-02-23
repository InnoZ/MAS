package com.innoz.toolbox.scenarioGeneration.population.surveys;

import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyObject;

public class SurveyStage extends SurveyObject {

	private String index;
	private String origin;
	private String destination;
	private String mode;
	private String travelTime;
	private String distance;
	private String startTime;
	private String endTime;
	private String purpose;
	
	SurveyStage() {
		super();
	}
	
	public String getIndex(){
		return index;
	}
	
	public void setIndex(String index){
		this.index = index;
	}
	
	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	public String getMode(){
		return mode;
	}
	
	public void setMode(String mode){
		this.mode = mode;
	}
	
	public String getTravelTime(){
		return travelTime;
	}
	
	public void setTravelTime(String travelTime){
		this.travelTime = travelTime;
	}
	
	public String getDistance(){
		return distance;
	}
	
	public void setDistance(String distance){
		this.distance = distance;
	}
	
	public String getStartTime(){
		return this.startTime;
	}
	
	public void setStartTime(String startTime){
		this.startTime = startTime;
	}
	
	public String getEndTime(){
		return this.endTime;
	}
	
	public void setEndTime(String endTime){
		this.endTime = endTime;
	}

	public String getPurpose(){
		return this.purpose;
	}
	
	public void setPurpose(String purpose){
		this.purpose = purpose;
	}
	
}