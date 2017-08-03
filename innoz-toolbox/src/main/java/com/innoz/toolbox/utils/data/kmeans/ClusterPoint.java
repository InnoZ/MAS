package com.innoz.toolbox.utils.data.kmeans;

import org.matsim.api.core.v01.Coord;

/**
 * 
 * A point that is to be clustered by the Kmeans class.
 * 
 * @author dhosse
 *
 */
public class ClusterPoint {

	// MEMBERS //////////////////////////////////
	private Coord coord;
	private int clusterNumber;
	/////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param c The coordinate of the point. Must not be null and lie inside the boundaries of the scenario's network!
	 */
	public ClusterPoint(Coord c) {
		
		this.coord = c;
		
	}
	
	/**
	 * 
	 * Getter method for the point's coordinate.
	 * 
	 * @return The coordinate of the point.
	 */
	public Coord getCoord() {
		
		return this.coord;
		
	}
	
	/**
	 * 
	 * Setter method for the point's coordinate.
	 * 
	 * @param c A non-null coordinate 
	 */
	public void setCoord(Coord c) {
		this.coord = c;
	}
	
	/**
	 * 
	 * Getter for the cluster's identifier this point is assigned to.
	 * 
	 * @return
	 */
	public int getCluster() {
		
		return this.clusterNumber;
		
	}
	
	/**
	 * 
	 * Setter method for the point's cluster.
	 * 
	 * @param cluster
	 */
	public void setCluster(int cluster) {
		
		this.clusterNumber = cluster;
		
	}
	
}