package com.innoz.toolbox.matsim.modeAvailability;

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.core.config.groups.ChangeModeConfigGroup;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.replanning.PlanStrategy;
import org.matsim.core.replanning.PlanStrategyImpl;
import org.matsim.core.replanning.modules.ReRoute;
import org.matsim.core.replanning.modules.TripsToLegsModule;
import org.matsim.core.replanning.selectors.RandomPlanSelector;
import org.matsim.core.router.TripRouter;
import org.matsim.facilities.ActivityFacilities;

public class ChangeTripModeProvider implements Provider<PlanStrategy> {

	private final GlobalConfigGroup globalConfigGroup;
	private final ChangeModeConfigGroup changeLegModeConfigGroup;
	private final ModeAvailabilityConfigGroup modeAvailabilityConfigGroup;
	private Provider<TripRouter> tripRouterProvider;
	private ActivityFacilities activityFacilities;
	
	@Inject
    protected ChangeTripModeProvider(GlobalConfigGroup globalConfigGroup, ChangeModeConfigGroup changeLegModeConfigGroup,
    		ModeAvailabilityConfigGroup modeAvailabilityConfigGroup, ActivityFacilities activityFacilities, Provider<TripRouter> tripRouterProvider) {
		this.globalConfigGroup = globalConfigGroup;
		this.changeLegModeConfigGroup = changeLegModeConfigGroup;
		this.modeAvailabilityConfigGroup = modeAvailabilityConfigGroup;
		this.activityFacilities = activityFacilities;
		this.tripRouterProvider = tripRouterProvider;
	}
	
	@Override
	public PlanStrategy get() {
		PlanStrategyImpl strategy = new PlanStrategyImpl(new RandomPlanSelector());
		strategy.addStrategyModule(new TripsToLegsModule(tripRouterProvider, globalConfigGroup));
		strategy.addStrategyModule(new ChangeLegModeInnoZ(globalConfigGroup, changeLegModeConfigGroup, modeAvailabilityConfigGroup));
		strategy.addStrategyModule(new ReRoute(activityFacilities, tripRouterProvider, globalConfigGroup));
		return strategy;
	}

}
