package com.innoz.toolbox.run.controller.task;

import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigWriter;

import com.innoz.toolbox.io.pgsql.MatsimPsqlAdapter;
import com.innoz.toolbox.run.controller.Controller;

public class WriteOutputTask implements ControllerTask {

	String scenarioName;
	String railsEnvironment;
	
	private WriteOutputTask(Builder builder) {
		
		this.scenarioName = builder.scenarioName;
		this.railsEnvironment = builder.railsEnvironment;
		
	}
	
	@Override
	public void run() {
		
		new ConfigWriter(Controller.scenario().getConfig()).write(Controller.configuration().misc().getOutputDirectory() + "config.xml.gz");
		MatsimPsqlAdapter.writeScenarioToPsql(Controller.scenario(), scenarioName, railsEnvironment);
		new NetworkWriter(Controller.scenario().getNetwork()).write(Controller.configuration().misc().getOutputDirectory() + "network.xml.gz");
		new PopulationWriter(Controller.scenario().getPopulation()).write(Controller.configuration().misc().getOutputDirectory() + "plans.xml.gz");
//		MatsimPsqlAdapter.writeScenarioToPsql(Controller.scenario(), scenarioName);

	}
	
	public static class Builder {
		
		String scenarioName;
		String railsEnvironment;
		
		public Builder(String scenarioName, String railsEnvironment) {
			
			this.scenarioName = scenarioName;
			this.railsEnvironment = railsEnvironment;
			
		}
		
		public WriteOutputTask build() {
			
			return new WriteOutputTask(this);
			
		}
		
	}
	
}