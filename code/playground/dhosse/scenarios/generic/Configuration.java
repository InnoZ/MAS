package playground.dhosse.scenarios.generic;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.io.IOUtils;

/**
 * 
 * @author dhosse
 *
 */
public class Configuration {

	private static final Logger log = Logger.getLogger(Configuration.class);
	public enum PopulationType{dummy,commuter,complete};
	
	//TAGS
	private static final String SEP = "\t";
	private static final String COMMENT = "#";
	
	private static final String SURVEY_AREA_IDS = "surveyAreaIds";
	private static final String CRS = "coordinateSystem";
	private static final String WORKING_DIR = "workingDirectory";
	private static final String POPULATION_TYPE = "populationType";
	private static final String USE_BUILDINGS = "useBuildings";
	
	private static final String ONLY_WORKING_DAYS = "onlyWorkingDays";
	private static final String USE_HOUSEHOLDS = "useHouseholds";
	private static final String USE_CARS = "useCars";
	private static final String SQL_QUERY_MID = "midQuery"; //TODO find a way to remove this!
	
	private static final String DATABASE_USER = "databaseUser";
	private static final String DATABASE_PASSWD = "password";
	private static final String LOCAL_PORT = "localPort";
	
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS
	private String[] surveyAreaIds;
	private String crs;
	private String workingDirectory;
	private PopulationType popType;
	
	private boolean useHouseholds = false;
	private boolean useCars = false;
	private boolean onlyWorkingDays = false;
	private boolean useBuildings = false;
	
	private int localPort = 0;
	private final int remotePort = 5432;
	
	private String databaseUser;
	private String userPassword;
	
	private String query;

	//////////////////////////////////////////////////////////////////////////////////////////////////	
	
	public Configuration(String file){
		
		readConfigurationFile(file);
		validate();
		
	}
	
	private void readConfigurationFile(String file){
		
		BufferedReader reader = IOUtils.getBufferedReader(file);
		
		String line = null;
		
		try {
			
			while((line = reader.readLine()) != null){
				
				if(!line.startsWith(COMMENT)){
					
					String[] lineParts = line.split(SEP);
					
					if(SURVEY_AREA_IDS.equals(lineParts[0])){
						
						this.surveyAreaIds = lineParts[1].split(",");
						
					} else if(CRS.equals(lineParts[0])){
						
						this.crs = lineParts[1];
						
					} else if(WORKING_DIR.equals(lineParts[0])){
						
						this.workingDirectory = lineParts[1];
						
					} else if(POPULATION_TYPE.equals(lineParts[0])){
						
						this.popType = PopulationType.valueOf(lineParts[1]);
						
					} else if(USE_HOUSEHOLDS.equals(lineParts[0])){
						
						this.useHouseholds = Boolean.parseBoolean(lineParts[1]);
						
					} else if(DATABASE_USER.equals(lineParts[0])){
						
						this.databaseUser = lineParts[1];
						
					} else if(DATABASE_PASSWD.equals(lineParts[0])){
						
						this.userPassword = lineParts[1];
						
					} else if(SQL_QUERY_MID.equals(lineParts[0])){
						
						this.query = lineParts[1];
						
					} else if(ONLY_WORKING_DAYS.equals(lineParts[0])){
						
						this.onlyWorkingDays = Boolean.parseBoolean(lineParts[1]);
						
					} else if(USE_CARS.equals(lineParts[0])){
						
						this.useCars = Boolean.parseBoolean(lineParts[1]);
						
					} else if(USE_BUILDINGS.equals(lineParts[0])){
						
						this.useBuildings = Boolean.parseBoolean(lineParts[1]);
						
					} else if(LOCAL_PORT.equals(lineParts[0])){
						
						this.localPort = Integer.parseInt(lineParts[1]);
						
					}
					
				}
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Validates the configuration. Only errors that may cause exceptions are taken into account here.
	 */
	private void validate(){
		
		boolean validationError = false;
		
		if(this.surveyAreaIds.length < 1){
			
			validationError = true;
			log.error("You must specify at least one survey area by its id!");
			log.info("See table gadm.districts in mobility database for information.");
			
		}
		
		if(!this.useHouseholds && this.useCars){
			
			validationError = true;
			log.error("You disabled the use of households data but enabled cars. This won't work!");
			
		}
		
		if(validationError){
			
			throw new RuntimeException("Invalid configuration! Shutting down...");
			
		}
		
	}
	
	public String[] getSurveyAreaIds() {
		return this.surveyAreaIds;
	}

	public String getCrs() {
		return crs;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}
	
	public PopulationType getPopulationType() {
		return this.popType;
	}
	
	public boolean isUsingHouseholds(){
		return this.useHouseholds;
	}
	
	public String getDatabaseUsername(){
		return this.databaseUser;
	}
	
	public String getPassword(){
		return this.userPassword;
	}

	public String getSqlQuery() {
		return query;
	}
	
	public boolean isOnlyUsingWorkingDays(){
		return this.onlyWorkingDays;
	}
	
	public boolean isUsingCars(){
		return this.useCars;
	}
	
	public boolean isUsingBuildings(){
		return this.useBuildings;
	}
	
	public int getLocalPort(){
		return this.localPort;
	}
	
	public int getRemotePort(){
		return this.remotePort;
	}
	
	public void dumpSettings(){
		
		log.info("Dump of configuration settings:");
		log.info("surveyAreaId:              " + CollectionUtils.arrayToString(this.surveyAreaIds));
		log.info("coordinateReferenceSystem: " + this.crs);
		log.info("workingDirectory:          " + this.workingDirectory);
		log.info("populationType:            " + this.popType.name());
		log.info("onlyWorkingDays:           " + this.onlyWorkingDays);
		log.info("useBuildings:              " + this.useBuildings);
		log.info("useMiDHouseholds:          " + this.useHouseholds);
		log.info("useMiDVehicles:            " + this.useCars);
		
	}
	
}