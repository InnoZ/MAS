package com.innoz.toolbox.matsim.examples;

import java.util.Arrays;
import java.util.HashSet;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.routing.AccessConfigGroup;
import com.innoz.toolbox.matsim.routing.NetworkRoutingWithAccessRestriction;

public class AccessRestrictionExample {

	static final HashSet<String> allowedModes = new HashSet<>(Arrays.asList(new String[] {"car"}));
	
	public static void main(String args[]) {
		
		Config config = ConfigUtils.createConfig();
		
		config.controler().setLastIteration(0);
		config.controler().setOutputDirectory("/home/bmoehring/3connect/TestNetworkRoutingWithAccess/output_test/");
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		ActivityParams params = new ActivityParams();
		params.setActivityType("dummy");
		params.setMinimalDuration(3600);
		params.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(params);
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		
		AccessConfigGroup acg = new AccessConfigGroup();
		acg.setAccessAttribute("innercity");
		acg.setMode(TransportMode.car);
		acg.setExcludedFuelTypes("verbrenner");
		config.addModule(acg);
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		createNetwork(scenario);
		createAgents(scenario);
		
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				addRoutingModuleBinding(TransportMode.car).toProvider(NetworkRoutingWithAccessRestriction.class);
				
			}
		});
		
		controler.run();
		
	}
	
	private static void createNetwork(final Scenario scenario) {
		
		Network network = scenario.getNetwork();
		NetworkFactory factory = network.getFactory();
		
		Node nn0 = factory.createNode(Id.createNodeId("0"), new Coord(0, 0));
		network.addNode(nn0);
		Node nn1 = factory.createNode(Id.createNodeId("1"), new Coord(0, 5000));
		network.addNode(nn1);
		Node nn2 = factory.createNode(Id.createNodeId("2"), new Coord(10000, 0));
		network.addNode(nn2);
		Node nn3 = factory.createNode(Id.createNodeId("3"), new Coord(10000, 5000));
		network.addNode(nn3);
		Node nn4 = factory.createNode(Id.createNodeId("4"), new Coord(4000, 2000));
		network.addNode(nn4);
		Node nn5 = factory.createNode(Id.createNodeId("5"), new Coord(4000, 3000));
		network.addNode(nn5);
		Node nn6 = factory.createNode(Id.createNodeId("6"), new Coord(6000, 2000));
		network.addNode(nn6);
		Node nn7 = factory.createNode(Id.createNodeId("7"), new Coord(6000, 3000));
		network.addNode(nn7);
		{
			Link l01 = factory.createLink(Id.createLinkId("01"), nn0, nn1);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("13"), nn1, nn3);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("32"), nn3, nn2);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("20"), nn2, nn0);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("04"), nn0, nn4);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("15"), nn1, nn5);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("37"), nn3, nn7);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "no");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("26"), nn2, nn6);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("45"), nn4, nn5);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("57"), nn5, nn7);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("76"), nn7, nn6);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		{
			Link l01 = factory.createLink(Id.createLinkId("64"), nn6, nn4);
			l01.setAllowedModes(allowedModes);
			l01.setCapacity(1000);
			l01.setFreespeed(50/3.6);
			l01.setNumberOfLanes(1);
			l01.getAttributes().putAttribute("innercity", "yes");
			network.addLink(l01);
		}
		
	}
	
	private static void createAgents(final Scenario scenario) {
		
		Population population = scenario.getPopulation();
		PopulationFactory factory = population.getFactory();
		
		{
			Person p = factory.createPerson(Id.createPersonId("p0"));
			p.getAttributes().putAttribute("vehicleType", "verbrenner");
			
			Plan plan = factory.createPlan();
			Activity act1 = factory.createActivityFromCoord("dummy", new Coord(10,0));
			act1.setEndTime(6*3600);
			plan.addActivity(act1);
			
			Leg leg = factory.createLeg("car");
			plan.addLeg(leg);
			
			Activity act2 = factory.createActivityFromCoord("dummy", new Coord(4000,2990));
			plan.addActivity(act2);
			
			p.addPlan(plan);
			p.setSelectedPlan(plan);
			population.addPerson(p);
		}
		
		{
			Person p = factory.createPerson(Id.createPersonId("p1"));
			p.getAttributes().putAttribute("vehicleType", "electric");
			
			Plan plan = factory.createPlan();
			Activity act1 = factory.createActivityFromCoord("dummy", new Coord(10,0));
			act1.setEndTime(6*3600);
			plan.addActivity(act1);
			
			Leg leg = factory.createLeg("car");
			plan.addLeg(leg);
			
			Activity act2 = factory.createActivityFromCoord("dummy", new Coord(4000,2990));
			plan.addActivity(act2);
			
			p.addPlan(plan);
			p.setSelectedPlan(plan);
			population.addPerson(p);
		}
		
	}
	
}