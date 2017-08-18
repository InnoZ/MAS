package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.carsharing.config.FreeFloatingConfigGroup;
import org.matsim.contrib.carsharing.config.OneWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.TwoWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.manager.demand.RentalInfo;
import org.matsim.contrib.carsharing.manager.supply.costs.CompanyCosts;
import org.matsim.contrib.carsharing.manager.supply.costs.CostCalculation;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.utils.geometry.geotools.MGC;

public class OsCompanyCostStructure {

	public static final String COMPANY_NAME = "stadtteilauto";
	
	private OsCompanyCostStructure() {}
	
	public static CompanyCosts create(Scenario scenario, String path) {
		
		ServiceArea area = new ServiceArea();
		area.init(path);
		
		Map<String, CostCalculation> costCalculations = new HashMap<String, CostCalculation>();
		
		costCalculations.put("freefloating", new CostCalculationOS(scenario.getConfig().getModules().
				get(FreeFloatingConfigGroup.GROUP_NAME),
				scenario, area));
		costCalculations.put("twoway", new CostCalculationOS(scenario.getConfig().getModules().
				get(TwoWayCarsharingConfigGroup.GROUP_NAME),
				scenario, area));
		costCalculations.put("oneway", new CostCalculationOS(scenario.getConfig().getModules().
				get(OneWayCarsharingConfigGroup.GROUP_NAME), scenario, area));
		
		CompanyCosts costs = new CompanyCosts(costCalculations);
		
		return costs;
		
	}
	
	static class CostCalculationOS implements CostCalculation {
		
		ConfigGroup cg;
		Scenario scenario;
		ServiceArea area;
		
		public CostCalculationOS(ConfigGroup cg, Scenario scenario, ServiceArea area) {
			this.cg = cg;
			this.scenario = scenario;
			this.area = area;
		}

		@Override
		public double getCost(RentalInfo rentalInfo) {

			double rentalTIme = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			double distance = rentalInfo.getDistance();
			
			double timeCost = 0.0d;
			double distanceCost = 0.0d;
			double parkingCost = 0.0d;
			
			if(cg instanceof FreeFloatingConfigGroup){
				
				timeCost = Double.parseDouble(((FreeFloatingConfigGroup)cg).timeFeeFreeFloating());
				
				if(rentalInfo.getEndTime() <= 7 * 3600 || rentalInfo.getStartTime() >= 23 * 3600 ){
					timeCost = 2.4 / 3600;
				}
				
				distanceCost = Double.parseDouble(((FreeFloatingConfigGroup)cg).distanceFeeFreeFloating());
				
				if(distance >= 101000){
					distanceCost = 0.25 / 1000;
				}
				
				Coord startCoord = this.scenario.getNetwork().getLinks().get(rentalInfo.getOriginLinkId()).getCoord();
				Coord endCoord = this.scenario.getNetwork().getLinks().get(rentalInfo.getEndLinkId()).getCoord();
				
				if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 0d;
					
				} else if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 5d;
					
				} else if(area.getServiceArea().get("10Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 10d;
					
				}
				
				if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(startCoord))){
					
					if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
						
						parkingCost -= 5;
						
					}
					
				} else if(area.getServiceArea().get("10Euro").contains(MGC.coord2Point(startCoord))){
					
					if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
						
						parkingCost -= 10;
						
					} else if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(endCoord))){
				
						parkingCost -= 5;
						
					}
					
				}

			} else if(cg instanceof TwoWayCarsharingConfigGroup){
				
				timeCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).timeFeeTwoWayCarsharing());
				distanceCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).distanceFeeTwoWayCarsharing());
				
				if(rentalInfo.getEndTime() <= 7 * 3600 && rentalInfo.getStartTime() >= 24 * 3600 ){
					timeCost = 0.5 / 3600;
				}
				
				distanceCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).distanceFeeTwoWayCarsharing());
				
				if(distance >= 101000){
					distanceCost = 0.25 / 1000;
				}
				
			} else if(cg instanceof OneWayCarsharingConfigGroup) {
				
				timeCost = Double.parseDouble(((OneWayCarsharingConfigGroup)cg).timeFeeOneWayCarsharing());
				distanceCost = 0d;
				
			}
			
			return rentalTIme * timeCost + distance * distanceCost + parkingCost;
			
		}

	}
	
}