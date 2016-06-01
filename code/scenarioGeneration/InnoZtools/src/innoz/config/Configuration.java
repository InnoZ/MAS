package innoz.config;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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
	static final String SURVEY_AREA_IDS = "surveyAreaIds";
	static final String VICINITY_IDS = "vicinityIds";
	static final String CRS = "coordinateSystem";
	static final String OUTPUT_DIR = "outputDirectory";
	static final String POPULATION_TYPE = "populationType";
	static final String SCALE_FACTOR = "scaleFactor";
	static final String USE_BUILDINGS = "useBuildings";
	
	static final String ONLY_WORKING_DAYS = "onlyWorkingDays";
	static final String USE_HOUSEHOLDS = "useHouseholds";
	static final String USE_VEHICLES = "useVehicles";
	static final String NUMBER_OF_HH = "numberOfHouseholds"; //TODO write this into gadm.districs!
	
	static final String LOCAL_PORT = "localPort";
	static final String DB_SCHEMA_NAME = "databaseSchemaName";
	static final String WRITE_DB_OUTPUT = "writeTables";
	static final String WRITE_INTO_DATAHUB = "intoMobilityDatahub";
	
	static final String RANDOM_SEED = "randomSeed";
	static final String OVERWRITE_FILES = "overwriteExistingFiles";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	String surveyAreaIds;
	String vicinityIds;
	String crs;
	String outputDirectory;
	PopulationType popType;
	
	Set<AdminUnitEntry> adminUnits;
	
	boolean useHouseholds = false;
	boolean useVehicles = false;
	boolean onlyWorkingDays = false;
	boolean useBuildings = false;
	
	int localPort = 0;
	final int remotePort = 5432;
	
	int numberOfHouseholds = 0;
	
	String sshUser;
	String sshPassword;
	String databaseUser;
	String userPassword;
	
	long randomSeed = 4711L;
	
	double scaleFactor = 1.0d;

	boolean writeDatabaseTables = false;
	boolean writeIntoDatahub = false;
	String dbNameSpace;
	
	boolean overwriteExistingFiles = false;
	
	public enum PopulationType{dummy,commuter,complete};
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	/**
	 * 
	 * Creates a new configuration from the given file.
	 * 
	 * @param file Text file containing the configuration parameters.
	 */
	public Configuration(String file){
		
		this.adminUnits = new HashSet<Configuration.AdminUnitEntry>();
		
		if(file.endsWith(".txt")){
			new ConfigurationReaderTxt(this).read(file);
		} else if(file.endsWith(".xml")){
			new ConfigurationReaderXml(this).read(file);
		}
		
		validate();
		
	}
	
	Configuration(){};
	
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
		log.info("workingDirectory:          " + this.outputDirectory);
		log.info("populationType:            " + this.popType.name());
		log.info("onlyWorkingDays:           " + this.onlyWorkingDays);
		log.info("useBuildings:              " + this.useBuildings);
		log.info("useMiDHouseholds:          " + this.useHouseholds);
		log.info("useMiDVehicles:            " + this.useVehicles);
		
	}
	
	public static class AdminUnitEntry{
		
		String id;
		int numberOfHouseholds;
		
		AdminUnitEntry(String id, int nHouseholds){
			this.id = id;
			this.numberOfHouseholds = nHouseholds;
		}
		
		public String getId(){
			return this.id;
		}
		
		public int getNumberOfHouseholds(){
			return this.numberOfHouseholds;
		}
		
	}
	
	public Set<AdminUnitEntry> getAdminUnitEntries(){
		return this.adminUnits;
	}
	
}