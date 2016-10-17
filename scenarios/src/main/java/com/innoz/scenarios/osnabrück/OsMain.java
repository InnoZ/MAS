package com.innoz.scenarios.osnabrück;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.scenarioGeneration.network.NetworkModification;

public class OsMain {

	public static void main(String args[]){
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/dhosse/osGtfs/mergedNetwork.xml.gz");
		NetworkModification.addCycleways(scenario.getNetwork());
		new NetworkWriter(scenario.getNetwork()).write("/home/dhosse/networkWithCycleways.xml.gz");
		
	}
	
}