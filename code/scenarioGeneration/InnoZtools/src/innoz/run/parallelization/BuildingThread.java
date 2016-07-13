package innoz.run.parallelization;

import java.util.ArrayList;
import java.util.List;

import innoz.io.database.DatabaseReader;
import innoz.scenarioGeneration.geoinformation.Building;

public class BuildingThread extends AlgoThread {

	private DatabaseReader reader;
	private List<Building> buildings = new ArrayList<>();
	
	@Override
	public void run() {

		for(Building b : this.buildings){
			
			for(String actType : b.getActivityOptions()){
				
				if(actType != null){
			
					this.reader.addGeometry(actType, b.getGeometry());
				
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
