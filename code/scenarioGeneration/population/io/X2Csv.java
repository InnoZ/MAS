package playground.dhosse.scenarios.generic.population.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import playground.dhosse.gap.Global;

public class X2Csv {

	public void run(Scenario scenario, String baseDir, String demographicsFile, String vehicleLocationsFile,
			String suffix){
		
		ObjectAttributes demographics = new ObjectAttributes();
		new ObjectAttributesXmlReader(demographics).parse(demographicsFile);
		
		try {
			
			processPersons(scenario.getPopulation().getPersons().values(), demographics, baseDir, suffix);
			processTrips(scenario, baseDir, suffix);
			processCsStations(scenario.getNetwork(), vehicleLocationsFile, baseDir, suffix);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private void processPersons(Collection<? extends Person> persons, ObjectAttributes demographics,
			String baseDir, String suffix) throws IOException{
		
		BufferedWriter writer = IOUtils.getBufferedWriter(baseDir + "/agents_" + suffix + ".csv");

		for(Person person : persons){
			
			writer.write(person.getId().toString() + ";" +
					demographics.getAttribute(person.getId().toString(), Global.SEX) + ";" +
					demographics.getAttribute(person.getId().toString(), Global.AGE) + ";" +
					demographics.getAttribute(person.getId().toString(), Global.CAR_AVAIL) + ";" +
					demographics.getAttribute(person.getId().toString(), Global.LICENSE));
			writer.newLine();
			
		}
		
		writer.flush();
		writer.close();
		
	}
	
	private void processTrips(Scenario scenario, String baseDir, String suffix)
			throws IOException{
		
		BufferedWriter writer = IOUtils.getBufferedWriter(baseDir + "/trips_" + suffix + ".csv");

		for(Person person : scenario.getPopulation().getPersons().values()){
			
			Plan plan = person.getSelectedPlan();
			
			int tripCounter = 1;
			for(int i = 0; i < plan.getPlanElements().size(); i++){

				PlanElement pe = plan.getPlanElements().get(i);
				/*
				 * geometry
				 * person id
				 * trip idx
				 * act type
				 * act x
				 * act y
				 * main mode
				 * 
				 */
				if(pe instanceof Leg){
					
					Leg leg = (Leg)pe;
					Activity fromAct = (Activity)plan.getPlanElements().get(i - 1);
					Activity toAct = (Activity)plan.getPlanElements().get(i + 1);

					String geometry = "";
					String personId = person.getId().toString();
					String tripIndex = Integer.toString(tripCounter);
					String travelTime = Double.toString(leg.getTravelTime());
					String distance = Double.toString(leg.getRoute().getDistance());
					String startTime = Double.toString(fromAct.getEndTime());
					String endTime = Double.toString(fromAct.getEndTime() + leg.getTravelTime());
					String fromActType = getActType(fromAct);
					String fromX = Double.toString(fromAct.getCoord().getX());
					String fromY = Double.toString(fromAct.getCoord().getY());
					String toActType = getActType(toAct);
					String toX = Double.toString(toAct.getCoord().getX());
					String toY = Double.toString(toAct.getCoord().getY());
					String mainMode = leg.getMode();
					String accessTime = "0";
					String accessDistance = "0";
					String egressTime = "0";
					String egressDistance = "0";
					
					if(leg.getMode().equals("transit_walk")){
						
						if(toAct.getType().equals("pt interaction")){
							
							Leg accessLeg = leg;
							accessTime = Double.toString(accessLeg.getTravelTime());
							accessDistance = Double.toString(accessLeg.getRoute().getDistance());
							
							Leg mainModeLeg = (Leg)plan.getPlanElements().get(i + 2);
							mainMode = "pt";
							startTime = Double.toString(fromAct.getEndTime() + accessLeg.getTravelTime());
							endTime = Double.toString(fromAct.getEndTime() + accessLeg.getTravelTime() 
									+ mainModeLeg.getTravelTime());
							distance = Double.toString(mainModeLeg.getRoute().getDistance());
							
							travelTime = Double.toString(mainModeLeg.getTravelTime());
							
							Leg egressLeg = (Leg)plan.getPlanElements().get(i + 4);
							egressTime = Double.toString(egressLeg.getTravelTime());
							egressDistance = Double.toString(egressLeg.getRoute().getDistance());
							
							toAct = (Activity)plan.getPlanElements().get(i + 5);
							toActType = getActType(toAct);
							
							toX = Double.toString(toAct.getCoord().getX());
							toY = Double.toString(toAct.getCoord().getY());
							
							i += 5;
							
						} else {
							
							mainMode = "walk";
							
						}
						
					}
					
					writer.write(
									geometry + ";" +
									personId + ";" +
									tripIndex + ";" +
									travelTime + ";" +
									distance + ";" +
									startTime + ";" +
									endTime + ";" +
									fromActType + ";" +
									fromX + ";" +
									fromY + ";" +
									toActType + ";" +
									toX + ";" +
									toY + ";" +
									mainMode + ";" +
									accessTime + ";" +
									accessDistance + ";" +
									egressTime + ";" +
									egressDistance
							);
					
					tripCounter++;
					
					writer.newLine();
					
				}
				
			}
			
		}
		
		writer.flush();
		writer.close();
		
	}
	
	private String getActType(Activity activity){
		
		if(activity.getType().contains("home")){
			return "home";
		} else if(activity.getType().contains("work")){
			return "work";
		} else if(activity.getType().contains("educ")){
			return "education";
		} else if(activity.getType().contains("shop")){
			return "shopping";
		} else{
			return "other";
		}
		
	}
	
	private void processCsStations(Network network, String vehicleLocationsFile, String baseDir,
			String suffix) throws IOException{
		
		BufferedReader reader = IOUtils.getBufferedReader(vehicleLocationsFile);
		
		String line = reader.readLine();
		
		List<CsStation> stations = new ArrayList<X2Csv.CsStation>();
		
		while((line = reader.readLine()) != null){
			
			String[] l = line.split("\t");
			CsStation station = new CsStation();
			station.id = l[0];
			station.name = l[1];
			station.x = Double.parseDouble(l[2]);
			station.y = Double.parseDouble(l[3]);
			station.nVehicles = Integer.parseInt(l[6]);
			station.linkId = NetworkUtils.getNearestLink(network, new Coord(station.x, station.y))
					.getId().toString();
			stations.add(station);
			
		}
		
		reader.close();
		
		BufferedWriter writer = IOUtils.getBufferedWriter(baseDir + "/vehicles_" + suffix + ".csv");
		
		for(CsStation station : stations){
			
			writer.write(station.id + ";" +
					station.x + ";" +
					station.y + ";" +
					station.linkId + ";" +
					station.name + ";" + 
					station.nVehicles);
			
			writer.newLine();
			
		}
		
		writer.flush();
		writer.close();
		
	}
	
	class CsStation{

		private String id;
		private String name;
		private double x;
		private double y;
		private int nVehicles;
		private String linkId;
		
	}

}
