package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class ServiceArea {
	
	private Map<String, Geometry> areaMap = new HashMap<>();
	
	public void init(String file) {
		
		ShapeFileReader shapefileReader = new ShapeFileReader();
		Collection<SimpleFeature> features = shapefileReader.readFileAndInitialize(file);
		
		for(SimpleFeature feature : features) {
			
			String key = (String)feature.getAttribute("name");
			
			Geometry value = (Geometry)feature.getDefaultGeometry();
			
			this.areaMap.put(key, value);
			
		}
		
	}
	
	public Map<String, Geometry> getServiceArea() {
	
		return this.areaMap;
		
	}

}