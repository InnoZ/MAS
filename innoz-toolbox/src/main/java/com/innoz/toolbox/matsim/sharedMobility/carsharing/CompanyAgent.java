package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;

public interface CompanyAgent {

	public CSVehicle vehicleRequest(Id<Person> personId, Link locationLink, Link destinationLink, String carsharingType, String vehicleType);
	
}