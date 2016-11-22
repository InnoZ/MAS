package com.innoz.toolbox.scenarioGeneration.network;

import java.util.HashSet;
import java.util.Set;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.LinkImpl;

public class NetworkModification {

	public static void addCycleways(Network network){
		
		Set<String> acceptedWayTypes = new HashSet<>();
		acceptedWayTypes.add(NetworkCreatorFromPsql.LIVING_STREET);
		acceptedWayTypes.add(NetworkCreatorFromPsql.MINOR);
		acceptedWayTypes.add(NetworkCreatorFromPsql.PRIMARY);
		acceptedWayTypes.add(NetworkCreatorFromPsql.PRIMARY_LINK);
		acceptedWayTypes.add(NetworkCreatorFromPsql.RESIDENTIAL);
		acceptedWayTypes.add(NetworkCreatorFromPsql.SECONDARY);
		acceptedWayTypes.add(NetworkCreatorFromPsql.TERTIARY);
		acceptedWayTypes.add(NetworkCreatorFromPsql.UNCLASSIFIED);
		
		for(Link link : network.getLinks().values()){
			
			String type = ((LinkImpl)link).getType();
			
			if(acceptedWayTypes.contains(type)){
				
				Set<String> modes = new HashSet<>();
				modes.addAll(link.getAllowedModes());
				modes.add(TransportMode.bike);
				link.setAllowedModes(modes);
				
			}
			
		}
		
	}
	
}