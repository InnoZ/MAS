package com.innoz.toolbox.scenarioGeneration.population.tracks;

import org.matsim.api.core.v01.Coord;

public class Track {

	private final String id;
	private String startedAt;
	private String finishedAt;
	private Coord start;
	private Coord end;
	private double length;
	private String mode;
	
	public Track(String id) {
		
		this.id = id;
		
	}

	public String getId(){
		
		return this.id;
		
	}
	
	public String getStartDateAndTime(){
		
		return this.startedAt;
		
	}
	
	public void setStartDateAndTime(String s){
		
		this.startedAt = s;
		
	}
	
	public String getEndDateAndTime(){
		
		return this.finishedAt;
		
	}
	
	public void setEndDateAndTime(String s){
		
		this.finishedAt = s;
		
	}
	
	public Coord getStart(){
		
		return this.start;		
	
	}
	
	public void setStart(Coord c){
	
		this.start = c;
		
	}
	
	public Coord getEnd(){
		
		return this.end;
		
	}
	
	public void setEnd(Coord c){
		
		this.end = c;
		
	}
	
	public double getLength(){
		
		return this.length;
		
	}
	
	public void setLength(double d){
		
		this.length = d;
		
	}
	
	public String getMode(){
		
		return this.mode;
		
	}
	
	public void setMode(String m){
		
		this.mode = m;
		
	}
	
}