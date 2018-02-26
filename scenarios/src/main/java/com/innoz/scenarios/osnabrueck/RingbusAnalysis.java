package com.innoz.scenarios.osnabrueck;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.Facility;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.api.TransitLine;

import com.innoz.toolbox.utils.GeometryUtils;

public class RingbusAnalysis {

	public static void main(String[] args) throws IOException {	
		
		String inputPath = "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/output_trend/"; 
			
		Config config = ConfigUtils.createConfig(); 
		config.network().setInputFile(inputPath + "output_network.xml.gz");
		config.plans().setInputFile(inputPath + "output_plans.xml.gz"); 
		
		Scenario scenario = ScenarioUtils.loadScenario(config); 	
		
//		List<Ride> ringbusRides = getRides(scenario, Id.create("100000", TransitLine.class));
//			
//		for (Ride route : ringbusRides){
//			System.out.println(route);
//		}
//		
//		System.out.println("In total: " + ringbusRides.size());
//		
//		writeChangeFile(ringbusRides, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/ringbusRides.csv");
		
		Population ringbuspopulation = getRidePopulation(scenario, Id.create("100000", TransitLine.class));
		
		GeometryUtils.writeActivityLocationsToShapefile(ringbuspopulation, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/", "EPSG:32632");
		
//		List<Facility> fromFacility = new ArrayList<Facility>(); 
//		for (Ride route : ringbusRides){
//			System.out.println(route);
//		}
//		GeometryUtils.writeFacilities2Shapefile(facilities, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/", "EPSG:32632");
		
	}
	
	private static Population getRidePopulation(Scenario scenario, Id<TransitLine> ringbusLine) {

		Population ringbuspopulation = PopulationUtils.createPopulation(ConfigUtils.createConfig()); 
		
		int personCount = 0;
		for (Person person : scenario.getPopulation().getPersons().values()) { 						

			PersonUtils.removeUnselectedPlans(person);
			Plan plan = person.getSelectedPlan();
			if (plan != null) personCount ++;
			
			for (int peCount = 0; peCount < plan.getPlanElements().size();peCount++) { 
				
				PlanElement pe = plan.getPlanElements().get(peCount);

				//check if planElement is a Leg
				if (pe instanceof Leg) { 										
					
					//check if legmode is pt
					if (((Leg) pe).getMode().equalsIgnoreCase(TransportMode.pt)){ 
						
						ExperimentalTransitRoute route = (ExperimentalTransitRoute) ((Leg) pe).getRoute(); 						
						
						if (route.getLineId() == ringbusLine){
							try {
								ringbuspopulation.addPerson(person);
							} catch (IllegalArgumentException e) {
								continue;
							}
						};
										
					} 				
				} 			
			}

		} 
		System.out.println(ringbuspopulation.getPersons().size() + " " + personCount);
		return ringbuspopulation;
	}
	
	private static List<Ride> getRides(Scenario scenario, Id<TransitLine> ringbusLine) {

		List<Ride> rides = new ArrayList<Ride>(); 
		
		int personCount = 0;
		for (Person person : scenario.getPopulation().getPersons().values()) { 						
			
			Plan plan = person.getSelectedPlan();
			if (plan != null) personCount ++;
			Activity actFrom = null;
			Activity actTo = null;
			
			for (int peCount = 0; peCount < plan.getPlanElements().size();peCount++) { 
				
				PlanElement pe = plan.getPlanElements().get(peCount);
				
				
				
				if (pe instanceof Activity && !((Activity)pe).getType().toString().startsWith("pt")){
					actFrom = ((Activity)pe);
				}
				//check if planElement is a Leg
				if (pe instanceof Leg) { 										
					
					//check if legmode is pt
					if (((Leg) pe).getMode().equalsIgnoreCase(TransportMode.pt)){ 
						
						ExperimentalTransitRoute route = (ExperimentalTransitRoute) ((Leg) pe).getRoute(); 						
						
						if (route.getLineId() == ringbusLine){
							for(int peCount2 = peCount;peCount2 < plan.getPlanElements().size();peCount2++){
								PlanElement pe2 = plan.getPlanElements().get(peCount2);
								if (pe2 instanceof Activity && !((Activity)pe2).getType().toString().startsWith("pt")){
									actTo = ((Activity)pe2);
									break;
								}
							}
							rides.add(new Ride(route, person, actFrom, actTo));
							actFrom = null;
							actTo = null;
						};
										
					} 				
				} 			
			}

		} 
		System.out.println(rides.size() + " " + personCount);
		return rides;
	}
	
	private static void writeChangeFile(List<Ride> ringbusRides, String outputFile) throws IOException {
		
		FileWriter fw = new FileWriter(outputFile);
		BufferedWriter bw = new BufferedWriter(fw);
        bw.write("personId;age;"
        		+ "fromActivityType;fromX;fromY;"
        		+ "toActivityType;toX;toY;"
        		+ "routeId;accessStopId;travelTime;egressStopId;");
        
        for (Ride ride : ringbusRides) {
        	bw.newLine();
        	String line = "";
        	try{
        		line += ride.getPerson().getId()+";"+
        				ride.getPerson().getCustomAttributes().get("age")+";";
        	} catch (NullPointerException n) {
        		line += ";;";
        	}
        	try{
        		line += ride.getFromActivity().getType()+";"+
            			ride.getFromActivity().getCoord().getX()+";"+
            			ride.getFromActivity().getCoord().getY()+";";
        	} catch (NullPointerException n) {
        		line += ";;;";
        	}
        	try{
            	line += ride.getToActivity().getType()+";"+
            			ride.getToActivity().getCoord().getX()+";"+
            			ride.getToActivity().getCoord().getY()+";";
        	} catch (NullPointerException n) {
        		line += ";;;";
        	}
        	try{
            	line += ride.getRoute().getRouteId().toString()+";"+
            			ride.getRoute().getAccessStopId().toString()+";"+
            			ride.getRoute().getTravelTime()+";"+
            			ride.getRoute().getEgressStopId().toString()+";";
        	} catch (NullPointerException n) {
        		line += ";;;;";
        	}
        	bw.write(line);
        	System.out.println(line);
        }
        
        bw.close();
        fw.close();
        System.out.println();
        System.out.println("Output written to: " + outputFile);
	}
}
