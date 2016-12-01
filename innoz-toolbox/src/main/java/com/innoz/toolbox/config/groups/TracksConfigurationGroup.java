package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class TracksConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "tracksPopulation";
	
	public static final String DATE = "DATE";

	private String date;
	
	public TracksConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}
	
	@StringGetter(DATE)
	public String getDate(){
		
		return this.date;
		
	}
	
	@StringSetter(DATE)
	public void setDate(String d){
		
		this.date = d;
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put(DATE, "The date of the tracks that should be read from the database. Format <yyyy-mmm-dd>.");
		
		return map;
		
	}

}
