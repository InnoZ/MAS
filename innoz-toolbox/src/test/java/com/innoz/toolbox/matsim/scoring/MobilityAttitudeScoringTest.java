package com.innoz.toolbox.matsim.scoring;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;

import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParams;

public class MobilityAttitudeScoringTest {

	/*
	 * 0--1----------2--3
	 */
	static Config config;
	static Scenario scenario;
	
	@Before
	public void createTestScenario() {
		
		config = ConfigUtils.createConfig();

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(10);
		config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta.name());
			stratSets.setWeight(0.5);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ChangeTripMode.name());
			stratSets.setWeight(0.5);
			config.strategy().addStrategySettings(stratSets);
			
			config.changeLegMode().setModes(new String[]{TransportMode.car, TransportMode.bike});
			
		}
		
		{
			ActivityParams params = new ActivityParams("home");
			params.setScoringThisActivityAtAll(true);
			params.setTypicalDuration(6 * 3600);
			config.planCalcScore().addActivityParams(params);
		}
		{
			ActivityParams params = new ActivityParams("work");
			params.setScoringThisActivityAtAll(true);
			params.setTypicalDuration(12 * 3600);
			config.planCalcScore().addActivityParams(params);
		}
		
		MobilityAttitudeConfigGroup macg = new MobilityAttitudeConfigGroup();
		macg.setSubpopulationAttribute("mobilityAttitude");
		{
			MobilityAttitudeModeParams pars = new MobilityAttitudeModeParams();
			pars.setAttitudeGroup("carAffine");
			pars.setOffsetForMode(TransportMode.car, 100.0);
			macg.getModeParams().put(pars.getAttitudeGroup(), pars);
		}
		{
			MobilityAttitudeModeParams pars = new MobilityAttitudeModeParams();
			pars.setAttitudeGroup("bikeAffine");
			pars.setOffsetForMode(TransportMode.bike, 100.0);
			macg.getModeParams().put(pars.getAttitudeGroup(), pars);
		}
		config.addModule(macg);
		
		scenario = ScenarioUtils.createScenario(config);
		
		createNetwork();
		createPopulation();
		
		ScenarioUtils.loadScenario(scenario);
		
	}
	
	private void createNetwork() {
		
		NetworkFactory netFactory = scenario.getNetwork().getFactory();
		Node n0 = netFactory.createNode(Id.createNodeId("0"), new Coord(0,0));
		scenario.getNetwork().addNode(n0);
		Node n1 = netFactory.createNode(Id.createNodeId("1"), new Coord(20,0));
		scenario.getNetwork().addNode(n1);
		Node n2 = netFactory.createNode(Id.createNodeId("2"), new Coord(9960,0));
		scenario.getNetwork().addNode(n2);
		Node n3 = netFactory.createNode(Id.createNodeId("3"), new Coord(10000,0));
		scenario.getNetwork().addNode(n3);
		{
			Link l = netFactory.createLink(Id.createLinkId("01"), n0, n1);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		{
			Link l = netFactory.createLink(Id.createLinkId("12"), n1, n2);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		{
			Link l = netFactory.createLink(Id.createLinkId("23"), n2, n3);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		
	}
	
	public void createPopulation() {
		
		PopulationFactory popFactory = scenario.getPopulation().getFactory();
		
		{
			Person person = popFactory.createPerson(Id.createPersonId("0"));
			scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(), "mobilityAttitude", "carAffine");
			Plan plan = popFactory.createPlan();
			Activity home = popFactory.createActivityFromCoord("home", new Coord(0,0));
			home.setEndTime(6 * 3600);
			plan.addActivity(home);
			plan.addLeg(popFactory.createLeg("car"));
			Activity work = popFactory.createActivityFromCoord("work", new Coord(10000,0));
			plan.addActivity(work);
			person.addPlan(plan);
			person.setSelectedPlan(plan);
			scenario.getPopulation().addPerson(person);
		}
		{
			Person person = popFactory.createPerson(Id.createPersonId("1"));
			scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(), "mobilityAttitude", "bikeAffine");
			Plan plan = popFactory.createPlan();
			Activity home = popFactory.createActivityFromCoord("home", new Coord(0,0));
			home.setEndTime(6 * 3600);
			plan.addActivity(home);
			plan.addLeg(popFactory.createLeg("bike"));
			Activity work = popFactory.createActivityFromCoord("work", new Coord(10000,0));
			plan.addActivity(work);
			person.addPlan(plan);
			person.setSelectedPlan(plan);
			scenario.getPopulation().addPerson(person);
		}
		
	}
	
	@Test
	public void testScoring() {
		
		Controler controler = new Controler(scenario);
		
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
		
				bindScoringFunctionFactory().to(MobilityAttitudeScoringFunctionFactory.class);
				
			}

		});
		
		controler.run();
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			Plan selected = person.getSelectedPlan();
			
			Leg leg = (Leg)selected.getPlanElements().get(1);
			
			double actualScore = selected.getScore();
			double expectedScore = 0d;
			
			String expectedTransportMode = "";
			
			if(person.getId().toString().equals("0")) {
				
				expectedScore = 246.5579620441559;
				expectedTransportMode = TransportMode.car;
				
			} else {
				
				expectedScore = 240.4405848483607;
				expectedTransportMode = TransportMode.bike;
				
			}
			
			assertEquals("Agent " + person.getId().toString() + " uses wrong transportMode", expectedTransportMode, leg.getMode());
			assertEquals("Mobility attitude scoring failed!", expectedScore, actualScore, 0.00000001);
			
		}
		
	}
	
}