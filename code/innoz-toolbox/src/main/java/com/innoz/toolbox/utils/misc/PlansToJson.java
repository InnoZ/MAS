package com.innoz.toolbox.utils.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.opengis.feature.simple.SimpleFeature;

import com.innoz.toolbox.utils.GlobalNames;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * 
 * Convert a plans file into line strings for visualization.
 * 
 * TODO maybe make this a controller listener?
 * 
 * @author dhosse
 *
 */
public class PlansToJson {
	
	static final String PAR_DEPARTURE = "started_at";
	static final String PAR_ARRIVAL = "finished_at";
	static final String PAR_MODE = "mode";
	static final String PAR_SPEED = "kmh";
	
	enum Modes{car};
	
	/*
	 * EXAMPLE USAGE
	 */
	public static void main(String args[]){
	
		String filebase = "/home/dhosse/dataForDatahubVis/";
		try {
			PlansToJson.run(filebase + "output_plans.xml.gz", filebase + "output_network.xml.gz", 0.05);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void run(String plansFile, String networkFile, double d) throws IOException{
		
		Random random = MatsimRandom.getLocalInstance();
		
		// Init
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
		new MatsimPopulationReader(scenario).readFile(plansFile);

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(GlobalNames.UTM32N, GlobalNames.WGS84);
		
		List<SimpleFeature> features = new ArrayList<>();
		
		PolylineFeatureFactory pfactory = new PolylineFeatureFactory.Builder()
				.setCrs(MGC.getCRS(GlobalNames.WGS84))
				.addAttribute(PAR_DEPARTURE, Double.class)
				.addAttribute(PAR_ARRIVAL, Double.class)
				.addAttribute(PAR_MODE, String.class)
				.addAttribute(PAR_SPEED, Integer.class)
				.create();
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			if(random.nextDouble() <= d){
			
				Plan plan = person.getSelectedPlan();

				for(PlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof Leg){
						
						Leg leg = (Leg)pe;
						
						if(leg.getRoute() instanceof NetworkRoute){
							
							NetworkRoute route = (NetworkRoute)leg.getRoute();
							
							double departure = leg.getDepartureTime();
							double arrival = departure + leg.getTravelTime();
							
							Link current = scenario.getNetwork().getLinks().get(route.getStartLinkId());
							
							List<Coordinate> coordinates = new ArrayList<>();
							
							coordinates.add(MGC.coord2Coordinate(ct.transform(current.getFromNode().getCoord())));
							coordinates.add(MGC.coord2Coordinate(ct.transform(current.getToNode().getCoord())));

							
							for(Id<Link> id : route.getLinkIds()){
								
								current = scenario.getNetwork().getLinks().get(id);
								coordinates.add(MGC.coord2Coordinate(ct.transform(current.getToNode().getCoord())));
								
							}
							
							current = scenario.getNetwork().getLinks().get(route.getEndLinkId());
							coordinates.add(MGC.coord2Coordinate(ct.transform(current.getToNode().getCoord())));
						
							Coordinate[] coords = new Coordinate[coordinates.size()];
							for(int i = 0 ; i < coordinates.size() ; i++){
								coords[i] = coordinates.get(i);
							}
							
							SimpleFeature feature = pfactory.createPolyline(coords);
							feature.setAttribute(PAR_DEPARTURE, departure);
							feature.setAttribute(PAR_ARRIVAL, arrival);
							feature.setAttribute(PAR_MODE, Modes.car.name());
							feature.setAttribute(PAR_SPEED, (int)(3.6 * route.getDistance() / (arrival-departure)));
							features.add(feature);
							
						}
						
					}

				}
				
			}
			
		}
		
		FeatureJSON jFeature = new FeatureJSON();
		DefaultFeatureCollection coll = new DefaultFeatureCollection();
		coll.addAll(features);
		jFeature.writeFeatureCollection(coll, "/home/dhosse/dataForDatahubVis/features.json");
		
	}

}