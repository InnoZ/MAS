package com.innoz.toolbox.utils.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.LinkLeaveEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsReaderXMLv1;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;

//TODO definitely make this one a controller listener / online events handler (runs much too long)

public class EventsToJson {

	static List<Id<Person>> personIds = new ArrayList<>();
	
	/*
	 * EXAMPLE USAGE
	 */
	public static void main(String args[]){
		
		String filebase = "/home/dhosse/dataForDatahubVis/";
		
		try {
			
			EventsToJson.run(filebase + "100.events.xml.gz", filebase + "100.plans.xml.gz", filebase + "mergedNetwork.xml.gz");
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public static void run(String eventsFile, String plansFile, String networkFile) throws IOException{
		
		double d = 0.025d;
		
		Random random = MatsimRandom.getLocalInstance();
		
		// Init
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		new PopulationReader(scenario).readFile(plansFile);
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			if(random.nextDouble() <= d){
				
				personIds.add(person.getId());
				
			}
			
		}
		
		System.out.println(personIds.size());
		
		EventsManager em = EventsUtils.createEventsManager();
		
		ExporterEventsHandler handler = new ExporterEventsHandler();
		em.addHandler(handler);
		new EventsReaderXMLv1(em).readFile(eventsFile);
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("EPSG:32632", "EPSG:4326");
		
		PolylineFeatureFactory pfactory = new PolylineFeatureFactory.Builder()
				.addAttribute("started_at", Double.class)
				.addAttribute("finished_at", Double.class)
				.addAttribute("mode", String.class)
				.addAttribute("kmh", Integer.class)
				.create();
		
		List<SimpleFeature> features = new ArrayList<>();
		
		for(Entry<Id<Person>, List<RouteLogEntry>> entry : handler.personId2routeLog.entrySet()){
			
			for(RouteLogEntry e : entry.getValue()){
				
				Link current = scenario.getNetwork().getLinks().get(e.linkId);
				
				Coordinate[] coordinates = new Coordinate[]{
						MGC.coord2Coordinate(ct.transform(current.getFromNode().getCoord())),
						MGC.coord2Coordinate(ct.transform(current.getToNode().getCoord()))};
				
				SimpleFeature feature = pfactory.createPolyline(coordinates);
				feature.setAttribute("started_at", e.entryTime);
				feature.setAttribute("finished_at", e.leaveTime);
				feature.setAttribute("mode", "car");
				int speed = (int)(3.6 * current.getLength() / (e.leaveTime - e.entryTime));
				feature.setAttribute("kmh", speed);
				
				features.add(feature);
				
			}
			
		}
		
		FeatureJSON jFeature = new FeatureJSON();
		DefaultFeatureCollection coll = new DefaultFeatureCollection();
		coll.addAll(features);
		jFeature.writeFeatureCollection(coll, "/home/dhosse/dataForDatahubVis/features.json");
		
	}
	
	static class ExporterEventsHandler implements LinkEnterEventHandler, LinkLeaveEventHandler {

		Map<Id<Person>, List<RouteLogEntry>> personId2routeLog = new HashMap<>();
		
		@Override
		public void reset(int iteration) {
			
		}

		@Override
		public void handleEvent(LinkLeaveEvent event) {
			
			Id<Person> personId = Id.createPersonId(event.getVehicleId().toString());
			
			if(personIds.contains(personId)){

				if(personId2routeLog.containsKey(personId)){
					
					RouteLogEntry log = personId2routeLog.get(personId).get(personId2routeLog.get(personId).size()-1);
					
					log.leaveTime = event.getTime();
					
				}
				
			}
			
		}

		@Override
		public void handleEvent(LinkEnterEvent event) {
			
			Id<Person> personId = Id.createPersonId(event.getVehicleId().toString());
			
			if(personIds.contains(personId)){

				if(!personId2routeLog.containsKey(personId)){
					
					personId2routeLog.put(personId, new ArrayList<>());
					
				}
				
				RouteLogEntry log = new RouteLogEntry(event.getLinkId());
				log.entryTime = event.getTime();
				personId2routeLog.get(personId).add(log);
				
			}
			
		}
		
	}
	
	static class RouteLogEntry{

		final Id<Link> linkId;
		double entryTime;
		double leaveTime;
		
		public RouteLogEntry(Id<Link> linkId){
			this.linkId = linkId;
		}
		
		
	}
	
}