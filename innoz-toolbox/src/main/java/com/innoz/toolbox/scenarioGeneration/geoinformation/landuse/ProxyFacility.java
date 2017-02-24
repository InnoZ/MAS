package com.innoz.toolbox.scenarioGeneration.geoinformation.landuse;

import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.facilities.ActivityFacility;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class ProxyFacility implements Landuse {

	private final ActivityFacility facility;
	private double weight;
	
	public ProxyFacility(final ActivityFacility facility, double weight){
		
		this.facility = facility;
		this.weight = weight;
		
	}
	
	public ActivityFacility get(){
		
		return this.facility;
		
	}
	
	@Override
	public Geometry getGeometry(){
		
		return new GeometryFactory().createPoint(MGC.coord2Coordinate(facility.getCoord()));
		
	}
	
	@Override
	public double getWeight(){
		
		return this.weight;
		
	}
	
	@Deprecated
	public void setWeight(double s) {
		// stub
	}
	
}
