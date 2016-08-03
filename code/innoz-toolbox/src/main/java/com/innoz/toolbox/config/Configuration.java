package com.innoz.toolbox.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * 
 * Class that holds all relevant parameters for the semi-automatic generation of a MATSim pre-base scenario.
 * 
 * @author dhosse
 *
 */
public final class Configuration {

	//TAGS///////////////////////////////////////////////////////////////////////////////////
	static final String SEP = "\t";
	static final String COMMENT = "#";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(Configuration.class);
	
	public static final String SURVEY_AREA = "surveyArea";
	public static final String VICINITY = "vicinity";
	
	public static final String SURVEY_AREA_IDS = "surveyAreaIds";
	public static final String VICINITY_IDS = "vicinityIds";
	public static final String CRS = "coordinateSystem";
	public static final String OUTPUT_DIR = "outputDirectory";
	public static final String POPULATION_TYPE = "populationType";
	public static final String POPULATION_TYPE_V = "populationTypeVicinity";
	public static final String SCALE_FACTOR = "scaleFactor";
	public static final String USE_BUILDINGS = "useBuildings";
	public static final String CREATE_TRANSIT = "createTransit";
	
	public static final String ONLY_WORKING_DAYS = "onlyWorkingDays";
	public static final String USE_HOUSEHOLDS = "useHouseholds";
	public static final String USE_VEHICLES = "useVehicles";
	public static final String NUMBER_OF_HH = "numberOfHouseholds"; //TODO write this into gadm.districs!
	public static final String LOD_NETWORK = "networkDetail";
	public static final String DEMAND_DATA_SOURCE = "demandSource";
	
	public static final String USE_MAG = "useMobilityAttitudeGroups";
	
	public static final String LOCAL_PORT = "localPort";
	public static final String DB_SCHEMA_NAME = "databaseSchemaName";
	public static final String DB_TABLE_SUFFIX = "tableSuffix";
	public static final String WRITE_DB_OUTPUT = "writeTables";
	public static final String WRITE_INTO_DATAHUB = "intoMobilityDatahub";
	
	public static final String RANDOM_SEED = "randomSeed";
	public static final String OVERWRITE_FILES = "overwriteExistingFiles";
	
	public static final String N_THREADS = "numberOfThreads";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	//CONFIGURABLE///////////////////////////////////////////////////////////////////////////
	String surveyAreaIds;
	String vicinityIds;
	String crs = "EPSG:32632";
	String outputDirectory = ".";
	PopulationType popType = PopulationType.survey;
	PopulationType popTypeV = PopulationType.none;
	
	boolean useHouseholds = true;
	boolean useVehicles = false;
	boolean onlyWorkingDays = true;
	boolean useBuildings = true;
	boolean useMobilityAttitudeGroups = false;
	boolean useTransit = false;
	
	String demandSource = "mid";
	
	int numberOfHouseholds = 0;
	
	Map<String,AdminUnitEntry> adminUnits;
	
	long randomSeed = 4711L;
	int numberOfThreads = 1;
	
	double scaleFactor = 1.0d;

	boolean writeDatabaseTables = false;
	boolean writeIntoDatahub = false;
	String dbNameSpace;
	String tableSuffix;
	
	boolean overwriteExistingFiles = false;
	
	//NON-CONFIGURABLE///////////////////////////////////////////////////////////////////////
	int localPort = 3200;
	final int remotePort = 5432;
	
	String sshUser;
	String sshPassword;
	String databaseUser = "postgres";
	String userPassword = "postgres";
	
	public enum PopulationType{dummy,commuter,survey,none};
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
		
		this.adminUnits = new HashMap<String, Configuration.AdminUnitEntry>();
	
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
		
