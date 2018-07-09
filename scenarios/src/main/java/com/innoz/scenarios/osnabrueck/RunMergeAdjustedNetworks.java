package com.innoz.scenarios.osnabrueck;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

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
		
		config.network().setInputFile("/home/bmoehring/3connect/Scenarios/Fahrverbot_Brenner_und_NeumarktII/networkMerged_WPt_WInnercity_Wneumarkt.xml");
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		new NetworkCleaner().run(scenario.getNetwork());
		
//		shapefileLinksToAttributes(scenario, "/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/links_innercity.shp");
		
	}
		
	private static void shapefileLinksToAttributes(Scenario scenario, String shapefile) {
		
		ShapeFileReader shapeFileReader = new ShapeFileReader();
		shapeFileReader.readFileAndInitialize(shapefile);
		
		Collection<SimpleFeature> features = shapeFileReader.getFeatureSet();
		ArrayList<String> innercityIds = new ArrayList<>();
		for (SimpleFeature feature : features){
			System.out.println(feature.getAttribute("ID").toString() + " " + feature.getAttribute("origId"));
			innercityIds.add(feature.getAttribute("ID").toString());
		}
		int countinnercity= 0;
		int countouter = 0;
		
		for (Link link : scenario.getNetwork().getLinks().values()){
			String linkId = link.getId().toString();
			if (innercityIds.contains(linkId)){
				link.getAttributes().putAttribute("innercity", "yes");
				countinnercity++;
			} else {
				link.getAttributes().putAttribute("innercity", "no");
				countouter++;
			}
		}
		System.out.println("innercity " + countinnercity);
		System.out.println("outercity " + countouter);
		new NetworkWriter(scenario.getNetwork()).write("/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/networkMerged_WPt_WInnercity.xml.gz");

	}

}
