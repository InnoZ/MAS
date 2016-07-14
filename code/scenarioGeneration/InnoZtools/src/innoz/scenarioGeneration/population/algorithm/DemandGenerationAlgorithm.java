package innoz.scenarioGeneration.population.algorithm;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import com.vividsolutions.jts.geom.Geometry;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Distribution;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
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
	Distribution distribution;
	
	final Scenario scenario;
	
	Coord currentHomeLocation = null;
	Coord currentMainActLocation = null;
	SurveyPlanTrip lastLeg = null;
	Coord lastActCoord = null;
	double c = 0d;
	AdministrativeUnit currentHomeCell;
	AdministrativeUnit currentMainActCell;
	List<AdministrativeUnit> currentSearchSpace;
	AdministrativeUnit lastActCell = null;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DemandGenerationAlgorithm(final Scenario scenario, final Geoinformation geoinformation,
			final CoordinateTransformation transformation){
		
		this.geoinformation = geoinformation;
		this.transformation = transformation;
		this.scenario = scenario;
		
		// Initialize the disutilities for traveling from each cell to each other cell
				// to eventually get a gravitation model.
		this.distribution = new Distribution(scenario.getNetwork(), this.geoinformation, 
				this.transformation);
		
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
				// Shoot the work location
			
				return transformation.transform(GeometryUtils.shoot(g, random));

			}
			
		}
		
		return null;
		
	}
	
}
