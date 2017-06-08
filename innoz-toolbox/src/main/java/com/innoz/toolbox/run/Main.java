package com.innoz.toolbox.run;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.core.controler.OutputDirectoryLogging;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.run.controller.task.ConfigCreatorTask;
import com.innoz.toolbox.run.controller.task.DemandGenerationTask;
import com.innoz.toolbox.run.controller.task.NetworkGenerationTask;
import com.innoz.toolbox.run.controller.task.WriteOutputTask;

/**
 * 
 * Starting point for a shell-based version of the scenario generation framework. <br>
 * At the moment, the execution is a minimal example of the scenario generation. When invoked, the class needs four runtime arguments,
 * namely an id for the survey area, a (forecast) year, the output path and the rails environment (one of: development,
 * production, test). dhosse 05/17, 07/17 <br>
 * 
 * java -cp <path-to-jar> com.innoz.toolbox.run.Main args1 args2 args3
 * 
 * @author dhosse
 *
 */
public class Main {

	private static final Logger log = Logger.getLogger(Main.class);
	
	public static void main(String args[]) throws IOException {
		
		Logger.getLogger(org.matsim.matrices.Matrix.class).setLevel(Level.OFF);
		
		// If at least four runtime arguments were given, we can go on with our execution
		if(args.length > 3){

			// Set the name of the scenario (combination of gkz and forecast year)
			String scenarioName = args[0] + "_" + args[1];
			
			String outputDirectory = args[2] + "/" + scenarioName + "/";
			
			// Set the output directory according to the scenario name
			Controller.configuration().misc().setOutputDirectory(outputDirectory);
			
			Files.createDirectories(Paths.get(outputDirectory));
			
			OutputDirectoryLogging.initLoggingWithOutputDirectory(outputDirectory);
			
			log.info("Starting scenario geneartion with " + Main.class.getSimpleName());
			
			// Create a new area set containing a single county
			// The other parameters are set to more or less meaningful default values until we implement switches
			AreaSet set = new AreaSet();
			set.setIds(args[0]);
			set.setIsSurveyArea(true);
			set.setNetworkLevel(6);
			set.setPopulationSource(PopulationSource.SURVEY);
			Controller.configuration().scenario().addAreaSet(set);
			Controller.configuration().surveyPopulation().setUseHouseholds(false);

			log.info("Added survey area with AGKZ '" + args[0] + "'");
			
			Controller.configuration().psql().setPsqlPort(9999);
			
			// MATSim needs a Cartesian coordinate system that measures distances in meters
			Controller.configuration().misc().setCoordinateSystem("EPSG:32632");
			
			// Set the scenario year to whatever was passed in the second argument
			int forecastYear = Integer.parseInt(args[1]);
			Controller.configuration().scenario().setYear(forecastYear);
			
			Controller.configuration().psql().setPsqlPort(9999);
			
			log.info("Scenario year set to " + args[1]);
			
			log.info("Starting controller...");
			
			// Add all the necessary tasks to the controller queue
			Controller.submit(new ConfigCreatorTask.Builder(Controller.scenario()).build());
			Controller.submit(new NetworkGenerationTask.Builder(Controller.configuration(), Controller.scenario()).build());
			Controller.submit(new DemandGenerationTask.Builder(Controller.configuration(), Controller.scenario()).build());
			Controller.submit(new WriteOutputTask.Builder(scenarioName, args[3]).build());
			
			// Start the actual execution
			Controller.run();
			
		} else {

			// Not enough runtime arguments to continue
			
			log.error("You must pass at least four arguments to the controller ('region id', 'scenario year',"
					+ "'output directory' and 'Rails environment'.");
			throw new RuntimeException();
			
		}
		
		OutputDirectoryLogging.closeOutputDirLogging();
		
	}
	
}