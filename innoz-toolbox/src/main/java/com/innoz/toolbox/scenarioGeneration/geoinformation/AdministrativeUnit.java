package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.vividsolutions.jts.geom.Geometry;

public class AdministrativeUnit {
	
	private String id;
	
	private Integer bland;
	private Integer regionType;
	
	private Integer networkDetail = 6;
	
	private HashMap<String, Integer> populationByAgeGroup;
	private int nHouseholds;
	
	private Geometry geometry;
	
	private Map<String, List<Landuse>> landuseGeometries;
	
	private Map<String, Double> weightForKey = new HashMap<>();
	
	public AdministrativeUnit(String id){
		
		this.id = id;
		this.landuseGeometries = new HashMap<String, List<Landuse>>();
		
	}
	
	public String getId(){
		
		return this.id;
		
	}

	public Integer getRegionType() {
	
		return this.regionType;
		
	}

	public void setRegionType(Integer regionType) {
		
		this.regionType = regionType;
		
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public void addLanduse(String key, Landuse value){
		
		if(!this.landuseGeometries.containsKey(key)){
			
			this.landuseGeometries.put(key, new ArrayList<>());
			this.weightForKey.put(key, 0.);
			
		}
		
		this.landuseGeometries.get(key).add(value);
		double weight = this.weightForKey.get(key) + value.getWeight();
		this.weightForKey.put(key, weight);
		
	}
	
	public double getWeightForKey(String key){
		if(this.weightForKey.containsKey(key)){
			return this.weightForKey.get(key);
		} else return 0d;
	}
	
	public Map<String, List<Landuse>> getLanduseGeometries(){
		return this.landuseGeometries;
	}
	
	public void setBland(int bland){
		this.bland = bland;
	}
	
	public Integer getBland(){
		return this.bland;
	}
	
	public void setNumberOfHouseholds(int n){
		this.nHouseholds = n;
	}
	
	public int getNumberOfHouseholds(){
		return this.nHouseholds;
	}
	
	public Integer getNetworkDetail(){
		return this.networkDetail;
	}
	
	public void setNetworkDetail(Integer lod){
		if(lod != null){
			this.networkDetail = lod;
		}
	}
	
	@Override
	public String toString(){
		return this.id;
	}
	
	public void setPopulationMap(HashMap<String, Integer> map){
		
		this.populationByAgeGroup = map;
		
	}
	
	public Map<String, Integer> getPopulationMap(){
		
		return this.populationByAgeGroup;
		
	}

}