package com.innoz.toolbox.matsim.sharedMobility.bikesharing;

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

public class BikesharingRoutingModule implements RoutingModule {

	@Override
	public List<? extends PlanElement> calcRoute(Facility<?> fromFacility,
	        Facility<?> toFacility, double departureTime, Person person) {
		
		final List<PlanElement> trip = new ArrayList<PlanElement>();
		
		final Leg leg1 = PopulationUtils.createLeg("pedelec");
		CarsharingRoute route1 = new CarsharingRoute(fromFacility.getLinkId(), toFacility.getLinkId()); // TODO check if this works
		leg1.setRoute(route1);
		trip.add(leg1);
		return trip;
		
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		
		return EmptyStageActivityTypes.INSTANCE;
		
	}

}