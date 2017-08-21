package com.innoz.scenarios.osnabrueck;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipReader;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitScheduleReader;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleReaderV1;
import org.matsim.vehicles.VehicleWriterV1;

import com.innoz.toolbox.utils.analysis.LegModeDistanceDistribution;

public class OsUtilities {

	// Method for modal split analysis of 3connect scenarios
	public static void main(String args[]) {
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

		new PopulationReader(scenario).readFile(args[0]);
		
		scenario.getPopulation().getPersons().values().stream().forEach(person -> PersonUtils.removeUnselectedPlans(person));
		
//		samplePopulation(scenario.getPopulation());
//		extractCsUsers(scenario.getPopulation());
		
		lmdd(scenario);
		getToActivityTypesForCsLegs(scenario);
		getSubstitutedModesForCsLegs(scenario, args[1]);
		
	}
	
	static void extractCsUsers(Population population) {
		
		MembershipReader reader = new MembershipReader();
		reader.readFile("/home/dhosse/scenarios/3connect/csMembers.xml");
		
		Scenario scenarioOut = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		for(Entry<Id<Person>,PersonMembership> entry : reader.getMembershipContainer().getPerPersonMemberships().entrySet()){
			
			PersonMembership m = entry.getValue();
			
			for(Entry<String, Set<String>> entries : m.getMembershipsPerCSType().entrySet()){
				if(entries.getKey().equals("freefloating") || entries.getKey().equals("twoway")){
					scenarioOut.getPopulation().addPerson(population.getPersons().get(entry.getKey()));
					break;
				}
			}
			
		}
		
		new PopulationWriter(scenarioOut.getPopulation()).write("/home/dhosse/samplePlans.xml.gz");
		
	}
	
	static void samplePopulation(Population population){
		
		double p = 0.1;
		Random random = MatsimRandom.getRandom();
		
		Scenario scenarioOut = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		for(Person person : population.getPersons().values()){
			
			if(random.nextDouble() <= p){
				
				scenarioOut.getPopulation().addPerson(person);
				
			}
			
		}
		
		new PopulationWriter(scenarioOut.getPopulation()).write("/home/dhosse/scenarios/3connect/samplePlans.xml.gz");
		
	}
	
	static void filterTransitVehicles(Scenario scenario){
		
		Set<Id<Vehicle>> vehiclesWithDepartures = new HashSet<>();
		
		new TransitScheduleReader(scenario).readFile("/home/dhosse/osGtfs/scheduleSimplified.xml.gz");
		new VehicleReaderV1(scenario.getTransitVehicles()).readFile("/home/dhosse/osGtfs/transitVehicles.xml.gz");
		
		for(TransitLine line : scenario.getTransitSchedule().getTransitLines().values()){
			
			for(TransitRoute route : line.getRoutes().values()){

				for(Departure dep : route.getDepartures().values()){
					
					vehiclesWithDepartures.add(dep.getVehicleId());
					
				}
				
			}
			
		}
		
		Scenario scenarioOut = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		for(Vehicle vehicle : scenario.getTransitVehicles().getVehicles().values()){
			
			if(vehiclesWithDepartures.contains(vehicle.getId())){
				
				if(!scenarioOut.getTransitVehicles().getVehicleTypes().containsKey(vehicle.getType().getId())){
				
					scenarioOut.getTransitVehicles().addVehicleType(vehicle.getType());
				
				}
				
				scenarioOut.getTransitVehicles().addVehicle(vehicle);
				
			}
			
		}
		
		new VehicleWriterV1(scenarioOut.getTransitVehicles()).writeFile("/home/dhosse/osGtfs/transitVehiclesFiltered.xml.gz");
		
	}
	
	static void lmdd(Scenario scenario) {
		
		scenario.getPopulation().getPersons().entrySet().removeIf(entry -> !entry.getKey().toString().startsWith("03404"));
		
		LegModeDistanceDistribution lmdd = new LegModeDistanceDistribution();
		lmdd.init(scenario);
		lmdd.preProcessData();
		lmdd.postProcessData();
		lmdd.writeResults("/home/dhosse/osGtfs/");
		
	}
	
	static void assignCsMembers(Scenario scenario) throws IOException{
		
		new PopulationReader(scenario).readFile("/home/dhosse/osGtfs/run3/output_plans.xml.gz");
		ObjectAttributes attributes = new ObjectAttributes();
		new ObjectAttributesXmlReader(attributes).readFile("/home/dhosse/osGtfs/run3/output_personAttributes.xml.gz");
		
		Set<Id<Person>> csMembers = new HashSet<>();
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			if(attributes.getAttribute(person.getId().toString(), "OW_CARD") != null){
				
				csMembers.add(person.getId());
				
			}
			
		}
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/osGtfs/csMembers.xml");
		
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		writer.newLine();
		writer.write("<!DOCTYPE memberships SYSTEM \"CSMembership.dtd\">");
		writer.newLine();
		writer.write("<memberships>");
		writer.newLine();
		
