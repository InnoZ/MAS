package com.innoz.toolbox.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.DayTypes;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyType;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyVehicleType;

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
		
		assertNull("There is already a survey area defined in the configuration!",
				configuration.scenario().getSurveyAreaId());
		
		assertEquals(".", configuration.misc().getOutputDirectory());
		assertFalse(configuration.misc().isOverwritingExistingFiles());
		
		assertEquals(ActivityLocationsType.BUILDINGS, configuration.scenario().getActivityLocationsType());
		assertEquals(4711L, configuration.scenario().getRandomSeed());
		assertEquals(1.0d, configuration.scenario().getScaleFactor(), 0.0d);
		
		assertEquals(SurveyType.MiD, configuration.surveyPopulation().getSurveyType());
		assertEquals(DayTypes.weekday, configuration.surveyPopulation().getDayTypes());
		assertEquals(SurveyVehicleType.DEFAULT, configuration.surveyPopulation().getVehicleType());
		assertTrue(configuration.surveyPopulation().isUsingHouseholds());
		
		assertFalse(configuration.psql().isWritingIntoMobilityDatahub());
		assertEquals(0, configuration.psql().getParameterSets().size());
		
	}
	
}