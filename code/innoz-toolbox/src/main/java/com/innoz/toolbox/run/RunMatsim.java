package com.innoz.toolbox.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.withinday.controller.WithinDayControlerListener;
import org.matsim.withinday.controller.WithinDayModule;

/**
 * 
 * Entry point for a minimal execution of the MATSim controler. To execute it, just run the main method.
 * No additional settings are made aside from the settings in the config file given as the only argument.
 * 
 * @author dhosse
 *
 */
public class RunMatsim {

	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig(args[0]);
		
		config.qsim().setEndTime(30*3600);
		
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.TimeAllocationMutator_ReRoute.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
		
		config.subtourModeChoice().setChainBasedModes(new String[]{TransportMode.bike, TransportMode.car});
		config.subtourModeChoice().setConsiderCarAvailability(true);
		config.subtourModeChoice().setModes(new String[]{TransportMode.bike,TransportMode.car,TransportMode.pt,TransportMode.walk});
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		NetworkFilterManager mng = new NetworkFilterManager(scenario.getNetwork());
		mng.addLinkFilter(new NetworkLinkFilter() {
			
			@Override
			public boolean judgeLink(Link l) {
				
				if(((LinkImpl)l).getType() != null){

					boolean motorway = ((LinkImpl)l).getType().equals("motorway") || ((LinkImpl)l).getType().equals("motorway_link")
							|| ((LinkImpl)l).getType().equals("trunk") || ((LinkImpl)l).getType().equals("trunk_link");
					
					if(l.getAllowedModes().contains("pt") || motorway) return false;
					
					return true;
					
				}
				
				return false;
				
			}
			
		});
		
		Network carNet = mng.applyFilters();
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			for(Plan plan : person.getPlans()){
				
				for(PlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof Activity){
						
						Activity act = (Activity)pe;
						Link l = NetworkUtils.getNearestLink(carNet, act.getCoord());
						act.setLinkId(l.getId());
						
					} else {
						
						Leg leg = (Leg)pe;
						if(leg.getMode().equals(TransportMode.walk)){
							
							Coord prev = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)-1)).getCoord();
							Coord next = ((Activity)plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)+1)).getCoord();
							if(CoordUtils.calcEuclideanDistance(prev, next) > 5000){
								leg.setMode(TransportMode.other);
							}
							
						}
						
					}
					
				}
				
			}
			
		}
		
		Controler controler = new Controler(scenario);

//		WithinDayControlerListener ctrl = new WithinDayControlerListener();
//		ctrl.setWithinDayTripRouterFactory(controler.getTripRouterProvider());
//		ctrl.setLeastCostPathCalculatorFactory(controler.getLeastCostPathCalculatorFactory());
//		ctrl.setNumberOfReplanningThreads(4);
//		ctrl.setTransitRouterFactory(transitRouterFactory);
//		controler.addOverridingModule(new AbstractModule() {
//			
//			@Override
//			public void install() {
//				install(new WithinDayModule());
//				addControlerListenerBinding().to(WithinDayControlerListener.class);
//			}
//		});
		
		controler.run();
		
	}
	
}