package com.innoz.toolbox.scenarioGeneration.facilities;

import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacilitiesFactory;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.ActivityOptionImpl;
import org.matsim.facilities.OpeningTimeImpl;

import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Building;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.ProxyFacility;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GlobalNames;
import com.innoz.toolbox.utils.data.Tree.Node;

public class FacilitiesCreator {

	public void create(final Scenario scenario, final Geoinformation geoinformation, List<Building> buildingList,
			double minX, double minY, double maxX, double maxY){
		
		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(GlobalNames.WGS84
				, GlobalNames.UTM32N);
		
		ActivityFacilitiesFactory factory = scenario.getActivityFacilities().getFactory();
		
		int cnt = 0;
		
		for(Building building : buildingList){
			
			if(building.getGeometry() != null){
				
			ActivityFacility facility = factory.createActivityFacility(Id.create(cnt, ActivityFacility.class), MGC.point2Coord(building.getGeometry().getCentroid()));
			
			for(String act : building.getActivityOptions()){

				if(act != null){
					
					String actType = act.split(GlobalNames.UNDERLINE)[0];
					
					if(!facility.getActivityOptions().containsKey(actType)){
						
						ProxyFacility proxy = new ProxyFacility(facility);

						// If the activity is of any of the sub types, take only the main activity type
						ActivityOption option = factory.createActivityOption(actType);
						((ActivityOptionImpl)option).addOpeningTime(getOpeningTimeForActivityOption(option.getType()));
						facility.addActivityOption(option);
						option.setCapacity(building.getGeometry().getArea());
						
						if(geoinformation.getLanduseOfType(act) == null){
							
							geoinformation.createQuadTreeForActType(act, new double[]{minX,minY,maxX,maxY});
							
						}
						
						Coord c = transformation.transform(MGC.point2Coord(building.getGeometry().getCentroid()));
						geoinformation.getLanduseOfType(act).put(c.getX(), c.getY(), proxy);
						
						for(Node<AdministrativeUnit> node : geoinformation.getAdminUnits()){
							
							AdministrativeUnit unit = node.getData();
							
							if(unit.getGeometry() != null && unit.getGeometry().contains(building.getGeometry())){
								
								unit.addLanduse(actType, proxy);
								
							}
							
						}
						
					}
					
				}
				
			}
			
			if(!facility.getActivityOptions().containsKey(ActivityTypes.LEISURE) &&
					!facility.getActivityOptions().containsKey(ActivityTypes.HOME) &&
					!facility.getActivityOptions().containsKey(ActivityTypes.WORK)){
				
				ActivityOption option = factory.createActivityOption(ActivityTypes.WORK);
				((ActivityOptionImpl)option).addOpeningTime(getOpeningTimeForActivityOption(option.getType()));
				facility.addActivityOption(option);
				
			}
			
			scenario.getActivityFacilities().addActivityFacility(facility);
			
			cnt++;
			
			}
			
		}
	
	}
	
	private OpeningTimeImpl getOpeningTimeForActivityOption(String type){
		
		if(type.equals(ActivityTypes.EDUCATION)) {
			
			return new OpeningTimeImpl(7 * 3600, 20 * 3600);
			
		} else if(type.equals(ActivityTypes.KINDERGARTEN)){
			
			return new OpeningTimeImpl(7 * 3600, 16 * 3600);
			
		} else if(type.equals(ActivityTypes.OTHER)){
			
			return new OpeningTimeImpl(8 * 3600, 18 * 3600);
			
		} else if(type.equals(ActivityTypes.SHOPPING)){
			
			return new OpeningTimeImpl(9 * 3600, 20 * 3600);
			
		} else {
			
			return new OpeningTimeImpl(0, Time.MIDNIGHT);
			
		}
		
	}
	
}