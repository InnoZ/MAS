package com.innoz.toolbox.matsim.modeAvailability;

import org.matsim.core.controler.AbstractModule;

public class ModeAvailabilityModule extends AbstractModule {

	@Override
	public void install() {

		addPlanStrategyBinding("SubtourModeChoice").toProvider(SubtourModeChoiceProvider.class);
		addPlanStrategyBinding("ChangeTripMode").toProvider(ChangeTripModeProvider.class);
		addPlanStrategyBinding("ChangeLegMode").toProvider(ChangeTripModeProvider.class);
		
	}

}
