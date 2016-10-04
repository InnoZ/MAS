package com.innoz.energy.config;

import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;

public class ActivityEnergyConsumptionConfigGroup extends ConfigGroup {

	private static final String GROUP_NAME = "activityEnergyConsumption";
	
	public ActivityEnergyConsumptionConfigGroup() {
		
		super(GROUP_NAME);
		
	}
	
	public static class EnergyConsumptionParams extends ReflectiveConfigGroup implements MatsimParameters {

		private static final String SET_TYPE = "energyConsumptionParams";
		
		public EnergyConsumptionParams(String actType) {
			
			super(SET_TYPE);
			
		}
		
	}

}