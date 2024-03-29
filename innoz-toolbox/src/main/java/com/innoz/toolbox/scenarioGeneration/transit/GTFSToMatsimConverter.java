package com.innoz.toolbox.scenarioGeneration.transit;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleFactory;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.pt.utils.CreatePseudoNetwork;
import org.matsim.vehicles.VehicleWriterV1;
import org.matsim.vehicles.Vehicles;

import com.conveyal.gtfs.GTFSFeed;

public class GTFSToMatsimConverter {
	
	public static void main(String args[]){

//		new GTFSToMatsimConverter().runV1();
		new GTFSToMatsimConverter().runV2();
		
	}
	
	public void runV1(){
		
		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		Network network = scenario.getNetwork();
		new MatsimNetworkReader(network).readFile(
				"/home/dhosse/scenarios/3connect/network.xml.gz");
		CoordinateTransformation transformation = TransformationFactory.
				getCoordinateTransformation("EPSG:32632", TransformationFactory.WGS84);
		for(Node node : network.getNodes().values()){
			
			node.setCoord(transformation.transform(node.getCoord()));
			
		}
		
		String filebase = "/home/dhosse/02_Data/GTFS/VBN/";
		GTFS2MATSimTransitSchedule g2m = new GTFS2MATSimTransitSchedule(
				new File[]{new File(filebase)},
				new String[]{"road","rail"},
				network,
				new String[]{"weekday"},
				"EPSG:32632");
		
		TransitSchedule schedule = g2m.getTransitSchedule();
		
		Network transitNet = NetworkUtils.createNetwork();
		CreatePseudoNetwork creator = new CreatePseudoNetwork(schedule, network, "pt_");
		creator.createNetwork();
		
		new TransitScheduleWriter(schedule).writeFile("/home/dhosse/schedule.xml.gz");
		new NetworkWriter(transitNet).write("/home/dhosse/networkMod.xml.gz");
		
	}
	
	public void runV2(){
		
		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		CoordinateTransformation transformation = TransformationFactory.
				getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:32632");
		
		TransitSchedule schedule = scenario.getTransitSchedule();
		
		String agencies = "";
		
		com.innoz.toolbox.scenarioGeneration.transit.GtfsConverter converter =
				new com.innoz.toolbox.scenarioGeneration.transit.GtfsConverter(GTFSFeed.fromFile(
				"/home/dhosse/02_Data/GTFS/VBN.zip"), scenario, transformation, agencies);
		converter.setDate(LocalDate.of(2016, 6, 1));
		converter.convert();
		
		File output = new File("/home/dhosse/osGtfs/");
		if(!output.exists()) output.mkdirs();
		
		TransitSchedule cleaned = cleanTransitSchedule(schedule);
		((MutableScenario)scenario).setTransitSchedule(cleaned);
		
		Network transitNet = NetworkUtils.createNetwork();
		InnoZCreatePseudoNetwork creator = new InnoZCreatePseudoNetwork(cleaned, transitNet, "pt_");
		creator.createNetwork();
		
		new NetworkWriter(transitNet).write(output.getAbsolutePath() +
				"/transitNetwork.xml.gz");
		new TransitScheduleWriter(cleaned).writeFile(output.getAbsolutePath() +
				"/schedule.xml.gz");
		
		TransitScheduleSimplifier.simplifyTransitSchedule(scenario, "/home/dhosse/osGtfs/scheduleSimplified.xml.gz");
		
		Vehicles tv = scenario.getTransitVehicles();
		new VehicleWriterV1(tv).writeFile(output.getAbsolutePath() +
				"/transitVehicles.xml.gz");
		
	}
	
	private TransitSchedule cleanTransitSchedule(TransitSchedule schedule){
		
		TransitSchedule schedule2 = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getTransitSchedule();
		TransitScheduleFactory factory = schedule2.getFactory();
		
		double[] bb = new double[]{7.2729,51.6623,8.8824,52.7396};
		
		CoordinateTransformation transformation = TransformationFactory.
				getCoordinateTransformation("EPSG:32632", TransformationFactory.WGS84);
		
		for(TransitStopFacility f : schedule.getFacilities().values()){
			
			Coord c = transformation.transform(f.getCoord());
			
			if(c.getX() >= bb[0] && c.getX() <= bb[2] && c.getY() >= bb[1] && c.getY() <= bb[3]){
				schedule2.addStopFacility(f);
			}
			
		}
		
		for(TransitLine line : schedule.getTransitLines().values()){
			
			if(!line.getRoutes().isEmpty()){
			
				TransitLine l2 = factory.createTransitLine(line.getId());
				l2.setName(line.getName());
				
				for(TransitRoute route : line.getRoutes().values()){
					
					if(route.getDepartures().size() > 0 && route.getStops().size() > 2){
						
						List<TransitRouteStop> stops = new ArrayList<>();

						boolean first = true;
						double arrivalOffset = 0d;
						double departureOffset = 0d;
						
						for(TransitRouteStop stop : route.getStops()){
							
							if(schedule2.getFacilities().get(stop.getStopFacility().getId()) != null){
								
								if(first){
									arrivalOffset = stop.getArrivalOffset();
									departureOffset = stop.getDepartureOffset();
									first = false;
								}
								
								TransitRouteStop s = factory.createTransitRouteStop(stop.getStopFacility(),
										stop.getArrivalOffset() - arrivalOffset, stop.getDepartureOffset() - departureOffset);
								
								stops.add(s);
								
							}
							
						}
						
						if(stops.size() > 1){

							TransitRoute r = factory.createTransitRoute(route.getId(), route.getRoute(), stops,
									route.getTransportMode());
							
							for(Departure d : route.getDepartures().values()){
								
								Departure departure = factory.createDeparture(d.getId(), d.getDepartureTime() + arrivalOffset);
								departure.setVehicleId(d.getVehicleId());
								r.addDeparture(departure);
								
							}

							l2.addRoute(r);
							
						}
						
					}
					
				}
				
				if(!l2.getRoutes().isEmpty()){
					schedule2.addTransitLine(l2);
				}
				
			}
			
		}
		
		return schedule2;
		
	}
	
}