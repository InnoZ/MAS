package com.innoz.toolbox.matsim.modeAvailability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Route;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.population.routes.NetworkRoute;

public class ChooseRandomLegModeInnoZ implements PlanAlgorithm {

	private final String[] possibleModes;
	private final List<String> restrictedModes;

	private final Random rng;

	/**
	 * @param possibleModes
	 * @param rng The random number generator used to draw random numbers to select another mode.
	 * @see TransportMode
	 * @see MatsimRandom
	 */
	public ChooseRandomLegModeInnoZ(final String[] possibleModes, String[] restrictedModes, final Random rng) {
		this.possibleModes = possibleModes.clone();
		this.restrictedModes = Arrays.asList(restrictedModes.clone());
		this.rng = rng;
	}

	@Override
	public void run(final Plan plan) {
		List<PlanElement> tour = plan.getPlanElements();
		changeToRandomLegMode(tour, plan);
	}

	private void changeToRandomLegMode(final List<PlanElement> tour, final Plan plan) {
		if (tour.size() > 1) {

			final Person person;
			
			try {
				person = plan.getPerson();
			}
			catch (ClassCastException e) {
				throw new IllegalArgumentException( "I need a PersonImpl to get car availability" );
			}
			
			final String currentMode = getTransportMode(tour);

			String newMode;
			
			List<String> possibleModes = new ArrayList<>();
			for(String mode : this.possibleModes) {
				
				boolean avail = true;
				
				if(this.restrictedModes.contains(mode)) {
					
					Object o = person.getCustomAttributes().get(mode+"Avail");
					avail = o != null ? (boolean)o : false;
					
				}
				
				if(avail) possibleModes.add(mode);
				
			}
			
				
			if(possibleModes.size() < 2) {
				newMode = currentMode;
			} else {
				int newModeIdx = chooseModeOtherThan(currentMode, possibleModes);
				newMode = possibleModes.get(newModeIdx);
			}
			
			changeLegModeTo(tour, newMode);
		}
	}

	private String getTransportMode(final List<PlanElement> tour) {
		return ((Leg) (tour.get(1))).getMode();
	}

	private void changeLegModeTo(final List<PlanElement> tour, final String newMode) {
		for (PlanElement pe : tour) {
			if (pe instanceof Leg) {
				Leg leg = ((Leg) pe);
				leg.setMode(newMode);
				Route route = leg.getRoute();
				if(route!=null && route instanceof NetworkRoute) {
					((NetworkRoute) route).setVehicleId(null);
				}
			}
		}
	}

	private int chooseModeOtherThan(final String currentMode, List<String> possibleModes) {
		int newModeIdx = this.rng.nextInt(possibleModes.size() - 1);
		for (int i = 0; i <= newModeIdx; i++) {
			if (possibleModes.get(i).equals(currentMode)) {
				/* if the new Mode is after the currentMode in the list of possible
				 * modes, go one further, as we have to ignore the current mode in
				 * the list of possible modes. */
				newModeIdx++;
				break;
			}
		}
		return newModeIdx;
	}

}