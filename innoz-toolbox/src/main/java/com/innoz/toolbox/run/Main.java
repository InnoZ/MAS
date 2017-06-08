package com.innoz.toolbox.run;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigWriter;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.run.controller.task.ConfigCreatorTask;
import com.innoz.toolbox.run.controller.task.DemandGenerationTask;
import com.innoz.toolbox.run.controller.task.NetworkGenerationTask;
import com.innoz.toolbox.run.controller.task.WriteOutputTask;
import com.innoz.toolbox.scenarioGeneration.config.InitialConfigCreator;
import com.innoz.toolbox.utils.GlobalNames;

/**
 * 
 * Starting point for a shell-based version of the scenario generation framework. This class be used as starting point for
 * a shell execution of the framework. <br>
 * At the moment, the execution is a minimal example of the scenario generation. When invoked, the class needs three runtime arguments,
 * namely an id for the survey area, a (forecast) year and the output path. dhosse 05/17 <br>
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
		
		// If at least two runtime arguments were given, we can go on with our execution
		if(args.length > 1){
			
			// Create a new area set containing a single county
			// The other parameters are set to more or less meaningful default values until we implement switches
			AreaSet set = new AreaSet();
			set.setIds(args[0]);
			set.setIsSurveyArea(true);
			set.setNetworkLevel(6);
			set.setPopulationSource(PopulationSource.SURVEY);
			Controller.configuration().scenario().addAreaSet(set);
			Controller.configuration().surveyPopulation().setUseHouseholds(false);
			Controller.configuration().scenario().setScaleFactor(0.001);
			Controller.configuration().psql().setPsqlPort(9999);
			Controller.configuration().psql().setPsqlUser("bmoehring");
			Controller.configuration().psql().setPsqlPassword("yBGJ3NKeus");
			
			// MATSim needs a Cartesian coordinate system that measures distances in meters
			Controller.configuration().misc().setCoordinateSystem("EPSG:32632");
			
			// Set the scenario year to whatever was passed in the second argument
			int forecastYear = Integer.parseInt(args[1]);
			Controller.configuration().scenario().setYear(forecastYear);
			
			// Set the name of the scenario (combination of gkz and forecast year)
			String scenarioName = args[0] + "_" + args[1];
			
			// Set the output directory according to the scenario name
			Controller.configuration().misc().setOutputDirectory(args[2] + "/" + scenarioName + "/");
			
			log.info("Starting controller...");
			
			// Add all the necessary tasks to the controller queue
			Controller.submit(new ConfigCreatorTask.Builder(Controller.scenario()).build());
			Controller.submit(new NetworkGenerationTask.Builder(Controller.configuration(), Controller.scenario()).build());
			Controller.submit(new DemandGenerationTask.Builder(Controller.configuration(), Controller.scenario()).build());
			Controller.submit(new WriteOutputTask.Builder(scenarioName).build());
			
			// Start the actual execution
			Controller.run();
			
		} else {

			// Not enough runtime arguments to continue
			throw new RuntimeException("You must pass at least two arguments to the controller ('region id' and 'scenario year'.");
			
		}
		
	}
	
}