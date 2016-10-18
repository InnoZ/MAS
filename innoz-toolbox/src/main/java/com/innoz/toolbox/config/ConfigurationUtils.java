package com.innoz.toolbox.config;

public class ConfigurationUtils {

	public static void loadConfiguration(String file, Configuration configuration){
		configuration.load(file);
	}
	
	public static Configuration createConfiguration(){
		return new Configuration();
	}
	
//	public static void set(Configuration configuration, String parameter, Object value){
//		
//		configuration.setParam(parameter, value);
//		
//	}
	
}
