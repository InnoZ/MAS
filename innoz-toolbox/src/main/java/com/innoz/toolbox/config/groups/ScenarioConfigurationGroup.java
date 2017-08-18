package com.innoz.toolbox.config.groups;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class ScenarioConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "scenario";
	
	public static final String ACT_LOCATIONS_TYPE = "activityLocationsType";
	public static final String RANDOM_SEED = "randomSeed";
	public static final String SCALE_FACTOR = "scaleFactor";
	public static final String YEAR = "year";
	
	public static final String SURVEY_AREA_ID = "surveyAreaId";
	public static final String NETWORK_LEVEL = "networkLevel";
	public static final String POPULATION_SOURCE = "populationSource";
	
	private long randomSeed = 4711L;
	private double scaleFactor = 1d;
	private ActivityLocationsType actLocsType = ActivityLocationsType.BUILDINGS;
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	
	// The survey area
	private String surveyAreaId;
	private int networkLevel = 6;
	private PopulationSource populationSource;
	
	public enum ActivityLocationsType {
		BUILDINGS,
		FACILITIES,
		GRID,
		LANDUSE
	};
	
	public enum PopulationSource {
		COMMUTER,
		NONE,
		SURVEY,
		TRACKS
	};
	
	public ScenarioConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}
	
	@StringGetter(ACT_LOCATIONS_TYPE)
	public ActivityLocationsType getActivityLocationsType(){
		
		return this.actLocsType;
		
	}

	@StringSetter(ACT_LOCATIONS_TYPE)
	public void setActivityLocationsType(ActivityLocationsType type){
		
		this.actLocsType = type;
		
	}
	
	@StringGetter(RANDOM_SEED)
	public long getRandomSeed(){
		
		return this.randomSeed;
		
	}
	
	@StringSetter(RANDOM_SEED)
	public void setRandomSeed(long seed){
		
		this.randomSeed = seed;
		
	}
	
	@StringGetter(SCALE_FACTOR)
	public double getScaleFactor(){
		
		return this.scaleFactor;
		
	}
	
	@StringSetter(SCALE_FACTOR)
	public void setScaleFactor(double scaleFactor) {
	
		this.scaleFactor = scaleFactor;
		
	}
	
	@StringGetter(YEAR)
	public int getYear(){
		
		return this.year;
		
	}
	
	@StringSetter(YEAR)
	public void setYear(int year){
		
		this.year = year;
		
	}

	@StringGetter(SURVEY_AREA_ID)
	public String getSurveyAreaId() {
		
		return this.surveyAreaId;
		
	}
	
	@StringSetter(SURVEY_AREA_ID)
	public void setSurveyAreaId(String id) {
		
		this.surveyAreaId = id;
		
	}
	
	@StringGetter(NETWORK_LEVEL)
	public int getNetworkLevel() {
		
		return this.networkLevel;
		
	}
	
	@StringSetter(NETWORK_LEVEL)
	public void setNetworkLevel(int level) {
		
		this.networkLevel = level;
		
	}
	
	@StringGetter(POPULATION_SOURCE)
	public PopulationSource getPopulationSource() {
		
		return this.populationSource;
		
	}
	
	@StringSetter(POPULATION_SOURCE)
	public void setPopulationSource(PopulationSource source) {
		
		this.populationSource = source;
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(ACT_LOCATIONS_TYPE, "Possible values: BUILDINGS, FACILITIES, GRID, LANDUSE.");
		map.put(RANDOM_SEED, "The seed for the pseudo random number generator for the generation of the scenario.");
		map.put(SCALE_FACTOR, "The scale factor for the amount of households / persons to be created and for the supply"
				+ " side to scale capacities. Any numeric value between 0 and 1.");
		map.put(YEAR, "The year of the scenario.");
		
		return map;
		
	}

}