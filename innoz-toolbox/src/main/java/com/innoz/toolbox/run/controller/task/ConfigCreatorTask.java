package com.innoz.toolbox.run.controller.task;

import org.matsim.api.core.v01.Scenario;

import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.config.InitialConfigCreator;

public class ConfigCreatorTask implements ControllerTask{
	
	Scenario scenario;

	private ConfigCreatorTask(Builder builder) {
		
		this.scenario = builder.scenario;
		
	}
	
	@Override
	public void run() {
		
		try {
			
			// Write initial Config
			InitialConfigCreator.adapt(scenario.getConfig());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static class Builder {
		
		public Scenario scenario;
		
		public Builder(Scenario scenario) {
			
			this.scenario = scenario;
			
		}
		
		public ConfigCreatorTask build() {
			
			return new ConfigCreatorTask(this);
			
		}
		
	}


}
