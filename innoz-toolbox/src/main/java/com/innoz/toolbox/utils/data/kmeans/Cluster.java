package com.innoz.toolbox.utils.data.kmeans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * This class represents a cluster inside the k-means algorithm. It consists of an id, a centroid and a list of points.
 * Per definition, all points in this list lie closer to the cluster's centroid than to any centroid of the other clusters
 * (after iterating). 
 * 
 * @author dhosse
 *
 */
public class Cluster {

	// MEMBERS //////////////////////////////////
	private List<ClusterPoint> points;
	private ClusterPoint centroid;
	private final int id;
	/////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param id The cluster's unique identifier.
	 */
	public Cluster(int id) {
		
		this.id = id;
		this.points = new ArrayList<ClusterPoint>();
		this.centroid = null;
		
	}
	
	/**
	 * 
	 * Getter method for all the points attached to this cluster (meaning they lie possibly closer to this cluster's centroid
	 * than to any of the other clusters'). Note: The points contained may change between iterations!
	 * 
	 * @return The points contained within this cluster's geometry.
	 */
	public List<ClusterPoint> getPoints() {
		
		return this.points;
		
	}
	
	/**
	 * 
	 * Adds a new point to the cluster.
	 * 
	 * @param point
	 */
	public void addPoint(ClusterPoint point) {
		
		this.points.add(point);
		
	}
	
	/**
	 * 
	 * Returns the centroid of the cluster. Note: The centroid's coordinates may change between iterations!
	 * 
	 * @return
	 */
	public ClusterPoint getCentroid() {
		
		return this.centroid;
		
	}
	
	/**
	 * 
	 * Setter method for the centroid. The coordinate of the cluster's centroid is re-computed in every iteration based on the
	 * points contained in it. 
	 * 
	 * @param point
	 */
	public void setCentroid(ClusterPoint point) {
		
		this.centroid = point;
		
	}
	
	/**
	 * 
	 * Getter method for the identifier of the cluster.
	 * 
	 * @return
	 */
	public int getId() {
		
		return this.id;
		
	}
	
	/**
	 * 
	 * Method to clear the points contained in this cluster between iterations. This is needed in order to compute a new
	 * centroid.
	 * 
	 */
	public void clear() {
		
		this.points.clear();
		
	}
	
}