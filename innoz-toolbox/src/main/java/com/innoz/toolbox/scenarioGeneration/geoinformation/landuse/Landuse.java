package com.innoz.toolbox.scenarioGeneration.geoinformation.landuse;

import com.vividsolutions.jts.geom.Geometry;

public interface Landuse {

	public double getWeight();
	
	public Geometry getGeometry();
	
}
