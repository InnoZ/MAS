package com.innoz.toolbox.matsim.scoring;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.api.internal.MatsimParameters;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

/**
 * 
 * MATSim extension for InnoZ mobility attitude groups (Mobilitätstypen).
 * 
 * @author dhosse
 *
 */
public class MobilityAttitudeConfigGroup extends ConfigGroup {

	public static final String GROUP_NAME = "mobilityAttitude";
	
	public static final String SCALE_FACTOR = "scaleFactor";
	public static final String SUBPOPULATION_ATTRIBUTE = "subpopulationAttribute";
	
	private Map<String, MobilityAttitudeModeParameterSet> groupParams;
	private double scaleFactor = 1;
	private String subpopulationAttribute;
	
	public MobilityAttitudeConfigGroup() {

		super(GROUP_NAME);
		
		this.addParam(SCALE_FACTOR, Double.toString(this.scaleFactor));
		this.addParam(SUBPOPULATION_ATTRIBUTE, this.subpopulationAttribute);
		
		this.groupParams = new HashMap<String, MobilityAttitudeModeParameterSet>();
		
	}
	
	public Map<String,MobilityAttitudeModeParameterSet> getModeParams() {
		
		return this.groupParams;
		
	}
	
	public void addModeParams(MobilityAttitudeModeParameterSet params) {
		
		this.groupParams.put(params.getAttitudeGroup(), params);
		super.addParameterSet(params);
		
	}
	
	public MobilityAttitudeModeParameterSet getParamsForGroup(String group) {
		
		if(this.groupParams.containsKey(group)) {
			
			return this.groupParams.get(group);
			
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

	public static class MobilityAttitudeModeParameterSet extends ReflectiveConfigGroup implements MatsimParameters {

		public static final String SET_NAME = "mobilityAttitudeModeParameterSet";
		
		public static final String ATTITUDE_GROUP = "attitudeGroup";
		
		private String attitudeGroup;
		private Map<String, MobilityAttitudeModeParams> modeParams;
		
		public MobilityAttitudeModeParameterSet() {
		
			super(SET_NAME);
			this.modeParams = new HashMap<>();

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

			if(this.modeParams.containsKey(mode)) {
				
				return this.modeParams.get(mode).getOffset();
				
			}
			
			return 1d;
			
		}
		
		public void addModeParams(MobilityAttitudeModeParams params) {
			
			this.modeParams.put(params.getMode(), params);
			super.addParameterSet(params);
			
		}
		
	}
	
	public static class MobilityAttitudeModeParams extends ReflectiveConfigGroup implements MatsimParameters {

		public static final String SET_TYPE = "mobilityAttitudeModeParams";
		
		public static final String MODE = "mode";
		public static final String OFFSET = "offset";
		
		private String mode;
		private double offset;
		
		public MobilityAttitudeModeParams() {
			super(SET_TYPE);
		}

		@StringGetter(MODE)
		public String getMode() {
			return mode;
		}

		@StringSetter(MODE)
		public void setMode(String mode) {
			this.mode = mode;
		}

		@StringGetter(OFFSET)
		public double getOffset() {
			return offset;
		}

		@StringSetter(OFFSET)
		public void setOffset(double offset) {
			this.offset = offset;
		}
		
		
		
	}
	
}