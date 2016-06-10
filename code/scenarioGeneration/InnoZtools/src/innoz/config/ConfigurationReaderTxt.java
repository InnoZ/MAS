package innoz.config;

import innoz.config.Configuration.AdminUnitEntry;
import innoz.config.Configuration.PopulationType;

import java.io.BufferedReader;
import java.io.IOException;

import org.matsim.core.utils.io.IOUtils;

public class ConfigurationReaderTxt {

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
				
				if(!line.startsWith(Configuration.COMMENT)){
					
					String[] lineParts = line.split(Configuration.SEP);
					
					if(Configuration.SURVEY_AREA_IDS.equals(lineParts[0])){
						
						this.configuration.surveyAreaIds = lineParts[1];
						
					} else if(Configuration.VICINITY_IDS.equals(lineParts[0])){
						
						this.configuration.vicinityIds = lineParts[1];
						
					} else if(Configuration.CRS.equals(lineParts[0])){
						
						this.configuration.crs = lineParts[1];
						
					} else if(Configuration.OUTPUT_DIR.equals(lineParts[0])){
						
						this.configuration.outputDirectory = lineParts[1];
						
					} else if(Configuration.POPULATION_TYPE.equals(lineParts[0])){
						
						this.configuration.popType = PopulationType.valueOf(lineParts[1]);
						
					} else if(Configuration.USE_HOUSEHOLDS.equals(lineParts[0])){
						
						this.configuration.useHouseholds = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.ONLY_WORKING_DAYS.equals(lineParts[0])){
						
						this.configuration.onlyWorkingDays = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.USE_VEHICLES.equals(lineParts[0])){
						
						this.configuration.useVehicles = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.USE_BUILDINGS.equals(lineParts[0])){
						
						this.configuration.useBuildings = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.LOCAL_PORT.equals(lineParts[0])){
						
						this.configuration.localPort = Integer.parseInt(lineParts[1]);
						
					} else if(Configuration.NUMBER_OF_HH.equals(lineParts[0])){
						
						this.configuration.numberOfHouseholds = Integer.parseInt(lineParts[1]);
						
					} else if(Configuration.RANDOM_SEED.equals(lineParts[0])){
						
						this.configuration.randomSeed = Long.parseLong(lineParts[1]);
						
					} else if(Configuration.DB_SCHEMA_NAME.equals(lineParts[0])){
						
						this.configuration.dbNameSpace = lineParts[1];
						
					} else if(Configuration.WRITE_INTO_DATAHUB.equals(lineParts[0])){
						
						this.configuration.writeIntoDatahub = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.WRITE_DB_OUTPUT.equals(lineParts[0])){
						
						this.configuration.writeDatabaseTables = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.OVERWRITE_FILES.equals(lineParts[0])){
						
						this.configuration.overwriteExistingFiles = Boolean.parseBoolean(lineParts[1]);
						
					} else if(Configuration.SCALE_FACTOR.equals(lineParts[0])){
						
						this.configuration.scaleFactor = Double.parseDouble(lineParts[1]);
						
					}
					
				}
				
			}
			
//			this.configuration.adminUnits.put(new AdminUnitEntry(this.configuration.getSurveyAreaIds(), this.configuration.getNumberOfHouseholds(), null));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
}