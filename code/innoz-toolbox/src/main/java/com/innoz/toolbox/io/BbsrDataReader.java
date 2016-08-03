package com.innoz.toolbox.io;

import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.District;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.utils.io.AbstractCsvReader;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BbsrDataReader {

	private Map<String, Integer> key2Type = new HashMap<String, Integer>();

	public void read(final Geoinformation geoinformation, InputStreamReader in){
		
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
		
		reader.read(in);
		
		for(District d : geoinformation.getAdminUnits().values()){

			for(String key : d.getAdminUnits().keySet()){

				String subKey = key.substring(0, 5);
				if(subKey.startsWith("0")) subKey = subKey.substring(1);
				AdministrativeUnit unit = d.getAdminUnits().get(key);
				
				int regionType = this.key2Type.get(subKey);
				
				unit.setRegionType(regionType);
				
//				geoinformation.getStatesSet().add(unit.getBland());
				
				if(!geoinformation.getRegionTypes().containsKey(unit.getRegionType())){
					
					geoinformation.getRegionTypes().put(unit.getRegionType(), new HashSet<Integer>());
					
				}
				
				geoinformation.getRegionTypes().get(unit.getRegionType()).add(this.key2Type.get(subKey));
				
			}
			
		}
		
	}
	
	public void read(final Geoinformation geoinformation, String file){
		
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
		
		reader.read(file);
		
		for(District d : geoinformation.getAdminUnits().values()){

			for(String key : d.getAdminUnits().keySet()){

				String subKey = key.substring(0, 5);
				if(subKey.startsWith("0")) subKey = subKey.substring(1);
				AdministrativeUnit unit = d.getAdminUnits().get(key);
				
				if(this.key2Type.containsKey(subKey)){

					int regionType = this.key2Type.get(subKey);
					
					unit.setRegionType(regionType);
					
//					geoinformation.getStatesSet().add(unit.getBland());
					
					if(!geoinformation.getRegionTypes().containsKey(unit.getRegionType())){
					
						geoinformation.getRegionTypes().put(unit.getRegionType(), new HashSet<Integer>());
						
					}
					
					geoinformation.getRegionTypes().get(unit.getRegionType()).add(this.key2Type.get(subKey));
					
				}
				
			}
			
		}
		
	}
	
	public void read(final Geoinformation geoinformation){
		
		this.read(geoinformation, "../../../ressources/regionstypen.csv");
		
	}
	
}
