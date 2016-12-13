package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.facilities.ActivityFacility;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.ZensusGrid.ZensusGridNode;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.ProxyFacility;
import com.innoz.toolbox.scenarioGeneration.population.PopulationCreator;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanTrip;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.data.Tree.Node;

public abstract class DemandGenerationAlgorithm {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	final Random random = MatsimRandom.getLocalInstance();
	final Geoinformation geoinformation;
	
	//Comparator that sorts households by their weights
	Comparator<SurveyHousehold> householdComparator = new Comparator<SurveyHousehold>() {

		@Override
		public int compare(SurveyHousehold o1, SurveyHousehold o2) {
			return Double.compare(o1.getWeight(), o2.getWeight());
		
		}
		
	};
	
	static final Logger log = Logger.getLogger(PopulationCreator.class);
	/////////////////////////////////////////////////////////////////////////////////////////

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	CoordinateTransformation transformation;
	final Distribution distribution;
	
	final Scenario scenario;
	
	Coord currentHomeLocation = null;
	Coord currentMainActLocation = null;
	ActivityFacility currentHomeFacility = null;
	ActivityFacility currentMainActFacility = null;
	SurveyPlanTrip lastLeg = null;
	Coord lastActCoord = null;
	double c = 0d;
	AdministrativeUnit currentHomeCell;
	AdministrativeUnit currentMainActCell;
	Set<AdministrativeUnit> currentSearchSpace;
	AdministrativeUnit lastActCell = null;
	Matrix od;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DemandGenerationAlgorithm(final Scenario scenario, final Geoinformation geoinformation,
			final CoordinateTransformation transformation, final Matrix od, final Distribution distribution){
		
		this.geoinformation = geoinformation;
		this.transformation = transformation;
		this.scenario = scenario;
		this.od = od;
		
		// Initialize the disutilities for traveling from each cell to each other cell
		// to eventually get a gravitation model.
		this.distribution = distribution;
		
	}
	
	public abstract void run(final Configuration configuration, String ids);

	AdministrativeUnit chooseAdminUnit(AdministrativeUnit district, String activityType){
		
		double r = random.nextDouble() * this.geoinformation.getTotalWeightForLanduseKey(district.getId(), activityType);
		double r2 = 0.;
		
		for(Node<AdministrativeUnit> node : geoinformation.getAdminUnit(district.getId()).getChildren()){
			
			AdministrativeUnit admin = node.getData();
			
			double w = admin.getWeightForKey(activityType);
			
			if(w > 0){

				r2 += w;
				
				if(r <= r2 && admin.getLanduseGeometries().get(activityType) != null){
					
					return admin;
					
				}
				
			}
			
		}
		
		return null;
		
	}
	
	Coord chooseActivityCoordInAdminUnit(AdministrativeUnit admin, String activityType){
		
		double p = random.nextDouble() * admin.getWeightForKey(activityType);
		double accumulatedWeight = 0.;

		if(activityType.equals(ActivityTypes.HOME)){
			
			return chooseActivityCoordAccordingToZensusGrid(admin);
			
		}
		
		for(Landuse g : admin.getLanduseGeometries().get(activityType)){
			
			accumulatedWeight += g.getWeight();
			
			if(p <= accumulatedWeight){
				
				// Shoot the location
				return transformation.transform(GeometryUtils.shoot(g.getGeometry(), random));

			}
			
		}
		
		return null;
		
	}
	
	Map<String, Tuple<List<ZensusGridNode>, Integer>> map = new HashMap<>();
	
	Coord chooseActivityCoordAccordingToZensusGrid(AdministrativeUnit admin) {
		
		if(!map.containsKey(admin.getId())) {
			
			List<ZensusGridNode> nodes = new ArrayList<>();
			int weight = 0;
			
			for(ZensusGridNode node : PopulationCreator.grid.getNodes()){
				
				if(admin.getGeometry().contains(MGC.coord2Point(node.getCoord()))){
					
					nodes.add(node);
					weight += node.getNumberOfInhabitants();
					
				}
				
			}
			
			map.put(admin.getId(), new Tuple<List<ZensusGridNode>, Integer>(nodes, weight));
			
		}
		
		Tuple<List<ZensusGridNode>, Integer> entry = map.get(admin.getId());
		
		double p = random.nextDouble() * entry.getSecond();
		double accumulatedWeight = 0.0;
		
		for(ZensusGridNode node : entry.getFirst()){
			
			accumulatedWeight += node.getNumberOfInhabitants();
			if(p <= accumulatedWeight){
				return transformation.transform(node.getCoord());
			}
			
		}
		return null;
		
	}
	
