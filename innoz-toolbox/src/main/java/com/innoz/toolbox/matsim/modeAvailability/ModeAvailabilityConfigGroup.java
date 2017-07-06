package com.innoz.toolbox.matsim.modeAvailability;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.ReflectiveConfigGroup;
import org.matsim.core.utils.misc.StringUtils;

public class ModeAvailabilityConfigGroup extends ReflectiveConfigGroup {

	public final static String GROUP_NAME = "modeAvailability";

	private final static String RESTRICTED_MODES = "restrictedModes";
	private String[] restrictedModes = new String[] {TransportMode.car};
	
	public ModeAvailabilityConfigGroup() {

		super(GROUP_NAME);
		
	}

	@StringSetter(RESTRICTED_MODES)
	public void setRestrictedModes(String restrictedModes) {
		
		this.restrictedModes = toArray(restrictedModes);
		
	}
	
	@StringGetter(RESTRICTED_MODES)
	public String getRestrictedModes() {
		
		return toString(this.restrictedModes);
		
	}
	
	private static String toString( final String[] modes ) {
		
		StringBuilder b = new StringBuilder();

		if (modes.length > 0) b.append( modes[ 0 ] );
		for (int i=1; i < modes.length; i++) {
			b.append( ',' );
			b.append( modes[ i ] );
		}
	
		return b.toString();
		
	}

	private static String[] toArray( final String modes ) {
		String[] parts = StringUtils.explode(modes, ',');

		for (int i = 0, n = parts.length; i < n; i++) {
			parts[i] = parts[i].trim().intern();
		}

		return parts;
	}
	
	public String[] getModes() {
		return this.restrictedModes;
	}
			
}