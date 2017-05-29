package com.innoz.toolbox.scenarioGeneration.network;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

public class CombineNetworks {

	public static void main(String args[]){
		
		String base = "/home/dhosse/osGtfs/";
		CombineNetworks.writeToFile(base + "network.xml.gz", base + "transitNetwork.xml.gz", base + "mergedNetwork.xml.gz");
		
	}
	
	public static void writeToFile(String networkFile1, String networkFile2, String outFile){
		
		Scenario s1 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(s1.getNetwork()).readFile(networkFile1);
		
		Scenario s2 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(s2.getNetwork()).readFile(networkFile2);
		
		for(Node n : s2.getNetwork().getNodes().values()){
			
			Node nn = s1.getNetwork().getFactory().createNode(n.getId(), n.getCoord());
			s1.getNetwork().addNode(nn);
			
		}
		for(Link l : s2.getNetwork().getLinks().values()){
		
			Link ll = s1.getNetwork().getFactory().createLink(l.getId(), l.getFromNode(), l.getToNode());
			ll.setAllowedModes(l.getAllowedModes());
			ll.setCapacity(l.getCapacity());
			ll.setFreespeed(l.getFreespeed());
			ll.setLength(l.getLength());
			ll.setNumberOfLanes(l.getNumberOfLanes());
			s1.getNetwork().addLink(ll);
		
		}
		
		new NetworkWriter(s1.getNetwork()).write(outFile);
		
	}
	
}
