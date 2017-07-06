package com.innoz.toolbox.matsim.modeAvailability;

import org.matsim.core.config.groups.ChangeModeConfigGroup;
import org.matsim.core.config.groups.GlobalConfigGroup;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;

public class ChangeLegModeInnoZ extends AbstractMultithreadedModule {

	private String[] availableModes;
	private String[] restrictedModes;
	
	public ChangeLegModeInnoZ(final GlobalConfigGroup globalConfigGroup, ChangeModeConfigGroup changeLegModeConfigGroup, ModeAvailabilityConfigGroup modeAvailabilityConfigGroup) {

		super(globalConfigGroup.getNumberOfThreads());
		
		this.availableModes = changeLegModeConfigGroup.getModes();
		this.restrictedModes = modeAvailabilityConfigGroup.getModes();
		
	}
	
	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
	
		ChooseRandomLegModeInnoZ algo = new ChooseRandomLegModeInnoZ(this.availableModes, this.restrictedModes, MatsimRandom.getLocalInstance());
		return algo;
		
	}

}