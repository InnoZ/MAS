package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class MiscConfigurationGroup extends ConfigurationGroup {

	static final String GROUP_NAME = "misc";
	
	public static final String CRS = "coordinateSystem";
	public static final String NUMBER_OF_THREADS = "numberOfThreads";
	public static final String OUTPUT_DIR = "outputDirectory";
	public static final String OVERWRITE_EXISTING_FILES = "overwriteExistingFiles";
	
	private String coordinateSystem = "EPSG:4326";
	private int numberOfThreads = 1;
	private String outputDirectory = ".";
	private boolean overwriteExistingFiles = false;
	
	public MiscConfigurationGroup() {
		
		super(GROUP_NAME);
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		map.put(CRS, "The coordinate reference system that applies to the study area.");
		map.put(NUMBER_OF_THREADS, "Number of threads that are executed at the same time. Default value is '1'.");
		map.put(OUTPUT_DIR, "The directory containing all output files of the scenario generation process.");
		map.put(OVERWRITE_EXISTING_FILES, "Switch to 'yes' to overwrite existing files in the output directory. Default: false.");
		
		return map;
	}

	@StringGetter(CRS)
	public String getCoordinateSystem(){
		
		return this.coordinateSystem;
		
	}
	
	@StringSetter(CRS)
	public void setCoordinateSystem(String crs){
		
		this.coordinateSystem = crs;
		
	}
	
	@StringGetter(NUMBER_OF_THREADS)
	public int getNumberOfThreads(){
		
		return this.numberOfThreads;
		
	}
	
	@StringSetter(NUMBER_OF_THREADS)
	public void setNumberOfThreads(int n){
		
		this.numberOfThreads = n;
		
	}
	
	@StringGetter(OUTPUT_DIR)
	public String getOutputDirectory(){
		
		return this.outputDirectory;
		
	}
	
	@StringSetter(OUTPUT_DIR)
	public void setOutputDirectory(String dir){
		
		this.outputDirectory = dir;
		
	}
	
	@StringGetter(OVERWRITE_EXISTING_FILES)
	public boolean isOverwritingExistingFiles(){
		
		return this.overwriteExistingFiles;
		
	}
	
	@StringSetter(OVERWRITE_EXISTING_FILES)
	public void setOverwriteExistingFiles(boolean b){
		
		this.overwriteExistingFiles = b;
		
	}
}