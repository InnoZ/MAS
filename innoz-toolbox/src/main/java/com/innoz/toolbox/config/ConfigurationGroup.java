package com.innoz.toolbox.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ConfigurationGroup {

	final String groupName;
	Map<String, Object> params;
	
	ConfigurationGroup(String name){
		
		this.groupName = name;
		this.params = new HashMap<String, Object>();
		
	}
	
	abstract void initParams();
	
	abstract void addParam(String key, String value);
	
	public Map<String, String> getParams(){
			
		Map<String, String> map = new HashMap<>();
		
		for(Entry<String, Object> entry : this.params.entrySet()){
			
			map.put(entry.getKey(), entry.getValue().toString());
			
		}
		
		return map;
			
	}
	
	abstract Map<String, String> getComments();
	
}