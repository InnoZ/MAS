package com.innoz.toolbox.scenarioGeneration.transit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.NetworkReaderMatsimV2;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleReaderV1;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehicleWriterV1;

public class RunPTAdjustmentOS {
	
	private static int nodeIdCount = 1000000;
	private static int linkIdCount = 10000;

	public static void main(String[] args) {
		
		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		TransitScheduleReader tsr = new TransitScheduleReader(scenario);
		tsr.readFile("/home/dhosse/scenarios/3connect/scheduleSimplified.xml.gz");
		NetworkReaderMatsimV2 nwr = new NetworkReaderMatsimV2(scenario.getNetwork());
		nwr.readFile("/home/dhosse/scenarios/3connect/scenarios/3connect/mergedNetwork_trend.xml");
		VehicleReaderV1 vhr = new VehicleReaderV1(scenario.getTransitVehicles());
		vhr.readFile("/home/dhosse/scenarios/3connect/transitVehiclesFiltered.xml.gz");
		
		scenario.getTransitVehicles().addVehicleType(VehicleUtils.getDefaultVehicleType());
		
		Network network = scenario.getNetwork();
		TransitSchedule schedule = scenario.getTransitSchedule();
		
		{	
			Node nLutherkirche = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(435567, 5790431));
			network.addNode(nLutherkirche);
			Node nBuenderStrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(436015, 5790594));
			network.addNode(nBuenderStrasse);
			Node nHannoverscheStrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 	new Coord(437069, 5790432));
			network.addNode(nHannoverscheStrasse);
			Node nKarmannstrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(437239, 5790806));
			network.addNode(nKarmannstrasse);
			Node nJeggenerWeg = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(437586, 5791698));
			network.addNode(nJeggenerWeg);
			Node nRosenburg = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436937, 5792025));
			network.addNode(nRosenburg);
			Node nTannenburgstrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 	new Coord(436960, 5792418));
			network.addNode(nTannenburgstrasse);
			Node nEbertallee = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436974, 5792653));
			network.addNode(nEbertallee);
			Node nSchuetzenstrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 	new Coord(437002, 5793022));
			network.addNode(nSchuetzenstrasse);
			Node nLuhrmannsweg = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(437184, 5793413));
			network.addNode(nLuhrmannsweg);
			Node nWiderhall = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436329, 5794088));
			network.addNode(nWiderhall);
			Node nEberleplatz = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(435120, 5795486));
			network.addNode(nEberleplatz);
			Node nElbestrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(433726, 5794319));
			network.addNode(nElbestrasse);
			
			List<Link> newLinks = new ArrayList<Link>();
			Link link;
			link = network.getFactory().createLink(Id.createLinkId("pt_1000007"), 
					network.getNodes().get(Id.createNodeId("pt_0170102")), network.getNodes().get(Id.createNodeId("pt_0179322")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000010"), 
					network.getNodes().get(Id.createNodeId("pt_0179323")), network.getNodes().get(Id.createNodeId("pt_0170330")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000014"), 
					network.getNodes().get(Id.createNodeId("pt_0170374")), nLutherkirche);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000015"), 
					nLutherkirche, nBuenderStrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000016"), 
					nBuenderStrasse, network.getNodes().get(Id.createNodeId("pt_0170357")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000017"), 
					network.getNodes().get(Id.createNodeId("pt_0170357")), nHannoverscheStrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000018"), 
					nHannoverscheStrasse, nKarmannstrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000019"), 
					nKarmannstrasse, nJeggenerWeg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000020"), 
					nJeggenerWeg, network.getNodes().get(Id.createNodeId("pt_0179034")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000021"), 
					network.getNodes().get(Id.createNodeId("pt_0179034")), nRosenburg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000022"), 
					nRosenburg, nTannenburgstrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000023"), 
					nTannenburgstrasse, nEbertallee);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000024"), 
					nEbertallee, nSchuetzenstrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000025"), 
					nSchuetzenstrasse, nLuhrmannsweg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000026"), 
					nLuhrmannsweg, nWiderhall);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000027"), 
					nWiderhall, network.getNodes().get(Id.createNodeId("pt_0190096")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000030"), 
					network.getNodes().get(Id.createNodeId("pt_0177316")), nEberleplatz);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000031"), 
					nEberleplatz, network.getNodes().get(Id.createNodeId("pt_0179299")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000034"), 
					network.getNodes().get(Id.createNodeId("pt_0179365")), nElbestrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_1000035"), 
					nElbestrasse, network.getNodes().get(Id.createNodeId("pt_0170281")));
			newLinks.add(link);
			
			Set<String> transportModes = new HashSet<String>();
			transportModes.add(TransportMode.pt);
			for ( int i = 0; i < newLinks.size(); i++){
				link = newLinks.get(i);
				link.setAllowedModes(transportModes);
				link.setFreespeed(8.33333333333);
				network.addLink(link);
			}
			
			List<TransitRouteStop> stops = new ArrayList<TransitRouteStop>();
			double time = 0;
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170280", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0202952", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0202972", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170220", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170221", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170103", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170103", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170102", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000007";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Jahnplatz");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179093", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179323", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000010";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Laischaftsstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170331", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170080", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170374", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000014";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Lutherkirche");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000015";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck BuenderStrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000016";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck RheinischeStrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000017";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck HannoverscheStrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000018";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Karmannstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 120;
			}
			{
				String linkId = "pt_1000019";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Jeggener Weg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000020";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Finkenweg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000021";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Rosenburg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000022";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Tannenburgstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000023";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Ebertallee");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000024";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Schützenstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000025";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Luhrmannsweg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000026";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Widerhall");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000027";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Dammer Hof");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 120;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0190369", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0177316", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000030";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Eberleplatz");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000031";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Wilhelm-von-Euch-Strasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179128", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179365", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000034";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Elbestrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_1000035";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Artilleriestrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 120;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170280", TransitStopFacility.class)), time, time));
			}
			
