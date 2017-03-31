package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.EngineInformation;
import org.matsim.vehicles.EngineInformation.FuelType;
import org.matsim.vehicles.FreightCapacity;
import org.matsim.vehicles.VehicleCapacity;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehiclesFactory;

public class OsVehicleTypes {

	public final Map<Id<VehicleType>, VehicleType> vehicleTypes = new HashMap<>();
	
	private OsVehicleTypes(){};
	
	public VehicleType get(String vType){
		
		if(vehicleTypes.isEmpty()){
			
			this.init();
			
		}
		
		return this.vehicleTypes.get(vType);
		
	}
	
	public Map<Id<VehicleType>, VehicleType> getAll(){
		
		if(this.vehicleTypes.isEmpty()){
			this.init();
		}
		
		return this.vehicleTypes;
		
	}
	
	private void init(){
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		VehiclesFactory vehiclesFactory = scenario.getVehicles().getFactory();
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("elektro", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			cap.setSeats(4);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.electricity, 0.0);
			type.setEngineInformation(currentEngineInfo);
			type.setMaximumVelocity(130/3.6);
			this.vehicleTypes.put(type.getId(), type);
		}
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("mini", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			cap.setSeats(4);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.gasoline, 0.0);
			type.setEngineInformation(currentEngineInfo);
			this.vehicleTypes.put(type.getId(), type);
		}
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("kompakt", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			cap.setSeats(4);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.gasoline, 0.0);
			type.setEngineInformation(currentEngineInfo);
			this.vehicleTypes.put(type.getId(), type);
		}
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("komfort", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			cap.setSeats(5);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.gasoline, 0.0);
			type.setEngineInformation(currentEngineInfo);
			this.vehicleTypes.put(type.getId(), type);
		}
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("maxi", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			FreightCapacity fc = vehiclesFactory.createFreigthCapacity();
			fc.setVolume(9);
			cap.setFreightCapacity(fc);
			cap.setSeats(2);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.gasoline, 0.0);
			type.setEngineInformation(currentEngineInfo);
			this.vehicleTypes.put(type.getId(), type);
		}
		{	
			VehicleType type = vehiclesFactory.createVehicleType(Id.create("flowk", VehicleType.class));
			VehicleCapacity cap = vehiclesFactory.createVehicleCapacity();
			cap.setSeats(4);
			cap.setStandingRoom(0);
			type.setCapacity(cap);
			EngineInformation currentEngineInfo = vehiclesFactory.createEngineInformation(FuelType.gasoline, 0.0);
			type.setEngineInformation(currentEngineInfo);
			this.vehicleTypes.put(type.getId(), type);
		}
		
	}
	
}
