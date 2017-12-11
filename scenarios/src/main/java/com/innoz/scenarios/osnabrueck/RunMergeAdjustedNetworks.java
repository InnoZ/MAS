package com.innoz.scenarios.osnabrueck;

import java.awt.List;
import java.util.LinkedList;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

/**
 * 
 * Merging the two previously adjusted networks. Deleting all links from network Verbrenner, that aren't contained in network Neumarkt.
 * 
 * @author bmoehring
 *
 */
public class RunMergeAdjustedNetworks {

	public static void main(String[] args) {
		
		Config config = ConfigUtils.createConfig();
		
		config.network().setInputFile("/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/mergedNetworkInnerCity.xml");
		
		Scenario scenarioVerbrenner = ScenarioUtils.loadScenario(config);
		
		Network verbrenner = scenarioVerbrenner.getNetwork();
		
		config.network().setInputFile("/home/bmoehring/3connect/Scenarios/NeumarktII_Neumarkt_Fußgängerzone(kein IV)/mergedNetwork_links_neumarktANDwittekindstraße_deleted.xml");

		Scenario scenarioNeumarkt = ScenarioUtils.loadScenario(config);
		
		Network neumarkt = scenarioNeumarkt.getNetwork();
		
		LinkedList<Id<Link>> remove = new LinkedList<Id<Link>>();
		
		for (Link link : verbrenner.getLinks().values()){
			if (neumarkt.getLinks().containsKey(link.getId())){
				continue;
			} else {
				remove.add(link.getId());
				System.out.println("remove Link: " +  link.getId());
			}
		}
		
		for (Id<Link> linkId : remove){
			verbrenner.removeLink(linkId);
		}
		
		new NetworkWriter(verbrenner).write("/home/bmoehring/3connect/Scenarios/Fahrverbot_Brenner_und_NeumarktII/network_verbrenner_and_neumarkt.xml");
		
		
	}

}
