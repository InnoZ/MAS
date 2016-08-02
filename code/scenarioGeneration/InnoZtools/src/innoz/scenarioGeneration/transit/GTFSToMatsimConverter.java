package innoz.scenarioGeneration.transit;

import java.io.File;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;

public class GTFSToMatsimConverter {
	
	public static void main(String args[]){

		Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/dhosse/scenarios/3connect/network.xml.gz");
		new GTFSToMatsimConverter().run(scenario);
		
	}

	public void run(Scenario scenario){
		
		Network network = scenario.getNetwork();
		String filebase = "/home/dhosse/02_Data/GTFS/VBN/";
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