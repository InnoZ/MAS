package com.innoz.toolbox.utils.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geojson.feature.FeatureJSON;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.misc.Time;
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
	
	static final String PAR_ID = "person_id";
	static final String PAR_DEPARTURE = "started_at";
	static final String PAR_ARRIVAL = "finished_at";
	static final String PAR_MODE = "mode";
	static final String PAR_SPEED = "kmh";
	
	// non-instantiable
	private PlansToJson(){};
	
	public static void run(final Scenario scenario, String outputFile, String crs) {
		
		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(crs, GlobalNames.WGS84);
		
		List<SimpleFeature> features = new ArrayList<>();
		
		PolylineFeatureFactory pfactory = new PolylineFeatureFactory.Builder()
				.setCrs(MGC.getCRS(GlobalNames.WGS84))
				.addAttribute(PAR_ID, String.class)
				.addAttribute(PAR_DEPARTURE, Double.class)
				.addAttribute(PAR_ARRIVAL, Double.class)
				.addAttribute(PAR_MODE, String.class)
				.addAttribute(PAR_SPEED, Integer.class)
				.create();
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			Plan plan = person.getSelectedPlan();

			for(PlanElement pe : plan.getPlanElements()){
				
				if(pe instanceof Leg){
					
					Leg leg = (Leg)pe;
					
					Activity fromAct = (Activity) plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) - 1);
					Activity toAct = (Activity) plan.getPlanElements().get(plan.getPlanElements().indexOf(leg) + 1);
					
					Coordinate from = MGC.coord2Coordinate(ct.transform(fromAct.getCoord()));
					Coordinate to = MGC.coord2Coordinate(ct.transform(toAct.getCoord()));
					
					double startTime = leg.getDepartureTime() != Time.UNDEFINED_TIME ? leg.getDepartureTime() : fromAct.getEndTime();
					double endTime = leg.getDepartureTime() != Time.UNDEFINED_TIME ? leg.getDepartureTime() + leg.getTravelTime() : toAct.getStartTime();
					
					SimpleFeature feature = pfactory.createPolyline(new Coordinate[]{from,to});
					feature.setAttribute(PAR_ID, person.getId().toString());
					feature.setAttribute(PAR_DEPARTURE, startTime);
					feature.setAttribute(PAR_ARRIVAL, endTime);
					feature.setAttribute(PAR_MODE, leg.getMode());
					feature.setAttribute(PAR_SPEED,
							(int)(3.6 * CoordUtils.calcEuclideanDistance(fromAct.getCoord(), toAct.getCoord()) / 1.3 / (endTime - startTime)));
					features.add(feature);
					
				}
				
			}
			
		}
		
		try {

			FeatureJSON jFeature = new FeatureJSON();
			DefaultFeatureCollection coll = new DefaultFeatureCollection();
			coll.addAll(features);
			jFeature.writeFeatureCollection(coll, outputFile);
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}

}