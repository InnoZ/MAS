package com.innoz.toolbox.scenarioGeneration.population.tracks;

import java.util.TreeMap;

public class TrackedPerson {

	private final String id;
	
	private TreeMap<String, Track> tracks = new TreeMap<>();
	
	public TrackedPerson(String id) {
		
		this.id = id;
		
	}
	
	public String getId(){
		
		return this.id;
		
	}
	
	public TreeMap<String, Track> getTracks(){
		
		return this.tracks;
		
	}

}