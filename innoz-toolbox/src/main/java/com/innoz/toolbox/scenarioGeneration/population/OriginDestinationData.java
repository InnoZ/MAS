package com.innoz.toolbox.scenarioGeneration.population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.io.database.CommuterDatabaseParser;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.ZensusGrid;
import com.innoz.toolbox.scenarioGeneration.geoinformation.ZensusGrid.ZensusGridNode;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.data.Tree.Node;
import com.innoz.toolbox.utils.data.WeightedSelection;

/**
 * 
 * Container class for all data that has sth. to do with demand-related origin-destination data (e.g. od matrices or grids).
 * 
 * @author dhosse
 *
 */
public class OriginDestinationData {

	private static Distribution distribution;
	private static Matrix od;
	private static Map<String, Tuple<List<ZensusGridNode>, Integer>> map;
	
	private static OriginDestinationData instance = new OriginDestinationData();
	
	private OriginDestinationData(){
		
		CommuterDatabaseParser parser = new CommuterDatabaseParser();
		parser.run();
		od = parser.getOD();
	
		distribution = new Distribution();
		map = new HashMap<>();
		
		Map<String,ArrayList<ZensusGridNode>> nodes = new HashMap<>();
		
		for(ZensusGridNode node : ZensusGrid.getInstance().getNodes()){
			
			for(AdministrativeUnit unit : Geoinformation.getInstance().getAdminUnitsWithGeometry()) {
				
				if(unit.getGeometry().contains(MGC.coord2Point(node.getCoord()))) {
				
					String id = unit.getId().substring(0, 5);
					
					if(!nodes.containsKey(id)) {
						nodes.put(id, new ArrayList<>());
					}
					
					nodes.get(id).add(node);
					
				}
				
			}
			
		}
		
		for(String adminId : nodes.keySet()) {
			
			int totalWeight = (int) nodes.get(adminId).stream().collect(Collectors.summarizingInt(ZensusGridNode::getNumberOfInhabitants)).getSum();
			
			map.put(adminId, new Tuple<List<ZensusGridNode>, Integer>(nodes.get(adminId), totalWeight));
			
		}
		
	}
	
	public static OriginDestinationData getInstance() {
		
		return instance;
		
	}
	
	public static ArrayList<Entry> getFromLocations(String fromId) {
		
		return od.getFromLocEntries(fromId);
		
	}
	
	public static ArrayList<Entry> getToLocations(String fromId) {
		
		return od.getToLocEntries(fromId);
		
	}
	
	public static Coord chooseHomeLocationFromGrid(AdministrativeUnit unit) {
		
		Tuple<List<ZensusGridNode>, Integer> entry = map.get(unit.getId().substring(0,5));
		
		double p = MatsimRandom.getRandom().nextDouble() * entry.getSecond();
		double accumulated = 0.0;
		
		for(ZensusGridNode node : entry.getFirst()){
			
			accumulated += node.getNumberOfInhabitants();
			
			if(p <= accumulated) {
				
				Coord center = node.getCoord();
				
				Coord temp = Geoinformation.getTransformation().transform(center);
				Coord topLeft = new Coord(temp.getX() - 50, temp.getY() + 50);
				Coord bottomRight = new  Coord(temp.getX() + 50, temp.getY() - 50);
				
				List<Landuse> results = new ArrayList<>();
				
				results = (List<Landuse>) Geoinformation.getInstance().getLanduseOfType(ActivityTypes.HOME)
						.getRectangle(topLeft.getX(), bottomRight.getY(), bottomRight.getX(), topLeft.getY(), results);

				if(results.isEmpty()) {
					return Geoinformation.getTransformation().transform(center);
				}
				
				return Geoinformation.getTransformation().transform(GeometryUtils.shoot(((Landuse)
						WeightedSelection.choose(results, MatsimRandom.getRandom().nextDouble())).getGeometry(), MatsimRandom.getLocalInstance()));

			}
			
		}
		
		return null;
		
	}
	
	public static AdministrativeUnit chooseWorkLocation(String homeCellId, double p) {
		
		double weight = od.getFromLocations().get(homeCellId.substring(0, 5)).stream().filter(entry -> entry.getToLocation().length() > 3)
				.collect(Collectors.summarizingDouble(Entry::getValue)).getSum();
		
		double accumulatedWeight = 0d;
		
		for(Entry entry : od.getFromLocations().get(homeCellId.substring(0, 5))){
			
			if(entry.getToLocation().length() > 3) {
				
				accumulatedWeight += entry.getValue() / weight;
				
				if(p <= accumulatedWeight){

					List<Node<AdministrativeUnit>> candidates = Geoinformation.getInstance().getAdminUnit(
							entry.getToLocation()).getChildren();
					
					weight = candidates.stream().map(Node::getData).collect(Collectors.summarizingDouble(
							u -> u.getWeightForKey(ActivityTypes.WORK))).getSum();
					
//					p = MatsimRandom.getRandom().nextDouble() * weight;
					accumulatedWeight = 0.0;
					
					for(Node<AdministrativeUnit> nn : candidates) {
						
						accumulatedWeight += nn.getData().getWeightForKey(ActivityTypes.WORK) / weight;
						if(p <= accumulatedWeight) {
							return nn.getData();
						}
						
					}
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
	public static Distribution getDistribution() {
		return distribution;
	}
	
}