package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.HashMap;
import java.util.Map;

import org.matsim.contrib.carsharing.manager.demand.RentalInfo;
import org.matsim.contrib.carsharing.manager.supply.costs.CompanyCosts;
import org.matsim.contrib.carsharing.manager.supply.costs.CostCalculation;

public class TeilautoCompanyCostStructure {

	private TeilautoCompanyCostStructure() {}
	
	public static CompanyCosts create() {
		
		Map<String, CostCalculation> costCalculations = new HashMap<String, CostCalculation>();
		
		costCalculations.put("twoway", new TeilautoTwoWayCostCalculation());
		
		CompanyCosts costs = new CompanyCosts(costCalculations);
		
		return costs;
		
	}
	
	static class TeilautoTwoWayCostCalculation implements CostCalculation {

		@Override
		public double getCost(RentalInfo rentalInfo) {

			double rentalTime = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			double distance = rentalInfo.getDistance();

			double pricePerKm = 0.28;
			double pricePerHour = 2.4;
			
			if(rentalTime > 36000) {
				pricePerHour = 24;
				rentalTime = 3600;
			}
			
			if(rentalInfo.getDistance() >= 300000) {
				pricePerHour = 0.22;
			} else if(rentalInfo.getDistance() >= 1000000) {
				pricePerHour = 0.19;
			}
			
			return rentalTime / 3600 * pricePerHour + distance / 1000 * pricePerKm;
			
		}
		
	}
	
}