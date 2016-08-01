package innoz.scenarioGeneration.vehicles;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.EngineInformation.FuelType;
import org.matsim.vehicles.FreightCapacity;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleType.DoorOperationMode;
import org.matsim.vehicles.VehicleUtils;

public final class VehicleTypes {

	/**
	 * 
	 * Returns a vehicle type that represents a KBA vehicle class. See {@link http://www.kba.de} for more information.
	 * 
	 * @param k The key of the vehicle type that was reported.
	 * @param type The type of fuel.
	 * @return Vehicle type according to KBA vehicle class.
	 */
	public static VehicleType getVehicleTypeForKey(int k, FuelType type){
		
		switch(k){
		
			case 1: return createVehicleType("mini_" + type.name(), 2, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 3.5d, 2.0d);
			case 2: return createVehicleType("klein_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 3: return createVehicleType("kompakt_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 4: return createVehicleType("mittel_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 5: return createVehicleType("ober-mittel_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 6: return createVehicleType("ober_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 7: return createVehicleType("gelaende_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 8: return createVehicleType("sport_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 300.0d, 1.0d, 5.0d, 2.0d);
			case 9: return createVehicleType("mini-van_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 10: return createVehicleType("van_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 11: return createVehicleType("utilities_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			case 12: return createVehicleType("wohnmobil_" + type.name(), 4, 0.0d, 0, DoorOperationMode.serial, type, 0.0d, 200.0d, 1.0d, 5.0d, 2.0d);
			default: return null;
		
		}
		
	}
	
	private static VehicleType createVehicleType(String classId, int nSeats, double freightCapInCubicM, int standingRoom,
			DoorOperationMode mode, FuelType type, double gasConsumption, double vmax, double pcu, double length, double width){
		
		VehicleType t = VehicleUtils.getFactory().createVehicleType(Id.create(classId, VehicleType.class));
		
		VehicleCapacity cap = VehicleUtils.getFactory().createVehicleCapacity();
		cap.setSeats(nSeats);
		FreightCapacity freightCap = VehicleUtils.getFactory().createFreigthCapacity();
		freightCap.setVolume(freightCapInCubicM);
		cap.setFreightCapacity(freightCap);
		cap.setStandingRoom(standingRoom);
		t.setCapacity(cap);
		
		t.setDoorOperationMode(mode);
		
		EngineInformation engineInfo = VehicleUtils.getFactory().createEngineInformation(type, gasConsumption);
		t.setEngineInformation(engineInfo);
		
		t.setLength(length);
		t.setMaximumVelocity(vmax);
		t.setPcuEquivalents(pcu);
		t.setWidth(width);
		
		return t;
		
	}
	
}