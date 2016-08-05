package com.innoz.toolbox.config;

import com.innoz.toolbox.config.Configuration.ActivityLocations;
import com.innoz.toolbox.config.Configuration.AdminUnitEntry;
import com.innoz.toolbox.config.Configuration.PopulationType;
import com.innoz.toolbox.config.Configuration.Subpopulations;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ConfigurationReaderXml extends DefaultHandler {
	
	private final Configuration configuration;

	private boolean creatingSurveyArea = false;
	private boolean creatingVicinity = false;
	
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
		
		if(qName.equalsIgnoreCase("areaSet")){
			
			if(attributes.getValue("k").equalsIgnoreCase("surveyArea")){
				
				this.creatingSurveyArea = true;
				
			} else if(attributes.getValue("k").equalsIgnoreCase("vicinity")){
				
				this.creatingVicinity = true;
				
			}
			
		} else if(qName.equalsIgnoreCase("adminUnit")){
			
			createAdminUnit(attributes);
			
		} else if(qName.equalsIgnoreCase(Configuration.CRS)){
			
			this.configuration.crs = attributes.getValue("v");
			
		} else if(qName.equalsIgnoreCase("useTransit")){
			
		} else if(qName.equalsIgnoreCase(Configuration.POPULATION_TYPE)){
			
			this.configuration.popType = PopulationType.valueOf(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.POPULATION_TYPE_V)){
			
			this.configuration.popTypeV = PopulationType.valueOf(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.SCALE_FACTOR)){
			
			this.configuration.scaleFactor = Double.parseDouble(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.OUTPUT_DIR)){
			
			this.configuration.outputDirectory = attributes.getValue("v");
			
		} else if(qName.equalsIgnoreCase(Configuration.ACTIVITY_LOCATIONS_TYPE)){
			
			this.configuration.actLocs = ActivityLocations.valueOf(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.USE_HOUSEHOLDS)){
			
			this.configuration.useHouseholds = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.ONLY_WORKING_DAYS)){
			
			this.configuration.onlyWorkingDays = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.USE_VEHICLES)){
			
			this.configuration.useVehicles = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.LOCAL_PORT)){
			
			this.configuration.localPort = Integer.parseInt(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.OVERWRITE_FILES)){
			
			this.configuration.overwriteExistingFiles = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.WRITE_DB_OUTPUT)){
			
			this.configuration.writeDatabaseTables = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.WRITE_INTO_DATAHUB)){
			
			this.configuration.writeIntoDatahub = Boolean.parseBoolean(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.DB_TABLE_SUFFIX)){
			
			this.configuration.tableSuffix = attributes.getValue("v");
			
		} else if(qName.equalsIgnoreCase(Configuration.DEMAND_DATA_SOURCE)){
			
			this.configuration.demandSource = attributes.getValue("v");
			
		} else if(qName.equalsIgnoreCase(Configuration.SUBPOPULATIONS_TYPE)){

			this.configuration.subpopulation = Subpopulations.valueOf(attributes.getValue("v"));
			
		} else if(qName.equalsIgnoreCase(Configuration.N_THREADS)){
			
			this.configuration.numberOfThreads = Integer.parseInt(attributes.getValue("v"));
			
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){
		
		if(qName.equalsIgnoreCase("areaSet")){
			
			this.creatingSurveyArea = false;
			this.creatingVicinity = false;
			
		}
		
	}
	
	private void createAdminUnit(Attributes atts){
		
		String id = atts.getValue("id");
		
		String nHH = atts.getValue(Configuration.NUMBER_OF_HH);
		if(nHH == null) nHH = "0";
		
		int hh = Integer.parseInt(nHH);
		
		String levelOfDetail = atts.getValue(Configuration.LOD_NETWORK);
		if(levelOfDetail == null) levelOfDetail = "6";
		
		Integer lod = Integer.parseInt(levelOfDetail);
		
		for(String s : id.split(Configuration.COMMENT)){

			if(this.creatingSurveyArea){
				
				if(this.configuration.surveyAreaIds == null){
				
					this.configuration.surveyAreaIds = new String("");
					
				}
				
				this.configuration.surveyAreaIds += s + ",";
				
			} else if(this.creatingVicinity){
				
				if(this.configuration.vicinityIds == null){
					
					this.configuration.vicinityIds = new String("");
					
				}
				
				this.configuration.vicinityIds += s + ",";
				
			}
			
			this.configuration.adminUnits.put(id, new AdminUnitEntry(s, hh, lod));
			
		}
		
	}
	
}
