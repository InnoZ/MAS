package com.innoz.toolbox.run;

import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.carsharing.config.CarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.FreeFloatingConfigGroup;
import org.matsim.contrib.carsharing.config.OneWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.TwoWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.control.listeners.CarsharingListener;
import org.matsim.contrib.carsharing.events.handlers.PersonArrivalDepartureHandler;
import org.matsim.contrib.carsharing.manager.CarsharingManagerInterface;
import org.matsim.contrib.carsharing.manager.CarsharingManagerNew;
import org.matsim.contrib.carsharing.manager.demand.CurrentTotalDemand;
import org.matsim.contrib.carsharing.manager.demand.DemandHandler;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipReader;
import org.matsim.contrib.carsharing.manager.routers.RouteCarsharingTrip;
import org.matsim.contrib.carsharing.manager.routers.RouteCarsharingTripImpl;
import org.matsim.contrib.carsharing.manager.routers.RouterProvider;
import org.matsim.contrib.carsharing.manager.routers.RouterProviderImpl;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.contrib.carsharing.manager.supply.costs.CostsCalculatorContainer;
import org.matsim.contrib.carsharing.models.ChooseTheCompany;
import org.matsim.contrib.carsharing.models.ChooseTheCompanyExample;
import org.matsim.contrib.carsharing.models.ChooseVehicleType;
import org.matsim.contrib.carsharing.models.ChooseVehicleTypeExample;
import org.matsim.contrib.carsharing.models.KeepingTheCarModel;
import org.matsim.contrib.carsharing.readers.CarsharingXmlReaderNew;
import org.matsim.contrib.carsharing.replanning.CarsharingSubtourModeChoiceStrategy;
import org.matsim.contrib.carsharing.replanning.RandomTripToCarsharingStrategy;
import org.matsim.contrib.carsharing.router.FreeFloatingRoutingModule;
import org.matsim.contrib.carsharing.router.TwoWayCarsharingRoutingModule;
import org.matsim.contrib.carsharing.scoring.CarsharingScoringFunctionFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;

import com.innoz.toolbox.matsim.sharedMobility.carsharing.CarsharingQsimFactoryNewWithPt;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.KeepingTheCarModelInnoZ;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.MyCarsharingSupplyContainer;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.OneWayCarsharingRoutingModuleInnoZ;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.OsCompanyCostStructure;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.VehicleChoiceAgent;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.VehicleChoiceAgentImpl;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.supply.CarsharingSupplyControlerListener;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.supply.CarsharingSupplyEventHandler;

public class ThreeConnectMain {

	public static void main(String args[]) {
		
		Config config = ConfigUtils.loadConfig(args[0], new CarsharingConfigGroup(), new TwoWayCarsharingConfigGroup(),
				new OneWayCarsharingConfigGroup(), new FreeFloatingConfigGroup());
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(false);
		
		config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.15);
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
			stratSets.setDisableAfter(100);
			stratSets.setStrategyName("RandomTripToCarsharingStrategy");
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.025);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(100);
			stratSets.setStrategyName("CarsharingSubtourModeChoiceStrategy");
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.025);
			config.strategy().addStrategySettings(stratSets);
		}
		
		config.subtourModeChoice().setChainBasedModes(new String[]{TransportMode.bike, TransportMode.car});
		config.subtourModeChoice().setConsiderCarAvailability(true);
		config.subtourModeChoice().setModes(new String[]{TransportMode.bike,TransportMode.car,TransportMode.pt,
				TransportMode.walk});
		
		Scenario scenario = ScenarioUtils.loadScenario(config);

