package com.innoz.toolbox.matsim.sharedMobility.carsharing.supply;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.carsharing.events.NoVehicleCarSharingEvent;
import org.matsim.contrib.carsharing.events.StartRentalEvent;
import org.matsim.contrib.carsharing.events.handlers.NoVehicleCarSharingEventHandler;
import org.matsim.contrib.carsharing.events.handlers.StartRentalEventHandler;

/**
 * 
 * Class that collects counts for
 * <ul>
 * <li> Bookings of carsharing vehicles
 * <li> Stuck events caused by the absence of a vehicle
 * </ul>
 * 
 * This information is used to evaluate whether existing vehicles should be maintained or not and where profitable locations for
 * new vehicles / stations could be.
 * 
 * @author dhosse
 *
 */
public class CarsharingSupplyEventHandler implements NoVehicleCarSharingEventHandler, StartRentalEventHandler {

	Map<String, Integer> bookedVehicles = new HashMap<String, Integer>();
	Map<Id<Link>, Map<String, Integer>> stuckEventsCausedByNoCsVeh = new HashMap<Id<Link>, Map<String, Integer>>();
	
	@Override
	public void reset(int iteration) {
		
		if(iteration > 0 && iteration % 5 == 0) {

			this.bookedVehicles = new HashMap<String, Integer>();
			this.stuckEventsCausedByNoCsVeh = new HashMap<Id<Link>, Map<String, Integer>>();
			
		}
		
	}

	@Override
	public void handleEvent(NoVehicleCarSharingEvent event) {
		
		Id<Link> linkId = event.getOriginLinkId();
		String csType = event.getCarsharingType();
		
		if(!this.stuckEventsCausedByNoCsVeh.containsKey(linkId)) {
			
			this.stuckEventsCausedByNoCsVeh.put(linkId, new HashMap<String, Integer>());
			
			if(!this.stuckEventsCausedByNoCsVeh.get(linkId).containsKey(csType)) {
				
				this.stuckEventsCausedByNoCsVeh.get(linkId).put(csType, 0);
				
			}
			
		}
		
		this.stuckEventsCausedByNoCsVeh.get(linkId).put(csType, this.stuckEventsCausedByNoCsVeh.get(linkId).get(csType) + 1);
		
	}

	@Override
	public void handleEvent(StartRentalEvent event) {
		
		if(!this.bookedVehicles.containsKey(event.getvehicleId())) {
			
			this.bookedVehicles.put(event.getvehicleId(), 0);
			
		}
		
		this.bookedVehicles.put(event.getvehicleId(), this.bookedVehicles.get(event.getvehicleId()) + 1);
		
	}

}