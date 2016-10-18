package com.innoz.toolbox.config;

import java.io.File;

import org.apache.log4j.Logger;

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
	//NON-CONFIGURABLE///////////////////////////////////////////////////////////////////////
	int localPort = 3200;
	final int remotePort = 5432;
	
	String sshUser;
	String sshPassword;
	String databaseUser = "postgres";
	String userPassword = "postgres";
	
//	public enum PopulationType{persons,households};
//	public enum PopulationSource{dummy,commuter,survey,none};
//	public enum Subpopulations{none,mobility_attitude};
//	public enum VehicleSource{matsim, survey};
//	public enum ActivityLocations{landuse, buildings, facilities};
//	public enum DayType{weekday, weekend, all};
//	public enum SurveyType{mid,srv};
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
		this.psql = new PsqlConfigurationGroup();
		this.scenario = new ScenarioConfigurationGroup();
		this.surveyPopulation = new SurveyPopulationConfigurationGroup();
	
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
	 * 
	 * Sets the configurable parameters of the configuration to their default values.
	 * 
	 */
	public void reset(){
		
//		this.surveyAreaIds = null;
//		this.vicinityIds = null;
//		this.crs = "EPSG:32632";
//		this.outputDirectory = ".";
//		this.popSource = PopulationSource.survey;
//		this.popSourceV = PopulationSource.none;
//		this.popType = PopulationType.households;
//		this.vehSource = VehicleSource.matsim;
//		this.dayType = DayType.weekday;
//		this.actLocs = ActivityLocations.buildings;
//		this.adminUnits = new HashMap<String, Configuration.AdminUnitEntry>();
//		this.randomSeed = 4711L;
//		this.scaleFactor = 1.0d;
//		this.writeDatabaseTables = false;
//		this.writeIntoDatahub = false;
//		this.dbNameSpace = null;
//		this.overwriteExistingFiles = false;
		
	}
	
	/**
	 * Validates the configuration. Only errors that may eventually cause exceptions are taken into account here.
	 */
	private void validate(){
		
		boolean validationError = false;

		// A survey area must be defined!
//		if(this.surveyAreaIds.isEmpty()){
//			
//			validationError = true;
//			log.error("You must specify at least one survey area by its id!");
//			log.info("See table gadm.districts in mobility database for information.");
//			
//		}
		
		// Non-generic cars can only be used along w/ households.
//		if(!this.popType.equals(PopulationType.households) && this.vehSource.equals(VehicleSource.survey)){
//			
//			validationError = true;
//			log.error("You disabled the use of households data but enabled cars. This won't work!");
//			
//		}
//		
//		if((this.subpopulation.equals(Subpopulations.mobility_attitude) || this.surveyType.equals("srv")) && !this.surveyAreaIds.contains("03404")){
//			
//			validationError = true;
//			log.error("SrV data as well as data for mobility attitude groups (Mobilitätstypen) are only valid for Osnabrück!");
//			
//		}
		
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
	
}