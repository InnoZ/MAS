package com.innoz.toolbox.scenarioGeneration.population.tracks;

import java.util.Date;

import org.matsim.api.core.v01.Coord;

public class Track {

	private final String id;
	private Date startDate;
	private Date endDate;
	private double startTime;
	private double endTime;
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
	
	public Date getStartDate(){
		
		return this.startDate;
		
	}
	
	public void setStartDate(Date date){
		
		this.startDate = date;
		
	}
	
	public Date getEndDate(){
		
		return this.endDate;
		
	}
	
	public void setEndDate(Date date){
		
		this.endDate = date;
		
	}
	
	public double getStartTime(){
		
		return this.startTime;
		
	}
	
	public void setStartTime(double t){
		
		this.startTime = t;
		
	}
	
	public double getEndTime(){
		
		return this.endTime;
		
	}
	
	public void setEndTime(double t){
		
		this.endTime = t;
		
	}
	
}