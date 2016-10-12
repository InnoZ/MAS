package com.innoz.toolbox.config.groups;

import java.util.Map;

public class DemandConfigGroup extends ConfigurationGroup {

	static final String GROUP_NAME = "demand";

	static final String SCALE_FACTOR = "scaleFactor";
	
	private double scaleFactor = 1.0d;
	
	public DemandConfigGroup() {
		
		super(GROUP_NAME);
		
	}
	
	@Override
	Map<String, String> getComments() {
		
		return null;
		
	}
	
	public double getScaleFactor(){
		
		return this.scaleFactor;
		
	}
	
	public void setScaleFactor(double scaleFactor){
		
		this.scaleFactor = scaleFactor;
		
	}

}