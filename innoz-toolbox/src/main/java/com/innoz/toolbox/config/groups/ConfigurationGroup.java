package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ConfigurationGroup {

	public final String groupName;
	Map<String, Object> params = new HashMap<String, Object>();
	Map<String, Map<String, ConfigurationGroup>> parameterSets = new HashMap<>();
	
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
	
	public Map<String, Map<String, ConfigurationGroup>> getParameterSets(){
		
		return this.parameterSets;
		
	}
	
	public abstract Map<String, String> getComments();
	
	public ConfigurationGroup createParameterSet(String name){
		
		return new ConfigurationGroup(name) {
			
			@Override
			public Map<String, String> getComments() {
				return null;
				
			}
			
		};
		
	}
	
	public void addParameterSet(ConfigurationGroup parameterSet){
		//TODO
	}
	
	public void addParam(String name, String value){
		if(this.params.containsKey(name)){
			this.params.put(name, value);
		}
	}
	
}