//			nRoute.setStartLinkId(network.getLinks().get("pt_10104").getId());
//			nRoute.setEndLinkId(network.getLinks().get("pt_7319").getId());
			List<Id<Link>> linkIds = new ArrayList<Id<Link>>();
//			linkIds.add(Id.createLinkId("pt_10104"));
			linkIds.add(Id.createLinkId("pt_10105"));
			linkIds.add(Id.createLinkId("pt_10106"));
			linkIds.add(Id.createLinkId("pt_10107"));
			linkIds.add(Id.createLinkId("pt_10108"));
			linkIds.add(Id.createLinkId("pt_4436"));
			linkIds.add(Id.createLinkId("pt_1000007"));
			linkIds.add(Id.createLinkId("pt_8263"));
			linkIds.add(Id.createLinkId("pt_8264"));
			linkIds.add(Id.createLinkId("pt_1000010"));
			linkIds.add(Id.createLinkId("pt_10000"));
			linkIds.add(Id.createLinkId("pt_10001"));
			linkIds.add(Id.createLinkId("pt_10002"));
			linkIds.add(Id.createLinkId("pt_1000014"));
			linkIds.add(Id.createLinkId("pt_1000015"));
			linkIds.add(Id.createLinkId("pt_1000016"));
			linkIds.add(Id.createLinkId("pt_1000017"));
			linkIds.add(Id.createLinkId("pt_1000018"));
			linkIds.add(Id.createLinkId("pt_1000019"));
			linkIds.add(Id.createLinkId("pt_1000020"));
			linkIds.add(Id.createLinkId("pt_1000021"));
			linkIds.add(Id.createLinkId("pt_1000022"));
			linkIds.add(Id.createLinkId("pt_1000023"));
			linkIds.add(Id.createLinkId("pt_1000024"));
			linkIds.add(Id.createLinkId("pt_1000025"));
			linkIds.add(Id.createLinkId("pt_1000026"));
			linkIds.add(Id.createLinkId("pt_1000027"));
			linkIds.add(Id.createLinkId("pt_4674"));
			linkIds.add(Id.createLinkId("pt_4675"));
			linkIds.add(Id.createLinkId("pt_1000030"));
			linkIds.add(Id.createLinkId("pt_1000031"));
			linkIds.add(Id.createLinkId("pt_785"));
			linkIds.add(Id.createLinkId("pt_786"));
			linkIds.add(Id.createLinkId("pt_1000034"));
			linkIds.add(Id.createLinkId("pt_1000035"));
