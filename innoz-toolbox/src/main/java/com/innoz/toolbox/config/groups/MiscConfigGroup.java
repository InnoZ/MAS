package com.innoz.toolbox.config.groups;

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