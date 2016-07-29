package innoz.scenarioGeneration.population.algorithm;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import org.matsim.core.utils.geometry.CoordinateTransformation;

import com.vividsolutions.jts.geom.Geometry;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Distribution;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;
import innoz.utils.GeometryUtils;

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
	SurveyPlanTrip lastLeg = null;
	Coord lastActCoord = null;
	double c = 0d;
	AdministrativeUnit currentHomeCell;
	AdministrativeUnit currentMainActCell;
	Set<AdministrativeUnit> currentSearchSpace;
	AdministrativeUnit lastActCell = null;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DemandGenerationAlgorithm(final Scenario scenario, final Geoinformation geoinformation,
			final CoordinateTransformation transformation, final Distribution distribution){
		
		this.geoinformation = geoinformation;
		this.transformation = transformation;
		this.scenario = scenario;
		
		// Initialize the disutilities for traveling from each cell to each other cell
				// to eventually get a gravitation model.
		this.distribution = distribution;
		
	}
	
	public abstract void run(final Configuration configuration, String ids);

	AdministrativeUnit chooseAdminUnitInsideDistrict(District district, String activityType){
		
		double r = random.nextDouble() * this.geoinformation.getTotalWeightForLanduseKey(district.getId(), activityType);
		double r2 = 0.;
		
		for(AdministrativeUnit admin : district.getAdminUnits().values()){
			
			r2 += admin.getWeightForKey(activityType);
			
			if(r <= r2 && admin.getLanduseGeometries().get(activityType) != null){
				
				return admin;
				
			}
			
		}
		
		return null;
		
	}
	
	Coord chooseActivityCoordInAdminUnit(AdministrativeUnit admin, String activityType){
		
		double p = random.nextDouble() * admin.getWeightForKey(activityType);
		double accumulatedWeight = 0.;
		
		for(Geometry g : admin.getLanduseGeometries().get(activityType)){
			
			accumulatedWeight += g.getArea();
			
			if(p <= accumulatedWeight){

				// Shoot the location
				return transformation.transform(GeometryUtils.shoot(g, random));

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
		
		if(mode != null){

			// If the person walked, it most likely didn't leave the last cell (to avoid very long walk legs)
			if(mode.equals(TransportMode.walk) && fromId != null){
				
				return this.geoinformation.getAdminUnitById(fromId);
				
			}
			
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
			adminUnits.addAll(this.geoinformation.getSubUnits().values());
//			adminUnits.remove(this.currentHomeCell);
			
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
				
				if(Double.isFinite(disutility)){
					
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
				result = this.geoinformation.getSubUnits().get(entry.getKey());
				if(result == null){
					result = this.geoinformation.getSubUnits().get(entry.getKey());
				}
				break;
			}
			
		}
		
		return result;
		
	}
	
}
