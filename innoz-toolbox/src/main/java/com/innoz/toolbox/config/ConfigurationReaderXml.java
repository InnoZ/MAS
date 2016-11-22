package com.innoz.toolbox.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.ConfigurationNames;

public class ConfigurationReaderXml extends DefaultHandler {
	
	private final Configuration configuration;

	private Deque<ConfigurationGroup> groupStack = new ArrayDeque<>();
	
	ConfigurationReaderXml(final Configuration configuration){
		
		this.configuration = configuration;
		
	}
	
	void read(String file){
		
		File inputFile = new File(file);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		
		try {
		
			SAXParser parser = saxFactory.newSAXParser();
			parser.parse(inputFile, this);
		
		} catch (ParserConfigurationException | SAXException | IOException e) {
		
			e.printStackTrace();
			
		}
		
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
