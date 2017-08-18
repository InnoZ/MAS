package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacility;
import org.matsim.pt.PtConstants;

import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.ProxyFacility;
import com.innoz.toolbox.scenarioGeneration.population.OriginDestinationData;
import com.innoz.toolbox.scenarioGeneration.population.PopulationCreator;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanTrip;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.data.Tree.Node;
import com.innoz.toolbox.utils.data.WeightedSelection;

public abstract class DemandGenerationAlgorithm {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	final Random random = MatsimRandom.getRandom();
	
	static final Logger log = Logger.getLogger(PopulationCreator.class);
	/////////////////////////////////////////////////////////////////////////////////////////

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
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
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DemandGenerationAlgorithm() {}
	
	public abstract void run(String ids);

	AdministrativeUnit chooseAdminUnit(AdministrativeUnit district, String activityType){

		double r = random.nextDouble() * Geoinformation.getInstance().getTotalWeightForLanduseKey(district.getId(), activityType);
		double r2 = 0.;
		
		for(Node<AdministrativeUnit> node : Geoinformation.getInstance().getAdminUnit(district.getId()).getChildren()){
			
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
		
		return (activityType.equals(ActivityTypes.HOME)) ?
				OriginDestinationData.chooseHomeLocationFromGrid(admin) : 
				Geoinformation.getTransformation().transform(GeometryUtils.shoot(
				((Landuse)WeightedSelection.choose(admin.getLanduseGeometries().get(activityType), this.random.nextDouble()))
				.getGeometry(), this.random));
		
	}
	
	ActivityFacility chooseActivityFacilityInAdminUnit(AdministrativeUnit admin, String activityType){
		
		return ((ProxyFacility) WeightedSelection.choose(admin.getLanduseGeometries().get(activityType), this.random.nextDouble())).get();
		
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
			fromId = this.lastActCell != null ? this.lastActCell.getId() : this.currentHomeCell.getId();
		}
		
		if(activityType.equals(ActivityTypes.WORK)) {
			if (mode != null && mode.equals(TransportMode.walk)) return this.currentHomeCell;
			return OriginDestinationData.chooseWorkLocation(this.currentHomeCell.getId(), MatsimRandom.getRandom().nextDouble());
		}
		
		if(activityType.split("_")[0].equals(ActivityTypes.EDUCATION) && this.currentHomeCell.getWeightForKey(ActivityTypes.EDUCATION) > 0){
			return this.currentHomeCell;
		}
		
		if(mode != null){

			if(mode.equals(TransportMode.walk)) return this.lastActCell != null ? this.lastActCell : this.currentHomeCell;
			// Add the transport mode used
			modes.add(mode);
			
		} else {
			
			// If the transport mode wasn't reported, consider all modes the person could have used
			modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt);
			
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
			adminUnits.addAll(Geoinformation.getInstance().getAdminUnitsWithGeometry());
			
		}
		
		// Go through all administrative units in the search space
		// Sum up the disutilities of all connections and map the entries for further work
		for(AdministrativeUnit au : adminUnits){
			
			double disutility = Double.NEGATIVE_INFINITY;
				
			for(String m : modes){
				
				if(fromId != null){
					
					disutility = OriginDestinationData.getDistribution().getDisutilityForActTypeAndMode(fromId, au.getId(),
							activityType, m);
					
				} else {
					
					disutility = OriginDestinationData.getDistribution().getDisutilityForActTypeAndMode(
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
				result = Geoinformation.getInstance().getAdminUnit(entry.getKey()).getData();
				if(result == null){
					result = Geoinformation.getInstance().getAdminUnit(entry.getKey()).getData();
				}
				break;
			}
			
		}
		
		if(result == null) return this.currentHomeCell;
		
		return result;
		
	}
	
	double timeShift = 0.0;
	double delta = 0;
	
	void mutateActivityEndTimes(Plan plan) {
		
		timeShift = 0.0;
		
		plan.getPlanElements().stream().filter(pe -> pe instanceof Activity).map(pe -> (Activity)pe)
			.filter(pe -> pe.getStartTime() != Time.UNDEFINED_TIME && pe.getEndTime() != Time.UNDEFINED_TIME).forEach(pe -> {
			
			if(pe.getStartTime() - pe.getEndTime() == 0) timeShift += 1800;
			
		});
		
		Activity firstAct = (Activity) plan.getPlanElements().get(0);
		Activity lastAct = (Activity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
		
		delta = 0;
		while(delta == 0) {
			delta = com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.createRandomEndTime();
			if(firstAct.getEndTime() + delta < 0)
				delta = 0;
			if(lastAct.getStartTime() + delta + timeShift > Time.MIDNIGHT)
				delta = 0;
			if(lastAct.getEndTime() != Time.UNDEFINED_TIME) {
				if(lastAct.getStartTime() + delta + timeShift >= lastAct.getEndTime())
					delta = 0;
			}
		}
		
		plan.getPlanElements().stream().filter(pe -> pe instanceof Activity).map(pe -> (Activity)pe)
			.filter(pe -> !pe.getType().equals(PtConstants.TRANSIT_ACTIVITY_TYPE)).forEach(pe -> {
				
				if(plan.getPlanElements().indexOf(pe) > 0) {
					pe.setStartTime(pe.getStartTime() + delta);
				}
				if(plan.getPlanElements().indexOf(pe) < plan.getPlanElements().size()-1) {
					pe.setEndTime(pe.getEndTime() + delta);
				}
				
			});
		
	}
	
}