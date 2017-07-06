package com.innoz.toolbox.matsim.modeAvailability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.algorithms.PermissibleModesCalculator;

public class PermissibleModesCalculatorInnoZ implements PermissibleModesCalculator {

	private final List<String> availableModes;
	private final List<String> accessRestrictedModes;
	
	public PermissibleModesCalculatorInnoZ(final String[] availableModes, final String[] accessRestrictedModes) {
		
		this.availableModes = Arrays.asList(availableModes);
		this.accessRestrictedModes = Arrays.asList(accessRestrictedModes);
		
	}
	
	@Override
	public Collection<String> getPermissibleModes(Plan plan) {
		
		List<String> availableModes = new ArrayList<>(this.availableModes);
		
		final Person person;
		
		try {
			person = plan.getPerson();
		}
		catch (ClassCastException e) {
			throw new IllegalArgumentException( "I need a PersonImpl to get car availability" );
		}
		
		final boolean carAvail =
				!"no".equals( PersonUtils.getLicense(person) ) &&
				!"never".equals( PersonUtils.getCarAvail(person) );
		if(!carAvail) availableModes.remove(TransportMode.car);
		
		// Here, we iterate over the modes with access restrictions and check if the person
		// fulfills the necessary condition 
		this.accessRestrictedModes.stream().filter(s -> !s.equals(TransportMode.car)).forEach(s -> {
			
			Object o = person.getCustomAttributes().get(s + "Avail");
			final boolean avail = o != null ? (boolean) o : false;
			if(!avail) availableModes.remove(s);
			
		});
		
		return availableModes;
		
	}

}
