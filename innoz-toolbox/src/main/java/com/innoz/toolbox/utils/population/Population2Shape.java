package com.innoz.toolbox.utils.population;



import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.utils.GeometryUtils;

public class Population2Shape {

	private Population2Shape(){};
	
	public static void main(String[] args) {
		
		final Logger log = Logger.getLogger(Population2Shape.class);
		
		log.info("start");
		
		try {
			
			String input = "/home/bmoehring/3connect/TestNetworkRoutingWithAccess/input_positiv/config2.xml.gz";
			String output = "/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/";
			
			Config config = new Config();
			
			ConfigUtils.loadConfig( config, input );
			
			Scenario scenario = ScenarioUtils.loadScenario(config);
				
//			GeometryUtils.writeActivityLocationsToShapefile(scenario.getPopulation(), output + "facilities", "EPSG:4326");
			
			GeometryUtils.writeNetwork2Shapefile(scenario.getNetwork(), output , "EPSG:32326"); //"EPSG:4326"
			
//			List<String> icLinks = new ArrayList<String>();
//			icLinks.add("35454");

//			Network network = scenario.getNetwork();
//			for(Link l : network.getLinks().values()){
//				if (icLinks.contains(l.getId().toString())){
//					l.getAttributes().putAttribute("innercity", "yes");
//				} else {
//					l.getAttributes().putAttribute("innercity", "no");
//				}
//			}
			
//			new NetworkWriter(network).write(output + "mergedNetworkInnerCity.xml.gz");
		
			log.info("done");
			
		} catch ( Exception e){
			
			e.printStackTrace();
			
		}

	}

}
