package com.innoz.scenarios.osnabrueck;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipReader;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

public class Analysis {
	
	static String BASE_DIR = "D:/01_Projekte/InnoZ-Simulation/Trendszenario/";
	
	public static void main(String args[]) {
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile(BASE_DIR + "output_trend/output_plans.xml.gz");
		
		MembershipReader mReader = new MembershipReader();
		mReader.readFile(BASE_DIR + "input_trend/memberships_filtered.xml.gz");
		MembershipContainer membership = mReader.getMembershipContainer();
		
		getSocDemAttributesOfCarsharingMembersAndUsers(scenario, membership);
		
	}

	private static void getSocDemAttributesOfCarsharingMembersAndUsers(Scenario scenario, MembershipContainer membership) {
		
		Map<String, Integer> members = new HashMap<String, Integer>();
		Map<String, Integer> users = new HashMap<String, Integer>();
		
		for(Person person : scenario.getPopulation().getPersons().values()) {
			
			PersonMembership pMembership = membership.getPerPersonMemberships().get(person.getId());
			Map<String, Set<String>> companyMembership = pMembership.getMembershipsPerCompany();
			
			if(!companyMembership.isEmpty()) {
				String personHash = createPersonHash(person);
				if(!members.containsKey(personHash)) {
					members.put(personHash, 0);
				}
				members.put(personHash, members.get(personHash) + 1);
			}
			
			for(PlanElement pe : person.getSelectedPlan().getPlanElements()) {
				if(pe instanceof Leg) {
					if(((Leg)pe).getMode().equals("onway") || ((Leg)pe).getMode().equals("twoway") 
							|| ((Leg)pe).getMode().equals("freefloating")) {
						String personHash = createPersonHash(person);
						if(!users.containsKey(personHash)) {
							users.put(personHash, 0);
						}
						users.put(personHash, users.get(personHash) + 1);
						break;
					}
					
				}
				
			}
			
		}
		
		System.out.println("members");
		members.entrySet().stream().forEach(e -> {
			System.out.println(e.getKey() + ": " + e.getValue());
		});
		System.out.println("users:");
		users.entrySet().stream().forEach(e -> {
			System.out.println(e.getKey() + ": " + e.getValue());
		});
		
	}
	
	private static String getAgeHash(int age) {
		
		int base = age / 10;
		return Integer.toString(base * 10) + "-" + Integer.toString((base+1)*10);
		
	}
	
	private static String createPersonHash(Person person) {
		String sex = (String)person.getAttributes().getAttribute("sex");
		int age = ((Integer)person.getAttributes().getAttribute("age")).intValue();
		boolean employed = (Boolean)person.getAttributes().getAttribute("employed");
		String carOwner = (String)person.getAttributes().getAttribute("carAvail");
		return ("sex=" + sex + "_age=" + getAgeHash(age) + "_employed=" + Boolean.toString(employed) +
				"_carAvail=" + carOwner);
	}
	
}