package com.innoz.toolbox.matsim.modeAvailability;

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.config.groups.SubtourModeChoiceConfigGroup;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;

public class SubtourModeChoiceProvider implements Provider<PlanStrategy> {

	@Inject private Provider<TripRouter> tripRouterProvider;
	@Inject private GlobalConfigGroup globalConfigGroup;
	@Inject private SubtourModeChoiceConfigGroup subtourModeChoiceConfigGroup;
	@Inject private ModeAvailabilityConfigGroup modeAvailabilityConfigGroup;
	@Inject private ActivityFacilities facilities;
	
	@Override
	public PlanStrategy get() {
		PlanStrategyImpl strategy = new PlanStrategyImpl(new RandomPlanSelector());
		strategy.addStrategyModule(new SubtourModeChoiceInnoZ(tripRouterProvider, globalConfigGroup, modeAvailabilityConfigGroup,
				subtourModeChoiceConfigGroup));
		strategy.addStrategyModule(new ReRoute(facilities, tripRouterProvider, globalConfigGroup));
		return strategy;
	}

}