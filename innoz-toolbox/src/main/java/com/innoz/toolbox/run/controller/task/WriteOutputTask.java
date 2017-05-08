package com.innoz.toolbox.run.controller.task;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.population.PopulationWriter;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.utils.misc.PlansToJson;

public class WriteOutputTask implements ControllerTask {

	Scenario scenario;
	String outputDirectory;
	String crs;
	
	private WriteOutputTask(Builder builder) {
		
		this.scenario = builder.scenario;
		this.outputDirectory = builder.outputDirectory;
		this.crs = builder.crs;
		
	}
	
	@Override
	public void run() {
		
		new ConfigWriter(scenario.getConfig()).write(this.outputDirectory + "config.xml.gz");
		new NetworkWriter(this.scenario.getNetwork()).write(this.outputDirectory + "network.xml.gz");
		new PopulationWriter(scenario.getPopulation()).write(this.outputDirectory + "plans.xml.gz");
		PlansToJson.run(this.scenario, this.outputDirectory + "features.json", this.crs);

	}
	
	public static class Builder {
		
		Scenario scenario;
		String outputDirectory;
		String crs;
		
		public Builder(Configuration configuration, Scenario scenario) {
			
			this.scenario = scenario;
			this.outputDirectory = configuration.misc().getOutputDirectory();
			this.crs = configuration.misc().getCoordinateSystem();
			
		}
		
		public WriteOutputTask build() {
			
			return new WriteOutputTask(this);
			
		}
		
	}
	
}