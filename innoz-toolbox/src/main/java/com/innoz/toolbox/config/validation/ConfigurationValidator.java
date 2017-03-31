package com.innoz.toolbox.config.validation;

import java.io.File;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.run.parallelization.MultithreadedModule;

public class ConfigurationValidator {

	private final static Logger log = Logger.getLogger(MultithreadedModule.class);

	private ConfigurationValidator(){};
	
	public static void validate(final Configuration configuration) {
		
		boolean validationError = false;

		// Check if the output directory exists and has files in it.
		File f = new File(configuration.misc().getOutputDirectory());
		
		if(f.exists()) {
			
			if(f.list().length > 0) {
				
				log.warn("The output directory " + configuration.misc().getOutputDirectory() + " already exists and has files in it!");
				
				if(!configuration.misc().isOverwritingExistingFiles()) {
					
					log.error("Since you disabled overwriting of existing files, you must either delete existing files or pick another"
							+ " output directory!");
					validationError = true;
					
				} else {
					
					log.warn("All existing files will be overwritten!");
					
				}
				
			}
			
		}
		
		int nProcessors = Runtime.getRuntime().availableProcessors();
		int n = configuration.misc().getNumberOfThreads();
		
		if(n > nProcessors) {
			
			log.warn("Specified number of threads: " + n + ", but you have only " + nProcessors + " cores available...");
			log.info("Thus, the programm will only use these " + nProcessors + " cores.");
			configuration.misc().setNumberOfThreads(nProcessors);
			
		} else if(n == 0) {
			
			log.warn("Specified number of threads: " + n + "!");
			log.info("Thus, the programm will use all " + nProcessors + " cores.");
			configuration.misc().setNumberOfThreads(nProcessors);
			
		}
		
		if(configuration.scenario().getYear() > 2040) {
			
			log.error("BBSR population forecast data only covers the time until 2040, but you chose " + configuration.scenario().getYear() + "!");
			validationError = true;
			
		}
		
		// If anything should cause the configuration to be invalid, abort!
		if(validationError) {
			
			throw new RuntimeException("Invalid configuration! Shutting down...");
			
		}
		
	}
	
}