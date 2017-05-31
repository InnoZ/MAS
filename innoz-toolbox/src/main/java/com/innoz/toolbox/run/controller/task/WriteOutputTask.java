package com.innoz.toolbox.run.controller.task;

import org.matsim.core.config.ConfigWriter;

import com.innoz.toolbox.analysis.AggregatedAnalysis;
import com.innoz.toolbox.io.pgsql.MatsimPsqlAdapter;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.utils.GlobalNames;
import com.innoz.toolbox.utils.misc.PlansToJson;

public class WriteOutputTask implements ControllerTask {

	String scenarioName;
	
	private WriteOutputTask(Builder builder) {
		
		this.scenarioName = builder.scenarioName;
		
	}
	
	@Override
	public void run() {
		
		new ConfigWriter(Controller.scenario().getConfig()).write(Controller.configuration().misc().getOutputDirectory()+ "config.xml.gz");
		MatsimPsqlAdapter.writeScenarioToPsql(Controller.scenario(), scenarioName);
		
	}
	
	public static class Builder {
		
		String scenarioName;
		
		public Builder(String scenarioName) {
			this.scenarioName = scenarioName;
		}
		
		public WriteOutputTask build() {
			
			return new WriteOutputTask(this);
			
		}
		
	}
	
}