package com.innoz.toolbox.scenarioGeneration.transit;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.VehicleReaderV1;

public class RunPTAdjustmentOS {

	public static void main(String[] args) {
		
		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		TransitScheduleReader tsr = new TransitScheduleReader(scenario);
		tsr.readFile(filename);
		NetworkReaderMatsimV2 nwr = new NetworkReaderMatsimV2(scenario.getNetwork());
		nwr.readFile(filename);
		VehicleReaderV1 vhr = new VehicleReaderV1(scenario.getTransitVehicles());
		vhr.readFile(filename);
		
		Network network = scenario.getNetwork();
		
		{
			Node n1 = network.getFactory().createNode(id, coord);
			Node n1 = network.getFactory().createNode(id, coord);
			
			Link link = network.getFactory().createLink(id, fromNode, toNode);
			link.setAllowedModes(modes);
			link.setFreespeed(freespeed);
			network.addLink(link);
			
			TransitSchedule schedule = scenario.getTransitSchedule();
			
			TransitLine line = schedule.getFactory().createTransitLine(lineId);
					
			TransitRoute route ;
			
//			evtl neue stop facility
			TransitRouteStop trs;
			route.getStops().add(trs);
			
			line.addRoute(route);
			
			TransitStopFacility newStop = schedule.getFactory().createTransitStopFacility(facilityId, coordinate, blocksLane);
			newStop.setLinkId(linkId);
			schedule.addStopFacility(newStop);
			schedule.addTransitLine(line);	
		}
		
		
		
	}

}
