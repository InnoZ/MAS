package simulationWithFacilitiesForMID_Data;

import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.utils.collections.QuadTree;

/*
 * this class builds a QuadTree for facilities with a specified activityType. The Tree stores the
 * facilities themselves and their x-/and y-coordinate.
 */
public class FacilitiesTreeBuilder {

	private final static Logger log = Logger
			.getLogger(CreateDemandWithMID_Data.class);
	
	public QuadTree<ActivityFacility> buildFacQuadTree(String type,
			Map<Id, ? extends ActivityFacility> facilities_of_type) {
		log.info(" building " + type + " facility quad tree");
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (final ActivityFacility f : facilities_of_type.values()) {
			if (f.getCoord().getX() < minx) {
				minx = f.getCoord().getX();
			}
			if (f.getCoord().getY() < miny) {
				miny = f.getCoord().getY();
			}
			if (f.getCoord().getX() > maxx) {
				maxx = f.getCoord().getX();
			}
			if (f.getCoord().getY() > maxy) {
				maxy = f.getCoord().getY();
			}
		}
		minx -= 1.0;
		miny -= 1.0;
		maxx += 1.0;
		maxy += 1.0;
		System.out.println("        xrange(" + minx + "," + maxx + "); yrange("
				+ miny + "," + maxy + ")");
		QuadTree<ActivityFacility> quadtree = new QuadTree<ActivityFacility>(minx,
				miny, maxx, maxy);
		for (final ActivityFacility f : facilities_of_type.values()) {
			quadtree.put(f.getCoord().getX(), f.getCoord().getY(), f);
		}
		log.info("Quadtree size: " + quadtree.size());
		return quadtree;
	}
	
}
