package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class TracksConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "tracksPopulation";
	
	public static final String DATE = "date";
	public static final String ACT_THRESHOLD = "activityThreshold";

	private String date;
	private double activityThreshold = 15 * 3600;
	
	public TracksConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}
	
	@StringGetter(DATE)
	public String getDate() {
		
		return this.date;
		
	}
	
	@StringSetter(DATE)
	public void setDate(String d) {
		
		this.date = d;
		
	}
	
	@StringGetter(ACT_THRESHOLD)
	public double getActivityThreshold() {
		
		return this.activityThreshold;
		
	}
	
	@StringSetter(ACT_THRESHOLD)
	public void setActivityThreshold(double d) {
		
		this.activityThreshold = d;
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put(DATE, "The date of the tracks that should be read from the database. Format <yyyy-mmm-dd>.");
		map.put(ACT_THRESHOLD, "If a sighting (time between two tracks) within a person's tracks is below this threshold,"
				+ " no activity is created (because it's probably some sort of interaction, e.g. line change in pt).");
		
		return map;
		
	}

}