package innoz.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * 
 * Class that holds all relevant parameters for the semi-automatic generation of a MATSim pre-base scenario.
 * 
 * @author dhosse
 *
 */
public class Configuration {

	//TAGS///////////////////////////////////////////////////////////////////////////////////
	static final String SEP = "\t";
	static final String COMMENT = "#";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(Configuration.class);
	public static final String SURVEY_AREA_IDS = "surveyAreaIds";
	public static final String VICINITY_IDS = "vicinityIds";
	public static final String CRS = "coordinateSystem";
	public static final String OUTPUT_DIR = "outputDirectory";
	public static final String POPULATION_TYPE = "populationType";
	public static final String SCALE_FACTOR = "scaleFactor";
	public static final String USE_BUILDINGS = "useBuildings";
	
	public static final String ONLY_WORKING_DAYS = "onlyWorkingDays";
	public static final String USE_HOUSEHOLDS = "useHouseholds";
	public static final String USE_VEHICLES = "useVehicles";
	public static final String NUMBER_OF_HH = "numberOfHouseholds"; //TODO write this into gadm.districs!
	public static final String LOD_NETWORK = "networkDetail";
	
	public static final String LOCAL_PORT = "localPort";
	public static final String DB_SCHEMA_NAME = "databaseSchemaName";
	public static final String WRITE_DB_OUTPUT = "writeTables";
	public static final String WRITE_INTO_DATAHUB = "intoMobilityDatahub";
	
	public static final String RANDOM_SEED = "randomSeed";
	public static final String OVERWRITE_FILES = "overwriteExistingFiles";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	//CONFIGURABLE///////////////////////////////////////////////////////////////////////////
	String surveyAreaIds;
	String vicinityIds;
	String crs = "EPSG:32632";
	String outputDirectory;
	PopulationType popType = PopulationType.complete;
	
	boolean useHouseholds = true;
	boolean useVehicles = false;
	boolean onlyWorkingDays = true;
	boolean useBuildings = true;
	
	int numberOfHouseholds = 0;
	
	Map<String,AdminUnitEntry> adminUnits;
	
	long randomSeed = 4711L;
	
	double scaleFactor = 1.0d;

	boolean writeDatabaseTables = false;
	boolean writeIntoDatahub = false;
	String dbNameSpace;
	
	boolean overwriteExistingFiles = false;
	
	//NON-CONFIGURABLE///////////////////////////////////////////////////////////////////////
	int localPort = 3200;
	final int remotePort = 5432;
	
	String sshUser;
	String sshPassword;
	String databaseUser = "postgres";
	String userPassword = "postgres";
	
	public enum PopulationType{dummy,commuter,complete,none};
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
		this.popType = PopulationType.complete;
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
		log.info("surveyAreaId(s):           " + this.surveyAreaIds);
		log.info("vicinityId(s):             " + this.vicinityIds);
		log.info("coordinateReferenceSystem: " + this.crs);
		log.info("outputDirectory:           " + this.outputDirectory);
		log.info("populationType:            " + this.popType.name());
		log.info("onlyWorkingDays:           " + this.onlyWorkingDays);
		log.info("useBuildings:              " + this.useBuildings);
		log.info("useMiDHouseholds:          " + this.useHouseholds);
		log.info("useMiDVehicles:            " + this.useVehicles);
		
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
	
}