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
	private ActivityLocationsType actLocsType = ActivityLocationsType.BUILDINGS;
	
	public enum ActivityLocationsType{
		BUILDINGS,
		FACILITIES,
		LANDUSE
	};
	
	public ScenarioConfigurationGroup() {
		
		super(GROUP_NAME);
		this.params.put(ACT_LOCATIONS_TYPE, this.actLocsType);
		this.params.put(RANDOM_SEED, this.randomSeed);
		this.params.put(SCALE_FACTOR, this.scaleFactor);
		
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
		
		return map;
		
	}
	
	public static class AreaSet extends ConfigurationGroup {

		public static final String SET_TYPE = "areaSet";
		
		public static final String IDS = "ids";
		public static final String NETWORK_LEVEL = "networkLevel";
		public static final String POPULATION_SOURCE = "populationSource";
		
		private String ids;
		private int networkLevel = 6;
		private PopulationSource populationSource;
		
		public enum PopulationSource{
			COMMUTER,
			NONE,
			SURVEY
		};
		
		public AreaSet() {
			
			super(SET_TYPE);
			this.params.put(IDS, this.ids);
			this.params.put(NETWORK_LEVEL, this.networkLevel);
			this.params.put(POPULATION_SOURCE, this.populationSource);
			
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
		
		public String getIds(){
			
			return this.ids;
			
		}
		
		public void setIds(String ids){
			
			this.ids = ids;
			this.params.put(IDS, ids);
			
		}
		
		public int getNetworkLevel(){
			
			return this.networkLevel;
			
		}
		
		public void setNetworkLevel(int level){
			
			this.networkLevel = level;
			
		}
		
		public PopulationSource getPopulationSource(){
			
			return this.populationSource;
			
		}
		
		public void setPopulationSource(PopulationSource source){
			
			this.populationSource = source;
			this.params.put(POPULATION_SOURCE, source);
			
		}
		
	}

}