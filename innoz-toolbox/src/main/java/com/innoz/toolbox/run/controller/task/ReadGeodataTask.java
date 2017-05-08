package com.innoz.toolbox.run.controller.task;

import org.matsim.api.core.v01.Scenario;

import com.innoz.toolbox.io.database.DatabaseReader;

public final class ReadGeodataTask implements ControllerTask {
	
	Scenario scenario;
	
	private ReadGeodataTask(Builder builder) {
		
		this.scenario = builder.scenario;
		
	}
	
	@Override
	public void run() {
		
		DatabaseReader.getInstance().readGeodataFromDatabase(scenario);
		DatabaseReader.getInstance().readPopulationFromDatabase(scenario);
		
	}
	
	public static class Builder {
		
		// Optional
		Scenario scenario = null; // If using facilities
		
		public Builder() {}
		
		public Builder scenario(Scenario scenario) {
			
			this.scenario = scenario;
			return this;
			
		}
		
		public ReadGeodataTask build() {
			
			return new ReadGeodataTask(this);
				
		}
		
	}
	
}