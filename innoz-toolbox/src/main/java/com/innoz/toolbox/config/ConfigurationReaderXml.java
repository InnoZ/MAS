package com.innoz.toolbox.config;

import java.util.ArrayDeque;
import java.util.Deque;

import org.xml.sax.Attributes;

import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.ConfigurationNames;
import com.innoz.toolbox.io.DefaultXmlReader;

public class ConfigurationReaderXml extends DefaultXmlReader {
	
	private final Configuration configuration;

	private Deque<ConfigurationGroup> groupStack = new ArrayDeque<>();
	
	ConfigurationReaderXml(final Configuration configuration){
		
		this.configuration = configuration;
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes){

		
		if(qName.equals(ConfigurationNames.GROUP)){
			
			startGroup(qName, attributes);
			
		} else if(qName.equals(ConfigurationNames.PARAMETER_SET)){
			
			startParameterSet(attributes);
			
		} else if(qName.equals(ConfigurationNames.PARAM)){
	
			startParameter(attributes);
			
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		
		if(qName.equals(ConfigurationNames.GROUP)||qName.equals(ConfigurationNames.PARAMETER_SET)){
			
			ConfigurationGroup m = groupStack.removeFirst();
			
			if(!groupStack.isEmpty()){
				
				groupStack.getFirst().addParameterSet(m);
				
			}
		
		}
		
	}
	
	private void startGroup(String qName, Attributes atts){
		
		final ConfigurationGroup group = this.configuration.getModule(atts.getValue(ConfigurationNames.NAME));
		groupStack.addFirst(group);
		
	}
	
	private void startParameterSet(Attributes atts){
		
		ConfigurationGroup group = groupStack.getFirst().createParameterSet(atts.getValue(ConfigurationNames.NAME));
		groupStack.addFirst(group);
		
	}
	
	private void startParameter(Attributes atts){
		
		groupStack.getFirst().addParam(atts.getValue(ConfigurationNames.NAME), atts.getValue(ConfigurationNames.VALUE));
		
	}
	
	public static void main(String args[]){
		Configuration c = ConfigurationUtils.createConfiguration();
		new ConfigurationReaderXml(c).read("/home/dhosse/newConfiguration.xml");
	}
	
}
