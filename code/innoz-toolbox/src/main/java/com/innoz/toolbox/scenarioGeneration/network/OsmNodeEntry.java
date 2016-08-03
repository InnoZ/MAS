package com.innoz.toolbox.scenarioGeneration.network;

import org.matsim.api.core.v01.Coord;

public class OsmNodeEntry {

	private String id;
	private Coord coord;
	private int ways;
	private boolean used;
	
	public OsmNodeEntry(String id, double x, double y) {
		
		this.id = id;
		this.coord = new Coord(x,y);
		
	}
	
	public OsmNodeEntry(String id, Coord coord) {
		
		this.id = id;
		this.coord = coord;
		
	}
	
	public String getId(){
	
		return this.id;
		
	}
	
	public Coord getCoord(){
		
		return this.coord;
		
	}
	
	public int getWays(){
		
		return this.ways;
		
	}
	
	public void incrementWays(){
		
		this.ways++;
		
	}
	
	public void setUsed(boolean b){
		
		this.used = b;
		
	}
	
	public boolean isUsed(){
		
		return this.used;
		
	}
	
}