		this.surveyAreaIds = null;
		this.vicinityIds = null;
		this.crs = "EPSG:32632";
		this.outputDirectory = null;
		this.popType = PopulationType.survey;
		this.useHouseholds = true;
		this.useVehicles = false;
		this.onlyWorkingDays = true;
		this.useBuildings = true;
		this.numberOfHouseholds = 0;
		this.adminUnits = new HashMap<String, Configuration.AdminUnitEntry>();
		this.randomSeed = 4711L;
		this.scaleFactor = 1.0d;
		this.writeDatabaseTables = false;
		this.writeIntoDatahub = false;
		this.dbNameSpace = null;
		this.overwriteExistingFiles = false;
		
	}
	
	/**
	 * 
	 * Setter method for the configurable parameters of the configuration.
	 * 
	 * @param param The string identifier of the parameter.
	 * @param value The value to which the parameter is to be set.
	 */
	void setParam(String param, Object value){
		
		switch(param){
		
			case SURVEY_AREA_IDS: this.surveyAreaIds = (String) value;
				break;
			
			case VICINITY_IDS: this.vicinityIds = (String) value;
				break;
			
			case OUTPUT_DIR: this.outputDirectory = (String) value;
				break;
			
			case OVERWRITE_FILES: this.overwriteExistingFiles = (Boolean) value;
				break;
				
			case USE_HOUSEHOLDS: this.useHouseholds = (Boolean) value;
				break;
				
			case DB_SCHEMA_NAME: this.dbNameSpace = (String) value;
				break;
				
			case DB_TABLE_SUFFIX: this.tableSuffix = (String) value;
				break;
				
			case WRITE_INTO_DATAHUB: this.writeIntoDatahub = (Boolean) value;
				break;
				
			case LOCAL_PORT: this.localPort = (Integer) value;
				break;
			
			default: return ;
			
		}
		
	}
	
	/**
	 * Validates the configuration. Only errors that may eventually cause exceptions are taken into account here.
	 */
	private void validate(){
		
		boolean validationError = false;

		// A survey area must be defined!
		if(this.surveyAreaIds.isEmpty()){
			
			validationError = true;
			log.error("You must specify at least one survey area by its id!");
			log.info("See table gadm.districts in mobility database for information.");
			
		}
		
		// Non-generic cars can only be used along w/ households.
		if(!this.useHouseholds && this.useVehicles){
			
			validationError = true;
			log.error("You disabled the use of households data but enabled cars. This won't work!");
			
		}
		
		if((this.useMobilityAttitudeGroups || this.demandSource.equals("srv")) && !this.surveyAreaIds.contains("03404")){
			
			validationError = true;
			log.error("SrV data as well as data for mobility attitude groups (Mobilitätstypen) are only valid for Osnabrück!");
			
		}
		
		// Check if the output directory exists and has files in it.
		File f = new File(this.outputDirectory);
		if(f.exists()){
			
			if(f.list().length > 0){
				
				log.warn("The output directory " + this.outputDirectory + " already exists and has files in it!");
				
				if(!this.overwriteExistingFiles){
					
					log.error("Since you disabled overwriting of existing files, you must either delete existing files or pick another"
							+ " output directory!");
					validationError = true;
					
				} else {
					
					log.warn("All existing files will be overwritten!");
					
				}
				
			}
			
		}
		
		int nProcessors = Runtime.getRuntime().availableProcessors();
		if(this.numberOfThreads > nProcessors){
			
			log.warn("Specified number of threads: " + this.numberOfThreads + ", but you have only " + nProcessors + " cores available...");
			log.info("Thus, the programm will only use these " + nProcessors + " cores.");
			this.numberOfThreads = nProcessors;
			
		} else if(this.numberOfThreads == 0){
			
			log.warn("Specified number of threads: " + this.numberOfThreads + "!");
			log.info("Thus, the programm will use all " + nProcessors + " cores.");
			this.numberOfThreads = nProcessors;
			
		}
		
		// If anything should cause the configuration to be invalid, abort!
		if(validationError){
			
			throw new RuntimeException("Invalid configuration! Shutting down...");
			
		}
		
	}
	
	/**
	 * 
	 * Getter for survey area ids.
	 * 
	 * @return Comma-separated string of the survey area id(s)
	 */
	public String getSurveyAreaIds() {
		return this.surveyAreaIds;
	}
	
	/**
	 * 
	 * Getter for vicinity of the survey area.
	 * 
	 * @return Comma-separated string of the vicinity id(s)
	 */
	public String getVicinityIds() {
		return this.vicinityIds;
	}

	/**
	 * 
	 * Getter for coordinate reference system.
	 * 
	 * @return String representation of the CRS.
	 */
	public String getCrs() {
		return this.crs;
	}

	/**
	 * 
	 * Getter for the output directory path.
	 * 
	 * @return System path to the output directory.
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	/**
	 * 
	 * Getter for the demand generation method.
	 * 
	 * @return The chosen type of population to generate.
	 */
	public PopulationType getPopulationType() {
		return this.popType;
	}
	
	/**
	 * 
	 * Getter for the {@code useHouseholds} parameter.
	 * 
	 * @return {@code True} if households are used in demand generation, {@code false} otherwise.
	 */
	public boolean isUsingHouseholds(){
		return this.useHouseholds;
	}
	
	/**
	 * 
	 * Getter for the database user name.
	 * 
	 * @return The user name for the MobilityDatahub connection.
	 */
	public String getDatabaseUsername(){
		return this.databaseUser;
	}
	
	/**
	 * 
	 * Setter for the database user name.
	 * 
	 * @param user The user name for the MobilityDatahub connection.
	 */
	void setDatabaseUser(String user){ //package
		
		this.databaseUser = user;
		
	}
	
	/**
	 * 
	 * Getter for the database password.
	 * 
	 * @return The user's password for the MobilityDatahub connection.
	 */
	public String getDatabasePassword(){
		return this.userPassword;
	}
	
	/**
	 * 
	 * Setter for the database password.
	 * 
	 * @param password The password for the MobilityDatahub connection.
	 */
	void setDatabasePassword(String password){ //package
		
		this.userPassword = password;
		
	}

	/**
	 * 
	 * Getter for the {@code isOnlyUsingWorkingDays} parameter.
	 * 
	 * @return {@code True} if only working days should be considered in demand generation, {@code false} otherwise.
	 */
	public boolean isOnlyUsingWorkingDays(){
		return this.onlyWorkingDays;
	}
	
	/**
	 * 
	 * Getter for the {@code isUsingCars} parameter.
	 * 
	 * @return {@code True} if vehicles from mobility surveys should be used, {@code false} otherwise (= generic vehicles}.
	 */
	public boolean isUsingVehicles(){
		return this.useVehicles;
	}
	
	/**
	 * 
	 * Getter for the {@code isUsingBuildings} parameter.
	 * 
	 * @return {@code True} if buildings should be used to locate activities, {@code false} otherwise (= only landuse data).
	 */
	public boolean isUsingBuildings(){
		return this.useBuildings;
	}
	
	/**
	 * 
	 * Getter for the port number of the ssh local host.
	 * 
	 * @return The local port number.
	 */
	public int getLocalPort(){
		return this.localPort;
	}
	
	/**
	 * 
	 * Getter for the port number of the ssh remote host.
	 * 
	 * @return The remote port number.
	 */
	public int getRemotePort(){
		return this.remotePort;
	}
	
	/**
	 * 
	 * Getter for the number of households in the survey area.
	 * 
	 * @return The number of households.
	 */
	public int getNumberOfHouseholds(){
		return this.numberOfHouseholds;
	}

	/**
	 * 
	 * Getter for the random seed used for the random number generator.
	 * 
	 * @return The random seed.
	 */
	public long getRandomSeed(){
		return this.randomSeed;
	}
	
	/**
	 * 
	 * Getter for the namespace of the database tables containing the scenario generation results.
	 * 
	 * @return The namespace (schema name).
	 */
	public String getDatabaseSchemaName(){
		
		return this.dbNameSpace;
		
	}
	
	/**
	 * 
	 * Getter for the name of the psql trips table.
	 * 
	 * @return The name of the trips psql table.
	 */
	public String getTableSuffix(){
		
		return this.tableSuffix;
		
	}
	
	/**
	 * 
	 * Getter for the {@code writeDatabaseTables} parameter.
	 * 
	 * @return {@code True} if the pre-base scenario data should be written into database tables, {@code false} otherwise.
	 */
	public boolean isWritingDatabaseOutput(){
		
		return this.writeDatabaseTables;
		
	}
	
	/**
	 * 
	 * Getter for the {@code writeIntoDatahub} parameter.
	 * 
	 * @return {@code True} if the pre-base scenario data should be written into the MobilityDatahub. {@code False} means, they're written
	 * into a local database.
	 */
	public boolean isWritingIntoMobilityDatahub(){
		
		return this.writeIntoDatahub;
		
	}
	
	/**
	 * 
	 * Getter for the {@code scaleFactor} parameter.
	 * 
	 * @return The factor for scaling demand and supply.
	 */
	public double getScaleFactor(){
		
		return this.scaleFactor;
	
	}
	
	/**
	 * 
	 * Dumps the configuration settings to the log.
	 * 
	 */
	public void dumpSettings(){
		
		log.info("Dump of configuration settings:");
		
		for(Entry<String,String> entry : getParams().entrySet()){
			
			log.info(entry.getKey() + "\t" + entry.getValue());
			
		}
//		log.info("surveyAreaId(s):           " + this.surveyAreaIds);
//		log.info("vicinityId(s):             " + this.vicinityIds);
//		log.info("coordinateReferenceSystem: " + this.crs);
//		log.info("outputDirectory:           " + this.outputDirectory);
//		log.info("populationType:            " + this.popType.name());
//		log.info("onlyWorkingDays:           " + this.onlyWorkingDays);
//		log.info("useBuildings:              " + this.useBuildings);
//		log.info("useMiDHouseholds:          " + this.useHouseholds);
//		log.info("useMiDVehicles:            " + this.useVehicles);
		
	}
	
	/**
	 * 
	 * Class to temporarily store information about administrative units.
	 * 
	 * @author dhosse
	 *
	 */
	public static class AdminUnitEntry{
		
		String id;
		int numberOfHouseholds;
		Integer lodNetwork;
		
		/**
		 * 
		 * Creates a new admin unit object with the specified parameters.
		 * 
		 * @param id Identifier of the administrative unit.
		 * @param nHouseholds The number of households contained.
		 * @param lod The level of detail the network should have inside the administrative unit's geometry.
		 */
		public AdminUnitEntry(String id, int nHouseholds, Integer lod){
			
			this.id = id;
			this.numberOfHouseholds = nHouseholds;
			this.lodNetwork = lod;
			
		}
		
		/**
		 * 
		 * Getter for the admin unit's identifier.
		 * 
		 * @return String representation of the identifier.
		 */
		public String getId(){
			return this.id;
		}
		
		/**
		 * 
		 * Getter for the number of households inside the administrative unit.
		 * 
		 * @return The number of households.
		 */
		public int getNumberOfHouseholds(){
			return this.numberOfHouseholds;
		}
		
		/**
		 * 
		 * Getter for the level of detail the network should have inside the administrative unit.
		 * 
		 * @return Level of detail.
		 */
		public Integer getNetworkDetail(){
			return this.lodNetwork;
		}
		
	}
	
	/**
	 * 
	 * Getter for the administrative unit map.
	 * 
	 * @return The map containing the administrative units.
	 */
	public Map<String, AdminUnitEntry> getAdminUnitEntries(){
		return this.adminUnits;
	}
	
	public String getDatasource(){
		return this.demandSource;
	}
	
	public boolean isUsingMobilityAttitudeGroups(){
		
		return this.useMobilityAttitudeGroups;
		
	}
	
	public boolean isUsingTransit(){
		
		return this.useTransit;
		
	}
	
	public int getNumberOfThreads(){
		
		return this.numberOfThreads;
		
	}
	
	public PopulationType getVicinityPopulationType(){
		return this.popTypeV;
	}
	
	public Map<String, String> getParams(){
		
		Map<String, String> map = new HashMap<>();
		
		map.put(CRS, this.crs);
		map.put(CREATE_TRANSIT, Boolean.toString(this.useTransit));
		map.put(POPULATION_TYPE, this.popType.name());
		map.put(POPULATION_TYPE_V, this.popTypeV.name());
		map.put(SCALE_FACTOR, Double.toString(this.scaleFactor));
		map.put(OUTPUT_DIR, this.outputDirectory);
		map.put(OVERWRITE_FILES, Boolean.toString(this.overwriteExistingFiles));
		map.put(DEMAND_DATA_SOURCE, this.demandSource);
		map.put(USE_MAG, Boolean.toString(this.useMobilityAttitudeGroups));
		map.put(USE_BUILDINGS, Boolean.toString(this.useBuildings));
		map.put(USE_HOUSEHOLDS, Boolean.toString(this.useHouseholds));
		map.put(ONLY_WORKING_DAYS, Boolean.toString(this.onlyWorkingDays));
		map.put(USE_VEHICLES, Boolean.toString(this.useVehicles));
		map.put(LOCAL_PORT, Integer.toString(this.localPort));
		map.put(WRITE_DB_OUTPUT, Boolean.toString(this.writeDatabaseTables));
		map.put(N_THREADS, Integer.toString(numberOfThreads));
		map.put(SURVEY_AREA_IDS, this.surveyAreaIds);
		map.put(VICINITY_IDS, this.vicinityIds);
		
		return map;
		
	}
	
	public Map<String, String> getComments(){
		
		Map<String, String> map = new HashMap<>();
		
		map.put(CRS, "The coordinate reference system that applies to the study area.");
		map.put(CREATE_TRANSIT, "NOT IMPLEMENTED YET! Defines if MATSim transit should be modeled or not. Default: false.");
		map.put(POPULATION_TYPE, "The type of population that is created as initial demand. Possible values are: none, dummy, commuter, survey.");
		map.put(POPULATION_TYPE_V, "The type of population that is created as initial demand for the vicinity. Possible values are: none, dummy, commuter, survey.");
		map.put(SCALE_FACTOR, "The scale factor for the amount of households / persons to be created and for the supply side to reduce capacities. Any numeric value between 0 and 1.");
		map.put(OUTPUT_DIR, "The directory containing all output files of the scenario generation process.");
		map.put(OVERWRITE_FILES, "Switch to 'yes' to overwrite existing files in the output directory. Default: false.");
		map.put(DEMAND_DATA_SOURCE, "The data source for demand generation. Only applies to population type 'survey' at the moment. Default is 'mid', for Osnabrück, also 'srv' is possible.");
		map.put(USE_MAG, "Defines if mobility attitude groups (Mobilitätstypen) should be created as persons' subpopulation attributes. At the moment, this is only possible for Osnabrück!");
		map.put(USE_BUILDINGS, "'Yes' means: Demand is spatially distributed on the level of individual buildings. If switched to 'no', activities will be randomly distributed in landuse areas. Default: yes.");
		map.put(USE_HOUSEHOLDS, "Defines if househols should be created or not. Default: yes.");
		map.put(ONLY_WORKING_DAYS, "Defines if all days or only working days (Mo-Fr) should be used for generating plans. Default: yes.");
		map.put(USE_VEHICLES, "Defines if household vehicles should be created or not. This only works, if 'useHouseholds' is set to 'true'. Default: no.");
		map.put(LOCAL_PORT, "The local network port for the ssh connection.");
		map.put(WRITE_DB_OUTPUT, "Defines if the data created according to this configuration should be written into database tables or not. Default: no.");
		map.put(N_THREADS, "Number of threads that are executed at the same time. Deault value is '1'.");
		
		return map;
		
	}
	
}