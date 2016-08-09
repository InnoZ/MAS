package com.innoz.toolbox.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * 
 * Entry point for a minimal execution of the MATSim controler. To execute it, just run the main method.
 * No additional settings are made aside from the settings in the config file given as the only argument.
 * 
 * @author dhosse
 *
 */
public class RunMatsim {

	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig(args[0]);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		controler.run();
		
	}
	
}
