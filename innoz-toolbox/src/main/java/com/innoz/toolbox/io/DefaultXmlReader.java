package com.innoz.toolbox.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultXmlReader extends DefaultHandler {

	private static final Logger log = Logger.getLogger(DefaultXmlReader.class);
	
	public void read(String file){
		
		File inputFile = new File(file);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		
		log.info("Reading file " + file);
		
		try {
		
			SAXParser parser = saxFactory.newSAXParser();
			parser.parse(inputFile, this);
		
		} catch (ParserConfigurationException | SAXException | IOException e) {
		
			e.printStackTrace();
			
		}
		
		log.info("Done.");
		
	}
	
	public void read(InputStream stream) {
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		
		try {
		
			SAXParser parser = saxFactory.newSAXParser();
			parser.parse(stream, this);
		
		} catch (ParserConfigurationException | SAXException | IOException e) {
		
			e.printStackTrace();
			
		}
		
		log.info("Done.");
		
		
	}
	
}