package com.innoz.toolbox.run.controller.task;

import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.io.database.population.PopulationDatabaseReader;

public final class ReadGeodataTask implements ControllerTask {
	
	private ReadGeodataTask(Builder builder) {}
	
	@Override
	public void run() {
		
		DatabaseReader.getInstance().readGeodataFromDatabase();
		PopulationDatabaseReader.getInstance().readPopulationFromDatabase();
		
	}
	
	public static class Builder {
		
		public Builder() {}
		
		public ReadGeodataTask build() {
			
			return new ReadGeodataTask(this);
				
		}
		
	}
	
}