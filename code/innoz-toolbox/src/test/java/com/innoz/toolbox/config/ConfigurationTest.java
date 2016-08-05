package com.innoz.toolbox.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.innoz.toolbox.config.Configuration.PopulationSource;
import com.innoz.toolbox.config.Configuration.VehicleSource;

public class ConfigurationTest {

	/**
	 * 
	 * Tests if the default configuration parameters created by {@code ConfigurationUtils.createConfiguration()}
	 * equal the conceptional defaults.
	 * 
	 */
	@Test
	public void testDefaultConfiguration(){
	
		Configuration configuration = ConfigurationUtils.createConfiguration();
		
		assertTrue(configuration != null);
		
		assertTrue(configuration.surveyAreaIds == null);
		assertTrue(configuration.vicinityIds == null);
		assertTrue(configuration.outputDirectory.equals("."));
		assertTrue(configuration.popSource.equals(PopulationSource.survey));
		assertTrue(configuration.popSourceV.equals(PopulationSource.none));
		assertTrue(configuration.vehSource.equals(VehicleSource.matsim));
		
	}
	
}
