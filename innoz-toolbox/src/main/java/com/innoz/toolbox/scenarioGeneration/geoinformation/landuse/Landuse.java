package com.innoz.toolbox.scenarioGeneration.geoinformation.landuse;

import com.innoz.toolbox.scenarioGeneration.utils.Weighted;
import com.vividsolutions.jts.geom.Geometry;

public interface Landuse extends Weighted {

	public Geometry getGeometry();
	
}
