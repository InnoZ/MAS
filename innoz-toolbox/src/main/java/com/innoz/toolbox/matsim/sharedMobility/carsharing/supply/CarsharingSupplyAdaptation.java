package com.innoz.toolbox.matsim.sharedMobility.carsharing.supply;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.contrib.carsharing.vehicles.FFVehicleImpl;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;

/**
 * 
 * @author dhosse
 *
 */
public class CarsharingSupplyAdaptation {

	static int internalCounter = 0;

	/**
	 * 
	 * @param allVehicles A map containing all vehicles. The mapping is (VehicleId, Vehicle).
	 * @param allLocations A map containing all vehicles and their locations in the network. The mapping is (Vehicle, Link).
	 * @param bookedVehicles A map of all the vehicles that have at least been rent once in the last iteration. The mapping is (VehicleId, Number of Bookings).
	 * @param threshold The minimum number of bookings that have to be at least reached before a vehicle can be declared 'idle'.
	 */
	static void removeIdleVehicles(Map<String, CSVehicle> allVehicles, Map<CSVehicle, Link> allLocations,
			Map<String, Integer> bookedVehicles, int threshold) {
		
		allVehicles.entrySet().removeIf(entry -> !bookedVehicles.containsKey(entry.getKey())
				|| bookedVehicles.get(entry.getKey()) < threshold);
		allLocations.entrySet().removeIf(entry -> !allVehicles.values().contains(entry.getKey()));
		
	}
	
	/**
	 * 
	 * @param network The scenario's network (links and nodes)
	 * @param allVehicles A map containing all vehicles. The mapping is (VehicleId, Vehicle).
	 * @param allLocations A map containing all vehicles and their locations in the network. The mapping is (Vehicle, Link).
	 * @param stuckCountsPerCsType A map containing stuck counts for each carsharing type. 'Stuck' means no carsharing vehicle was 
	 * available when an agent arrived. This is used for the generation of new vehicles. The mapping is (LinkId, Map(Carsharing Type,
	 * Number of Stuck Events)).
	 * @param averageOverIterations The number of iterations after which the supply side is modified. Normally any number greater or equal than 1.
	 * Since too small values tend to cause oscillation of demand, it should be set with caution.
	 */
	static void addNewVehicles(final Network network, Map<String, CSVehicle> allVehicles, Map<CSVehicle, Link> allLocations,
			Map<Id<Link>, Map<String, Integer>> stuckCountsPerCsType, int averageOverIterations) {
		
		stuckCountsPerCsType.entrySet().forEach(entry -> {

			Link l = network.getLinks().get(entry.getKey());
			
			if(entry.getValue().containsKey("twoway")) {
				createTwowayVehicles(l, allVehicles, allLocations,
						Math.max(entry.getValue().get("twoway") / averageOverIterations, 1));
			}
			if(entry.getValue().containsKey("freefloating")) {
				createFreefloatingVehicles(l, allVehicles, allLocations, 
						Math.max(entry.getValue().get("freefloating") / averageOverIterations, 1));
			}
			if(entry.getValue().containsKey("oneway")) {
				createOnewayVehicles(l, allVehicles, allLocations, Math.max(entry.getValue().get("oneway") / averageOverIterations, 1));
			}
			
			
		});
		
	}
	
	private static void createTwowayVehicles(Link link, Map<String, CSVehicle> allVehicles, Map<CSVehicle, Link> allLocations,
			int count) {

		// Search for a vehicles that matches the given link and carsharing type
		StationBasedVehicle ref = null;
		Optional<Entry<CSVehicle, Link>> opt = allLocations.entrySet().stream().filter(entry ->
		entry.getValue().equals(link) && entry.getKey() instanceof StationBasedVehicle).findAny();
		if(opt.isPresent()) ref = (StationBasedVehicle) opt.get().getKey();
		
		while(count > 0) {
		
			String stationId = null;
			if(ref != null) {
				stationId = ref.getStationId();
			} else {
				stationId = "station_" + Integer.toString(internalCounter);
				internalCounter++;
			}
			
			StationBasedVehicle vehicle = new StationBasedVehicle("car", "twoway_" + Integer.toString(internalCounter),
					stationId, "twoway", "stadtteilauto");
			allVehicles.put(vehicle.getVehicleId(), vehicle);
			allLocations.put(vehicle, link);
			
			internalCounter++;
			count--;
			
		}
		
	}
	
	private static void createFreefloatingVehicles(Link link, Map<String, CSVehicle> allVehicles, Map<CSVehicle, Link> allLocations,
			int count) {
		
		while(count > 0) {

			FFVehicleImpl vehicle = new FFVehicleImpl("car", "ff_" + Integer.toString(internalCounter), "stadtteilauto");
			allVehicles.put(vehicle.getVehicleId(), vehicle);
			allLocations.put(vehicle, link);
			
			internalCounter++;
			count--;
			
		}
		
	}
	
	private static void createOnewayVehicles(Link link, Map<String, CSVehicle> allVehicles, Map<CSVehicle, Link> allLocations,
			int count) {
		
		StationBasedVehicle ref = null;
		Optional<Entry<CSVehicle, Link>> opt = allLocations.entrySet().stream().filter(entry ->
		entry.getValue().equals(link) && entry.getKey() instanceof StationBasedVehicle).findAny();
		if(opt.isPresent()) ref = (StationBasedVehicle) opt.get().getKey();
		
		while(count > 0) {
		
			String stationId = null;
			if(ref != null) {
				stationId = ref.getStationId();
			} else {
				stationId = "station_" + Integer.toString(internalCounter);
				internalCounter++;
			}
			
			StationBasedVehicle vehicle = new StationBasedVehicle("car", "oneway_" + Integer.toString(internalCounter),
					stationId, "oneway", "stadtteilauto");
			allVehicles.put(vehicle.getVehicleId(), vehicle);
			allLocations.put(vehicle, link);
			
			internalCounter++;
			count--;
			
		}
		
	}
	
}