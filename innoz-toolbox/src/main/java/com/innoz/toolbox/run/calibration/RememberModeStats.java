package com.innoz.toolbox.run.calibration;

import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

import com.innoz.toolbox.utils.analysis.LegModeDistanceDistribution;

/**
 * 
 * Controler listener that calculates the modal split of the last iteration and passes it to the {@code ASCModalSplitCallibration}
 * class.
 * 
 * @author bsmoehring
 *
 */
public class RememberModeStats implements IterationEndsListener{

	@Override
	public void notifyIterationEnds(IterationEndsEvent event) {

		// Only execute if the simulation has just finished its last iteration
		if(event.getIteration() == event.getServices().getConfig().controler().getLastIteration()) {

			// Execute leg mode distance distribution and update the calibration class' modal split map with the results
			LegModeDistanceDistribution lmdd = new LegModeDistanceDistribution();
			lmdd.init(event.getServices().getScenario());
			lmdd.preProcessData();
			lmdd.postProcessData();
			ASCModalSplitCallibration.updateModalSplit(lmdd.getMode2Share());
			
		}

	}

}
