package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.collections.QuadTree;

import com.innoz.toolbox.config.Configuration.ActivityLocations;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;

public class LanduseDataContainer {

	private Map<String, QuadTree<Landuse>> geometryQuadtree;
	private final ActivityLocations type;
	
	public LanduseDataContainer(ActivityLocations type){
		
		this.type = type;
		this.geometryQuadtree = new HashMap<>();
			
	}
	
	ActivityLocations getType(){
		
		return this.type;
		
	}
	
	void create(String key, double[] bounds){
		
		this.geometryQuadtree.put(key, new QuadTree<Landuse>(bounds[0], bounds[1], bounds[2], bounds[3]));
		
	}
	
	QuadTree<Landuse> getLanduseOfType(String key){
		
		return this.geometryQuadtree.get(key);
		
	}
	
}