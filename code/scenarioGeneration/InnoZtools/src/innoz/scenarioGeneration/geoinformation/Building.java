package innoz.scenarioGeneration.geoinformation;

import java.util.HashSet;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;

public class Building {

	private Set<String> activityOptions;
	private final Geometry geometry;
	
	public Building(final Geometry geometry){
		this.activityOptions = new HashSet<String>();
		this.geometry = geometry;
	}
	
	public Set<String> getActivityOptions(){
		return this.activityOptions;
	}
	
	public void addActivityOption(String activityType){
		this.activityOptions.add(activityType);
	}
	
	public Geometry getGeometry(){
		return this.geometry;
	}
	
}
