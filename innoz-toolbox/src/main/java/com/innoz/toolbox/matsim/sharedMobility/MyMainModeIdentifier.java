package com.innoz.toolbox.matsim.sharedMobility;

import java.util.List;

import javax.inject.Inject;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.av.intermodal.router.config.VariableAccessConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.pt.PtConstants;

public class MyMainModeIdentifier implements MainModeIdentifier {

	private final MainModeIdentifier delegate = new MainModeIdentifierImpl();
	private final VariableAccessConfigGroup va;
	
	/**
	 * 
	 */
	@Inject
	public MyMainModeIdentifier(Config config) {
		 va = (VariableAccessConfigGroup) config.getModules().get(VariableAccessConfigGroup.GROUPNAME);
	}
	
	/* (non-Javadoc)
	 * @see org.matsim.core.router.MainModeIdentifier#identifyMainMode(java.util.List)
	 */
	@Override
	public String identifyMainMode(List<? extends PlanElement> tripElements) {
		String mode = ((Leg) tripElements.get( 0 )).getMode();

		if ( mode.equals( TransportMode.transit_walk ) ) {
			return TransportMode.pt ;

		}
		
		
		for (PlanElement pe : tripElements)
		{
			if (pe instanceof Activity){
				if (((Activity) pe).getType().equals(PtConstants.TRANSIT_ACTIVITY_TYPE)){
					return (va.getMode());
				}
			}
			else if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "twoway" ) ) {
                return "twoway";
            }
            else if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "oneway" ) ) {
                return "oneway";
            }
            else if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "freefloating" ) ) {
                return "freefloating";
            }
		}
		return delegate.identifyMainMode(tripElements);
	}
	
}