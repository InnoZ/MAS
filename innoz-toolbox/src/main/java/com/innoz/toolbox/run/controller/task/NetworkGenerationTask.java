package com.innoz.toolbox.run.controller.task;

import java.sql.SQLException;

import org.matsim.api.core.v01.network.NetworkWriter;
import org.opengis.referencing.FactoryException;

import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.network.NetworkCreatorFromPsql;
import com.vividsolutions.jts.io.ParseException;

public final class NetworkGenerationTask implements ControllerTask {

	private NetworkGenerationTask(Builder builder) {
		
	}
	
	@Override
	public void run() {

		try {
		
			NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(Controller.scenario().getNetwork(), Controller.configuration());
			nc.create();

			new NetworkWriter(Controller.scenario().getNetwork()).write(Controller.configuration().misc().getOutputDirectory() + "network.xml.gz");
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException | ParseException
		        | FactoryException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static class Builder {
	
		public Builder() {
			
		}
		
		public NetworkGenerationTask build() {
			
			return new NetworkGenerationTask(this);
			
		}
		
	}

}