package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class NetworkConfigurationGroup extends ConfigurationGroup {

	public static final String GROUP_NAME = "network";

	static final Logger log = Logger.getLogger(NetworkConfigurationGroup.class);
	
	public static final String SIMPLIFY_NETWORK = "simplifyNetwork";
	public static final String CLEAN_NETWORK = "cleanNetwork";
	public static final String SCALE_MAXSPEED = "scaleMaxSpeed";
	
	private boolean simplifyNetwork = true;
	private boolean cleanNetwork = true;
	private boolean scaleMaxSpeed = true;
	
	public NetworkConfigurationGroup() {
		super(GROUP_NAME);
		this.parameterSets.put(HighwayDefaults.SET_TYPE, new HashMap<>());
	}
	
	public void addHighwayDefaults(HighwayDefaults defaults) {
		
		String key = defaults.highwayType;
		
		if(this.parameterSets.get(HighwayDefaults.SET_TYPE).containsKey(key)) {
			
			log.warn("The highway defaults already contain defaults for road type '" + key + "'!");
			log.warn("Existing defaults will be overwritten...");
			
		}
		
		this.parameterSets.get(HighwayDefaults.SET_TYPE).put(key, defaults);
		
	}
	
	public Map<String, ConfigurationGroup> getHighwayDefaults(){
		
		return this.getParameterSets().get(HighwayDefaults.SET_TYPE);
		
	}
	
	@StringGetter(SIMPLIFY_NETWORK)
	public boolean isSimplifyNetwork() {
		return this.simplifyNetwork;
	}
	
	@StringSetter(SIMPLIFY_NETWORK)
	public void setSimplifyNetwork(boolean b) {
		this.simplifyNetwork = b;
	}
	
	@StringGetter(CLEAN_NETWORK)
	public boolean cleanNetwork() {
		return this.cleanNetwork;
	}
	
	@StringSetter(CLEAN_NETWORK)
	public void setCleanNetwork(boolean b) {
		this.cleanNetwork = b;
	}
	
	@StringGetter(SCALE_MAXSPEED)
	public boolean scaleMaxSpeed() {
		return this.scaleMaxSpeed;
	}
	
	@StringSetter(SCALE_MAXSPEED)
	public void setScaleMaxSpeed(boolean b) {
		this.scaleMaxSpeed = b;
	}

	@Override
	public Map<String, String> getComments() {
		Map<String, String> map = new HashMap<>();
		return map;
	}

	/**
	 * 
	 * Stores default values for link characteristics.
	 * 
	 * @author dhosse
	 *
	 */
	public static class HighwayDefaults extends ConfigurationGroup {
		
		public static final String SET_TYPE = "highwayDefaults";
		
		public static final String HIERARCHY_LEVEL = "hierarchyLevel";
		public static final String HIGHWAY_TYPE = "highwayType";
		public static final String FREESPEED = "freespeed";
		public static final String FREESPEED_FACTOR = "freespeedFactor";
		public static final String LANES_PER_DIRECTION = "lanesPerDirection";
		public static final String LANE_CAPACITY = "laneCapacity";
		public static final String ONEWAY = "oneway";
		public static final String MODES = "modes";
		
		int hierarchyLevel;
		String highwayType;
		double freespeed;
		double freespeedFactor;
		double lanesPerDirection;
		double laneCapacity;
		boolean oneway;
		String modes;
		
		public HighwayDefaults() {
			super(SET_TYPE);
		}
		
		public HighwayDefaults(int hierarchyLevel, String highwayType, double freespeed, double freespeedFactor, double lanesPerDirection, double laneCapacity, 
				boolean oneway, String modes){
			
			super(SET_TYPE);
			
			this.hierarchyLevel = hierarchyLevel;
			this.highwayType = highwayType;
			this.freespeed = freespeed;
			this.freespeedFactor = freespeedFactor;
			this.lanesPerDirection = lanesPerDirection;
			this.laneCapacity = laneCapacity;
			this.oneway = oneway;
			this.modes = modes;
		}
		
		@StringGetter(HIERARCHY_LEVEL)
		public int getHierarchyLevel() {
			return this.hierarchyLevel;
		}
		
		@StringSetter(HIERARCHY_LEVEL)
		public void setHierarchyLevel(int level) {
			this.hierarchyLevel = level;
		}
		
		@StringGetter(HIGHWAY_TYPE)
		public String getHighwayType() {
			return this.highwayType;
		}
		
		@StringSetter(HIGHWAY_TYPE)
		public void setHighwayType(String type) {
			this.highwayType = type;
		}
		
		@StringGetter(FREESPEED)
		public double getFreespeed() {
			return this.freespeed;
		}
		
		@StringSetter(FREESPEED)
		public void setFreespeed(double freespeed) {
			this.freespeed = freespeed;
		}
		
		@StringGetter(FREESPEED_FACTOR)
		public double getFreespeedFactor() {
			return this.freespeedFactor;
		}
		
		@StringSetter(FREESPEED_FACTOR)
		public void setFreespeedFactor(double factor) {
			this.freespeedFactor = factor;
		}
		
		@StringGetter(LANES_PER_DIRECTION)
		public double getLanesPerDirection() {
			return this.lanesPerDirection;
		}
		
		@StringSetter(LANES_PER_DIRECTION)
		public void setLanesPerDirection(double lanesPerDirection) {
			this.lanesPerDirection = lanesPerDirection;
		}
		
		@StringGetter(LANE_CAPACITY)
		public double getLaneCapacity() {
			return this.laneCapacity;
		}
		
		@StringSetter(LANE_CAPACITY)
		public void setLaneCapacity(double laneCapacity) {
			this.laneCapacity = laneCapacity;
		}
		
		@StringGetter(ONEWAY)
		public boolean isOneway() {
			return this.oneway;
		}

		@StringSetter(ONEWAY)
		public void setOneway(boolean oneway) {
			this.oneway = oneway;
		}
		
		@StringGetter(MODES)
		public String getModes() {
			return this.modes;
		}

		@StringSetter(MODES)
		public void setModes(String modes) {
			this.modes = modes;
		}
		
		@Override
		public Map<String, String> getComments() {
			Map<String, String> map = new HashMap<>();
			return map;
		}
		
	}
	
}