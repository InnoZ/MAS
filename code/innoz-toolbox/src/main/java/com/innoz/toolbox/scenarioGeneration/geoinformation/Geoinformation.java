package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.facilities.ActivityFacility;
import org.opengis.feature.simple.SimpleFeature;

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
 * There are standard procedures for reading shapefiles or geometry tables of the MobilityDatahub.
 * 
 * @author dhosse
 *
 */
public class Geoinformation {
	
	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	public static final String AUTH_KEY_WGS84 = "EPSG:4326";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Map<String, District> adminUnits;
	private Map<String, AdministrativeUnit> subUnits;
//	private List<District> vicinity;
//	private Map<String, AdministrativeUnit> adminUnits;
	
	private Set<Integer> states;
	private Map<Integer, Set<Integer>> regionTypesToDistricts;
	
	private Geometry surveyAreaBoundingBox;
	private Geometry vicinityBoundingBox;
	private Geometry completeGeometry;
	protected Map<String,QuadTree<Geometry>> actType2QT;
	protected Map<String, QuadTree<ActivityFacility>> facilityQuadTree;
	protected Geometry catchmentAreaPt;
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	public Geoinformation(){
		
//		this.surveyArea = new ArrayList<District>();
//		this.vicinity = new ArrayList<District>();
		this.adminUnits = new HashMap<String, District>();
		this.subUnits = new HashMap<String, AdministrativeUnit>();
		this.actType2QT = new HashMap<String, QuadTree<Geometry>>();
		this.facilityQuadTree = new HashMap<>();
		
		this.states = new HashSet<Integer>();
		this.regionTypesToDistricts = new HashMap<Integer, Set<Integer>>();
		
	}
	
	/**
	 * Reads the geometries of the specified id(s) from an ESRI shapefile into the
	 * administrative unit collection.
	 * 
	 * @param filename Input shapefile path.
	 * @param ids The id's of the geometries we want to read in.
	 */
	public void readGeodataFromShapefile(String filename, Set<String> ids){
		
		// Read in the shapefile
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		// Go through all features. If the GKZ (Gemeindekennzahl) is one of the specified ids,
		// add a new administrative unit with the feature's geometry.
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			if(ids.contains(kennzahl)){
				
				AdministrativeUnit au = new AdministrativeUnit(kennzahl);
				au.setGeometry((Geometry)feature.getDefaultGeometry());
//				surveyArea.put(kennzahl, au);
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Same functionality as {@link #readGeodataFromShapefile(String, Set)}. The difference is that
	 * the ids of the shapefile features are tested wheter they start with any of the id strings inside
	 * the filter ids set. By that, it's possible to read in e.g. all municipalities of a district or region.
	 * 
	 * @param filename Input shapefile path.
	 * @param filterIds
	 */
	public void readGeodataFromShapefileWithFilter(String filename, Set<String> filterIds){
		
		// Read in the shapefile
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(filename);
		
		// Go through all features. If the GKZ (Gemeindekennzahl) starts with one of the specified ids,
		// add a new administrative unit with the feature's geometry.
		for(SimpleFeature feature : features){
			
			String kennzahl = Long.toString((Long)feature.getAttribute("GEM_KENNZ"));
			
			for(String id : filterIds){
				
				if(kennzahl.startsWith(id)){
	
					AdministrativeUnit au = new AdministrativeUnit(kennzahl);
					au.setGeometry((Geometry)feature.getDefaultGeometry());
//					surveyArea.put(kennzahl, au);
					break;
					
				}
				
			}
			
		}
		
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
		
		for(AdministrativeUnit au : adminUnits.get(districtId).getAdminUnits().values()){
			
			weight += au.getWeightForKey(key);
			
		}
		
		return weight;
		
	}
	
	public Map<String, District> getAdminUnits(){
		
		return this.adminUnits;
		
	}
	
//	public List<District> getSurveyArea(){
//		
//		return surveyArea;
//		
//	}
	
//	public List<District> getVicinity(){
//		
//		return vicinity;
//		
//	}
	
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
	
	public Set<Integer> getStatesSet(){
		return this.states;
	}
	
	public Map<Integer, Set<Integer>> getRegionTypes(){
		return this.regionTypesToDistricts;
	}
	
	public AdministrativeUnit getAdminUnitById(String id){
		return this.subUnits.get(id);
	}
	
	public Map<String, AdministrativeUnit> getSubUnits(){
		return this.subUnits;
	}
	
	public void addSubUnit(AdministrativeUnit au){
		this.subUnits.put(au.getId(), au);
	}
	
}