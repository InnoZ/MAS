package playground.dhosse.scenarioGeneration.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class AdministrativeUnit {
	
	private String id;
	
	private Integer bland;
	private Integer regionType;
	private Integer districtType;
	private Integer municipalityType;
	
	private int nInhabitants;
	private double pChild;
	private double pAdult;
	private double pPensioner;
	
	private Geometry geometry;
	
	private Map<String, List<Geometry>> landuseGeometries;
	private Map<String, List<Geometry>> buildingsGeometries;
	
	private Map<String, Double> weightForKey = new HashMap<>();
	
	public AdministrativeUnit(String id){
		
		this.id = id;
		this.landuseGeometries = new HashMap<String, List<Geometry>>();
		this.buildingsGeometries = new HashMap<String, List<Geometry>>();
		
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

	public Integer getDistrictType() {
		
		return this.districtType;
		
	}

	public void setDistrictType(Integer districtType) {
		
		this.districtType = districtType;
		
	}

	public Integer getMunicipalityType() {
		
		return this.municipalityType;
		
	}

	public void setMunicipalityType(Integer municipalityType) {
		
		this.municipalityType = municipalityType;
		
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public void addLanduseGeometry(String key, Geometry value){
		if(!this.landuseGeometries.containsKey(key)){
			this.landuseGeometries.put(key, new ArrayList<>());
			this.weightForKey.put(key, 0.);
		}
		this.landuseGeometries.get(key).add(value);
		double weight = this.weightForKey.get(key) + value.getArea();
		this.weightForKey.put(key, weight);
	}
	
	public double getWeightForKey(String key){
		if(this.weightForKey.containsKey(key)){
			return this.weightForKey.get(key);
		} else return 0d;
	}
	
	public Map<String, List<Geometry>> getLanduseGeometries(){
		return this.landuseGeometries;
	}
	
	public Map<String, List<Geometry>> getBuildingsGeometries(){
		return this.buildingsGeometries;
	}
	
	public int getNumberOfInhabitants(){
		return this.nInhabitants;
	}

	public double getpChild() {
		return pChild;
	}

	public double getpAdult() {
		return pAdult;
	}

	public double getpPensioner() {
		return pPensioner;
	}
	
	public void setBland(int bland){
		this.bland = bland;
	}
	
	public Integer getBland(){
		return this.bland;
	}

}