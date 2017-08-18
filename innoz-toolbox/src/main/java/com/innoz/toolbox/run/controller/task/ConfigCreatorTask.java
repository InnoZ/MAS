package com.innoz.toolbox.run.controller.task;

import com.innoz.toolbox.scenarioGeneration.config.InitialConfigCreator;

public class ConfigCreatorTask implements ControllerTask{
	
	private ConfigCreatorTask(Builder builder) {}
	
	@Override
	public void run() {
		
		try {
			
			// Write initial Config
			InitialConfigCreator.adapt();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static class Builder {
		
		public Builder() {}
		
		public ConfigCreatorTask build() {
			
			return new ConfigCreatorTask(this);
			
		}
		
	}


}
