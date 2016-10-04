package com.innoz.toolbox.scenarioGeneration.population.mobilityAttitude.scoring;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelScoringParametersForPerson;
import org.matsim.core.scoring.functions.SubpopulationCharyparNagelScoringParameters;
import org.matsim.deprecated.scoring.functions.CharyparNagelAgentStuckScoring;

public class MobilityAttitudesScoringFunctionFactory implements ScoringFunctionFactory {

	private final Scenario scenario;
	private final CharyparNagelScoringParametersForPerson params;
	
	@Inject
	MobilityAttitudesScoringFunctionFactory(final Scenario sc) {
		this.scenario = sc;
		this.params = new SubpopulationCharyparNagelScoringParameters(sc);
	}
	
	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		
		SumScoringFunction scoringFunctionSum = new SumScoringFunction();
		
		/*
		 * Here, scoring functions for the mobility attitude groups have to be 
		 * added
		 */
		
		scoringFunctionSum.addScoringFunction(new CharyparNagelActivityScoring(
				this.params.getScoringParameters(person)));
		scoringFunctionSum.addScoringFunction(new CharyparNagelAgentStuckScoring(
				this.params.getScoringParameters(person)));
		
		return null;
	}

}
