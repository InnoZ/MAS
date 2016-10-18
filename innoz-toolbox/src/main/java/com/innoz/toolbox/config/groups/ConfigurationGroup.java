package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ConfigurationGroup {

	public final String groupName;
	Map<String, Object> params = new HashMap<String, Object>();
	Map<String, ConfigurationGroup> parameterSets = new HashMap<>();
	
	public ConfigurationGroup(String name){
		
		this.groupName = name;
		
	}
	
	public Map<String, String> getParams(){
			
		Map<String, String> map = new HashMap<>();
		
		for(Entry<String, Object> entry : this.params.entrySet()){
			
			map.put(entry.getKey(), entry.getValue().toString());
			
		}
		
		return map;
			
	}
	
	public Map<String, ConfigurationGroup> getParameterSets(){
		
		return this.parameterSets;
		
	}
	
	public void addParameterSet(ConfigurationGroup set){
		
		parameterSets.put(set.groupName, set);
		
	}
	
	public abstract Map<String, String> getComments();
	
}