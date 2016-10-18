package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

public class ScenarioConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "scenario";
	
	public static final String ACT_LOCATIONS_TYPE = "activityLocationsType";
	public static final String RANDOM_SEED = "randomSeed";
	public static final String SCALE_FACTOR = "scaleFactor";
	
	private long randomSeed = 4711L;
	private double scaleFactor = 1d;
	private ActivityLocationsType actLocsType = ActivityLocationsType.buildings;
	
	public enum ActivityLocationsType{
		buildings,
		facilities
	};
	
	ScenarioConfigurationGroup() {
		
		super(GROUP_NAME);
	}
	
	public ActivityLocationsType getActivityLocationsType(){
		
		return this.actLocsType;
		
	}
	
	public void setActivityLocationsType(ActivityLocationsType type){
		
		this.actLocsType = type;
		
	}
	
	public long getRandomSeed(){
		
		return this.randomSeed;
		
	}
	
	public void setRandomSeed(long seed){
		
		this.randomSeed = seed;
		
	}
	
	public double getScaleFactor(){
		
		return this.scaleFactor;
		
	}
	
	public void setScaleFactor(double scaleFactor) {
	
		this.scaleFactor = scaleFactor;
		
	}

	@Override
	Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(ACT_LOCATIONS_TYPE, "Possible values: buildings, facilities.");
		map.put(RANDOM_SEED, "The seed for the pseudo random number generator for the generation of the scenario.");
		map.put(SCALE_FACTOR, "The scale factor for the amount of households / persons to be created and for the supply"
				+ " side to scale capacities. Any numeric value between 0 and 1.");
		
		return map;
		
	}

}