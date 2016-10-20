package com.innoz.energy.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public final class EnergyConsumptionConfigGroup extends ConfigGroup {

	public static final String GROUP_NAME = "activityEnergyConsumption";
	
	public static final String COMPUTATION_METHOD = "computationMethod";
	public static final String WRITER_FREQ = "writerFrequency";
	
	public enum ComputationMethod{daily,hourly};
	
	private ComputationMethod method = ComputationMethod.daily;
	private int writerFrequency = 10;
	
	public EnergyConsumptionConfigGroup() {
		
		super(GROUP_NAME);
		this.addParam(COMPUTATION_METHOD, this.method.name());
		this.addParam(WRITER_FREQ, Integer.toString(this.writerFrequency));
		
	}
	
	public void addActivityEneryConsumptionParams(String actType, double energyConsumption){
		
		ActivityEnergyConsumptionParams params = new ActivityEnergyConsumptionParams(actType);
		params.setEnergyConsumptionInKiloWattHours(energyConsumption);
		super.addParameterSet(params);
		
	}
	
	public void addLegEnergyConsumptionParams(String mode, double energyConsumption){
		
		LegEnergyConsumptionParams params = new LegEnergyConsumptionParams(mode);
		params.setEnergyConsumptionInKiloWattHours(energyConsumption);
		super.addParameterSet(params);
		
	}
	
	@Override
	public Map<String, String> getComments(){
		
		Map<String, String> map = new HashMap<String, String>();
		map.put(COMPUTATION_METHOD, "The method for calculating the energy consumption of activities. Possible values:"
				+ " daily, hourly.'daily' uses an average consumption for the whole (simulated) day. 'hourly' (NOT IMPLEMENTED)"
				+ "uses hourly consumption values.");
		map.put(WRITER_FREQ, "The frequency of dumping the energy consumption stats into a file. '0' means disabled.");
		return map;
		
	}
	
	@SuppressWarnings("unchecked")
	public ActivityEnergyConsumptionParams getEnergyConsumptionParams(String actType){
		
		Collection<ActivityEnergyConsumptionParams> params = (Collection<ActivityEnergyConsumptionParams>) getParameterSets().get(ActivityEnergyConsumptionParams.SET_TYPE);
		
		for(ActivityEnergyConsumptionParams pars : params){
			
			if(pars.getActivityType().equals(actType)){
				
				return pars;
				
			}
			
		}
		
		return null;
		
	}

	@StringGetter(COMPUTATION_METHOD)
	public ComputationMethod getComputationMethod(){
		
		return this.method;
		
	}
	
	@StringSetter(COMPUTATION_METHOD)
	public void setComputationMethod(ComputationMethod m){
		
		this.method = m;
		
	}
	
	@StringGetter(WRITER_FREQ)
	public int getWriterFrequency(){
		
		return this.writerFrequency;
		
	}
	
	@StringSetter(WRITER_FREQ)
	public void setWriterFrequency(int freq){
		
		this.writerFrequency = freq;
		
	}
	
	public static class ActivityEnergyConsumptionParams extends ReflectiveConfigGroup implements MatsimParameters {

		private static final String SET_TYPE = "activityEnergyConsumptionParams";
		
		public static final String ACT_TYPE = "actType";
		public static final String ENERGY_CONSUMPTION = "energyConsumptionInKiloWattHours";
		
		private String actType;
		private double energyConsumptionInKiloWattHours = 0.0;
		
		public ActivityEnergyConsumptionParams(String actType) {
			
			super(SET_TYPE);
			this.actType = actType;
			
		}
		
		@StringGetter(ACT_TYPE)
		public String getActivityType(){
			
			return this.actType;
			
		}
		
		@StringSetter(ACT_TYPE)
		void setActivityType(String actType){
			
			this.actType = actType;
			
		}
		
		@StringGetter(ENERGY_CONSUMPTION)
		public double getEnergyConsumptionInKiloWattHours(){
			
			return this.energyConsumptionInKiloWattHours;
			
		}
		
		@StringSetter(ENERGY_CONSUMPTION)
		void setEnergyConsumptionInKiloWattHours(double energyConsumptionInKiloWattHours){
			
			this.energyConsumptionInKiloWattHours = energyConsumptionInKiloWattHours;
			
		}
		
	}
	
	public static class LegEnergyConsumptionParams extends ReflectiveConfigGroup implements MatsimParameters {

		private static final String SET_TYPE = "legEnergyConsumptionParams";
		
		public static final String MODE = "mode";
		public static final String ENERGY_CONSUMPTION = "energyConsumptionInKiloWattHours";
		
		private String mode;
		private double energyConsumptionInKiloWattHours = 0.0;
		
		public LegEnergyConsumptionParams(String mode) {
			
			super(SET_TYPE);
			this.mode = mode;
			
		}
		
		@StringGetter(MODE)
		public String getActivityType(){
			
			return this.mode;
			
		}
		
		@StringSetter(MODE)
		void setActivityType(String actType){
			
			this.mode = actType;
			
		}
		
		@StringGetter(ENERGY_CONSUMPTION)
		public double getEnergyConsumptionInKiloWattHours(){
			
			return this.energyConsumptionInKiloWattHours;
			
		}
		
		@StringSetter(ENERGY_CONSUMPTION)
		void setEnergyConsumptionInKiloWattHours(double energyConsumptionInKiloWattHours){
			
			this.energyConsumptionInKiloWattHours = energyConsumptionInKiloWattHours;
			
		}
		
	}

}