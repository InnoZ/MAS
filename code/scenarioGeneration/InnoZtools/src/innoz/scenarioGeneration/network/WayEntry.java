package innoz.scenarioGeneration.network;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Wrapper class that stores the OSM data of a way.
 * 
 * @author dhosse
 *
 */
public class WayEntry{
		
	public String osmId;
	public String accessTag;
	public String highwayTag;
	public String junctionTag;
	public String lanesTag;
	public String maxspeedTag;
	public String onewayTag;
	public Geometry geometry;

	public WayEntry() {}

	public void setOsmId(String osmId){
		this.osmId = osmId;
	}
	
	public void setAccessTag(String accessTag){
		this.accessTag = accessTag;
	}
	
	public void setHighwayTag(String highwayTag){
		this.highwayTag = highwayTag;
	}
	
	public void setJunctionTag(String junctionTag){
		this.junctionTag = junctionTag;
	}
	
	public void setLanesTag(String lanesTag){
		this.lanesTag = lanesTag;
	}
	
	public void setMaxspeedTag(String maxspeedTag){
		this.maxspeedTag = maxspeedTag;
	}
	
	public void setOnewayTag(String onewayTag){
		this.onewayTag = onewayTag;
	}
	
	public void setGeometry(Geometry geometry){
		this.geometry = geometry;
	}
}