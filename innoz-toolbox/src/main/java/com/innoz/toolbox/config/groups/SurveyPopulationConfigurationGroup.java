package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class SurveyPopulationConfigurationGroup extends ConfigurationGroup {

	final static String GROUP_NAME = "surveyPopulation";
	
	public static final String SURVEY_TYPE = "surveyType";
	public static final String DAY_TYPES = "dayTypes";
	public static final String VEHICLE_TYPE = "vehicleType";
	public static final String USE_HOUSEHOLDS = "useHouseholds";
	
	private SurveyType source = SurveyType.MiD;
	private DayTypes dayTypes = DayTypes.weekday;
	private SurveyVehicleType vehicleType = SurveyVehicleType.DEFAULT;
	private boolean useHouseholds = true;
	
	public enum SurveyType{
		MiD,
		MOP,
		SrV
	};
	
	public enum DayTypes{
		weekday,
		weekend,
		all
	};
	
	public enum SurveyVehicleType{
		DEFAULT,
		SURVEY
	}
	
	public SurveyPopulationConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}

	@StringGetter(SURVEY_TYPE)
	public SurveyType getSurveyType(){
		
		return this.source;
		
	}
	
	@StringSetter(SURVEY_TYPE)
	public void setSurveyType(SurveyType type){
		
		this.source = type;
		
	}
	
	@StringGetter(DAY_TYPES)
	public DayTypes getDayTypes(){
		
		return this.dayTypes;
		
	}
	
	@StringSetter(DAY_TYPES)
	public void setDayTypes(DayTypes types){
		
		this.dayTypes = types;
		
	}
	
	@StringGetter(VEHICLE_TYPE)
	public SurveyVehicleType getVehicleType(){
		
		return this.vehicleType;
		
	}
	
	@StringSetter(VEHICLE_TYPE)
	public void setVehicleType(SurveyVehicleType type){
		
		this.vehicleType = type;
		
	}
	
	@StringGetter(USE_HOUSEHOLDS)
	public boolean isUsingHouseholds(){
		
		return this.useHouseholds;
		
	}
	
	@StringSetter(USE_HOUSEHOLDS)
	public void setUseHouseholds(boolean b){
		
		this.useHouseholds = b;
		
	}

	@Override
	public Map<String, String> getComments() {

		Map<String, String> map = new HashMap<>();
	
		map.put(SURVEY_TYPE, "The survey that provides data for the demand generation. Possible values: MiD, SrV, MOP.");
		map.put(DAY_TYPES, "Defines if all days or only working days (Mo-Fr) should be used for generating plans."
				+ " Possible values: weekday, weekend, all. Default: weekday.");
		map.put(USE_HOUSEHOLDS, "If 'true', households and their members will be taken from the survey. Else, "
				+ "the persons data is used.");
		map.put(VEHICLE_TYPE, "Possible values: DEFAULT, SURVEY.");
		
		return map;
	
	}

}