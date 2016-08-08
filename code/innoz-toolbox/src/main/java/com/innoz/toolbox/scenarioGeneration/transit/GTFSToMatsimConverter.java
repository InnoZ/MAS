package com.innoz.toolbox.scenarioGeneration.transit;

import java.io.File;
import java.time.LocalDate;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.gtfs.GtfsConverter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
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
			
			((NodeImpl)node).setCoord(transformation.transform(node.getCoord()));
			
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
		
		GtfsConverter converter = new GtfsConverter(GTFSFeed.fromFile(
				"/home/dhosse/02_Data/GTFS/VBN/VBN.zip"), scenario, transformation);
		converter.setDate(LocalDate.of(2016, 6, 1));
		converter.convert();
		
		File output = new File("/home/dhosse/osGtfs/");
		if(!output.exists()) output.mkdirs();
		
		Network transitNet = NetworkUtils.createNetwork();
		CreatePseudoNetwork creator = new CreatePseudoNetwork(schedule, transitNet, "pt_");
		creator.createNetwork();
		
		new NetworkWriter(transitNet).write(output.getAbsolutePath() +
				"/transitNetwork.xml.gz");
		new TransitScheduleWriter(schedule).writeFile(output.getAbsolutePath() +
				"/schedule.xml.gz");
		
		Vehicles tv = scenario.getTransitVehicles();
		new VehicleWriterV1(tv).writeFile(output.getAbsolutePath() +
				"/transitVehicles.xml.gz");
		
	}
	
}