package com.innoz.toolbox.matsim.routing;

import org.matsim.core.config.ReflectiveConfigGroup;

public class AccessConfigGroup extends ReflectiveConfigGroup {

	public static final String GROUP_NAME = "access";

	private static final String ACCESS_ATTRIBUTE = "linkAccessAttribute";
	private String accessAttribute;
	
	private static final String MODE = "mode";
	private String mode;
	
	private static final String EXCLUDED_FUEL_TYPES = "excludedFuelTypes";
	private String excludedFuelTypes;
	
	public AccessConfigGroup() {
		
		super(GROUP_NAME);
		
	}

	@StringGetter(ACCESS_ATTRIBUTE)
	public String getAccessAttribute() {
		return accessAttribute;
	}

	@StringSetter(ACCESS_ATTRIBUTE)
	public void setAccessAttribute(String accessAttribute) {
		this.accessAttribute = accessAttribute;
	}

	@StringGetter(MODE)
	public String getMode() {
		return mode;
	}

	@StringSetter(MODE)
	public void setMode(String mode) {
		this.mode = mode;
	}

	@StringGetter(EXCLUDED_FUEL_TYPES)
	public String getExcludedFuelTypes() {
		return excludedFuelTypes;
	}

	@StringSetter(EXCLUDED_FUEL_TYPES)
	public void setExcludedFuelTypes(String excludedFuelTypes) {
		this.excludedFuelTypes = excludedFuelTypes;
	}

}