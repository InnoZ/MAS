package com.innoz.toolbox.scenarioGeneration.geoinformation.landuse;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

public class Building implements Landuse {

	private Set<String> activityOptions;
	private final Geometry geometry;
	
	public Building(final Geometry geometry){
		this.activityOptions = new HashSet<String>();
		this.geometry = geometry;
	}
	
	public Set<String> getActivityOptions(){
		return this.activityOptions;
	}
	
	public void addActivityOption(String activityType){
		this.activityOptions.add(activityType);
	}
	
	@Override
	public Geometry getGeometry(){
		return this.geometry;
	}
	
	@Override
	public double getWeight(){
		
		return this.geometry.getArea();
		
	}
	
}
