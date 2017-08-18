package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.carsharing.router.CarsharingRoute;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.EmptyStageActivityTypes;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.facilities.Facility;

public class OneWayCarsharingRoutingModuleInnoZ implements RoutingModule {

	@Override
	public List<? extends PlanElement> calcRoute(Facility<?> fromFacility,
	        Facility<?> toFacility, double departureTime, Person person) {
		final List<PlanElement> trip = new ArrayList<PlanElement>();		
		
		final Leg csLeg = PopulationUtils.createLeg("oneway");
		CarsharingRoute csRoute = new CarsharingRoute(fromFacility.getLinkId(), toFacility.getLinkId());
		csLeg.setRoute(csRoute);
		trip.add( csLeg );	
	
		
		return trip;
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		return EmptyStageActivityTypes.INSTANCE;
	}

}