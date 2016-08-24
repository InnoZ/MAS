package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.core.utils.collections.QuadTree;
import org.matsim.facilities.ActivityFacility;

import com.innoz.toolbox.utils.data.Tree;
import com.innoz.toolbox.utils.data.Tree.Node;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * Class for reading, storing and accessing geoinformation, such as
 * <ul>
 * <li>administrative borders
 * <li>osm data
 * <ul>
 * <li>landuse
 * <li>buildings
 * <li>amenities (e.g. shops, schools)
 * </ul>
 * </ul>
 * 
 * There are standard procedures for reading geometry tables of the MobilityDatahub.
 * 
 * @author dhosse
 *
 */
public class Geoinformation {
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Tree<AdministrativeUnit> adminUnitTree;
	
	private Map<Integer, Set<Integer>> regionTypesToDistricts;
	
	private Geometry surveyAreaBoundingBox;
	private Geometry vicinityBoundingBox;
	private Geometry completeGeometry;
	protected Map<String,QuadTree<Geometry>> actType2QT;
	protected Map<String, QuadTree<ActivityFacility>> facilityQuadTree;
	protected Geometry catchmentAreaPt;
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	public Geoinformation(){
		
		// Initialize the tree with the highest level admin unit (Germany)
		this.adminUnitTree = new Tree<AdministrativeUnit>(new AdministrativeUnit("0"));
		
		this.actType2QT = new HashMap<String, QuadTree<Geometry>>();
		this.facilityQuadTree = new HashMap<>();
		
		this.regionTypesToDistricts = new HashMap<Integer, Set<Integer>>();
		
	}
	
	public void addAdministrativeUnit(AdministrativeUnit unit){
		
		this.adminUnitTree.add(unit);
		
	}
	
	public Node<AdministrativeUnit> getAdminUnit(String id){
		
		return this.adminUnitTree.get(id);
		
	}
	
	public List<Node<AdministrativeUnit>> getAdminUnits(){
		
		return this.adminUnitTree.getAll();
		
	}
	
	public List<AdministrativeUnit> getAdminUnitsWithGeometry(){
		
		List<AdministrativeUnit> units = new ArrayList<>();
		
		List<Node<AdministrativeUnit>> nodes = this.getAdminUnits();
		for(Node<AdministrativeUnit> node : nodes){
			if(node.getData().getGeometry() != null){
				units.add(node.getData());
			}
		}
		
		return units;
		
	}
	
	public int getNumberOfAdminUnits(){
		
		return this.adminUnitTree.getSize();
		
	}
	
	/**
	 * 
	 * Sums up the weights for all landuse geometries that are mapped to the specified key.
	 * 
	 * @param key A string representing the landuse type of interest.
	 * @return The total weight of the landuse geometries inside the survey area.
	 */
	public double getTotalWeightForLanduseKey(String districtId, String key){
		
		double weight = 0.;
		
		Node<AdministrativeUnit> node = this.adminUnitTree.get(districtId);
		
		for(Node<AdministrativeUnit> childNode : node.getChildren()){
			
			weight += childNode.getData().getWeightForKey(key);
			
		}
		
		return weight;
		
	}
	
	public void setSurveyAreaBoundingBox(Geometry g){
		this.surveyAreaBoundingBox = g;
	}
	
	public Geometry getSurveyAreaBoundingBox(){
		
		return this.surveyAreaBoundingBox;
		
	}
	
	public void setVicinityBoundingBox(Geometry g){
		this.vicinityBoundingBox = g;
	}
	
	public Geometry getVicinityBoundingBox(){
		
		return this.vicinityBoundingBox;
		
	}
	
	public Geometry getCompleteGeometry(){
		
		return completeGeometry;
		
	}
	
	public void setCompleteGeometry(Geometry g){
		
		completeGeometry = g;
		
	}
	
	public QuadTree<Geometry> getQuadTreeForActType(String actType){
		
		return actType2QT.get(actType);
		
	}
	
	public void createQuadTreeForActType(String actType, double[] bounds){
		
		actType2QT.put(actType, new QuadTree<Geometry>(bounds[0], bounds[1], bounds[2], bounds[3]));
		
	}
	
	public QuadTree<ActivityFacility> getQuadTreeForFacilityActType(String actType){
		
		return facilityQuadTree.get(actType);
		
	}
	
	public void createQuadTreeForFacilityActType(String actType, double[] bounds){
		
		facilityQuadTree.put(actType, new QuadTree<ActivityFacility>(bounds[0], bounds[1], bounds[2], bounds[3]));
		
	}
	
	public Geometry getCatchmentAreaPt(){
		
		return catchmentAreaPt;
		
	}
	
	public void setCatchmentAreaPt(Geometry geometry){
		
		catchmentAreaPt = geometry;
		
	}
	
	public Map<Integer, Set<Integer>> getRegionTypes(){
		return this.regionTypesToDistricts;
	}
	
}