package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.HashMap;
import java.util.Map;

import org.matsim.contrib.carsharing.manager.demand.RentalInfo;
import org.matsim.contrib.carsharing.manager.supply.costs.CompanyCosts;
import org.matsim.contrib.carsharing.manager.supply.costs.CostCalculation;

public class DriveNowCompanyCostStructure {

	public static final String COMPANY_NAME = "drive_now";
	
	private DriveNowCompanyCostStructure() {};
	
	public static CompanyCosts create() {
		
		Map<String, CostCalculation> costCalculations = new HashMap<String, CostCalculation>();
		
		costCalculations.put("freefloating", new DriveNowFreefloatingCostCalculation());
		
		CompanyCosts costs = new CompanyCosts(costCalculations);
		
		return costs;
		
	}
	
	static class DriveNowFreefloatingCostCalculation implements CostCalculation {
		
		@Override
		public double getCost(RentalInfo rentalInfo) {

			double rentalTime = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			
			double pricePerMinute = 0.31;
			
			return rentalTime / 60 * pricePerMinute;
			
		}
		
	}
	
}