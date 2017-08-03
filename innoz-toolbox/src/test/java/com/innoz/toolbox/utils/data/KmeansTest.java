package com.innoz.toolbox.utils.data;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.CollectionUtils;

import com.innoz.toolbox.utils.data.kmeans.Cluster;
import com.innoz.toolbox.utils.data.kmeans.ClusterPoint;
import com.innoz.toolbox.utils.data.kmeans.Kmeans;

/**
 * Basic test for the k-means clustering algorithm.
 * 
 * @author dhosse
 *
 */
public class KmeansTest {

	/*
	 * 0--1----------2--3
	 */
	static Scenario scenario;
	
	@Before
	public void setup() {
		
		// Init the MATsim scenario
		scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		// Set up the network containing 4 nodes and 3 connecting links
		// The connections are only unidirectional, but that should work for the purposes of this test
		NetworkFactory netFactory = scenario.getNetwork().getFactory();
		Node n0 = netFactory.createNode(Id.createNodeId("0"), new Coord(0,0));
		scenario.getNetwork().addNode(n0);
		Node n1 = netFactory.createNode(Id.createNodeId("1"), new Coord(20,0));
		scenario.getNetwork().addNode(n1);
		Node n2 = netFactory.createNode(Id.createNodeId("2"), new Coord(9960,0));
		scenario.getNetwork().addNode(n2);
		Node n3 = netFactory.createNode(Id.createNodeId("3"), new Coord(10000,0));
		scenario.getNetwork().addNode(n3);
		{
			Link l = netFactory.createLink(Id.createLinkId("01"), n0, n1);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		{
			Link l = netFactory.createLink(Id.createLinkId("12"), n1, n2);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		{
			Link l = netFactory.createLink(Id.createLinkId("23"), n2, n3);
			l.setCapacity(1000);
			l.setFreespeed(38/3.6);
			l.setNumberOfLanes(1);
			l.setAllowedModes(CollectionUtils.stringToSet("car"));
			scenario.getNetwork().addLink(l);
		}
		
	}
	
	@Test
	public void testKmeansClustering() {
		
		// The points are equal to the network nodes
		final List<ClusterPoint> points = new ArrayList<>();
		scenario.getNetwork().getNodes().values().stream().map(Node::getCoord).forEach(coord -> {
			points.add(new ClusterPoint(coord));
		});
		
		// Init the k-means algorithm with two clusters and execute it as long as necessary
		Kmeans kmeans = new Kmeans(scenario.getNetwork(), 2);
		kmeans.init(points);
		kmeans.calculate();

		// Check if the clusters were set up correctly
		List<Cluster> clusters = kmeans.getClusters();
		
		Assert.assertEquals("Wrong coordinate set for first cluster centroid!", new Coord(10, 0),
				clusters.get(0).getCentroid().getCoord());
		Assert.assertEquals("Wrong coordinate set for second cluster centroid!", new Coord(9980, 0),
				clusters.get(1).getCentroid().getCoord());
		
	}
	
}