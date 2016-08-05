package com.innoz.toolbox.run.controller;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.Configuration;

/**
 * 
 * Class to group all possible kinds of controllers.
 * 
 * @author dhosse
 *
 */
public abstract class DefaultController implements Runnable {

	static final Logger log = Logger.getLogger(ScenarioGenerationController.class);
	
	Configuration configuration;
	
	public DefaultController(Configuration configuration){
		
		this.configuration = configuration;
		
	}
	
}