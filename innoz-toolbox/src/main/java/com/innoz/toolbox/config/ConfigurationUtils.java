package com.innoz.toolbox.config;

public class ConfigurationUtils {

	public static void loadConfiguration(String file, Configuration configuration){
		configuration.load(file);
	}
	
	public static Configuration createConfiguration(){
		return new Configuration();
	}
	
}