package com.innoz.toolbox.matsim.carsharing;

import java.util.HashMap;
import java.util.Map;

import org.matsim.contrib.carsharing.manager.demand.RentalInfo;
import org.matsim.contrib.carsharing.manager.supply.costs.CompanyCosts;
import org.matsim.contrib.carsharing.manager.supply.costs.CostCalculation;

/**
 * 
 * Generic company cost for Flinkster round-trip based and oneway carsharing services.
 * Might be included in any study with Flinkster carsharing.
 * 
 * @author dhosse
 *
 */
public class FlinksterCompanyCostStructure {

	private FlinksterCompanyCostStructure() {}
	
	public static CompanyCosts create() {
		
		Map<String, CostCalculation> costCalculations = new HashMap<String, CostCalculation>();

		costCalculations.put("twoway", new FlinksterTwoWayCostCalculation());
		costCalculations.put("oneway", new FlinksterOneWayCostCalculation());
		
		CompanyCosts costs = new CompanyCosts(costCalculations);
		
		return costs;
		
	}
	
	static class FlinksterTwoWayCostCalculation implements CostCalculation {

		@Override
		public double getCost(RentalInfo rentalInfo) {

			double rentalTime = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			double distance = rentalInfo.getDistance();

			double pricePerKm = 0.18;
			double pricePerHour = 5;
			
			if(rentalTime > 36000) {
				pricePerHour = 50;
				rentalTime = 3600;
			}
			
			if(rentalInfo.getStartTime() >= 22 * 3600 || rentalInfo.getEndTime() <= 8 * 3600) {
				pricePerHour = 1.5;
			}
			
			return rentalTime / 3600 * pricePerHour + distance / 1000 * pricePerKm;
			
		}
		
	}
	
	static class FlinksterOneWayCostCalculation implements CostCalculation {

		@Override
		public double getCost(RentalInfo rentalInfo) {
			
			double rentalTime = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			double distance = rentalInfo.getDistance();
			
			double pricePerMinute = 0.28;
			
			if(distance > 200000) pricePerMinute = 0.29;
			
			return rentalTime / 60 * pricePerMinute;
			
		}
		
	}
	
}