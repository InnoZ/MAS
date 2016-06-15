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
		
	private String osmId;
	private String accessTag;
	private String highwayTag;
	private String junctionTag;
	private String lanesTag;
	private String maxspeedTag;
	private String onewayTag;
	Geometry geometry;

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

	public String getOsmId() {
		return osmId;
	}

	public String getAccessTag() {
		return accessTag;
	}

	public String getHighwayTag() {
		return highwayTag;
	}

	public String getJunctionTag() {
		return junctionTag;
	}

	public String getLanesTag() {
		return lanesTag;
	}

	public String getMaxspeedTag() {
		return maxspeedTag;
	}

	public String getOnewayTag() {
		return onewayTag;
	}

	public Geometry getGeometry() {
		return geometry;
	}
}