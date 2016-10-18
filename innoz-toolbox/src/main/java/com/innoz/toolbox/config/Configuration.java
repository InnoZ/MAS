package com.innoz.toolbox.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.groups.MiscConfigurationGroup;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup;
import com.innoz.toolbox.utils.GlobalNames;

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

	private MiscConfigurationGroup misc;
	private ScenarioConfigurationGroup scenario;
	private SurveyPopulationConfigurationGroup surveyPopulation;
	
//	public static final String SURVEY_AREA = "surveyArea";
//	public static final String VICINITY = "vicinity";
//	
//	public static final String SURVEY_AREA_IDS = "surveyAreaIds";
//	public static final String VICINITY_IDS = "vicinityIds";
//	public static final String CRS = "coordinateSystem";
//	public static final String OUTPUT_DIR = "outputDirectory";
//	public static final String SCALE_FACTOR = "scaleFactor";
//	public static final String ACTIVITY_LOCATIONS_TYPE = "activityLocationsType";
//	public static final String CREATE_TRANSIT = "createTransit";
//
//	public static final String DEMAND_DATA_SOURCE = "demandSource";
//	public static final String POPULATION_SOURCE = "populationSource";
//	public static final String POPULATION_SOURCE_V = "populationSourceVicinity";
//	public static final String VEHICLES_SOURCE = "vehiclesSource";
//	
//	public static final String DAY_TYPES = "dayTypes";
//	public static final String POPULATION_TYPE = "populationType";
//	
//	public static final String NUMBER_OF_HH = "numberOfHouseholds"; //TODO write this into gadm.districs!
//	public static final String NUMBER_OF_P = "numberOfInhabitants";
//	public static final String LOD_NETWORK = "networkDetail";
//	
//	public static final String SUBPOPULATIONS_TYPE = "subpopulationsType";
//	
//	public static final String LOCAL_PORT = "localPort";
//	public static final String DB_SCHEMA_NAME = "databaseSchemaName";
//	public static final String DB_TABLE_SUFFIX = "tableSuffix";
//	public static final String WRITE_DB_OUTPUT = "writeTables";
//	public static final String WRITE_INTO_DATAHUB = "intoMobilityDatahub";
//	
//	public static final String RANDOM_SEED = "randomSeed";
//	public static final String OVERWRITE_FILES = "overwriteExistingFiles";
//	
//	public static final String N_THREADS = "numberOfThreads";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	//CONFIGURABLE///////////////////////////////////////////////////////////////////////////
	String surveyAreaIds;
	String vicinityIds;
	String crs = GlobalNames.UTM32N;
	String outputDirectory = ".";
	
	PopulationType popType = PopulationType.households;
	
	PopulationSource popSource = PopulationSource.survey;
	PopulationSource popSourceV = PopulationSource.none;
	
	Subpopulations subpopulation = Subpopulations.none;
	
	ActivityLocations actLocs = ActivityLocations.buildings;
	
	VehicleSource vehSource = VehicleSource.matsim;
	
	DayType dayType = DayType.weekday;
	
	boolean useTransit = false;
	
	SurveyType surveyType = SurveyType.mid;
	
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
	
	public enum PopulationType{persons,households};
	public enum PopulationSource{dummy,commuter,survey,none};
	public enum Subpopulations{none,mobility_attitude};
	public enum VehicleSource{matsim, survey};
	public enum ActivityLocations{landuse, buildings, facilities};
	public enum DayType{weekday, weekend, all};
	public enum SurveyType{mid,srv};
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
		this.outputDirectory = ".";
		this.popSource = PopulationSource.survey;
		this.popSourceV = PopulationSource.none;
		this.popType = PopulationType.households;
		this.vehSource = VehicleSource.matsim;
		this.dayType = DayType.weekday;
		this.actLocs = ActivityLocations.buildings;
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
//	void setParam(String param, Object value){
//		
//		switch(param){
//		
//			case SURVEY_AREA_IDS: this.surveyAreaIds = (String) value;
//				break;
//			
//			case VICINITY_IDS: this.vicinityIds = (String) value;
//				break;
//			
//			case OUTPUT_DIR: this.outputDirectory = (String) value;
//				break;
//			
//			case OVERWRITE_FILES: this.overwriteExistingFiles = (Boolean) value;
//				break;
//				
//			case POPULATION_TYPE: this.popType = PopulationType.valueOf((String) value);
//				break;
//				
//			case DB_SCHEMA_NAME: this.dbNameSpace = (String) value;
//				break;
//				
//			case DB_TABLE_SUFFIX: this.tableSuffix = (String) value;
//				break;
//				
//			case WRITE_INTO_DATAHUB: this.writeIntoDatahub = (Boolean) value;
//				break;
//				
//			case LOCAL_PORT: this.localPort = (Integer) value;
//				break;
//			
//			default: return ;
//			
//		}
//		
//	}
	
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
		if(!this.popType.equals(PopulationType.households) && this.vehSource.equals(VehicleSource.survey)){
			
			validationError = true;
			log.error("You disabled the use of households data but enabled cars. This won't work!");
			
		}
		
		if((this.subpopulation.equals(Subpopulations.mobility_attitude) || this.surveyType.equals("srv")) && !this.surveyAreaIds.contains("03404")){
			
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
	public PopulationSource getPopulationSource() {
		return this.popSource;
	}
	
	/**
	 * 
	 * Getter for the {@code useHouseholds} parameter.
	 * 
	 * @return {@code True} if households are used in demand generation, {@code false} otherwise.
	 */
	public PopulationType getPopulationType(){
		return this.popType;
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
	public DayType getUsedDayTypes(){
		return this.dayType;
	}
	
	/**
	 * 
	 * Getter for the {@code isUsingCars} parameter.
	 * 
	 * @return {@code True} if vehicles from mobility surveys should be used, {@code false} otherwise (= generic vehicles}.
	 */
	public VehicleSource getVehicleSource(){
		return this.vehSource;
	}
	
	/**
	 * 
	 * Getter for the {@code activity locations type} parameter.
	 * 
	 * @return {@code ActivityLocations} type that is used to locate activity coordinates Default is 'buildings'.
	 */
	public ActivityLocations getActivityLocationsType(){
		
		return this.actLocs;
		
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
		
//		log.info("Dump of configuration settings:");
//		
//		for(Entry<String,String> entry : getParams().entrySet()){
//			
//			log.info(entry.getKey() + "\t" + entry.getValue());
//			
//		}
		
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
		int numberOfInhabitants;
		Integer lodNetwork;
		
		/**
		 * 
		 * Creates a new admin unit object with the specified parameters.
		 * 
		 * @param id Identifier of the administrative unit.
		 * @param nHouseholds The number of households contained.
		 * @param lod The level of detail the network should have inside the administrative unit's geometry.
		 */
		public AdminUnitEntry(String id, int nHouseholds, int nInhabitants, Integer lod){
			
			this.id = id;
			this.numberOfHouseholds = nHouseholds;
			this.numberOfInhabitants = nInhabitants;
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
		
		public int getNumberOfInhabitants(){
			return this.numberOfInhabitants;
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
	
	public SurveyType getSurveyType(){
		return this.surveyType;
	}
	
	public Subpopulations getSubpopulationsType(){
		
		return this.subpopulation;
		
	}
	
	public boolean isUsingTransit(){
		
		return this.useTransit;
		
	}
	
	public int getNumberOfThreads(){
		
		return this.numberOfThreads;
		
	}
	
	public PopulationSource getVicinityPopulationSource(){
		return this.popSourceV;
	}
	
//	public Map<String, String> getParams(){
//		
//		Map<String, String> map = new HashMap<>();
//		
//		map.put(CRS, this.crs);
//		map.put(CREATE_TRANSIT, Boolean.toString(this.useTransit));
//		map.put(POPULATION_SOURCE, this.popSource.name());
//		map.put(POPULATION_SOURCE_V, this.popSourceV.name());
//		map.put(SCALE_FACTOR, Double.toString(this.scaleFactor));
//		map.put(OUTPUT_DIR, this.outputDirectory);
//		map.put(OVERWRITE_FILES, Boolean.toString(this.overwriteExistingFiles));
//		map.put(DEMAND_DATA_SOURCE, this.surveyType.name());
//		map.put(SUBPOPULATIONS_TYPE, this.subpopulation.name());
//		map.put(ACTIVITY_LOCATIONS_TYPE, this.actLocs.name());
//		map.put(POPULATION_TYPE, this.popType.name());
//		map.put(DAY_TYPES, this.dayType.name());
//		map.put(VEHICLES_SOURCE, this.vehSource.name());
//		map.put(LOCAL_PORT, Integer.toString(this.localPort));
//		map.put(WRITE_DB_OUTPUT, Boolean.toString(this.writeDatabaseTables));
//		map.put(N_THREADS, Integer.toString(numberOfThreads));
//		map.put(SURVEY_AREA_IDS, this.surveyAreaIds);
//		map.put(VICINITY_IDS, this.vicinityIds);
//		
//		return map;
//		
//	}
	
//	public Map<String, String> getComments(){
//		
//		Map<String, String> map = new HashMap<>();
//		
//		map.put(CRS, "The coordinate reference system that applies to the study area.");
//		map.put(CREATE_TRANSIT, "NOT IMPLEMENTED YET! Defines if MATSim transit should be modeled or not. Default: false.");
//		map.put(POPULATION_SOURCE, "The source of the population that is created as initial demand in the survey area. Possible values are: none, dummy, commuter, survey.");
//		map.put(POPULATION_SOURCE_V, "The source of the population that is created as initial demand for the vicinity. Possible values are: none, dummy, commuter, survey.");
//		map.put(SCALE_FACTOR, "The scale factor for the amount of households / persons to be created and for the supply side to reduce capacities. Any numeric value between 0 and 1.");
//		map.put(OUTPUT_DIR, "The directory containing all output files of the scenario generation process.");
//		map.put(OVERWRITE_FILES, "Switch to 'yes' to overwrite existing files in the output directory. Default: false.");
//		map.put(DEMAND_DATA_SOURCE, "The data source for demand generation. Only applies to population type 'survey' at the moment. Default is 'mid', for Osnabrück, also 'srv' is possible.");
//		map.put(SUBPOPULATIONS_TYPE, "Defines the type of subpopulation that is used to classify persons. Possible values are 'none' and 'mobility_attitude' (only valid for Osnabrück).");
//		map.put(ACTIVITY_LOCATIONS_TYPE, "'Yes' means: Demand is spatially distributed on the level of individual buildings. If switched to 'no', activities will be randomly distributed in landuse areas. Default: yes.");
//		map.put(POPULATION_TYPE, "Defines the type of population created. Possible values are 'persons' and 'households' (default).");
//		map.put(DAY_TYPES, "Defines if all days or only working days (Mo-Fr) should be used for generating plans. Default: 'weekday'.");
//		map.put(VEHICLES_SOURCE, "Defines if household vehicles should be created from survey or not. This only works, if the population type is 'households'. Default: 'matsim' (i.e. use MATSim default vehicles).");
//		map.put(LOCAL_PORT, "The local network port for the ssh connection.");
//		map.put(WRITE_DB_OUTPUT, "Defines if the data created according to this configuration should be written into database tables or not. Default: no.");
//		map.put(N_THREADS, "Number of threads that are executed at the same time. Deault value is '1'.");
//		
//		return map;
//		
//	}
	
	public final MiscConfigurationGroup misc(){
		
		return this.misc;
		
	}
	
	public final ScenarioConfigurationGroup scenario(){
		
		return this.scenario;
		
	}
	
	public final SurveyPopulationConfigurationGroup surveyPopulation(){
		
		return this.surveyPopulation;
		
	}
	
}