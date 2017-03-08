package com.innoz.toolbox.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.innoz.toolbox.config.groups.NetworkConfigurationGroup.HighwayDefaults;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;

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
		Configuration c = ConfigurationUtils.createConfiguration();
		
		AreaSet set = new AreaSet();
		set.setIds("09180");
		set.setIsSurveyArea(true);
		set.setNetworkLevel(6);
		set.setPopulationSource(PopulationSource.SURVEY);
		c.scenario().addAreaSet(set);
		
		HighwayDefaults def = new HighwayDefaults(1, "rails", 120, 1, 1, 1000, true, "train");
		c.network().addHighwayDefaults(def);
		
		new ConfigurationWriter(c).write("/home/dhosse/configurationTest.xml");
	}
	
}