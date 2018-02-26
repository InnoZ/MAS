package com.innoz.scenarios.osnabrueck;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;

public class OSMergeNetworkAttributes {

	public static void main(String[] args) {
		
		Scenario scenarioInnerCity = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		NetworkReaderMatsimV2 nwr1 = new NetworkReaderMatsimV2(scenarioInnerCity.getNetwork());
		nwr1.readFile("/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/mergedNetworkInnerCity.xml.gz");
		
		Scenario scenarioPositiv = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		NetworkReaderMatsimV2 nwr2 = new NetworkReaderMatsimV2(scenarioPositiv.getNetwork());
		nwr2.readFile("/home/bmoehring/3connect/3connect_positiv/input_positiv/networkMerged_positivWPt.xml.gz");
		
		for(Link linkPositiv : scenarioPositiv.getNetwork().getLinks().values()){
			
			Link linkInnerCity = scenarioInnerCity.getNetwork().getLinks().get(linkPositiv.getId());
			Object innercity;
			try {
				innercity = linkInnerCity.getAttributes().getAttribute("innercity");
				System.out.println(linkPositiv.getId().toString() + " " + linkInnerCity.getId().toString() + " " + innercity.toString());
			} catch (NullPointerException e) {
				innercity = "no";
				System.out.println(linkPositiv.getId().toString() + " ---> " + innercity.toString());
			}
			linkPositiv.getAttributes().putAttribute("innercity", innercity);
			
		}
		
		new NetworkWriter(scenarioPositiv.getNetwork()).write("/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/networkMerged_positivWPt_WInnercity.xml.gz");

	}

}
