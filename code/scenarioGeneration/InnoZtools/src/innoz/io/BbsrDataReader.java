package innoz.io;

import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.utils.io.AbstractCsvReader;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BbsrDataReader {

	private Map<String, Integer> key2Type = new HashMap<String, Integer>();
	
	public void read(final Geoinformation geoinformation){
		
		AbstractCsvReader reader = new AbstractCsvReader(";", true) {
			
			@Override
			public void handleRow(String[] line) {
				
				String id = line[0];
				
				if(id.length() > 7){
					
					id = id.substring(0, 5);
					
				} else {
				
					id = id.substring(0, 4);
					
				}
				
				int rtypd7 = Integer.parseInt(line[4]);
				
				key2Type.put(id, rtypd7);
				
			}
			
		};
		
		reader.read("../../../ressources/regionstypen.csv");
		
		for(District d : geoinformation.getAdminUnits().values()){

			for(String key : d.getAdminUnits().keySet()){

				String subKey = key.length() > 8 ? key.substring(0, 4) : key.substring(0, 4);
				AdministrativeUnit unit = d.getAdminUnits().get(key);
				
				int regionType = this.key2Type.get(subKey);
				
				unit.setRegionType(regionType);
				
				geoinformation.getStatesSet().add(unit.getBland());
				
				if(!geoinformation.getStateId2RegionTypes().containsKey(unit.getBland())){
				
					geoinformation.getStateId2RegionTypes().put(unit.getBland(), new HashSet<Integer>());
					
				}
				
				geoinformation.getStateId2RegionTypes().get(unit.getBland()).add(this.key2Type.get(subKey));
				
			}
			
		}
		
	}
	
}
