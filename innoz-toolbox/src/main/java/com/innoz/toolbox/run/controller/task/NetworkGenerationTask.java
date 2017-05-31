package com.innoz.toolbox.run.controller.task;

import java.sql.SQLException;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.io.NetworkChangeEventsWriter;
import org.opengis.referencing.FactoryException;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.scenarioGeneration.network.NetworkCreatorFromPsql;
import com.vividsolutions.jts.io.ParseException;

public final class NetworkGenerationTask implements ControllerTask {

	Scenario scenario;
	
	Configuration configuration;
	
	private NetworkGenerationTask(Builder builder) {
		
		this.configuration = builder.configuration;
		this.scenario = builder.scenario;
		
	}
	
	@Override
	public void run() {

		try {
		
			NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(), configuration);
			nc.create();

			new NetworkWriter(scenario.getNetwork()).write(configuration.misc().getOutputDirectory() + "network.xml.gz");
			
//			new NetworkChangeEventsWriter().write(configuration.misc().getOutputDirectory() + "networkChangeEvents.xml.gz", nc.getNetworkChangeEvents());
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException | ParseException
		        | FactoryException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static class Builder {
	
		Scenario scenario;
		
		Configuration configuration;
		
		public Builder(Configuration configuration, Scenario scenario) {
			
			this.configuration = configuration;
			this.scenario = scenario;
			
		}
		
		public NetworkGenerationTask build() {
			
			return new NetworkGenerationTask(this);
			
		}
		
	}

}