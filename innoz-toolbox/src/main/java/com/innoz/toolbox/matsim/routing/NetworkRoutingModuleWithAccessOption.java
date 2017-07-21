package com.innoz.toolbox.matsim.routing;

import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.router.NetworkRoutingInclAccessEgressModule;
import org.matsim.core.router.NetworkRoutingModule;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.StageActivityTypes;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.Facility;

public final class NetworkRoutingModuleWithAccessOption implements RoutingModule {

	private final RoutingModule delegate;
	
	private final String mode;
	private final Network network;
	private final ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities("f");
	private final Network outer;

	private final AccessConfigGroup accessConfigGroup;
	
	public NetworkRoutingModuleWithAccessOption(final String mode, final PopulationFactory populationFactory, final Network network,
			final LeastCostPathCalculator routeAlgo, PlansCalcRouteConfigGroup plansCalcRouteConfigGroup, AccessConfigGroup accessConfig) {
		
		if(!plansCalcRouteConfigGroup.isInsertingAccessEgressWalk()) {
			this.delegate = new NetworkRoutingModule(mode, populationFactory, network, routeAlgo);
		} else {
			this.delegate = new NetworkRoutingInclAccessEgressModule(mode, populationFactory, network, routeAlgo, plansCalcRouteConfigGroup);
		}
		this.mode = mode;
		this.network = network;
		this.accessConfigGroup = accessConfig;
		this.outer = createOuterNet();
		
	}
	
	private Network createOuterNet() {
		
		NetworkFilterManager mng = new NetworkFilterManager(this.network);
		mng.addLinkFilter(new NetworkLinkFilter() {
			
			@Override
			public boolean judgeLink(Link l) {

				Object o = l.getAttributes().getAttribute(accessConfigGroup.getAccessAttribute());
				if(o != null) {
					return ((String)o).equals("no");
				}
				return true;
			
			}
			
		});
		
		return mng.applyFilters();
		
	}
	
	@Override
	public List<? extends PlanElement> calcRoute(Facility<?> fromFacility,
	        Facility<?> toFacility, double departureTime, Person person) {

		Gbl.assertNotNull(fromFacility);
		Gbl.assertNotNull(toFacility);
		
		if(this.mode.equals(this.accessConfigGroup.getMode())) {

			Link fromLink = this.network.getLinks().get(fromFacility.getLinkId());
			Link toLink = this.network.getLinks().get(toFacility.getLinkId());
			
			String vehType = (String)person.getAttributes().getAttribute("vehicleType");
			
			// Evaluate whether the person drives a car with internal combustion engine
			boolean ice = false;
			
			if(vehType != null) {
				if(vehType.equals("gasoline") || vehType.equals("diesel")) ice = true;
			}
			
			
			// if so, route the car leg to a link outside of the city center
			if(ice) {

				if(fromLink.getAttributes().getAttribute(this.accessConfigGroup.getAccessAttribute()) != null &&
						fromLink.getAttributes().getAttribute(this.accessConfigGroup.getAccessAttribute()).equals("yes")) {
					
					fromLink = NetworkUtils.getNearestLinkExactly(this.outer, fromLink.getCoord());
					fromFacility = facilities.getFactory().createActivityFacility(Id.create("from", ActivityFacility.class),
							Id.createLinkId(fromLink.getId()));
					
				}
				
				if(toLink.getAttributes().getAttribute(this.accessConfigGroup.getAccessAttribute()) != null &&
						toLink.getAttributes().getAttribute(this.accessConfigGroup.getAccessAttribute()).equals("yes")) {
					
					toLink = NetworkUtils.getNearestLinkExactly(this.outer, toLink.getCoord());
					toFacility = facilities.getFactory().createActivityFacility(Id.create("to", ActivityFacility.class),
							Id.createLinkId(toLink.getId()));
					
				}
				
			}
			
		}
		
		return this.delegate.calcRoute(fromFacility, toFacility, departureTime, person);
		
	}

	@Override
	public StageActivityTypes getStageActivityTypes() {
		
		return this.delegate.getStageActivityTypes();
		
	}

}
