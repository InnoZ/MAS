package com.innoz.toolbox.matsim.scoring;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class MobilityAttitudeConfigGroup extends ConfigGroup {

	public static final String GROUP_NAME = "mobilityAttitude";
	
	public static final String SCALE_FACTOR = "scaleFactor";
	public static final String SUBPOPULATION_ATTRIBUTE = "subpopulationAttribute";
	
	private Map<String, MobilityAttitudeModeParams> modeParams;
	private double scaleFactor = 1;
	private String subpopulationAttribute;
	
	public MobilityAttitudeConfigGroup() {

		super(GROUP_NAME);
		
		this.modeParams = new HashMap<String, MobilityAttitudeModeParams>();
		
	}
	
	public Map<String,MobilityAttitudeModeParams> getModeParams() {
		return this.modeParams;
	}
	
	public MobilityAttitudeModeParams getParamsForGroup(String group) {
		
		if(this.modeParams.containsKey(group)) {
			
			return this.modeParams.get(group);
			
		}
		
		throw new RuntimeException("No mode params for mobility attitude group '" + group + "'! Simulation ends since no score can be calculated!");
		
	}
	
	@StringSetter(SCALE_FACTOR)
	public void setScaleFactor(double f) {
		this.scaleFactor = f;
	}
	
	@StringGetter(SCALE_FACTOR)
	public double getScaleFactor() {
		return this.scaleFactor;
	}
	
	@StringGetter(SUBPOPULATION_ATTRIBUTE)
	public String getSubpopulationAttribute() {
		return subpopulationAttribute;
	}

	@StringSetter(SUBPOPULATION_ATTRIBUTE)
	public void setSubpopulationAttribute(String subpopulationAttribute) {
		this.subpopulationAttribute = subpopulationAttribute;
	}

	public static class MobilityAttitudeModeParams extends ReflectiveConfigGroup implements MatsimParameters {

		public static final String SET_NAME = "mobilityAttitudeModeParams";
		
		public static final String ATTITUDE_GROUP = "attitudeGroup";
		
		private String attitudeGroup;
		private Map<String, Double> offsetsPerMode;
		
		public MobilityAttitudeModeParams() {
		
			super(SET_NAME);
			this.offsetsPerMode = new HashMap<String, Double>();

		}

		@StringGetter(ATTITUDE_GROUP)
		public String getAttitudeGroup() {
			
			return this.attitudeGroup;
			
		}

		@StringSetter(ATTITUDE_GROUP)
		public void setAttitudeGroup(String attitudeGroup) {
			
			this.attitudeGroup = attitudeGroup;
			
		}
		
		public double getOffsetForMode(String mode) {

			if(this.offsetsPerMode.containsKey(mode)) {
				
				return this.offsetsPerMode.get(mode);
				
			}
			
			return 1d;
			
		}
		
		public void setOffsetForMode(String mode, double offset) {
			
			this.offsetsPerMode.put(mode, offset);
			
		}
		
	}
	
}