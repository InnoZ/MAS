package com.innoz.toolbox.run.parallelization;

import java.util.ArrayList;
import java.util.List;

import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Building;

/**
 * 
 * Thread that adds building geometries to a landuse dataset.
 * 
 * @author dhosse
 *
 */
public final class BuildingThread extends AlgoThread {

	private DatabaseReader reader;
	private List<Building> buildings = new ArrayList<>();
	
	@Override
	public void run() {

		for(Building b : this.buildings){
			
			for(String actType : b.getActivityOptions()){
				
				if(actType != null){
			
					this.reader.addGeometry(actType, b);
				
				}
			
			}
			
		}
		
	}

	@Override
	public void init(Object... args) {

		this.reader = (DatabaseReader)args[0];
		
	}

	@Override
	void addToThread(Object obj) {

		if(obj instanceof Building){
			this.buildings.add((Building)obj);
		}
		
	}

}
