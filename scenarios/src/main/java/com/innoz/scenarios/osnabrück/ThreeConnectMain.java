package com.innoz.scenarios.osnabrück;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
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
import org.matsim.contrib.carsharing.manager.demand.RentalInfo;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipReader;
import org.matsim.contrib.carsharing.manager.routers.RouteCarsharingTrip;
import org.matsim.contrib.carsharing.manager.routers.RouteCarsharingTripImpl;
import org.matsim.contrib.carsharing.manager.routers.RouterProvider;
import org.matsim.contrib.carsharing.manager.routers.RouterProviderImpl;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.contrib.carsharing.manager.supply.costs.CompanyCosts;
import org.matsim.contrib.carsharing.manager.supply.costs.CostCalculation;
import org.matsim.contrib.carsharing.manager.supply.costs.CostsCalculatorContainer;
import org.matsim.contrib.carsharing.models.ChooseTheCompany;
import org.matsim.contrib.carsharing.models.ChooseTheCompanyExample;
import org.matsim.contrib.carsharing.models.ChooseVehicleType;
import org.matsim.contrib.carsharing.models.ChooseVehicleTypeExample;
import org.matsim.contrib.carsharing.models.KeepingTheCarModel;
import org.matsim.contrib.carsharing.models.KeepingTheCarModelExample;
import org.matsim.contrib.carsharing.readers.CarsharingXmlReaderNew;
import org.matsim.contrib.carsharing.replanning.CarsharingSubtourModeChoiceStrategy;
import org.matsim.contrib.carsharing.replanning.RandomTripToCarsharingStrategy;
import org.matsim.contrib.carsharing.runExample.CarsharingUtils;
import org.matsim.contrib.carsharing.scoring.CarsharingScoringFunctionFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
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
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;

import com.innoz.toolbox.matsim.carsharing.CarsharingQsimFactoryNewWithPt;
import com.innoz.toolbox.matsim.carsharing.MyCarsharingSupplyContainer;
import com.innoz.toolbox.matsim.carsharing.ServiceArea;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParameterSet;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParams;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeScoringFunctionFactory;

public class ThreeConnectMain {

	public static void main(String args[]) {
		
		Config config = ConfigUtils.loadConfig(args[0], new CarsharingConfigGroup(), new TwoWayCarsharingConfigGroup(),
				new OneWayCarsharingConfigGroup(), new FreeFloatingConfigGroup());
		
		addMobilityAttitudeParams(config);
		
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
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(-1);
//			stratSets.setStrategyName(DefaultStrategy.TimeAllocationMutator_ReRoute.name());
//			stratSets.setSubpopulation(null);
//			stratSets.setWeight(0.1);
//			config.strategy().addStrategySettings(stratSets);
//		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(100);
			stratSets.setStrategyName("RandomTripToCarsharingStrategy");
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.05);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(100);
			stratSets.setStrategyName("CarsharingSubtourModeChoiceStrategy");
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.05);
			config.strategy().addStrategySettings(stratSets);
		}
		
		config.subtourModeChoice().setChainBasedModes(new String[]{TransportMode.bike, TransportMode.car});
		config.subtourModeChoice().setConsiderCarAvailability(true);
		config.subtourModeChoice().setModes(new String[]{TransportMode.bike,TransportMode.car,TransportMode.pt,
				TransportMode.walk});
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
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
		
		installCarsharing(controler,carNet);

		controler.run();
		
	}
	
	private static void installCarsharing(final Controler controler, Network carNet) {
		
		CarsharingXmlReaderNew reader = new CarsharingXmlReaderNew(carNet);
		
		final CarsharingConfigGroup configGroup = (CarsharingConfigGroup)
				controler.getScenario().getConfig().getModules().get(CarsharingConfigGroup.GROUP_NAME);

		reader.readFile(configGroup.getvehiclelocations());
		
		Set<String> carsharingCompanies = reader.getCompanies().keySet();
		
		MembershipReader membershipReader = new MembershipReader();
		
		membershipReader.readFile(configGroup.getmembership());

		final MembershipContainer memberships = membershipReader.getMembershipContainer();
		
		final CostsCalculatorContainer costsCalculatorContainer = createCompanyCostsStructure(carsharingCompanies, controler.getScenario());
		
		final CarsharingListener carsharingListener = new CarsharingListener();
		final CarsharingSupplyInterface carsharingSupplyContainer = new MyCarsharingSupplyContainer(controler.getScenario());
		carsharingSupplyContainer.populateSupply();
		final KeepingTheCarModel keepingCarModel = new KeepingTheCarModelExample();
		final ChooseTheCompany chooseCompany = new ChooseTheCompanyExample();
		final ChooseVehicleType chooseCehicleType = new ChooseVehicleTypeExample();
		final RouterProvider routerProvider = new RouterProviderImpl();
		final CurrentTotalDemand currentTotalDemand = new CurrentTotalDemand(controler.getScenario().getNetwork());
		final CarsharingManagerInterface carsharingManager = new CarsharingManagerNew();
		final RouteCarsharingTrip routeCarsharingTrip = new RouteCarsharingTripImpl();
		
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
			    bind(CarsharingSupplyInterface.class).toInstance(carsharingSupplyContainer);
			    bind(CarsharingManagerInterface.class).toInstance(carsharingManager);
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
				bindScoringFunctionFactory().to(CarsharingScoringFunctionFactory.class);		      
		        addEventHandlerBinding().to(PersonArrivalDepartureHandler.class);
		        addEventHandlerBinding().to(DemandHandler.class);
			}
		});
		//=== adding carsharing specific scoring factory ===
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				        
				bindScoringFunctionFactory().to(MobilityAttitudeScoringFunctionFactory.class);
