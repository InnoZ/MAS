package innoz.scenarioGeneration.transit;

import java.io.File;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;

import GTFS2PTSchedule.GTFSDefinitions;
import GTFS2PTSchedule.GTFSDefinitions.RouteTypes;

public class GTFSToMatsimConverter {
	
	public static void main(String args[]){

//		System.out.println(RouteTypes.values()[0]);
//		System.out.println(RouteTypes.values()[1]);
//		System.out.println(RouteTypes.values()[2]);
//		System.out.println(RouteTypes.values()[3]);
//		System.out.println(RouteTypes.values()[4]);
//		System.out.println(RouteTypes.values()[5]);
		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/dhosse/scenarios/3connect/network.xml.gz");
		new GTFSToMatsimConverter().run(scenario);
		
	}

	public void run(Scenario scenario){
		
		Network network = scenario.getNetwork();
		String filebase = "/run/user/1000/gvfs/smb-share:server=192.168.0.3,share=gisdata/MODELLINGDATA/3connect/VBNgTF/";
		GTFS2MATSimTransitSchedule g2m = new GTFS2MATSimTransitSchedule(
				new File[]{new File(filebase)},
				new String[]{"road","rail"},
				network,
				new String[]{"weekday"},
				"EPSG:32632");
		
		new TransitScheduleWriter(g2m.getTransitSchedule()).writeFile("/home/dhosse/schedule.xml.gz");
		new NetworkWriter(g2m.getNetwork()).write("/home/dhosse/networkMod.xml.gz");
		
	}
	
}