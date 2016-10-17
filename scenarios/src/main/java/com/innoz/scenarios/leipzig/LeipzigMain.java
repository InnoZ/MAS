package com.innoz.scenarios.leipzig;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkChangeEvent;
import org.matsim.core.network.NetworkChangeEvent.ChangeType;
import org.matsim.core.network.NetworkChangeEvent.ChangeValue;
import org.matsim.core.network.NetworkChangeEventFactoryImpl;
import org.matsim.core.scenario.ScenarioUtils;

public class LeipzigMain {

	public static void main(String args[]){

		Config config = ConfigUtils.loadConfig(args[0]);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		NetworkChangeEventFactoryImpl factory = new NetworkChangeEventFactoryImpl();
		
		//TODO This is a stub. The flow capacities and free speeds have to be changed according to traffic counts
		for(Link link : scenario.getNetwork().getLinks().values()){
			
			NetworkChangeEvent event = factory.createNetworkChangeEvent(7 * 3600);
			event.setFlowCapacityChange(new ChangeValue(ChangeType.ABSOLUTE, 1000.));
			event.setFreespeedChange(new ChangeValue(ChangeType.ABSOLUTE, 20/3.6));
			event.addLink(link);
			
		}
		
		Controler controler = new Controler(scenario);
		controler.run();
		
	}
	
}