package com.innoz.toolbox.scenarioGeneration.network;

import java.util.ArrayList;
import java.util.List;

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
	private String forwardLanesTag;
	private String backwardLanesTag;
	private String maxspeedTag;
	private String conditionalMaxspeedTag;
	private String onewayTag;
	Geometry geometry;
	
	private List<String> nodes = new ArrayList<>();

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
	
	public void setForwardLanesTag(String tag) {
		this.forwardLanesTag = tag;
	}
	
	public void setBackwardLanesTag(String tag) {
		this.backwardLanesTag = tag;
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
	
	public String getForwardLanesTag() {
		return this.forwardLanesTag;
	}
	
	public String getBackwardLanesTag() {
		return this.backwardLanesTag;
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
	
	public List<String> getNodes(){
		return this.nodes;
	}
	
	public String getConditionalMaxspeedTag() {
		return this.conditionalMaxspeedTag;
	}
	
	public void setConditionalMaxspeedTag(String tag) {
		this.conditionalMaxspeedTag = tag;
	}
	
}