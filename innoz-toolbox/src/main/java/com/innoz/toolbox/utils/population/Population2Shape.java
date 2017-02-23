package com.innoz.toolbox.utils.population;



import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.PopulationToCsvWriter;

public class Population2Shape {

	public static void main(String[] args) {
		
		final Logger log = Logger.getLogger(Population2Shape.class);
		
		log.info("start");
		
		try {
			
			String input = "/home/bmoehring/scenarios/heidelberg/";
			String output = input + "validation/";
			
			Config config = new Config();
			
			ConfigUtils.loadConfig( config, input + "config.xml" );
			
			Scenario scenario = ScenarioUtils.loadScenario(config);
				
			GeometryUtils.writeActivityLocationsToShapefile(scenario.getPopulation(), output + "facilities", "EPSG:4326");
//			
			GeometryUtils.writeNetwork2Shapefile(scenario.getNetwork(), output, "EPSG:4326");
			
			PopulationToCsvWriter.writePopulation2Csv(scenario.getPopulation(), output);
		
			log.info("done");
			
		} catch ( Exception e){
			
			e.printStackTrace();
			
		}

	}

}
