package com.innoz.toolbox.utils.io;

import java.util.ArrayList;
import java.util.List;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.misc.Time;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.innoz.toolbox.io.DefaultXmlReader;

public class GpxTrackReader extends DefaultXmlReader {

	public static void main(String args[]) {
		
		GpxTrackReader r = new GpxTrackReader();
		
		r.read("/home/dhosse/01_Proposals/Leuna_Werke/WG__Daten_GPS-Tracker_InfraLeuna/export_2017-01-17 06-03.gpx");
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		PopulationFactory factory = scenario.getPopulation().getFactory();
		Person p = factory.createPerson(Id.createPersonId(0));
		Plan plan = factory.createPlan();
		
		double last = 0;
		Coord lastCoord = null;
		
		for(TrackPoint point : r.points) {
			
			Coord c = point.coord;
			
			Activity prev = null;
			
			if(plan.getPlanElements().size() > 2) {
				prev = (Activity) plan.getPlanElements().get(plan.getPlanElements().size() - 2);
			}
			
			if(prev != null && prev.getCoord().equals(point.coord)) {
				
				prev.setEndTime(point.time);
				
			} else {
				
				if(point.coord.equals(lastCoord)) {

					Activity act = factory.createActivityFromCoord("sighting", c);
					act.setStartTime(last);
					act.setEndTime(point.time);
					plan.addActivity(act);
					
					Leg leg = factory.createLeg("car");
					plan.addLeg(leg);
					
				}
				
			}
			
			last = point.time;
			lastCoord = point.coord;
			
		}
		
		p.addPlan(plan);
		p.setSelectedPlan(plan);
		
		scenario.getPopulation().addPerson(p);
		
		new PopulationWriter(scenario.getPopulation()).write("/home/dhosse/01_Proposals/Leuna_Werke/plan.xml.gz");
		
	}
	
	private List<TrackPoint> points = new ArrayList<>();
	private TrackPoint current;
	
	private boolean hasToHandleTimeStamp = false;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		
		if("trkpt".equals(qName)) {

			this.startPoint(attributes);
			
		} else if("time".equals(qName) && this.current != null) {
			
			this.hasToHandleTimeStamp = true;
			
		}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		if(this.hasToHandleTimeStamp) {

			String timeStamp = new String(ch, start, length);
			String sub = timeStamp.substring(11, 19);
			this.current.time = Time.parseTime(sub);
		
		}
		
		this.hasToHandleTimeStamp = false;
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName){

		if("trkpt".equals(qName)) {
			
			this.endPoint();
			
		}
		
	}
	
	private void startPoint(Attributes attributes) {
		
		String lat = attributes.getValue("lat");
		String lon = attributes.getValue("lon");
		
		this.current = new TrackPoint();		
		this.current.coord = new Coord(Double.parseDouble(lon), Double.parseDouble(lat));
		
	}
	
	private void endPoint() {
		
		this.points.add(this.current);
		this.current = null;
		
	}
	
	static class TrackPoint {
		
		private Coord coord;
		private double time;
		
	}
	
}