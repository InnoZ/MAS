package com.innoz.toolbox.matsim.sharedMobility;

import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.carsharing.router.FreeFloatingRoutingModule;
import org.matsim.contrib.carsharing.router.OneWayCarsharingRoutingModule;
import org.matsim.contrib.carsharing.router.TwoWayCarsharingRoutingModule;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;

import com.innoz.toolbox.matsim.sharedMobility.bikesharing.BikesharingRoutingModule;

public class SharingUtils {

	public static AbstractModule createRoutingModule() {

		//=== routing moduels for carsharing trips ===
        return new AbstractModule() {

            @Override
            public void install() {
                addRoutingModuleBinding("twoway").toInstance(new TwoWayCarsharingRoutingModule());
                addRoutingModuleBinding("freefloating").toInstance(new FreeFloatingRoutingModule());
                addRoutingModuleBinding("oneway").toInstance(new OneWayCarsharingRoutingModule());
                addRoutingModuleBinding("pedelec").toInstance(new BikesharingRoutingModule());
                bind(MainModeIdentifier.class).toInstance(new MainModeIdentifier() {
                    final MainModeIdentifier defaultModeIdentifier = new MainModeIdentifierImpl();

                    @Override
                    public String identifyMainMode(
                            final List<? extends PlanElement> tripElements) {
                        // we still need to provide a way to identify our trips
                        // as being twowaycarsharing trips.
                        // This is for instance used at re-routing.
                        for ( PlanElement pe : tripElements ) {
                            if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "twoway" ) ) {
                                return "twoway";
                            }
                            else if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "oneway" ) ) {
                                return "oneway";
                            }
                            else if ( pe instanceof Leg && ((Leg) pe).getMode().equals( "freefloating" ) ) {
                                return "freefloating";
                            }
                            else if(pe instanceof Leg && ((Leg) pe).getMode().equals("pedelec")) {
                            	return "pedelec";
                            }
                        }
                        // if the trip doesn't contain a carsharing leg,
                        // fall back to the default identification method.
                        return defaultModeIdentifier.identifyMainMode( tripElements );
                    }
                });
            }
        };
	}
	
}