//			linkIds.add(Id.createLinkId("pt_7319"));
			
			new RouteUtils();
			NetworkRoute nRoute = RouteUtils.createNetworkRoute(linkIds, network);
			nRoute.setLinkIds(Id.createLinkId("pt_10104"), linkIds, Id.createLinkId("pt_7319"));
			
			TransitRoute tRoute = schedule.getFactory().createTransitRoute(Id.create("100000_2", TransitRoute.class), nRoute, stops, "bus");
			
			for(double dTime = 21600; dTime <= 79200; dTime += 1200){
				Departure d = schedule.getFactory().createDeparture(Id.create(tRoute.getId().toString() + "_" + dTime, Departure.class), dTime);
				Id<Vehicle> vehicleId = Id.create(tRoute.getId().toString() + "_" + dTime, Vehicle.class);
				Vehicle vehicle = scenario.getTransitVehicles().getFactory().createVehicle(vehicleId, VehicleUtils.getDefaultVehicleType());
				scenario.getTransitVehicles().addVehicle(vehicle);
				d.setVehicleId(vehicleId);
				tRoute.addDeparture(d);
			}
			
			TransitLine tLine = schedule.getFactory().createTransitLine(Id.create("100000", TransitLine.class));
			tLine.addRoute(tRoute);
			schedule.addTransitLine(tLine);
		}	
			
		{
			
			Node nElbestrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(433709, 5794306));
			network.addNode(nElbestrasse);
			Node nEberleplatz = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(435128, 5795475));
			network.addNode(nEberleplatz);
			Node nWiderhall = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436352, 5794036));
			network.addNode(nWiderhall);
			Node nLuhrmannsweg = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(437184, 5793413));
			network.addNode(nLuhrmannsweg);
			Node nSchuetzenstrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 	new Coord(436995, 5793018));
			network.addNode(nSchuetzenstrasse);
			Node nEbertallee = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436967, 5792654));
			network.addNode(nEbertallee);
			Node nRosenburg = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(436920, 5792022));
			network.addNode(nRosenburg);
			Node nJeggenerWeg = network.getFactory().createNode(Id.createNodeId(nodeId()), 			new Coord(437563, 5791709));
			network.addNode(nJeggenerWeg);
			Node nKarmannstrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(437238, 5790849));
			network.addNode(nKarmannstrasse);
			Node nBuenderStrasse = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(436021, 5790623));
			network.addNode(nBuenderStrasse);
			Node nLutherkirche = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(435618, 5790461));
			network.addNode(nLutherkirche);
			Node nHofmeyerplatz = network.getFactory().createNode(Id.createNodeId(nodeId()), 		new Coord(434409, 5791271));
			network.addNode(nHofmeyerplatz);
			
			List<Link> newLinks = new ArrayList<Link>();
			Link link;
			link = network.getFactory().createLink(Id.createLinkId("pt_2000002"), 
					network.getNodes().get(Id.createNodeId("pt_0179281")), 	nElbestrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000003"), 
					nElbestrasse, 											network.getNodes().get(Id.createNodeId("pt_0170365")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000006"), 
					network.getNodes().get(Id.createNodeId("pt_0170299")), 	nEberleplatz);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000007"), 
					nEberleplatz, 											network.getNodes().get(Id.createNodeId("pt_0178316")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000010"), 
					network.getNodes().get(Id.createNodeId("pt_0170096")),	nWiderhall);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000011"), 
					nWiderhall, 											nLuhrmannsweg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000012"), 
					nLuhrmannsweg, 											nSchuetzenstrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000013"), 
					nSchuetzenstrasse, 											nEbertallee);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000014"), 
					nEbertallee, 											network.getNodes().get(Id.createNodeId("pt_0170170")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000015"), 
					network.getNodes().get(Id.createNodeId("pt_0170170")), 	nRosenburg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000016"), 
					nRosenburg, 											network.getNodes().get(Id.createNodeId("pt_0170034")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000017"), 
					network.getNodes().get(Id.createNodeId("pt_0170034")), 	nJeggenerWeg);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000018"), 
					nJeggenerWeg, 											nKarmannstrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000019"), 
					nKarmannstrasse, 										network.getNodes().get(Id.createNodeId("pt_0170385")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000021"), 
					network.getNodes().get(Id.createNodeId("pt_0170078")), 	nBuenderStrasse);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000022"), 
					nBuenderStrasse, 										nLutherkirche);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000023"), 
					nLutherkirche, 											network.getNodes().get(Id.createNodeId("pt_0179374")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000027"), 
					network.getNodes().get(Id.createNodeId("pt_0179330")), 	nHofmeyerplatz);
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000028"), 
					nHofmeyerplatz, 										network.getNodes().get(Id.createNodeId("pt_0170093")));
			newLinks.add(link);
			link = network.getFactory().createLink(Id.createLinkId("pt_2000030"), 
					network.getNodes().get(Id.createNodeId("pt_0170322")), 	network.getNodes().get(Id.createNodeId("pt_0179102")));
			newLinks.add(link);			
			
			
	
			
			Set<String> transportModes = new HashSet<String>();
			transportModes.add(TransportMode.pt);
			for ( int i = 0; i < newLinks.size(); i++){
				link = newLinks.get(i);
				link.setAllowedModes(transportModes);
				link.setFreespeed(8.33333333333);
				network.addLink(link);
			}
			
			List<TransitRouteStop> stops = new ArrayList<TransitRouteStop>();
			double time = 0;
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179280", TransitStopFacility.class)), time, time));
				time += 120;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179281", TransitStopFacility.class)), time, time));
				time += 120;
			}
			{
				String linkId = "pt_2000003";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Elbestrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170365", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170128", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170299", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000007";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Eberleplatz");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0178316", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170369", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170096", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000011";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Widerhall");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000012";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Luhrmannsweg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000013";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Schützenstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000014";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Ebertallee");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170170", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000016";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Rosenburg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170034", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000018";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck JeggenerWeg");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000019";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Karmannstrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170385", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170078", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000022";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck BuenderStrasse");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000023";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Lutherkirche");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179374", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179080", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179331", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179330", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				String linkId = "pt_2000028";
				Id<Link> id = Id.createLinkId(linkId);
				TransitStopFacility tsf = schedule.getFactory().createTransitStopFacility(Id.create(linkId.substring(3), TransitStopFacility.class), 
						network.getLinks().get(id).getToNode().getCoord(), false);
				tsf.setLinkId(id);
				tsf.setName("Osnabrueck Hofmeyerplatz");
				schedule.addStopFacility(tsf);
				stops.add(schedule.getFactory().createTransitRouteStop(tsf, time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170093", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0170322", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179102", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0202012", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179221", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179220", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0202971", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0202951", TransitStopFacility.class)), time, time));
				time += 60;
			}
			{
				stops.add(schedule.getFactory().createTransitRouteStop(schedule.getFacilities().get(Id.create("0179280", TransitStopFacility.class)), time, time));
				time += 60;
			}
			
			

			List<Id<Link>> linkIds = new ArrayList<Id<Link>>();
			linkIds.add(Id.createLinkId("pt_7335"));
			linkIds.add(Id.createLinkId("pt_2000002"));
			linkIds.add(Id.createLinkId("pt_2000003"));
			linkIds.add(Id.createLinkId("pt_808"));
			linkIds.add(Id.createLinkId("pt_809"));
			linkIds.add(Id.createLinkId("pt_2000006"));
			linkIds.add(Id.createLinkId("pt_2000007"));
			linkIds.add(Id.createLinkId("pt_4577"));
			linkIds.add(Id.createLinkId("pt_4578"));
			linkIds.add(Id.createLinkId("pt_2000010"));
			linkIds.add(Id.createLinkId("pt_2000011"));
			linkIds.add(Id.createLinkId("pt_2000012"));
			linkIds.add(Id.createLinkId("pt_2000013"));
			linkIds.add(Id.createLinkId("pt_2000014"));
			linkIds.add(Id.createLinkId("pt_2000015"));
			linkIds.add(Id.createLinkId("pt_2000016"));
			linkIds.add(Id.createLinkId("pt_2000017"));
			linkIds.add(Id.createLinkId("pt_2000018"));
			linkIds.add(Id.createLinkId("pt_2000019"));
			linkIds.add(Id.createLinkId("pt_3371"));
			linkIds.add(Id.createLinkId("pt_2000021"));
			linkIds.add(Id.createLinkId("pt_2000022"));
			linkIds.add(Id.createLinkId("pt_2000023"));
			linkIds.add(Id.createLinkId("pt_9969"));
			linkIds.add(Id.createLinkId("pt_9970"));
			linkIds.add(Id.createLinkId("pt_9971"));
			linkIds.add(Id.createLinkId("pt_2000027"));
			linkIds.add(Id.createLinkId("pt_2000028"));
			linkIds.add(Id.createLinkId("pt_8259"));
			linkIds.add(Id.createLinkId("pt_2000030"));
			linkIds.add(Id.createLinkId("pt_10071"));
			linkIds.add(Id.createLinkId("pt_10072"));
			linkIds.add(Id.createLinkId("pt_10073"));
			linkIds.add(Id.createLinkId("pt_10074"));
			linkIds.add(Id.createLinkId("pt_10075"));
