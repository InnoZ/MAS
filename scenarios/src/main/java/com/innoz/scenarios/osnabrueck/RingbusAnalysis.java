package com.innoz.scenarios.osnabrueck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;

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
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;

public class RingbusAnalysis {

	public static void main(String[] args) throws IOException {	
		
		String inputPath = "/home/bmoehring/3connect/0_basisSzenario/";
//		String inputPath = "/home/bmoehring/3connect/1_negativSzenario/"; 
//		String inputPath = "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/output_trend/"; 
//		String inputPath = "/home/bmoehring/3connect/3connect_positiv/output_positiv/"; 
			
		Config config = ConfigUtils.createConfig(); 
		config.network().setInputFile(inputPath + "output_network.xml.gz");
		config.plans().setInputFile(inputPath + "output_plans.xml.gz"); 
		config.transit().setTransitScheduleFile(inputPath + "output_transitSchedule.xml.gz");
		
		Scenario scenario = ScenarioUtils.loadScenario(config); 	
		
		Map<String, List<String>> routeMap = filterOSRoutesFromCSV("/home/bmoehring/3connect/Scenarios/Ausbau transit/VBN_Eingangsdaten_pt/routes.txt");
		
		List<String> lines = new ArrayList<>();
		lines.add("100000");
		routeMap.put("ringbus", lines);
		
		getCustomersPerLine(scenario, routeMap);
		
//		printLines(scenario.getTransitSchedule());
		
//		List<Ride> ringbusRides = getRides(scenario, Id.create("100000", TransitLine.class));
//		writeChangeFile(ringbusRides, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/ringbusRides.csv");
		
//		Population ringbuspopulation = getRidePopulation(scenario, Id.create("100000", TransitLine.class));
//		GeometryUtils.writeActivityLocationsToShapefile(ringbuspopulation, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/", "EPSG:32632");
		
//		List<Facility> fromFacility = new ArrayList<Facility>(); 
//		for (Ride route : ringbusRides){
//			System.out.println(route);
//		}
//		GeometryUtils.writeFacilities2Shapefile(facilities, "/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/analysis/", "EPSG:32632");
		
	}
	
	private static Map<String, List<String>> filterOSRoutesFromCSV(String filepath){

		Map<String, List<String>> routeMap = new HashMap<>();
		
		File file = new File(filepath);
		
		int count = 0;

        try{
            // -read from filePooped with Scanner class
            BufferedReader br = new BufferedReader(new FileReader(filepath));
            br.readLine();
            String line = br.readLine();
            while((line = br.readLine()) != null){
                String[] route = line.split(",");
                String routeShort = null;
                String routeId = null;
                if (route[1].equalsIgnoreCase("XOS___")){
                	routeShort = route[2];
                	routeId = route[0];
                	count++;
                }
                
                if(routeShort != null && routeId != null){
                	if (routeMap.containsKey(routeShort)){
                		routeMap.get(routeShort).add(routeId);
                	} else {
                		List<String> list = new ArrayList<String>();
                		list.add(routeId);
                		routeMap.put(routeShort, list);
                	}
                }
                line = br.readLine();
            }


        }catch (IOException e){

            e.printStackTrace();
        }
        
        System.out.println("count " + count);
        System.out.println("routeMapSize: " + routeMap.size());
        
        return routeMap;
	}
	
	private static void getCustomersPerLine(Scenario scenario, Map<String, List<String>> routeMap){
		

		Map<String, Integer> customersPerLineMap = new HashMap<>();
		ValueComparator comparator = new ValueComparator(customersPerLineMap);
		TreeMap<String, Integer> sortedCustomersPerLineMap = new TreeMap<String, Integer>(comparator);
		
		for (Person p : scenario.getPopulation().getPersons().values()){
			
			for (PlanElement pe : p.getSelectedPlan().getPlanElements()){
				if (pe instanceof Leg && ((Leg)pe).getMode().equals(TransportMode.pt)){
					
					ExperimentalTransitRoute route = (ExperimentalTransitRoute) ((Leg) pe).getRoute();
					
					String lineId = route.getLineId().toString();
					
					for (Entry<String, List<String>> e : routeMap.entrySet()){
						for (String line : e.getValue()){
							if ( line.equalsIgnoreCase(lineId)){
								Integer count = customersPerLineMap.get(e.getKey());
								if (count == null){ 
									count = 1;
								} else {
									count ++;
								}
								customersPerLineMap.put(e.getKey(), count);
							}
						}
					}
					
				}
			}
			
		}
		
		sortedCustomersPerLineMap.putAll(customersPerLineMap);
		
		for (Entry<String, Integer> e : sortedCustomersPerLineMap.entrySet()){
			String lineCountLines = e.getKey() + "\t" + e.getValue() + "\t";
//			for(String line : routeMap.get(e.getKey())){
//				lineCountLines = lineCountLines + line + " ";
//			}
			System.out.println(lineCountLines);
		}
		
	}
	
	private static void printLines(TransitSchedule transitSchedule) {
		
		List<String> allmodes = new ArrayList<>();
		int numberOfRoutes = 0;
		
		for (TransitLine line : transitSchedule.getTransitLines().values()){
			System.out.println("----");
			System.out.println(line.getId());
			List<String> modes = new ArrayList<>();
			for (TransitRoute route : line.getRoutes().values()){
				numberOfRoutes ++;
				if (!modes.contains(route.getTransportMode())){
					modes.add(route.getTransportMode());
				}
				if (!allmodes.contains(route.getTransportMode())){
					allmodes.add(route.getTransportMode());
				}
			}
			System.out.println(modes.size() + " " + modes);
		}
		System.out.println("number of lines" + transitSchedule.getTransitLines().size());
		System.out.println("number of routes" + numberOfRoutes);
		System.out.println(allmodes);
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

class ValueComparator implements Comparator<String> {
    Map<String, Integer> base;

    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}
