package innoz.scenarioGeneration.geoinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

public class AdministrativeUnit {
	
	private String id;
	
	private Integer bland;
	private Integer regionType;
	
	private int nInhabitants;
	
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
	
	public void setBland(int bland){
		this.bland = bland;
	}
	
	public Integer getBland(){
		return this.bland;
	}

}