		for(Person person : scenario.getPopulation().getPersons().values()){

			Id<Person> personId = person.getId();
			
			writer.write("<person id=\"" + personId.toString() + "\">");
			writer.newLine();
			writer.write("<company id=\"stadtteilauto\">");
			writer.newLine();
				if(csMembers.contains(personId)){
				writer.write("<carsharing name=\"twoway\"/>");
				writer.newLine();
				writer.write("<carsharing name=\"freefloating\"/>");
				writer.newLine();
			}
			writer.write("</company>");
			writer.newLine();
			writer.write("</person>");
			writer.newLine();
			
		}
		
		writer.write("</memberships>");
		writer.flush();
		writer.close();

		
	}
	
	static void getToActivityTypesForCsLegs(Scenario scenario){
		
		Map<String, Integer> actType2Count = new HashMap<>();
		actType2Count.put("home", 0);
		actType2Count.put("work", 0);
		actType2Count.put("shopping", 0);
		actType2Count.put("leisure", 0);
		actType2Count.put("education", 0);
		actType2Count.put("kindergarten", 0);
		actType2Count.put("other", 0);
		
		Set<Coord> endCoords = new HashSet<>();
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			boolean nextElement = false;
			
			for(PlanElement pe : person.getSelectedPlan().getPlanElements()){
				
				if(pe instanceof Leg){
					
					if(((Leg)pe).getMode().equals("twoway")||((Leg)pe).getMode().equals("freefloating")){
						nextElement = true;
					}
					
				} else if(pe instanceof Activity && nextElement){
					
					String type = ((Activity)pe).getType();
					if(!type.contains("interaction")){
						int count = actType2Count.get(type) + 1;
						actType2Count.put(type, count);
						nextElement = false;
						endCoords.add(((Activity)pe).getCoord());
					}
					
				}
				
			}
			
		}
		
		for(Entry<String, Integer> entry : actType2Count.entrySet()){
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/scenarios/3connect/endCoords.csv");
		try {
		
			writer.write("x;y");
			
			for(Coord c : endCoords){
				
				writer.newLine();
				writer.write(c.getX() + ";" + c.getY());
				
			}
			
			writer.close();
		
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	static Set<Id<Person>> carsharingUsers = new HashSet<>();
	
	static void getSubstitutedModesForCsLegs(Scenario scenario, String referencePlansFile){
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			Plan plan = person.getSelectedPlan();
			purge(plan);
			
		}
		
		Scenario reference = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(reference).readFile(referencePlansFile);
		
		for(Person person : reference.getPopulation().getPersons().values()){
			
			PersonUtils.removeUnselectedPlans(person);
			
			Plan plan = person.getSelectedPlan();
			
			purge(plan);
			
		}
		
		Map<String, Integer> substituteModeCount = new HashMap<>();
		
		for(Id<Person> personId : carsharingUsers){
			
			Person ref = reference.getPopulation().getPersons().get(personId);
			Person p = scenario.getPopulation().getPersons().get(personId);
			
			for(PlanElement pe : p.getSelectedPlan().getPlanElements()){
				
				if(pe instanceof Leg){
					
					Leg leg = (Leg)pe;
					
					if(leg.getMode().contains("twoway") || leg.getMode().contains("freefloating") || leg.getMode().contains("oneway")){
						
						String mode = ((Leg)ref.getSelectedPlan().getPlanElements().get(p.getSelectedPlan()
								.getPlanElements().indexOf(leg))).getMode();
						
						if(!substituteModeCount.containsKey(mode)){

							substituteModeCount.put(mode, 0);
							
						}
						
						int c = substituteModeCount.get(mode) + 1;
						substituteModeCount.put(mode, c);
						
					}
					
				}
				
			}
			
		}
		
		for(Entry<String, Integer> entry : substituteModeCount.entrySet()){
			
			System.out.println(entry.getKey() + ": " + entry.getValue());
			
		}
		
	}

	private static void purge(Plan plan) {
		
		for(Iterator<PlanElement> it = plan.getPlanElements().iterator(); it.hasNext();){
			
			PlanElement pe = it.next();
			
			if(pe instanceof Activity){
				
				Activity act = (Activity)pe;
				if(act.getType().contains("interaction")){

					it.remove();
				
				}
				
			} else {
				
				Leg leg = (Leg)pe;
				if(leg.getMode().contains("access") || leg.getMode().contains("egress")) {
					
					it.remove();
					
				} else if(leg.getMode().contains("transit_walk") || leg.getMode().equals("pt")) {
					
					PlanElement next = plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) + 1);
					PlanElement previous = plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) - 1);
					
					if(((Activity)next).getType().contains("interaction")) {
						
						it.remove();
						continue;
						
					}
					
					if(previous instanceof Leg) {
						
						it.remove();
						continue;
						
					}
					
				}
				else if(leg.getMode().contains("twoway") || leg.getMode().equals("freefloating")) {
					carsharingUsers.add(plan.getPerson().getId());
				}
				
			}
			
		}
		
	}
	
}