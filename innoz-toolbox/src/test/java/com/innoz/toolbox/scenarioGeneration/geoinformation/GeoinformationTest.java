package com.innoz.toolbox.scenarioGeneration.geoinformation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Building;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class GeoinformationTest {

	@Before
	public void setup() {

		Geoinformation.init(ActivityLocationsType.BUILDINGS);

		AdministrativeUnit unit1 = new AdministrativeUnit("01");
		
		{
			Coordinate[] coordinates = new Coordinate[]{new Coordinate(0d, 0d), new Coordinate(1d,0d),new Coordinate(1d,1d),new Coordinate(0d,1d),
					new Coordinate(0d,0d)};
			unit1.addLanduse("foo", new Building(new GeometryFactory().createPolygon(coordinates)));
		}
		
		{
			Coordinate[] coordinates = new Coordinate[]{new Coordinate(1d, 1d), new Coordinate(2d,1d),new Coordinate(2d,3d),new Coordinate(1d,3d),
					new Coordinate(1d,1d)};
			unit1.addLanduse("foo", new Building(new GeometryFactory().createPolygon(coordinates)));
		}
		
		Geoinformation.getInstance().addAdministrativeUnit(unit1);
		
	}
	
	@Test
	public void testLanduseWeightGetter() {

		assertEquals("Wrong landuse weight returned!", 3d, Geoinformation.getInstance().getTotalWeightForLanduseKey("0", "foo"),0d);
		
	}
	
}