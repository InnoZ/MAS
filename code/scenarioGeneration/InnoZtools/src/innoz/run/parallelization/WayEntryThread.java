package innoz.run.parallelization;

import java.util.HashSet;
import java.util.Set;

import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;

import com.vividsolutions.jts.geom.Coordinate;

import innoz.scenarioGeneration.network.NetworkCreatorFromPsql;
import innoz.scenarioGeneration.network.WayEntry;

public final class WayEntryThread extends AlgoThread {

	private NetworkCreatorFromPsql networkCreator;
	private Set<WayEntry> wayEntries;
	
	@Override
	public void run() {

		// Iterate over all OSM ways
		for(WayEntry entry : wayEntries){

			// If access is restricted, we skip the way
			if("no".equals(entry.getAccessTag())) continue;
			
			Coordinate[] coordinates = entry.getGeometry().getCoordinates();
			
			if(coordinates.length > 1){
				
				// Set the from coordinate initially and the current way length to zero
				Coordinate from = coordinates[0];
				double length = 0.;
				Coordinate lastTo = from;
				
				// Go through all coordinates contained in the way
				for(int i = 1; i < coordinates.length; i++){
					
					// Get the next coordinate in the sequence and calculate the length between it and the last coordinate
					Coordinate next = coordinates[i];
					
					length = CoordUtils.calcEuclideanDistance(this.networkCreator.getTransformation().transform(
							MGC.coordinate2Coord(lastTo)), this.networkCreator.getTransformation().transform(
									MGC.coordinate2Coord(next)));

					boolean inSurveyArea = false;
					
					com.vividsolutions.jts.geom.Point lastPoint = this.networkCreator.getGeomFactory().createPoint(lastTo);
					com.vividsolutions.jts.geom.Point nextPoint = this.networkCreator.getGeomFactory().createPoint(next);
					
					// If the coordinates are contained in the survey area, add a new link to the network
					if(this.networkCreator.getBufferedArea().contains(lastPoint) || this.networkCreator.getBufferedArea().contains(nextPoint)){
						
						inSurveyArea = true;
						
					}
					
//					this.networkCreator.createLink(entry, length, lastTo, next, inSurveyArea);
						
					//Update last visited coordinate in the sequence
					lastTo = next;
					
				}
				
			}
			
		}
		
	}

	@Override
	public void init(Object... args) {
		
		this.wayEntries = new HashSet<>();
		this.networkCreator = (NetworkCreatorFromPsql)args[0];
		
	}

	@Override
	void addToThread(Object obj) {
		
		if(obj instanceof WayEntry){
			this.wayEntries.add((WayEntry)obj);
		}
		
	}
	
}
