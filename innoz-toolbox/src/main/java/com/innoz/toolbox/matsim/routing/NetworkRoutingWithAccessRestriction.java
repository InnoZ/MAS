package com.innoz.toolbox.matsim.routing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.TransportModeNetworkFilter;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.SingleModeNetworksCache;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelTime;

public class NetworkRoutingWithAccessRestriction implements Provider<RoutingModule> {

	@Inject
    Map<String, TravelTime> travelTimes;

	@Inject
	Map<String, TravelDisutilityFactory> travelDisutilityFactories;

	@Inject
	SingleModeNetworksCache singleModeNetworksCache;

	@Inject
	PlansCalcRouteConfigGroup plansCalcRouteConfigGroup;

	@Inject
    Network network;

	@Inject
    PopulationFactory populationFactory;

	@Inject
    LeastCostPathCalculatorFactory leastCostPathCalculatorFactory;

	@Inject
	AccessConfigGroup accessConfigGroup;

	@Override
	public RoutingModule get() {
		Network filteredNetwork = null;

		// Ensure this is not performed concurrently by multiple threads!
		synchronized (this.singleModeNetworksCache.getSingleModeNetworksCache()) {
			filteredNetwork = this.singleModeNetworksCache.getSingleModeNetworksCache().get(accessConfigGroup.getMode());
			if (filteredNetwork == null) {
				TransportModeNetworkFilter filter = new TransportModeNetworkFilter(network);
				Set<String> modes = new HashSet<>();
				modes.add(accessConfigGroup.getMode());
				filteredNetwork = NetworkUtils.createNetwork();
				filter.filter(filteredNetwork, modes);
				filteredNetwork.getLinks().values().stream().forEach(l -> {
					l.getAttributes().putAttribute(accessConfigGroup.getAccessAttribute(),
							network.getLinks().get(l.getId()).getAttributes().getAttribute(accessConfigGroup.getAccessAttribute()));
				});
				this.singleModeNetworksCache.getSingleModeNetworksCache().put(accessConfigGroup.getMode(), filteredNetwork);
			}
		}

		TravelDisutilityFactory travelDisutilityFactory = this.travelDisutilityFactories.get(accessConfigGroup.getMode());
		if (travelDisutilityFactory == null) {
			throw new RuntimeException("No TravelDisutilityFactory bound for mode "+accessConfigGroup.getMode()+".");
		}
		TravelTime travelTime = travelTimes.get(accessConfigGroup.getMode());
		if (travelTime == null) {
			throw new RuntimeException("No TravelTime bound for mode "+accessConfigGroup.getMode()+".");
		}
		LeastCostPathCalculator routeAlgo =
				leastCostPathCalculatorFactory.createPathCalculator(
						filteredNetwork,
						travelDisutilityFactory.createTravelDisutility(travelTime),
						travelTime);

		return new NetworkRoutingModuleWithAccessOption(accessConfigGroup.getMode(), populationFactory, filteredNetwork, routeAlgo,
				plansCalcRouteConfigGroup, accessConfigGroup);

	}
	
}