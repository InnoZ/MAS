package com.innoz.toolbox.config;

import java.util.HashMap;
import java.util.Map;

public class MiscConfigGroup extends ConfigurationGroup {

	static final String GROUP_NAME = "misc";
	
	static final String CRS = "coordinateSystem";
	static final String OUTPUT_DIR = "outputDirectory";
	
	private String coordinateSystem = "EPSG:4326";
	private String outputDirectory = ".";
	
	MiscConfigGroup() {
		
		super(GROUP_NAME);
		
	}

	@Override
	void initParams() {
		
		this.params.put(CRS, this.coordinateSystem);
		this.params.put(OUTPUT_DIR, this.outputDirectory);
		
	}

	@Override
	void addParam(String key, String value) {
		
		if(CRS.equals(key)){
		
			this.coordinateSystem = value;
		
		} else if(OUTPUT_DIR.equals(key)){
			
			this.outputDirectory = value;
		
		}
		
	}

	@Override
	Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(CRS, "");
		map.put(OUTPUT_DIR, "");
		
		return map;
	}
	
	public String getCoordinateSystem(){
		
		return this.coordinateSystem;
		
	}
	
	public void setCoordinateSystem(String crs){
		
		this.coordinateSystem = crs;
		
	}
	
	public String getOutputDirectory(){
		
		return this.outputDirectory;
		
	}
	
	public void setOutputDirectory(String dir){
		
		this.outputDirectory = dir;
		
	}

}