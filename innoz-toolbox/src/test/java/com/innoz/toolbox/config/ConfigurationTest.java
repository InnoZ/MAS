package com.innoz.toolbox.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.DayTypes;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.VehicleType;

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
		
		assertNull("There is at least one area set in a newly created configuration!",
				configuration.scenario().getAreaSets());
		
		assertEquals(".", configuration.misc().getOutputDirectory());
		
		assertEquals(ActivityLocationsType.BUILDINGS, configuration.scenario().getActivityLocationsType());
		assertEquals(4711L, configuration.scenario().getRandomSeed());
		assertEquals(1.0d, configuration.scenario().getScaleFactor(), 0.0d);
		
		assertEquals(SurveyType.MiD, configuration.surveyPopulation().getSurveyType());
		assertEquals(DayTypes.weekday, configuration.surveyPopulation().getDayTypes());
		assertEquals(VehicleType.DEFAULT, configuration.surveyPopulation().getVehicleType());
		
//		assertNull(configuration.dbNameSpace);
//		
//		assertEquals(PopulationType.households, configuration.popType);
//		assertEquals(PopulationSource.survey, configuration.popSource);
//		assertEquals(PopulationSource.none, configuration.popSourceV);
//		
//		assertTrue(configuration.adminUnits.isEmpty());
//		assertFalse(configuration.writeDatabaseTables);
//		assertFalse(configuration.writeIntoDatahub);
//		assertFalse(configuration.overwriteExistingFiles);
		
	}
	
}
