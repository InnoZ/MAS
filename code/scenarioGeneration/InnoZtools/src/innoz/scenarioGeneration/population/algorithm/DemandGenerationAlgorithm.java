package innoz.scenarioGeneration.population.algorithm;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Distribution;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.PopulationCreator;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.geometry.CoordinateTransformation;

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
	
	public DemandGenerationAlgorithm(final Geoinformation geoinformation,
			final CoordinateTransformation transformation){
		
		this.geoinformation = geoinformation;
		this.transformation = transformation;
		
	}
	
	public abstract void run(final Scenario scenario, final Configuration configuration);
	
}
