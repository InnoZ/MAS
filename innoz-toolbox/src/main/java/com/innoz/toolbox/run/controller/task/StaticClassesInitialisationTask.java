package com.innoz.toolbox.run.controller.task;

import org.matsim.core.gbl.MatsimRandom;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;

public class StaticClassesInitialisationTask implements ControllerTask {

	final Configuration configuration;
	
	private StaticClassesInitialisationTask(Builder builder) {
		
		this.configuration = builder.configuration;
		
	}
	
	@Override
	public void run() {
		
		Geoinformation.init(this.configuration.scenario().getActivityLocationsType());
		DatabaseReader.init(this.configuration);
		MatsimRandom.reset(this.configuration.scenario().getRandomSeed());
		
	}
	
	public static class Builder {
		
		final Configuration configuration;
		
		public Builder(Configuration configuration) {
			
			this.configuration = configuration;
			
		}
		
		public StaticClassesInitialisationTask build() {
			
			return new StaticClassesInitialisationTask(this);
			
		}
		
	}

}