package com.innoz.toolbox.utils.population;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;

public class PopulationToCsvWriter {
	
	/**
	 * 
	 * Writes all agents into a csv file, currently containing the fields:
	 * personID;age;sex;hasLicense;car_avail;employed;actCount;
	 * 
	 * @author bmoehring
	 *
	 */

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(PopulationToCsvWriter.class);
	private static final String SPACE = " ";
	private static final String TRIPLE_SPACE = "   ";
	private static final String TAB = "\t";
	private static final String END = "END";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	// No instance!
	private PopulationToCsvWriter(){};
	
	public static void writePopulation2Csv(Population population, String filepath) throws FileNotFoundException{
		
		BufferedWriter bw = null;
		
		String file = filepath + "/population.csv";
		int personCount = 0;
		
		log.info("reading population");
		
		try {
			
			bw = new BufferedWriter(new FileWriter(file));
			
			String line = "personID;"
					+ "age;"
					+ "sex;"
					+ "hasLicense;"
					+ "car_avail;"
					+ "employed;"
					+ "actcount; \n";
			
			bw.write(line);
			
			
			for(Person person : population.getPersons().values()){
				
				line = null;
				Map<String, Object> ca = person.getCustomAttributes();
				
				line = person.getId().toString() + ";"
						+ ca.get(com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_SEX).toString() + ";"
						+ ca.get(com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_AGE).toString() + ";"
						+ ca.get(com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_LICENSE).toString() + ";"
						+ ca.get(com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_CAR_AVAIL).toString() + ";"
						+ String.valueOf(ca.get(com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_EMPLOYED)) + ";"
						+ person.getSelectedPlan().getPlanElements().size() +  ";\n";
				
				if (line != null){

					bw.write(line);
					personCount ++;
					
				}	
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			log.error("Point feature collection is empty and thus there is no file to write...");
			
		} finally {
			try {
				
				if (bw != null){
					
					bw.flush();
					bw.close();
					
					log.info(personCount + " persons written to " + file);
					
				}
				
			} catch ( IOException e) {
				
				e.printStackTrace();
				
			}
		}
	}

}
