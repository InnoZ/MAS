package com.innoz.toolbox.config.groups;

import java.util.Map;

public class ScenarioConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "scenario";
	
	public static final String RANDOM_SEED = "randomSeed";
	
	private long randomSeed = 4711L;
	
	ScenarioConfigurationGroup() {
		super(GROUP_NAME);
	}
	
	public long getRandomSeed(){
		
		return this.randomSeed;
		
	}
	
	public void setRandomSeed(long seed){
		
		this.randomSeed = seed;
		
	}

	@Override
	Map<String, String> getComments() {
		// TODO Auto-generated method stub
		return null;
	}

}