	ActivityFacility chooseActivityFacilityInAdminUnit(AdministrativeUnit admin, String activityType){
		
		double p = random.nextDouble() * admin.getWeightForKey(activityType);
		double accumulatedWeight = 0;
		
		for(Landuse g : admin.getLanduseGeometries().get(activityType)){
			
			accumulatedWeight += g.getWeight();
			
			if(p <= accumulatedWeight){

				// Shoot the location
				return ((ProxyFacility)g).get();

			}
			
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * Randomly chooses an administrative unit in which an activity of a certain type should be located.
	 * The randomness depends on the distribution computed earlier, the activity type and the transport mode.
	 * 
	 * @param activityType The type of the activity to locate.
	 * @param mode The transport mode used to get to the activity.
	 * @param personTemplate The survey person.
	 * @return The administrative unit in which the activity most likely is located.
	 */
	AdministrativeUnit locateActivityInCell(String activityType, String mode, SurveyPerson personTemplate){
		
		return locateActivityInCell(null, activityType, mode, personTemplate);
		
	}
	
	AdministrativeUnit locateWorkActivity(){
		
		String fromId = this.currentHomeCell.getId().substring(0, 5);
		
		int weight = 0;
		
		for(org.matsim.matrices.Entry entry : this.od.getFromLocations().get(fromId)){
			
			weight += entry.getValue();
			
		}
		
		double p = this.random.nextDouble() * weight;
		double accumulatedWeight = 0d;
		
		for(org.matsim.matrices.Entry entry : this.od.getFromLocations().get(fromId)){
			
			accumulatedWeight += entry.getValue();
			if(p <= accumulatedWeight){

				AdministrativeUnit unit = this.geoinformation.getAdminUnit(fromId).getData();
				
				if(unit.getGeometry() != null){
					
					return unit;
					
				} else {
					
					return this.geoinformation.getAdminUnit(fromId).getChildren().get(random.nextInt(this.geoinformation.getAdminUnit(fromId).getChildren().size())).getData();
					
				}
				
			}
			
		}
		
		return null;
		
	}

	/**
	 * 
	 * Same method as {@link #locateActivityInCell(String, String, SurveyPerson)}, only that this method locates an activity of a sequence
	 * where the last activity and the distance traveled between the two locations is known.
	 * 
	 * @param fromId The identifier of the last administrative unit.
	 * @param activityType The type of the current activity.
	 * @param mode The transport mode used.
	 * @param personTemplate The survey person.
	 * @param distance The distance traveled between the last and the current activity.
	 * @return
	 */
	AdministrativeUnit locateActivityInCell(String fromId, String activityType, String mode, SurveyPerson personTemplate){
		
		Set<String> modes = new HashSet<String>();
		
		if(fromId == null){
			fromId = this.currentHomeCell.getId();
		}
		
		if(activityType.equals(ActivityTypes.WORK)){
			return locateWorkActivity();
		}
		
		if(activityType.split("_")[0].equals(ActivityTypes.EDUCATION) && this.currentHomeCell.getWeightForKey(ActivityTypes.EDUCATION) > 0){
			return this.currentHomeCell;
		}
		
		if(mode != null){

			// If the person walked, it most likely didn't leave the last cell (to avoid very long walk legs)
//			if(mode.equals(TransportMode.walk) && fromId != null){
//				
//				return this.geoinformation.getAdminUnit(fromId).getData();
//				
//			}
			
			// Add the transport mode used
			modes.add(mode);
			
		} else {
			
			// If the transport mode wasn't reported, consider all modes the person could have used
			modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt + ","
					+ TransportMode.walk);
			
			if(personTemplate.hasCarAvailable()){
			
				modes.add(TransportMode.ride);
				
				if(personTemplate.hasLicense()){
					
					modes.add(TransportMode.car);
					
				}
				
			}
			
		}
		
		AdministrativeUnit result = null;
		
		Map<String, Double> toId2Disutility = new HashMap<String, Double>();
		double sumOfWeights = 0d;
		
		// Set the search space to the person's search space if it's not null.
		// Else consider the whole survey area.
		Set<AdministrativeUnit> adminUnits = null;
		if(this.currentSearchSpace != null){
			
			if(this.currentSearchSpace.size() > 0){
				
				adminUnits = this.currentSearchSpace;
				
			}
			
		}
		
		if(adminUnits == null){
			
			adminUnits = new HashSet<AdministrativeUnit>();
			adminUnits.addAll(this.geoinformation.getAdminUnitsWithGeometry());
			
		}
		
		// Go through all administrative units in the search space
		// Sum up the disutilities of all connections and map the entries for further work
		for(AdministrativeUnit au : adminUnits){
			
			double disutility = Double.NEGATIVE_INFINITY;
				
			for(String m : modes){
				
				if(fromId != null){
					
					disutility = this.distribution.getDisutilityForActTypeAndMode(fromId, au.getId(),
							activityType, m);
					
				} else {
					
					disutility = this.distribution.getDisutilityForActTypeAndMode(
							this.currentHomeCell.getId(), au.getId(), activityType, m);
					
				}
				
				if(Double.isFinite(disutility) && disutility != 0){
					
					toId2Disutility.put(au.getId(), disutility);
					sumOfWeights += disutility;
					
				}
				
			}
			
		}
		
		// Randomly choose a connection out of the search space
		double r = this.random.nextDouble() * sumOfWeights;
		double accumulatedWeight = 0d;
		
		for(Entry<String, Double> entry : toId2Disutility.entrySet()){
			
			accumulatedWeight += entry.getValue();
			if(r <= accumulatedWeight){
				result = this.geoinformation.getAdminUnit(entry.getKey()).getData();
				if(result == null){
					result = this.geoinformation.getAdminUnit(entry.getKey()).getData();
				}
				break;
			}
			
		}
		
		if(result == null) return this.currentHomeCell;
		
		return result;
		
	}
	
}
