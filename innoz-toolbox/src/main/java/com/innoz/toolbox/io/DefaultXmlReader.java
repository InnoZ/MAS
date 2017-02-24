package com.innoz.toolbox.io;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DefaultXmlReader extends DefaultHandler {

	public void read(String file){
		
		File inputFile = new File(file);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		
		try {
		
			SAXParser parser = saxFactory.newSAXParser();
			parser.parse(inputFile, this);
		
		} catch (ParserConfigurationException | SAXException | IOException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
}