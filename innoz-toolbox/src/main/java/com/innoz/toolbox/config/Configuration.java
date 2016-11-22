package com.innoz.toolbox.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.MiscConfigurationGroup;
import com.innoz.toolbox.config.groups.PsqlConfigurationGroup;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup;

/**
 * 
 * Class that holds all relevant parameters for the semi-automatic generation of a MATSim pre-base scenario.
 * 
 * @author dhosse
 *
 */
public final class Configuration {
	
	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(Configuration.class);
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	//CONFIGURABLE///////////////////////////////////////////////////////////////////////////
	private MiscConfigurationGroup misc;
	private PsqlConfigurationGroup psql;
	private ScenarioConfigurationGroup scenario;
	private SurveyPopulationConfigurationGroup surveyPopulation;
	
	private Map<String,ConfigurationGroup> groups = new HashMap<>();
	//NON-CONFIGURABLE///////////////////////////////////////////////////////////////////////
	int localPort = 3200;
	final int remotePort = 5432;
	
	String databaseUser = "postgres";
	String userPassword = "postgres";
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * 
	 * Creates a new configuration and sets its parameters according to what's defined in the given file.
	 * 
	 * @param file Text file containing the configuration parameters.
	 */
	Configuration(String file){
		
		this();		
		this.load(file);
		
	}
	
	/**
	 * 
	 * Creates an empty (i.e. only default values) configuration.
	 * 
	 */
	Configuration(){
		
		this.misc = new MiscConfigurationGroup();
		this.groups.put("misc", misc);
		this.psql = new PsqlConfigurationGroup();
		this.groups.put("psql", psql);
		this.scenario = new ScenarioConfigurationGroup();
		this.groups.put("scenario", scenario);
		this.surveyPopulation = new SurveyPopulationConfigurationGroup();
		this.groups.put("surveyPopulation", surveyPopulation);
	
	}
	
	/**
	 * 
	 * Loads the configuration object by reading in data from an existing configuration file (*.xml).
	 * 
	 * @param file The configuration file to load.
	 */
	void load(String file){
		
		new ConfigurationReaderXml(this).read(file);
		
		validate();
		
	}
	
	/**
	 * Validates the configuration. Only errors that may eventually cause exceptions are taken into account here.
	 */
	private void validate(){
		
		boolean validationError = false;

		// Check if the output directory exists and has files in it.
		File f = new File(this.misc.getOutputDirectory());
		if(f.exists()){
			
			if(f.list().length > 0){
				
				log.warn("The output directory " + this.misc.getOutputDirectory() + " already exists and has files in it!");
				
				if(!this.misc.isOverwritingExistingFiles()){
					
					log.error("Since you disabled overwriting of existing files, you must either delete existing files or pick another"
							+ " output directory!");
					validationError = true;
					
				} else {
					
					log.warn("All existing files will be overwritten!");
					
				}
				
			}
			
		}
		
		int nProcessors = Runtime.getRuntime().availableProcessors();
		int n = this.misc.getNumberOfThreads();
		if(n > nProcessors){
			
			log.warn("Specified number of threads: " + n + ", but you have only " + nProcessors + " cores available...");
			log.info("Thus, the programm will only use these " + nProcessors + " cores.");
			this.misc.setNumberOfThreads(nProcessors);
			
		} else if(n == 0){
			
			log.warn("Specified number of threads: " + n + "!");
			log.info("Thus, the programm will use all " + nProcessors + " cores.");
			this.misc.setNumberOfThreads(nProcessors);
			
		}
		
		// If anything should cause the configuration to be invalid, abort!
		if(validationError){
			
			throw new RuntimeException("Invalid configuration! Shutting down...");
			
		}
		
	}
	
	public final MiscConfigurationGroup misc(){
		
		return this.misc;
		
	}
	
	public final PsqlConfigurationGroup psql(){
		
		return this.psql;
		
	}
	
	public final ScenarioConfigurationGroup scenario(){
		
		return this.scenario;
		
	}
	
	public final SurveyPopulationConfigurationGroup surveyPopulation(){
		
		return this.surveyPopulation;
		
	}
	
	public final ConfigurationGroup getModule(String name){
		
		return this.groups.get(name);
		
	}
	
}