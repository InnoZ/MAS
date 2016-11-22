package com.innoz.toolbox.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigurationWriter {

	final Configuration configuration;
	final ConfigurationWriterHandler handler;
	
	public ConfigurationWriter(final Configuration configuration){
		
		this.configuration = configuration;
		this.handler = new ConfigurationWriterHandler();
		
	}
	
	public void write(String file){
		
		try {

			BufferedWriter out = new BufferedWriter(new FileWriter(new File(file)));
			this.handler.startConfiguration(out);
			this.handler.writeConfiguration(this.configuration, out);
			this.handler.endConfiguration(out);
			out.flush();
			out.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void main(String args[]){
		new ConfigurationWriter(ConfigurationUtils.createConfiguration()).write("/home/dhosse/newConfiguration.xml");
	}
	
}