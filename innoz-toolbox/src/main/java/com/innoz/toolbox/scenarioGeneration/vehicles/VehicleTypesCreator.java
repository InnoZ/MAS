package com.innoz.toolbox.scenarioGeneration.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.EngineInformation.FuelType;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleType.DoorOperationMode;
import org.matsim.vehicles.Vehicles;
import org.matsim.vehicles.VehiclesFactory;

public class VehicleTypesCreator {
	
	public static void createOsCarsharingVehicleTypes(Scenario scenario){
		
		Vehicles vehicles = scenario.getVehicles();
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "statk_elektro", 4, 0,
				DoorOperationMode.serial, FuelType.electricity, 130/3.6, 1, 7.5, 2.5));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "stat_mini", 4, 0,
				DoorOperationMode.serial, FuelType.gasoline, 200/3.6, 1, 7.5, 2.5));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "statk_kompakt", 5, 0,
				DoorOperationMode.serial, FuelType.gasoline, 200/3.6, 1, 7.5, 2.5));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "statk_komfort", 5, 0,
				DoorOperationMode.serial, FuelType.gasoline, 200/3.6, 1, 7.5, 2.5));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "statk_maxi", 2, 0,
				DoorOperationMode.serial, FuelType.gasoline, 200/3.6, 1, 7.5, 2.5));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "flowk", 4, 0,
				DoorOperationMode.serial, FuelType.gasoline, 200/3.6, 1, 7.5, 2.5));
		
	}
	
	public static void createBasicRoadVehicleTypes(Scenario scenario){
		
		Vehicles vehicles = scenario.getVehicles();
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "pkw_diesel", 4, 0, 
				DoorOperationMode.serial, FuelType.diesel, 200 / 3.6, 1, 7.5, 2.5));
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "pkw_gasoline", 4, 0, 
				DoorOperationMode.serial, FuelType.gasoline, 200 / 3.6, 1, 7.5, 2.5));
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "pkw_biodiesel", 4, 0, 
				DoorOperationMode.serial, FuelType.biodiesel, 200 / 3.6, 1, 7.5, 2.5));
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "pkw_electric", 4, 0, 
				DoorOperationMode.serial, FuelType.electricity, 200 / 3.6, 1, 7.5, 2.5));
		
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "hgv", 2, 0, 
				DoorOperationMode.serial, FuelType.diesel, 200 / 3.6, 2, 7.5, 2.5));
		
	}
	
	public static void createAndAddGermanTrainTypes(Scenario scenario){
		
		Vehicles vehicles = scenario.getTransitVehicles();

		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), VehicleTypeIDs.GermanVehicleTypeIDs.BR101,
				0, 0, DoorOperationMode.parallel, FuelType.electricity, 200 / 3.6, 2, 0, 0));
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), VehicleTypeIDs.GermanVehicleTypeIDs.BR102,
				0, 0, DoorOperationMode.parallel, FuelType.electricity, 200 / 3.6, 2, 0, 0));
		
		//ICE 1
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), VehicleTypeIDs.GermanVehicleTypeIDs.BR401,
				700, 0, DoorOperationMode.parallel, FuelType.electricity, 280./3.6, 2, 410.7, 3.02));
		//ICE 2
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), VehicleTypeIDs.GermanVehicleTypeIDs.BR402,
				381, 0, DoorOperationMode.parallel, FuelType.electricity, 280./3.6, 2, 205.36, 3.07));
		//ICE 3
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), VehicleTypeIDs.GermanVehicleTypeIDs.BR403,
				460, 0, DoorOperationMode.parallel, FuelType.electricity, 330./3.6, 2, 200.84, 2.95));
		//ICE T
		vehicles.addVehicleType(createVehicleType(vehicles.getFactory(), "ICE-T",
				389, 0, DoorOperationMode.parallel, FuelType.electricity, 230./3.6, 2, 184.4, 2.85));
		
	}
	
	private static VehicleType createVehicleType(VehiclesFactory factory, String id,
			int seats, int standingRoom, DoorOperationMode dom, FuelType fuelType,
			double meterPerSecond, double pcuEquivalents, double length, double width){
		
		VehicleType type = factory.createVehicleType(Id.create(id, VehicleType.class));
		
		VehicleCapacity capacity = factory.createVehicleCapacity();
		capacity.setSeats(seats);
		capacity.setStandingRoom(standingRoom);
		type.setCapacity(capacity);
		
		type.setDoorOperationMode(dom);
		
		EngineInformation engineInfo = factory.createEngineInformation(fuelType, 0.);
		type.setEngineInformation(engineInfo);
		
		type.setLength(length);
		type.setMaximumVelocity(meterPerSecond);
		type.setPcuEquivalents(pcuEquivalents);
		type.setWidth(width);
		
		return type;
		
	}
	
}
