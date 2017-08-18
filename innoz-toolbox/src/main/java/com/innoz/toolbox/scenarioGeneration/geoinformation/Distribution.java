package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.Dijkstra;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.scenarioGeneration.utils.Modes;

/**
 * 
 * @author dhosse
 *
 */
public class Distribution {

	//activityType, mode, matrix
	private Map<String, Map<String,Matrix>> transitionMatrices = new HashMap<String, Map<String, Matrix>>();
	private Matrix distances;
	
	private LeastCostPathCalculator lcpc;
	
	public Distribution(){

		TravelDisutility tdis = new FreespeedTravelTimeAndDisutility(-6, 6, 0);
		TravelTime ttime = new FreespeedTravelTimeAndDisutility(-6, 6, 0);
		this.lcpc = new Dijkstra(Controller.scenario().getNetwork(), tdis, ttime);
		this.create();
		
	}
	
	private void create(){
		
		String[] activityTypes = {ActivityTypes.EDUCATION, ActivityTypes.SHOPPING,
				ActivityTypes.LEISURE, ActivityTypes.OTHER, ActivityTypes.KINDERGARTEN, ActivityTypes.SUPPLY,
				ActivityTypes.EATING, ActivityTypes.CULTURE, ActivityTypes.SPORTS, ActivityTypes.FURTHER,
				ActivityTypes.SERVICE, ActivityTypes.HEALTH, ActivityTypes.EVENT, ActivityTypes.ERRAND,
				ActivityTypes.PRIMARY_SCHOOL, ActivityTypes.SECONDARY_SCHOOL, ActivityTypes.PROFESSIONAL_SCHOOL,
				ActivityTypes.UNIVERSITY};
		String[] modes = {TransportMode.bike, TransportMode.car, TransportMode.pt, TransportMode.ride, TransportMode.other};
		
		distances = new Matrix("distances", "");
		
		Map<String, Double> rowMinima = new HashMap<>();
		
		List<AdministrativeUnit> adminUnits = new ArrayList<>();
		adminUnits.addAll(Geoinformation.getInstance().getAdminUnitsWithGeometry());
		
		for(AdministrativeUnit u1 : adminUnits){
			
			rowMinima.put(u1.getId(), Double.MAX_VALUE);
			
			for(AdministrativeUnit u2 : adminUnits){

				if(u1.getGeometry() != null && u2.getGeometry() != null){
			
					double distance = 0d;

					Coord u1Coord = Geoinformation.getTransformation().transform(MGC.point2Coord(u1.getGeometry().getCentroid()));
					Coord u2Coord = Geoinformation.getTransformation().transform(MGC.point2Coord(u2.getGeometry().getCentroid()));
					
					if(!u1.equals(u2)){

//						distance = CoordUtils.calcDistance(u1Coord, u2Coord);
						Node fromNode = NetworkUtils.getNearestRightEntryLink(Controller.scenario().getNetwork(), u1Coord).getToNode();
						Node toNode = NetworkUtils.getNearestRightEntryLink(Controller.scenario().getNetwork(), u2Coord).getFromNode();
						
						Path path = this.lcpc.calcLeastCostPath(fromNode, toNode, 0, null, null);
						
						for(Link link : path.links){
							distance += link.getLength();
						}
						
						if(distance < rowMinima.get(u1.getId())){
							rowMinima.put(u1.getId(), distance);
						}
						
					} else {
						
						distance = 1000d;
						
					}

					distances.createEntry(u1.getId(), u2.getId(), distance);
					
				}

			}
			
		}
		
		for(AdministrativeUnit u1 : adminUnits){
			for(AdministrativeUnit u2 : adminUnits){
				if(u1.equals(u2)){
					distances.createEntry(u1.getId(), u2.getId(), rowMinima.get(u1.getId()) / 3 );
				}
			}
		}
		
		for(AdministrativeUnit u1 : adminUnits){
			
			for(AdministrativeUnit u2 : adminUnits){
				
				if(distances.getEntry(u1.getId(), u2.getId()) != null){
				
					for(String key : activityTypes){
						
						//create one matrix per act type at the destination
						if(!transitionMatrices.containsKey(key)){
							transitionMatrices.put(key, new HashMap<>());
						}
						
						for(String mode : modes){
							
							if(!transitionMatrices.get(key).containsKey(mode)){
								
								transitionMatrices.get(key).put(mode, new Matrix(key + "_" + mode, ""));
							
							}

							double proba = Double.NEGATIVE_INFINITY;
							
							if(u2.getLanduseGeometries().containsKey(key)){
								
								double distance = distances.getEntry(u1.getId(), u2.getId()).getValue();
								double speed = Modes.getSpeedForMode(mode) / 1.3;
								double weight = u2.getLanduseGeometries().get(key).size();
								double a = Math.exp((-6d / 3600d) * (distance / speed));
								proba = weight * a;
								
							}
							
							transitionMatrices.get(key).get(mode).createEntry(u1.getId(), u2.getId(), proba);
								
						}
					
					}
					
				}
				
			}
			
		}
		
	}
	
	public double getDisutilityForActTypeAndMode(String fromId, String toId, String activityType, String mode){
		
		if(transitionMatrices.containsKey(activityType)){
			
			if(transitionMatrices.get(activityType).containsKey(mode)){
				
				return transitionMatrices.get(activityType).get(mode).getEntry(fromId, toId).getValue();
				
			}
			
		}
		
		return Double.NEGATIVE_INFINITY;
		
	}
	
	public double getDistance(String fromId, String toId){
		return distances.getEntry(fromId, toId).getValue();
	}
	
}