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

	public static void main(String args[]){
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario.getNetwork()).readFile("/home/dhosse/osGtfs/network.xml.gz");
		new CountsFromNetworkShapefile().run(scenario, "/home/dhosse/02_Data/DTV_Osna/Straсennetz OsnabrБck DTV.shp",
				"/home/dhosse/counts.xml.gz", TrafficGraph.mid.name());
		
	}
	
	public void run(final Scenario scenario, String inputShapefile, String outputCountsFile){
		
		this.run(scenario, inputShapefile, outputCountsFile, "uniform");
		
	}
	
	public void run(final Scenario scenario, String inputShapefile, String outputCountsFile, String trafficGraphType){
		
		Set<String> hashes = new HashSet<>();
		
		List<CountsLink> countsLinks = new ArrayList<>();
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(inputShapefile);
		
		for(SimpleFeature feature : features){
			
			String name = (String)feature.getAttribute("NAME");
			Double dtv = (Double)feature.getAttribute("DTV");
			Geometry geometry = (Geometry)feature.getDefaultGeometry();
			String hash = name + "_" + dtv;
			if(!hashes.contains(hash)){
				countsLinks.add(new CountsLink(name, dtv, geometry));
				hashes.add(hash);
			}
			
		}
		
		Counts<Link> counts = process(countsLinks, scenario, trafficGraphType);
		new CountsWriter(counts).write(outputCountsFile);
		
	}
	
	private Counts<Link> process(List<CountsLink> countsLinks, final Scenario scenario, String trafficGraphType){
		
		CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation("EPSG:3044", "EPSG:32632");
		Set<Id<Link>> visitedLinkIds = new HashSet<>();
		Counts<Link> counts = new Counts<Link>();
		counts.setYear(2016);
		counts.setName("OS");
		counts.setDescription("Counts from OS DTV network");
		
		for(CountsLink cl : countsLinks){
			
			Link l = ((NetworkImpl)scenario.getNetwork()).getNearestLinkExactly(transformation.transform(MGC.point2Coord(
					cl.geometry.getCentroid())));
			
			if(!visitedLinkIds.contains(l.getId())){

				visitedLinkIds.add(l.getId());
				Count<Link> count = counts.createAndAddCount(l.getId(), cl.name);
				
				double[] hourlyVolumes = resolveDTV(cl.dtv, trafficGraphType);
				
				for(int i = 1; i < hourlyVolumes.length; i++){
				
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
			
			hourlyVolumes[i] = dtv * graph.perc[i];
			
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
	
	enum TrafficGraph{
		
		mid(new double[]{
				0.0026290631, 0.0011950287,	0.0007170172, 0.0009560229, 0.0028680688, 0.0152963671,
				0.029875717, 0.0712237094, 0.0673996176, 0.0595124283, 0.0523422562, 0.060707457,
				0.0707456979, 0.0564053537, 0.0657265774, 0.0731357553, 0.0771988528, 0.0915391969,
				0.0702676864, 0.0554493308, 0.0332217973, 0.0193594646,	0.0157743786, 0.0064531549
		}),
		
		// types
		uniform(new double[]{
			1/24,1/24,1/24,1/24,1/24,1/24,
			1/24,1/24,1/24,1/24,1/24,1/24,
			1/24,1/24,1/24,1/24,1/24,1/24,
			1/24,1/24,1/24,1/24,1/24,1/24
		}),
		
		a(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		b(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		c(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		d(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		e(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		f(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		g(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		h(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		i(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		j(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		k(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		l(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		m(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		n(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		o(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		p(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		q(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		}),
		
		r(new double[]{
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0,
			0,0,0,0,0,0
		});
		
		double[] perc;
		
		TrafficGraph(double[] hourlyPercentage){
			this.perc = hourlyPercentage;
		}
		
	}
	
}