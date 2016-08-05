package com.innoz.toolbox.scenarioGeneration.counts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.counts.Count;
import org.matsim.counts.Counts;
import org.matsim.counts.CountsWriter;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

public class CountsFromNetworkShapefile {

	enum TrafficGraph{
		
		equil(new double[]{1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24,1/24});
		
		double[] p;
		
		TrafficGraph(double[] hourlyPercentage){
			this.p = hourlyPercentage;
		}
		
	}
	
	public static void main(String args[]){
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/dhosse/scenarios/3connect/network.xml.gz");
		new CountsFromNetworkShapefile().run(scenario, "/home/dhosse/02_Data/DTV_Osna/Straсennetz OsnabrБck DTV.shp", "");
		
	}
	
	public void run(final Scenario scenario, String inputShapefile, String outputCountsFile){
		
		this.run(scenario, inputShapefile, outputCountsFile, "equil");
		
	}
	
	public void run(final Scenario scenario, String inputShapefile, String outputCountsFile, String trafficGraphType){
		
		List<CountsLink> countsLinks = new ArrayList<>();
		Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(inputShapefile);
		
		for(SimpleFeature feature : features){
			
			String name = (String)feature.getAttribute("NAME");
			Double dtv = (Double)feature.getAttribute("DTV");
			Geometry geometry = (Geometry)feature.getDefaultGeometry();
			countsLinks.add(new CountsLink(name, dtv, geometry));
			
		}
		
		Counts<Link> counts = process(countsLinks, scenario, trafficGraphType);
		new CountsWriter(counts).write(outputCountsFile);
		
	}
	
	private Counts<Link> process(List<CountsLink> countsLinks, final Scenario scenario, String trafficGraphType){
		
		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:3044", "EPSG:32632");
		Set<Id<Link>> visitedLinkIds = new HashSet<>();
		Counts<Link> counts = new Counts<Link>();
		
		for(CountsLink cl : countsLinks){
			
			Link l = ((NetworkImpl)scenario.getNetwork()).getNearestLinkExactly(transformation.transform(MGC.point2Coord(cl.geometry.getCentroid())));
			
			if(!visitedLinkIds.contains(l.getId())){

				visitedLinkIds.add(l.getId());
				Count<Link> count = counts.createAndAddCount(l.getId(), cl.name);
				
				double[] hourlyVolumes = resolveDTV(cl.dtv, trafficGraphType);
				
				for(int i = 0; i < hourlyVolumes.length; i++){
				
					count.createVolume(i, hourlyVolumes[i]);
					
				}
				
			}
			
		}
		
		return counts;
		
	}
	
	private double[] resolveDTV(double dtv, String trafficGraphType){
		
		double[] hourlyVolumes = new double[24];
		
		TrafficGraph graph = TrafficGraph.valueOf(trafficGraphType);
		
		for(int i = 0; i < hourlyVolumes.length; i++){
			
			hourlyVolumes[i] = dtv * graph.p[i];
			
		}
		
		return hourlyVolumes;
		
	}
	
	class CountsLink{
		
		String name;
		double dtv;
		Geometry geometry;
		
		CountsLink(String name, double dtv, Geometry geometry){
			
			this.name = name;
			this.dtv = dtv;
			this.geometry = geometry;
			
		}
		
	}
	
}