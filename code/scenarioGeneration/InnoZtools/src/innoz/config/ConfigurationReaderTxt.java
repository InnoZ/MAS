package innoz.config;

import innoz.config.Configuration.PopulationType;

import java.io.BufferedReader;
import java.io.IOException;

import org.matsim.core.utils.io.IOUtils;

public class ConfigurationReaderTxt {

	//TAGS///////////////////////////////////////////////////////////////////////////////////
	private static final String SEP = "\t";
	private static final String COMMENT = "#";
	/////////////////////////////////////////////////////////////////////////////////////////

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final String SURVEY_AREA_IDS = "surveyAreaIds";
	private static final String VICINITY_IDS = "vicinityIds";
	private static final String CRS = "coordinateSystem";
	private static final String OUTPUT_DIR = "outputDirectory";
	private static final String POPULATION_TYPE = "populationType";
	private static final String SCALE_FACTOR = "scaleFactor";
	private static final String USE_BUILDINGS = "useBuildings";
	
	private static final String ONLY_WORKING_DAYS = "onlyWorkingDays";
	private static final String USE_HOUSEHOLDS = "useHouseholds";
	private static final String USE_VEHICLES = "useVehicles";
	private static final String NUMBER_OF_HH = "numberOfHouseholds"; //TODO write this into gadm.districs!
	
	private static final String LOCAL_PORT = "localPort";
	private static final String DB_SCHEMA_NAME = "databaseSchemaName";
	private static final String WRITE_DB_OUTPUT = "writeTables";
	private static final String WRITE_INTO_DATAHUB = "intoMobilityDatahub";
	
	private static final String RANDOM_SEED = "randomSeed";
	private static final String OVERWRITE_FILES = "overwriteExistingFiles";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private final Configuration configuration;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	ConfigurationReaderTxt(final Configuration config){
		
		this.configuration = config;
		
	}
	
	/**
	 * 
	 * Reads in the given text file and initializes the parameters according to its content.
	 * 
	 * @param file Text file containing the configuration parameters.
	 */
	void read(String file){
		
		BufferedReader reader = IOUtils.getBufferedReader(file);
		
		String line = null;
		
		try {
			
			while((line = reader.readLine()) != null){
				
				if(!line.startsWith(COMMENT)){
					
					String[] lineParts = line.split(SEP);
					
					if(SURVEY_AREA_IDS.equals(lineParts[0])){
						
						this.configuration.surveyAreaIds = lineParts[1];
						
					} else if(VICINITY_IDS.equals(lineParts[0])){
						
						this.configuration.vicinityIds = lineParts[1];
						
					} else if(CRS.equals(lineParts[0])){
						
						this.configuration.crs = lineParts[1];
						
					} else if(OUTPUT_DIR.equals(lineParts[0])){
						
						this.configuration.outputDirectory = lineParts[1];
						
					} else if(POPULATION_TYPE.equals(lineParts[0])){
						
						this.configuration.popType = PopulationType.valueOf(lineParts[1]);
						
					} else if(USE_HOUSEHOLDS.equals(lineParts[0])){
						
						this.configuration.useHouseholds = Boolean.parseBoolean(lineParts[1]);
						
					} else if(ONLY_WORKING_DAYS.equals(lineParts[0])){
						
						this.configuration.onlyWorkingDays = Boolean.parseBoolean(lineParts[1]);
						
					} else if(USE_VEHICLES.equals(lineParts[0])){
						
						this.configuration.useVehicles = Boolean.parseBoolean(lineParts[1]);
						
					} else if(USE_BUILDINGS.equals(lineParts[0])){
						
						this.configuration.useBuildings = Boolean.parseBoolean(lineParts[1]);
						
					} else if(LOCAL_PORT.equals(lineParts[0])){
						
						this.configuration.localPort = Integer.parseInt(lineParts[1]);
						
					} else if(NUMBER_OF_HH.equals(lineParts[0])){
						
						this.configuration.numberOfHouseholds = Integer.parseInt(lineParts[1]);
						
					} else if(RANDOM_SEED.equals(lineParts[0])){
						
						this.configuration.randomSeed = Long.parseLong(lineParts[1]);
						
					} else if(DB_SCHEMA_NAME.equals(lineParts[0])){
						
						this.configuration.dbNameSpace = lineParts[1];
						
					} else if(WRITE_INTO_DATAHUB.equals(lineParts[0])){
						
						this.configuration.writeIntoDatahub = Boolean.parseBoolean(lineParts[1]);
						
					} else if(WRITE_DB_OUTPUT.equals(lineParts[0])){
						
						this.configuration.writeDatabaseTables = Boolean.parseBoolean(lineParts[1]);
						
					} else if(OVERWRITE_FILES.equals(lineParts[0])){
						
						this.configuration.overwriteExistingFiles = Boolean.parseBoolean(lineParts[1]);
						
					} else if(SCALE_FACTOR.equals(lineParts[0])){
						
						this.configuration.scaleFactor = Double.parseDouble(lineParts[1]);
						
					}
					
				}
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}