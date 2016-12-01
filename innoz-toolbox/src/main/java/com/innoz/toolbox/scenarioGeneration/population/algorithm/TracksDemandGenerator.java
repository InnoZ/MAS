package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.TracksDatabaseReader;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;

public class TracksDemandGenerator extends DemandGenerationAlgorithm {

	public TracksDemandGenerator(Scenario scenario, Geoinformation geoinformation,
			CoordinateTransformation transformation, Distribution distribution) {
		
		super(scenario, geoinformation, transformation, distribution);
		
	}

	@Override
	public void run(Configuration configuration, String ids) {
		
		TracksDatabaseReader reader = new TracksDatabaseReader(configuration);
		reader.getPersons(); //and so on...

	}

}