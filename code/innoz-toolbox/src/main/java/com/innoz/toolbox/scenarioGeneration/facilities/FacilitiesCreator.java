package com.innoz.toolbox.scenarioGeneration.facilities;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;

import com.innoz.toolbox.scenarioGeneration.geoinformation.Building;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;

public class FacilitiesCreator {

	public void create(final Scenario scenario, final Geoinformation geoinformation, List<Building> buildingList){
		
		ActivityFacilitiesFactory factory = scenario.getActivityFacilities().getFactory();
		
		int cnt = 0;
		
		for(Building building : buildingList){
			
			ActivityFacility facility = factory.createActivityFacility(Id.create(cnt, ActivityFacility.class), MGC.point2Coord(building.getGeometry().getCentroid()));
			
			for(String act : building.getActivityOptions()){

				if(act != null){
					facility.addActivityOption(factory.createActivityOption(act));
				}
				
			}
			
			scenario.getActivityFacilities().addActivityFacility(facility);
			
			cnt++;
			
		}
	
	}
	
}