//			linkIds.add(Id.createLinkId("pt_10076"));
			
			new RouteUtils();
			NetworkRoute nRoute = RouteUtils.createNetworkRoute(linkIds, network);
			nRoute.setLinkIds(Id.createLinkId("pt_10076"), linkIds, Id.createLinkId("pt_10076"));
			
			TransitRoute tRoute = schedule.getFactory().createTransitRoute(Id.create("100000_1", TransitRoute.class), nRoute, stops, "bus");
			
			for(double dTime = 21600; dTime <= 79200; dTime += 1200){
				Departure d = schedule.getFactory().createDeparture(Id.create(tRoute.getId().toString() + "_" + dTime, Departure.class), dTime);
				Id<Vehicle> vehicleId = Id.create(tRoute.getId().toString() + "_" + dTime, Vehicle.class);
				Vehicle vehicle = scenario.getTransitVehicles().getFactory().createVehicle(vehicleId, VehicleUtils.getDefaultVehicleType());
				scenario.getTransitVehicles().addVehicle(vehicle);
				d.setVehicleId(vehicleId);
				d.setVehicleId(vehicleId);
				tRoute.addDeparture(d);
			}
			
			TransitLine tLine = schedule.getTransitLines().get(Id.create("100000", TransitLine.class));
			tLine.addRoute(tRoute);
		
		}
		
		new NetworkWriter(network).write("/home/dhosse/scenarios/3connect/scenarios/networkMerged_trendWPt.xml.gz");
		new VehicleWriterV1(scenario.getTransitVehicles()).writeFile("/home/dhosse/scenarios/3connect/scenarios/TransitVehiclesWithRingbus.xml.gz");
		new TransitScheduleWriter(schedule).writeFile("/home/dhosse/scenarios/3connect/scenarios/scheduleSimplifiedWithRingbus.xml.gz");
		
	}

	private static String nodeId(){
		nodeIdCount ++;
		String nodeId = "pt_" + nodeIdCount;
		return nodeId;
	}

}
