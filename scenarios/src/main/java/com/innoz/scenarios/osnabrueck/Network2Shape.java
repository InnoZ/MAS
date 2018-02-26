package com.innoz.scenarios.osnabrueck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.network.io.NetworkReaderMatsimV1;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import com.innoz.toolbox.utils.GeometryUtils;

public class Network2Shape {

	public static void main(String[] args) {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new NetworkReaderMatsimV2(scenario.getNetwork()).readFile("/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/mergedNetworkInnerCity.xml.gz");
		new TransitScheduleReader(scenario).readFile("/home/bmoehring/3connect/Scenarios/Ausbau transit/scheduleSimplifiedWithRingbus.xml.gz");
		
//		List<TransitStopFacility> stopFacilities = new ArrayList<>();
//		List<Id<Link>> ringbusLinkIdList = new ArrayList<>();
//		
//		TransitLine ringbus = scenario.getTransitSchedule().getTransitLines().get(Id.create("100000", TransitLine.class));
//		
//		for (TransitRoute ringbusRoute : ringbus.getRoutes().values()){
//			for (Id<Link> linkId : ringbusRoute.getRoute().getLinkIds()){
//				ringbusLinkIdList.add(linkId);
//			}
//			for (TransitRouteStop stop : ringbusRoute.getStops()){
//				stopFacilities.add(stop.getStopFacility());
//			}
//		}
//		int links = 0;
//		for (Link link : scenario.getNetwork().getLinks().values()){
//			if (ringbusLinkIdList.contains(link.getId())){
//				link.getAttributes().putAttribute("ringbus", "yes");
//				System.out.println(link.getId() + " " + link.getAttributes().getAttribute("ringbus").toString());
//				links ++;  
//				
//			} else {
//				link.getAttributes().putAttribute("ringbus", "no");
//			}
//		}
//		System.out.println(links);
				
//		GeometryUtils.writeNetwork2Shapefile(scenario.getNetwork(), "/home/bmoehring/3connect/Scenarios/Fahrverbot_Verbrenner/" , "EPSG:32632");
		
		GeometryUtils.writeStopFacilities2Shapefile(scenario.getTransitSchedule().getFacilities().values(), "/home/bmoehring/3connect/Scenarios/Ausbau transit/", "EPSG:32632");
	
	}

}
