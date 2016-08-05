package com.innoz.toolbox.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.innoz.toolbox.config.Configuration.ActivityLocations;
import com.innoz.toolbox.config.Configuration.PopulationSource;
import com.innoz.toolbox.config.Configuration.PopulationType;
import com.innoz.toolbox.config.Configuration.VehicleSource;

public class ConfigurationTest {

	/**
	 * 
	 * Tests if the default configuration parameters created by {@code ConfigurationUtils.createConfiguration()}
	 * equal the conceptional defaults.
	 * 
	 */
	@Test
	public void testDefaultConfigurationParams(){
	
		Configuration configuration = ConfigurationUtils.createConfiguration();
		
		assertTrue(configuration != null);
		
		assertNull("Survey area id(s) are not null!", configuration.surveyAreaIds);
		assertNull("Vicinity id(s) are not null!", configuration.vicinityIds);
		assertNull(configuration.dbNameSpace);
		
		assertEquals(".", configuration.outputDirectory);
		assertEquals(PopulationType.households, configuration.popType);
		assertEquals(PopulationSource.survey, configuration.popSource);
		assertEquals(PopulationSource.none, configuration.popSourceV);
		assertEquals(VehicleSource.matsim, configuration.vehSource);
		assertEquals(ActivityLocations.buildings, configuration.actLocs);
		assertEquals(4711L, configuration.randomSeed);
		assertEquals(1.0d, configuration.scaleFactor, 0.0d);
		
		assertTrue(configuration.adminUnits.isEmpty());
		assertTrue(configuration.onlyWorkingDays);
		assertFalse(configuration.writeDatabaseTables);
		assertFalse(configuration.writeIntoDatahub);
		assertFalse(configuration.overwriteExistingFiles);
		
	}
	
}