//		scenario.getPopulation().getPersons().values().removeIf(person -> MatsimRandom.getRandom().nextDouble() > 0.1);
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		
		NetworkFilterManager mng = new NetworkFilterManager(scenario.getNetwork());
		mng.addLinkFilter(new NetworkLinkFilter() {
			
			@Override
			public boolean judgeLink(Link l) {
				
				String type = NetworkUtils.getType(l);
				
				if(type != null){
	
					boolean motorway = l.getFreespeed() > 50/3.6;
					
					if(l.getAllowedModes().contains("pt") || motorway) return false;
					
					return true;
					
				}
				
				return false;
				
			}
			
		});
		
		Network carNet = mng.applyFilters();
		// Clean filtered network to remove nodes w/o links attached
		new NetworkCleaner().run(carNet);
		
		scenario.getPopulation().getPersons().values().stream().map(Person::getSelectedPlan).forEach(plan -> {
			
			plan.getPlanElements().stream().forEach(pe -> {
				
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
				
			});
			
		});
		
		Controler controler = new Controler(scenario);
		
		installCarsharing(controler,carNet, args[1]);

		controler.run();
		
	}
	
	private static void installCarsharing(final Controler controler, Network carNet, String path) {
		
		CarsharingXmlReaderNew reader = new CarsharingXmlReaderNew(carNet);
		
		final CarsharingConfigGroup configGroup = (CarsharingConfigGroup)
				controler.getScenario().getConfig().getModules().get(CarsharingConfigGroup.GROUP_NAME);

		reader.readFile(configGroup.getvehiclelocations());
		
		Set<String> carsharingCompanies = reader.getCompanies().keySet();
		
		MembershipReader membershipReader = new MembershipReader();

		membershipReader.readFile(configGroup.getmembership());

		final MembershipContainer memberships = membershipReader.getMembershipContainer();
		
		final CostsCalculatorContainer costsCalculatorContainer = createCompanyCostsStructure(carsharingCompanies, controler.getScenario(), path);
		
		final CarsharingListener carsharingListener = new CarsharingListener();
		final CarsharingSupplyInterface supply = new MyCarsharingSupplyContainer(controler.getScenario());
		supply.populateSupply();
		final KeepingTheCarModel keepingCarModel = new KeepingTheCarModelInnoZ();
		final ChooseTheCompany chooseCompany = new ChooseTheCompanyExample();
		final ChooseVehicleType chooseCehicleType = new ChooseVehicleTypeExample();
		final RouterProvider routerProvider = new RouterProviderImpl();
		final CurrentTotalDemand currentTotalDemand = new CurrentTotalDemand(controler.getScenario().getNetwork());
		final CarsharingManagerInterface carsharingManager = new CarsharingManagerNew();
		final RouteCarsharingTrip routeCarsharingTrip = new RouteCarsharingTripImpl();
		final VehicleChoiceAgent vehicleChoiceAgent = new VehicleChoiceAgentImpl();
		
		//===adding carsharing objects on supply and demand infrastructure ===
		controler.addOverridingModule(new AbstractModule() {

			@Override
			public void install() {
				bind(KeepingTheCarModel.class).toInstance(keepingCarModel);
				bind(ChooseTheCompany.class).toInstance(chooseCompany);
				bind(ChooseVehicleType.class).toInstance(chooseCehicleType);
				bind(RouterProvider.class).toInstance(routerProvider);
				bind(CurrentTotalDemand.class).toInstance(currentTotalDemand);
				bind(RouteCarsharingTrip.class).toInstance(routeCarsharingTrip);
				bind(CostsCalculatorContainer.class).toInstance(costsCalculatorContainer);
				bind(MembershipContainer.class).toInstance(memberships);
			    bind(CarsharingSupplyInterface.class).toInstance(supply);
			    bind(CarsharingManagerInterface.class).toInstance(carsharingManager);
			    bind(VehicleChoiceAgent.class).toInstance(vehicleChoiceAgent);
			    bind(DemandHandler.class).asEagerSingleton();
			}
		});
		
		//=== carsharing specific replanning strategies ===
		controler.addOverridingModule( new AbstractModule() {
			@Override
			public void install() {
				this.addPlanStrategyBinding("RandomTripToCarsharingStrategy").to( RandomTripToCarsharingStrategy.class ) ;
				this.addPlanStrategyBinding("CarsharingSubtourModeChoiceStrategy").to( CarsharingSubtourModeChoiceStrategy.class ) ;
			}
		});
		
		//=== adding qsimfactory, controller listeners and event handlers
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				bindMobsim().toProvider(CarsharingQsimFactoryNewWithPt.class);
		        addControlerListenerBinding().toInstance(carsharingListener);
		        addControlerListenerBinding().to(CarsharingManagerNew.class);		        
		        addEventHandlerBinding().to(PersonArrivalDepartureHandler.class);
		        addEventHandlerBinding().to(DemandHandler.class);
			}
		});
		
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				bindScoringFunctionFactory().to(CarsharingScoringFunctionFactory.class);
			}
		});

		//=== routing modules for carsharing trips ===

		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				addRoutingModuleBinding("twoway").toInstance(new TwoWayCarsharingRoutingModule());
                addRoutingModuleBinding("freefloating").toInstance(new FreeFloatingRoutingModule());
                addRoutingModuleBinding("oneway").toInstance(new OneWayCarsharingRoutingModuleInnoZ());
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
                        }
                        // if the trip doesn't contain a carsharing leg,
                        // fall back to the default identification method.
                        return defaultModeIdentifier.identifyMainMode( tripElements );
                    }
                });
			}
		});
		
		CarsharingSupplyEventHandler eventHandler = new CarsharingSupplyEventHandler();
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				
				addEventHandlerBinding().toInstance(eventHandler);
				TwoWayCarsharingConfigGroup twConfig = (TwoWayCarsharingConfigGroup)controler.getConfig().getModules().get(TwoWayCarsharingConfigGroup.GROUP_NAME);
				OneWayCarsharingConfigGroup owConfig = (OneWayCarsharingConfigGroup)controler.getConfig().getModules().get(OneWayCarsharingConfigGroup.GROUP_NAME);
				addControlerListenerBinding().toInstance(new CarsharingSupplyControlerListener(eventHandler, configGroup, twConfig, owConfig));				
				
			}
		});
		
	}
	
	public static CostsCalculatorContainer createCompanyCostsStructure(Set<String> companies, Scenario scenario, String path) {
		
		CostsCalculatorContainer companyCostsContainer = new CostsCalculatorContainer();
		
		companyCostsContainer.getCompanyCostsMap().put(OsCompanyCostStructure.COMPANY_NAME, OsCompanyCostStructure.create(scenario, path));
		
		return companyCostsContainer;
		
	}
	
}