package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;

public interface VehicleChoiceAgent {

	public CSVehicle chooseVehicle(Set<CSVehicle> vehicleOptions, Link startLink);
	
}