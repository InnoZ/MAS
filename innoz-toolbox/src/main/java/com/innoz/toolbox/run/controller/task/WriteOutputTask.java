package com.innoz.toolbox.run.controller.task;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.population.PopulationWriter;

import com.innoz.toolbox.config.Configuration;

public class WriteOutputTask implements ControllerTask {

	Scenario scenario;
	String outputDirectory;
	
	private WriteOutputTask(Builder builder) {
		
		this.scenario = builder.scenario;
		this.outputDirectory = builder.outputDirectory;
		
	}
	
	@Override
	public void run() {
		
		new ConfigWriter(scenario.getConfig()).write(this.outputDirectory + "config.xml.gz");
		new NetworkWriter(this.scenario.getNetwork()).write(this.outputDirectory + "network.xml.gz");
		new PopulationWriter(scenario.getPopulation()).write(this.outputDirectory + "plans.xml.gz");

	}
	
	public static class Builder {
		
		Scenario scenario;
		String outputDirectory;
		
		public Builder(Configuration configuration, Scenario scenario) {
			
			this.scenario = scenario;
			this.outputDirectory = configuration.misc().getOutputDirectory();
			
		}
		
		public WriteOutputTask build() {
			
			return new WriteOutputTask(this);
			
		}
		
	}
	
}