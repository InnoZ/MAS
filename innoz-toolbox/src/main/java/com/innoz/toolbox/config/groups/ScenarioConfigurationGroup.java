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
	
	private long randomSeed = 4711L;
	private double scaleFactor = 1d;
	private ActivityLocationsType actLocsType = ActivityLocationsType.BUILDINGS;
	private int year = Calendar.getInstance().get(Calendar.YEAR);
	
	public enum ActivityLocationsType{
		BUILDINGS,
		FACILITIES,
		LANDUSE
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
	
	public void addAreaSet(AreaSet set){
		
		if(!this.parameterSets.containsKey(set.groupName)){
			
			this.parameterSets.put(set.groupName, new HashMap<>());
			
		}
		
		if(set.populationSource != null){
			this.parameterSets.get(set.groupName).put(set.populationSource.name(), set);
		} else {
			this.parameterSets.get(set.groupName).put(null, set);
		}
		
	}
	
	public Map<String, ConfigurationGroup> getAreaSets(){
		
		return this.getParameterSets().get(AreaSet.SET_TYPE);
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(ACT_LOCATIONS_TYPE, "Possible values: BUILDINGS, FACILITIES, LANDUSE.");
		map.put(RANDOM_SEED, "The seed for the pseudo random number generator for the generation of the scenario.");
		map.put(SCALE_FACTOR, "The scale factor for the amount of households / persons to be created and for the supply"
				+ " side to scale capacities. Any numeric value between 0 and 1.");
		map.put(YEAR, "The year of the scenario.");
		
		return map;
		
	}
	
	public static class AreaSet extends ConfigurationGroup {

		public static final String SET_TYPE = "areaSet";
		
		public static final String IDS = "ids";
		public static final String NETWORK_LEVEL = "networkLevel";
		public static final String POPULATION_SOURCE = "populationSource";
		public static final String IS_SURVEY_AREA = "isSurveyArea";
		
		private String ids;
		private int networkLevel = 6;
		private PopulationSource populationSource;
		private boolean isSurveyArea;
		
		public enum PopulationSource{
			COMMUTER,
			NONE,
			SURVEY
		};
		
		public AreaSet() {
			
			super(SET_TYPE);
			
		}

		@Override
		public Map<String, String> getComments() {
			
			Map<String, String> map = new HashMap<>();
			
			map.put(IDS, "The keys (GKZ) of the administrative areas. Multiple keys should be comma-separated.");
			map.put(NETWORK_LEVEL, "The lowest osm road level that should be displayed in the area. "
					+ "1: motorway, motorway_link; 2: trunk, trunk_link; 3: primary, primary_link; "
					+ "4: secondary; 5: tertiary; 6: living_street, minor, residential, unclassified."
					+ " For a documentation of the osm road types see http://wiki.openstreetmap.org/wiki/Key:highway");
			map.put(POPULATION_SOURCE, "The source for the demand generation. Possible values: COMMUTER, SURVEY.");
			
			return map;
			
		}
		
		@StringGetter(IDS)
		public String getIds(){
			
			return this.ids;
			
		}
		
		@StringSetter(IDS)
		public void setIds(String ids){
			
			this.ids = ids;
			
		}
		
		@StringGetter(NETWORK_LEVEL)
		public int getNetworkLevel(){
			
			return this.networkLevel;
			
		}
		
		@StringSetter(NETWORK_LEVEL)
		public void setNetworkLevel(int level){
			
			this.networkLevel = level;
			
		}
		
		@StringGetter(POPULATION_SOURCE)
		public PopulationSource getPopulationSource(){
			
			return this.populationSource;
			
		}
		
		@StringSetter(POPULATION_SOURCE)
		public void setPopulationSource(PopulationSource source){
			
			this.populationSource = source;
			
		}
		
		@StringGetter(IS_SURVEY_AREA)
		public boolean isSurveyArea(){
			
			return this.isSurveyArea;
			
		}
		
		@StringSetter(IS_SURVEY_AREA)
		public void setIsSurveyArea(boolean b){
			
			this.isSurveyArea = b;
			
		}
		
	}

}