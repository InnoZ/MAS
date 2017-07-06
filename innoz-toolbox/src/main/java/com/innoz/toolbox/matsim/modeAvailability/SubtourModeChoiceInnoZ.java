package com.innoz.toolbox.matsim.modeAvailability;

import javax.inject.Provider;

import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.config.groups.SubtourModeChoiceConfigGroup;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.algorithms.ChooseRandomLegModeForSubtour;
import org.matsim.core.population.algorithms.PermissibleModesCalculator;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.core.router.TripRouter;

public final class SubtourModeChoiceInnoZ extends AbstractMultithreadedModule {

	private final Provider<TripRouter> tripRouterProvider;

	private PermissibleModesCalculator permissibleModesCalculator;
	
	private final String[] chainBasedModes;
	private final String[] modes;
	private final String[] accessRestrictedModes;
	
	public SubtourModeChoiceInnoZ(Provider<TripRouter> tripRouterProvider, GlobalConfigGroup globalConfigGroup,
			ModeAvailabilityConfigGroup modeAvailabilityConfigGroup, SubtourModeChoiceConfigGroup subtourModeChoiceConfigGroup) {
	
		this(globalConfigGroup.getNumberOfThreads(), subtourModeChoiceConfigGroup.getModes(), subtourModeChoiceConfigGroup.getChainBasedModes(),
				modeAvailabilityConfigGroup.getModes(), tripRouterProvider);

	}
	
	public SubtourModeChoiceInnoZ(final int numberOfThreads, final String[] modes,
			final String[] chainBasedModes,
			String[] accessRestrictedModes, Provider<TripRouter> tripRouterProvider) {
		
		super(numberOfThreads);
		this.tripRouterProvider = tripRouterProvider;
		this.modes = modes.clone();
		this.chainBasedModes = chainBasedModes.clone();
		this.accessRestrictedModes = accessRestrictedModes;
		this.permissibleModesCalculator =
			new PermissibleModesCalculatorInnoZ(
					this.modes, this.accessRestrictedModes);
		
	}
	
	protected String[] getModes() {
		return modes.clone();
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {

		final TripRouter tripRouter = tripRouterProvider.get();
		final ChooseRandomLegModeForSubtour chooseRandomLegMode =
				new ChooseRandomLegModeForSubtour(
						tripRouter.getStageActivityTypes(),
						tripRouter.getMainModeIdentifier(),
						this.permissibleModesCalculator,
						this.modes,
						this.chainBasedModes,
						MatsimRandom.getLocalInstance());
		chooseRandomLegMode.setAnchorSubtoursAtFacilitiesInsteadOfLinks( false );
		return chooseRandomLegMode;
		
	}
	
	public void setPermissibleModesCalculator(PermissibleModesCalculator permissibleModesCalculator) {
	
		this.permissibleModesCalculator = permissibleModesCalculator;
		
	}

}