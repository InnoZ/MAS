package com.innoz.toolbox.config;

import java.util.Map;

public class DemandConfigGroup extends ConfigurationGroup {

	static final String GROUP_NAME = "demand";

	static final String SCALE_FACTOR = "scaleFactor";
	
	private double scaleFactor = 1.0d;
	
	public DemandConfigGroup() {
		
		super(GROUP_NAME);
		
	}
	
	@Override
	public void initParams(){
		
		this.params.put(SCALE_FACTOR, this.scaleFactor);
		
	}
	
	@Override
	void addParam(String key, String value){
		
		if(SCALE_FACTOR.equals(key)){
			
			this.params.put(SCALE_FACTOR, Double.parseDouble(value));
			
		}
		
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