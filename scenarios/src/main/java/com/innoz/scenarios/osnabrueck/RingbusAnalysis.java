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
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.api.TransitLine;

public class RingbusAnalysis {

	public static void main(String[] args) throws IOException {	
		
		String inputPath = "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/output_trend/"; 
			
		Config config = ConfigUtils.createConfig(); 
		config.network().setInputFile(inputPath + "output_network.xml.gz");
		config.plans().setInputFile(inputPath + "output_plans.xml.gz"); 
		
		Scenario scenario = ScenarioUtils.loadScenario(config); 	
		
		List<Ride> ringbusRides = getRides(scenario, Id.create("100000", TransitLine.class));
			
		for (Ride route : ringbusRides){
			System.out.println(route);
		}
		
		System.out.println("In total: " + ringbusRides.size());
		
		writeChangeFile(ringbusRides, inputPath + "/ringbusRides.csv");
		
	}
	
	private static List<Ride> getRides(Scenario scenario, Id<TransitLine> ringbusLine) {

		List<Ride> rides = new ArrayList<Ride>(); 
		
		int personCount = 0;
		for (Person person : scenario.getPopulation().getPersons().values()) { 						
			
			Plan plan = person.getSelectedPlan();
			if (plan != null) personCount ++;
			
			for (int peCount = 0; peCount < plan.getPlanElements().size();peCount++) { 
				
				PlanElement pe = plan.getPlanElements().get(peCount);
				
				Activity actFrom = null;
				Activity actTo = null;
				if (pe instanceof Activity && ((Activity)pe).getType()!="pt_interaction"){
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
								if (pe2 instanceof Activity && ((Activity)pe2).getType()!="pt_interaction"){
									actTo = ((Activity)pe2);
									continue;
								}
							}
							rides.add(new Ride(route, person, actFrom, actTo));
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
        	line += ride.getPerson().getId()+";"+
        			ride.getPerson().getCustomAttributes().get("age")+";"+
        			ride.getFromActivity().getType()+";"+
        			ride.getFromActivity().getCoord().getX()+";"+
        			ride.getFromActivity().getCoord().getY()+";";
        	line += ride.getToActivity().getType()+";"+
        			ride.getToActivity().getCoord().getX()+";"+
        			ride.getToActivity().getCoord().getY()+";";
        	line += ride.getRoute().getRouteId().toString()+";"+
        			ride.getRoute().getAccessStopId().toString()+";"+
        			ride.getRoute().getTravelTime()+";"+
        			ride.getRoute().getEgressStopId().toString()+";";
        	bw.write(line);
        	System.out.println(line);
        }
        
        bw.close();
        fw.close();
        System.out.println();
        System.out.println("Output written to: " + outputFile);
	}
}
