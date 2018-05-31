package com.innoz.scenarios.osnabrueck;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.scenarioGeneration.carsharing.CSMembersXmlWriter;

public class Utilities {

	public static void main(String args[]) {
		
//		filterZeroDistanceLegs();
		
		createCsUsers();
		
//		getFractionOfWorkers();
		
	}

	private static void getFractionOfWorkers() {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile("D:/01_Projekte/InnoZ-Simulation/Trendszenario/input_trend/plans_clean_filtered.xml.gz");
		
		int nFemale = 0;
		int nMale = 0;
		int maleEmployed = 0;
		int femaleEmployed = 0;
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			if(person.getAttributes().getAttribute("age") != null &&
					(Integer)person.getAttributes().getAttribute("age") >= 18) {
				if(person.getAttributes().getAttribute("sex").equals("f")) {
					nFemale += 1;
					if(person.getAttributes().getAttribute("employed").equals(true)) {
						femaleEmployed += 1;
					}
				} else {
					nMale += 1;
					if(person.getAttributes().getAttribute("employed").equals(true)) {
						maleEmployed += 1;
					}
				}
			}
		}
		
		System.out.println("Male: " + nMale + "; employed: " + maleEmployed);
		System.out.println("Female: " + nFemale + "; employed: " + femaleEmployed);
	}

	private static void filterZeroDistanceLegs() {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile("/home/bmoehring/3connect/3connect_trend/input_trend/plans_2025.xml.gz");
		
		Scenario scenarioOut = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		Population population = scenario.getPopulation();
		
		for(Person p : population.getPersons().values()) {
			
			boolean addToPopulation = true;
			
			for(PlanElement pe : p.getSelectedPlan().getPlanElements()) {
				
				if(pe instanceof Leg) {

					Leg leg = (Leg) pe;
					if(leg.getRoute().getDistance() < 100) {
						addToPopulation = false;
						break;
					}
					
				}
				
			}
			
			if(addToPopulation) {
				scenarioOut.getPopulation().addPerson(p);
			}
			
		}
		
		new PopulationWriter(scenarioOut.getPopulation()).write("D:/01_Projekte/InnoZ-Simulation/Trendszenario/bestehende_plans_bmoehring/plans_filtered.xml.gz");
	}

	private static void createCsUsers() {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile("/home/bmoehring/3connect/3connect_positiv/input_positiv/plans_2025.xml.gz");
		
		System.out.println(scenario.getPopulation().getPersons().size() + " persons");
		
		MembershipContainer membership = new MembershipContainer();
		
		Scenario scenarioOut = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		Population population = scenario.getPopulation();
		
		int members = 0;
		
		for(Person p : population.getPersons().values()) {
			
			boolean hasZeroDistanceLegs = false;
			
//			if(p.getSelectedPlan().getPlanElements().size() > 1) {
//				
//				for(PlanElement pe : p.getSelectedPlan().getPlanElements()) {
//					if(pe instanceof Leg) {
//						Leg leg = (Leg) pe;
//						if(leg.getRoute().getDistance() < 100) {
//							hasZeroDistanceLegs = true;
//						}
//					}
//				}
//			}
			
			scenarioOut.getPopulation().addPerson(p);
			Map<String, Set<String>> membershipsPerCompany = new HashMap<>();
			Map<String, Set<String>> membershipsPerCSType = new HashMap<>();
			
			if(p.getAttributes().getAttribute("hasLicense") != null && p.getAttributes().getAttribute("hasLicense").equals("yes") &&
					!hasZeroDistanceLegs) {
				membershipsPerCompany.put("stadtteilauto", new HashSet<>(Arrays.asList(new String[] {"oneway","freefloating","twoway"})));
				members ++;
			}
			
			PersonMembership personMembership = new PersonMembership(membershipsPerCompany, membershipsPerCSType);
			membership.addPerson(p.getId().toString(), personMembership);
			
			System.out.println(p.getId().toString() + " " + membershipsPerCompany.toString());
			
		}
		
		System.out.println(membership.getPerPersonMemberships().size() + " CS members defined");
		System.out.println(members);
		
//		new PopulationWriter(scenarioOut.getPopulation()).write("D:/01_Projekte/InnoZ-Simulation/Trendszenario/bestehende_plans_bmoehring/plans_clean.xml.gz");
		new CSMembersXmlWriter(membership).writeFile("/home/bmoehring/3connect/3connect_positiv/input_positiv/carsharingMembers_all.xml");
	}
	
}
