package com.innoz.toolbox.matsim.scoring;

import javax.inject.Inject;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.CharyparNagelActivityScoring;
import org.matsim.core.scoring.functions.CharyparNagelAgentStuckScoring;
import org.matsim.core.scoring.functions.CharyparNagelMoneyScoring;
import org.matsim.core.scoring.functions.ScoringParameters;
import org.matsim.core.scoring.functions.ScoringParametersForPerson;
import org.matsim.utils.objectattributes.ObjectAttributes;

public class MobilityAttitudeScoringFunctionFactory implements ScoringFunctionFactory {

	protected Network network;
	private final ScoringParametersForPerson params;
	private final MobilityAttitudeConfigGroup config;
	private final ObjectAttributes personAttributes;
	
	@Inject
	public MobilityAttitudeScoringFunctionFactory(final ScoringParametersForPerson params, final MobilityAttitudeConfigGroup ma,
			final Network network, final Population population) {
		
		this.params = params;
		this.network = network;
		this.config = ma;
		this.personAttributes = population.getPersonAttributes();
		
	}
	
	@Override
	public ScoringFunction createNewScoringFunction(Person person) {
		
		final ScoringParameters parameters = params.getScoringParameters( person );

		SumScoringFunction sumScoringFunction = new SumScoringFunction();
		sumScoringFunction.addScoringFunction(new CharyparNagelActivityScoring( parameters ));
		sumScoringFunction.addScoringFunction(new MobilityAttitudeLegScoring( parameters ,
				this.config.getParamsForGroup((String)(this.personAttributes.getAttribute(person.getId().toString(),
				this.config.getSubpopulationAttribute()))),	this.network));
		sumScoringFunction.addScoringFunction(new CharyparNagelMoneyScoring( parameters ));
		sumScoringFunction.addScoringFunction(new CharyparNagelAgentStuckScoring( parameters ));
		return sumScoringFunction;
		
	}

}