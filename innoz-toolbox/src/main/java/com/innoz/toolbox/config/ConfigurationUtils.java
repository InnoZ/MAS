package com.innoz.toolbox.config;

import java.io.InputStream;

public class ConfigurationUtils {

	public static void loadConfiguration(String file, Configuration configuration){
		configuration.load(file);
	}
	
	public static void loadConfiguration(InputStream stream, Configuration configuration){
		configuration.load(stream);
	}
	
	public static Configuration createConfiguration(){
		return new Configuration();
	}
	
}