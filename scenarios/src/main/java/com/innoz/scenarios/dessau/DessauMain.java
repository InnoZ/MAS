package com.innoz.scenarios.dessau;

import java.util.Arrays;
import java.util.Random;

import org.matsim.api.core.v01.Scenario;
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
import org.matsim.contrib.carsharing.qsim.CarsharingQsimFactoryNew;
import org.matsim.contrib.carsharing.readers.CarsharingXmlReaderNew;
import org.matsim.contrib.carsharing.replanning.CarsharingSubtourModeChoiceStrategy;
import org.matsim.contrib.carsharing.replanning.RandomTripToCarsharingStrategy;
import org.matsim.contrib.carsharing.runExample.CarsharingUtils;
import org.matsim.contrib.carsharing.scoring.CarsharingScoringFunctionFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.sharedMobility.carsharing.KeepingTheCarModelInnoZ;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.MyCarsharingSupplyContainer;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.TeilautoCompanyCostStructure;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.supply.CarsharingSupplyControlerListener;
import com.innoz.toolbox.matsim.sharedMobility.carsharing.supply.CarsharingSupplyEventHandler;

public class DessauMain {

	static String filebase = "/home/dhosse/scenarios/main/15001_2017/";
	
	public static void main(String args[]) {

		Config config = ConfigUtils.loadConfig(filebase + "config.xml.gz");

		config.plansCalcRoute().setInsertingAccessEgressWalk(false);
		
		CarsharingConfigGroup csConfig = new CarsharingConfigGroup();
		csConfig.setmembership(filebase + "csMembers.xml");
		csConfig.setStatsWriterFrequency("5");
		csConfig.setvehiclelocations(filebase + "csVehicles.xml");
		config.addModule(csConfig);
		
		TwoWayCarsharingConfigGroup twConfig = new TwoWayCarsharingConfigGroup();
		twConfig.setConstantTwoWayCarsharing("0");
		twConfig.setsearchDistance("100000");
		twConfig.setUseTwoWayCarsharing(true);
		twConfig.setUtilityOfTravelling("-6");
		twConfig.setvehiclelocations(filebase + "csVehicles.xml");
		config.addModule(twConfig);
		
		FreeFloatingConfigGroup ffConfig = new FreeFloatingConfigGroup();
		ffConfig.setUseFeeFreeFloating(false);
		config.addModule(ffConfig);
		OneWayCarsharingConfigGroup owConfig = new OneWayCarsharingConfigGroup();
		owConfig.setUseOneWayCarsharing(false);
		config.addModule(owConfig);
		
		config.plans().setInputFile(filebase + "plans.xml.gz");
		
		config.controler().setOutputDirectory(filebase + "output");
		config.controler().setLastIteration(50);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		config.qsim().setEndTime(30*3600);
		config.qsim().setMainModes(Arrays.asList(new String[] {"car","twoway_vehicle"}));
		
		config.subtourModeChoice().setChainBasedModes(new String[]{"car","bike"});
		config.subtourModeChoice().setModes(new String[] {"car","bike","pt", "walk"});
		
		{

			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
			
		}
		
		{

			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setWeight(0.2);
			config.strategy().addStrategySettings(stratSets);
			
		}
		
		{

			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
			
		}
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(25);
//			stratSets.setStrategyName("RandomTripToCarsharingStrategy");
//			stratSets.setSubpopulation(null);
//			stratSets.setWeight(0.05);
//			config.strategy().addStrategySettings(stratSets);
//		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(25);
			stratSets.setStrategyName("CarsharingSubtourModeChoiceStrategy");
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.05);
			config.strategy().addStrategySettings(stratSets);
		}
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Random random = MatsimRandom.getRandom();
		scenario.getPopulation().getPersons().values().removeIf(p -> random.nextDouble() > 0.1);
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		
		Controler controler = new Controler(scenario);
		
		installCarsharing(controler, "");
		
		controler.run();
		
	}
	
	private static void installCarsharing(final Controler controler, String path) {
		
		CarsharingXmlReaderNew reader = new CarsharingXmlReaderNew(controler.getScenario().getNetwork());
		
		final CarsharingConfigGroup configGroup = (CarsharingConfigGroup)
				controler.getScenario().getConfig().getModules().get(CarsharingConfigGroup.GROUP_NAME);

		reader.readFile(configGroup.getvehiclelocations());
		
		MembershipReader membershipReader = new MembershipReader();

		membershipReader.readFile(configGroup.getmembership());

		final MembershipContainer memberships = membershipReader.getMembershipContainer();
		
		final CostsCalculatorContainer costsCalculatorContainer = createCompanyCostsStructure();
		
		final CarsharingListener carsharingListener = new CarsharingListener();
		final CarsharingSupplyInterface carsharingSupplyContainer = new MyCarsharingSupplyContainer(controler.getScenario());
		carsharingSupplyContainer.populateSupply();
		final KeepingTheCarModel keepingCarModel = new KeepingTheCarModelInnoZ();
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
				bindMobsim().toProvider(CarsharingQsimFactoryNew.class);
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
				        
//				bindScoringFunctionFactory().to(MobilityAttitudeScoringFunctionFactory.class);
				bindScoringFunctionFactory().to(CarsharingScoringFunctionFactory.class);	
			}
		});

		//=== routing modules for carsharing trips ===
		controler.addOverridingModule(CarsharingUtils.createRoutingModule());
		
		// Example usage of carsharing supply mutation
		final CarsharingSupplyEventHandler handler = new CarsharingSupplyEventHandler();
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				addEventHandlerBinding().toInstance(handler);
			}
			
		});
		controler.addControlerListener(new CarsharingSupplyControlerListener(handler, configGroup,
				(TwoWayCarsharingConfigGroup)controler.getConfig().getModules().get(TwoWayCarsharingConfigGroup.GROUP_NAME),
				(OneWayCarsharingConfigGroup)controler.getConfig().getModules().get(OneWayCarsharingConfigGroup.GROUP_NAME)));
		
	}
	
	public static CostsCalculatorContainer createCompanyCostsStructure() {
		
		CostsCalculatorContainer companyCostsContainer = new CostsCalculatorContainer();
		
		companyCostsContainer.getCompanyCostsMap().put(TeilautoCompanyCostStructure.COMPANY_NAME,
				TeilautoCompanyCostStructure.create());
		
		return companyCostsContainer;
		
	}
	
}

//Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//new PopulationReader(scenario).readFile(filebase + "plans.xml.gz");
//
//MembershipContainer container = new MembershipContainer();
//
//scenario.getPopulation().getPersons().values().stream().forEach(person -> {
//	
//	Map<String, Set<String>> membershipsPerCompany = new HashMap<>();
//	membershipsPerCompany.put("teilauto", new HashSet<>(Arrays.asList(new String[] {"twoway"})));
//	Map<String, Set<String>> membershipsPerCSType = new HashMap<>();
//	membershipsPerCSType.put("twoway", new HashSet<>(Arrays.asList(new String[] {"teilauto"})));
//	
//	PersonMembership personMembership = new PersonMembership(membershipsPerCompany, membershipsPerCSType);
//	container.addPerson(person.getId().toString(), personMembership);
//	
//});
//
//new CSMembersXmlWriter(container).writeFile(filebase + "csMembers.xml"); 