//				bindScoringFunctionFactory().to(CarsharingScoringFunctionFactory.class);	
			}
		});

		//=== routing modules for carsharing trips ===

		controler.addOverridingModule(CarsharingUtils.createRoutingModule());
		
	}
	
	public static CostsCalculatorContainer createCompanyCostsStructure(Set<String> companies, Scenario scenario) {
		
		CostsCalculatorContainer companyCostsContainer = new CostsCalculatorContainer();
		
		ServiceArea area = new ServiceArea();
		area.init("/home/dhosse/01_Projects/3connect/serviceAreaUTM32N.shp");
		
		for (String s : companies) {
			
			Map<String, CostCalculation> costCalculations = new HashMap<String, CostCalculation>();
			
			//=== here customizable cost structures come in ===
			//===what follows is just an example!! and should be modified according to the study at hand===
			costCalculations.put("freefloating", new CostCalculationOS(scenario.getConfig().getModules().
					get(FreeFloatingConfigGroup.GROUP_NAME),
					scenario, area));
			costCalculations.put("twoway", new CostCalculationOS(scenario.getConfig().getModules().
					get(TwoWayCarsharingConfigGroup.GROUP_NAME),
					scenario, area));
			CompanyCosts companyCosts = new CompanyCosts(costCalculations);
			
			companyCostsContainer.getCompanyCostsMap().put(s, companyCosts);
		}
		
		return companyCostsContainer;
		
	}
	
	public static class CostCalculationOS implements CostCalculation {
		
		ConfigGroup cg;
		Scenario scenario;
		ServiceArea area;
		
		public CostCalculationOS(ConfigGroup cg, Scenario scenario, ServiceArea area) {
			this.cg = cg;
			this.scenario = scenario;
			this.area = area;
		}

		@Override
		public double getCost(RentalInfo rentalInfo) {

			double rentalTIme = rentalInfo.getEndTime() - rentalInfo.getStartTime();
			double distance = rentalInfo.getDistance();
			
			double timeCost = 0.0d;
			double distanceCost = 0.0d;
			double parkingCost = 0.0d;
			
			if(cg instanceof FreeFloatingConfigGroup){
				
				timeCost = Double.parseDouble(((FreeFloatingConfigGroup)cg).timeFeeFreeFloating());
				
				if(rentalInfo.getEndTime() <= 7 * 3600 || rentalInfo.getStartTime() >= 23 * 3600 ){
					timeCost = 2.4 / 3600;
				}
				
				distanceCost = Double.parseDouble(((FreeFloatingConfigGroup)cg).distanceFeeFreeFloating());
				
				if(distance >= 101000){
					distanceCost = 0.25 / 1000;
				}
				
				Coord startCoord = this.scenario.getNetwork().getLinks().get(rentalInfo.getOriginLinkId()).getCoord();
				Coord endCoord = this.scenario.getNetwork().getLinks().get(rentalInfo.getEndLinkId()).getCoord();
				
				if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 0d;
					
				} else if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 5d;
					
				} else if(area.getServiceArea().get("10Euro").contains(MGC.coord2Point(endCoord))){
					
					parkingCost = 10d;
					
				}
				
				if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(startCoord))){
					
					if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
						
						parkingCost -= 5;
						
					}
					
				} else if(area.getServiceArea().get("10Euro").contains(MGC.coord2Point(startCoord))){
					
					if(area.getServiceArea().get("0Euro").contains(MGC.coord2Point(endCoord))){
						
						parkingCost -= 10;
						
					} else if(area.getServiceArea().get("5Euro").contains(MGC.coord2Point(endCoord))){
				
						parkingCost -= 5;
						
					}
					
				}

			} else if(cg instanceof TwoWayCarsharingConfigGroup){
				
				timeCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).timeFeeTwoWayCarsharing());
				distanceCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).distanceFeeTwoWayCarsharing());
				
				if(rentalInfo.getEndTime() <= 7 * 3600 && rentalInfo.getStartTime() >= 24 * 3600 ){
					timeCost = 0.5 / 3600;
				}
				
				distanceCost = Double.parseDouble(((TwoWayCarsharingConfigGroup)cg).distanceFeeTwoWayCarsharing());
				
				if(distance >= 101000){
					distanceCost = 0.25 / 1000;
				}
				
			}
			
			return rentalTIme * timeCost + distance * distanceCost + parkingCost;
			
		}

	}
	
	private static void addMobilityAttitudeParams(final Config config) {
		
		MobilityAttitudeConfigGroup group = new MobilityAttitudeConfigGroup();
		
		group.setSubpopulationAttribute("mobilityAttitude");
		group.setScaleFactor(1f);
		
		//tradCar
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("tradCar");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.5509808129);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-0.6728124661);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-1.3728579165);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(-0.6857201345);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(-0.6857201345);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		//flexCar
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("flexCar");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.6380481272);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.1034697375);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-0.2605919306);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(0.1112731663);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(0.1112731663);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		//urbanPt
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("urbanPt");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.7808252647);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-1.6880941473);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.700257666);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(-0.6383066621);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(-0.6383066621);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		//convBike
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("convBike");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.582954545);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.5985309311);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.3420721502);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(-0.763100895);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(-0.763100895);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		//envtPtBike
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("envtPtBike");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.5953667551);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.4392845925);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.5825576487);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(1.0688883612);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(1.0688883612);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		//multiOpt
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("multiOpt");
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.3446035305);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.3974286492);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.2730661675);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("freefloating_vehicle");
				params.setOffset(0.7339369662);
				set.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode("twoway_vehicle");
				params.setOffset(0.7339369662);
				set.addModeParams(params);
			}
			group.addModeParams(set);
		}
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup("none");
			group.addModeParams(set);
		}
		{
			MobilityAttitudeModeParameterSet set = new MobilityAttitudeModeParameterSet();
			set.setAttitudeGroup(null);
			group.addModeParams(set);
		}
		
		config.addModule(group);
		
//		Traditionelle Auto-Affine	0,5509808129	-0,6728124661	-1,3728579165	-1,3610076297	-0,6857201345
//		Flexible Auto-Affine	0,6380481272	0,1034697375	-0,2605919306	0,084952628	0,1112731663
//		Urban-orientierte ÖV-Affine 	-0,7808252647	-1,6880941473	0,700257666	0,1119985679	-0,6383066621
//		Konventionelle Fahrrad-Affine 	-0,582954545	0,5985309311	0,3420721502	0,2053885907	-0,763100895
//		Umweltbewusste ÖV- und Rad-Affine 	-0,5953667551	0,4392845925	0,5825576487	0,605441547	1,0688883612
//		Innovative technikaffine Multioptionale 	0,3446035305	0,3974286492	0,2730661675	0,3151664461	0,7339369662

		
	}
	
}