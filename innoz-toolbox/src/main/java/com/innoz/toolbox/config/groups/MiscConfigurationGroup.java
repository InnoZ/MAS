package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

public class MiscConfigurationGroup extends ConfigurationGroup {

	static final String GROUP_NAME = "misc";
	
	public static final String CRS = "coordinateSystem";
	public static final String OUTPUT_DIR = "outputDirectory";
	public static final String OVERWRITE_EXISTING_FILES = "overwriteExistingFiles";
	
	private String coordinateSystem = "EPSG:4326";
	private String outputDirectory = ".";
	private boolean overwriteExistingFiles = false;
	
	MiscConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}

	@Override
	Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(CRS, "The coordinate reference system that applies to the study area.");
		map.put(OUTPUT_DIR, "The directory containing all output files of the scenario generation process.");
		
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
	
	public boolean isOverwritingExistingFiles(){
		
		return this.overwriteExistingFiles;
		
	}
	
	public void setOverwriteExistingFiles(boolean b){
		
		this.overwriteExistingFiles = b;
		
	}
}