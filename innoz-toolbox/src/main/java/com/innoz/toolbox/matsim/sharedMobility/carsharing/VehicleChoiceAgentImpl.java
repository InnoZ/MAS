package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.Set;

import javax.inject.Inject;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.core.utils.geometry.CoordUtils;

public class VehicleChoiceAgentImpl implements VehicleChoiceAgent {
	
	@Inject private CarsharingSupplyInterface carsharingSupplyContainer;	
	
	@Override
	public CSVehicle chooseVehicle(Set<CSVehicle> vehicleOptions, Link startLink) {
		
		double distance = -1.0;
		CSVehicle chosenVehicle = null;
		for (CSVehicle vehicle : vehicleOptions) {
			
			Link vehicleLocation = this.carsharingSupplyContainer.getAllVehicleLocations().get(vehicle);
			
			double distanceCurr = CoordUtils.calcEuclideanDistance(vehicleLocation.getCoord(), startLink.getCoord());
			
			if (distance == -1.0 || distanceCurr < distance) 
				chosenVehicle = vehicle;
		}
		
		return chosenVehicle;		
	}

}
