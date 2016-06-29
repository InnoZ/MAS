package innoz.scenarioGeneration.population.mobilityAttitude;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ScoringParameterSet;

import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.scenarioGeneration.utils.Modes;

public class MobilityAttitudeGroups {

	/*
	 * Traditionelle Auto-Affine
	 * Flexible Auto-Affine
	 * Urban-orientierte ÖV-Affine
	 * Konventionelle Fahrrad-Affine
	 * Umweltbewusste ÖV- und Rad-Affine
	 * Innovative technikaffine Multioptionale
	 */
	public static void addScoringParameterSets(final Config config){

		String[] modes = new String[]{TransportMode.bike, TransportMode.car,
				TransportMode.other, TransportMode.pt, TransportMode.ride,
				TransportMode.walk, Modes.OW, Modes.TW, Modes.FF};
		
		String[] subpops = new String[]{null, "tradCar", "flexCar", "urbanPt",
				"convBike", "envtPtBike", "multiOpt"};
		
		for(String subpop : subpops){
			
			ScoringParameterSet set = config.planCalcScore().getOrCreateScoringParameters(subpop);
			
			ActivityParams education = new ActivityParams(ActivityTypes.EDUCATION);
			education.setTypicalDuration(6 * 3600);
			config.planCalcScore().addActivityParams(education);
			
			ActivityParams home = new ActivityParams(ActivityTypes.HOME);
			home.setTypicalDuration(12 * 3600);
			config.planCalcScore().addActivityParams(home);
			
			ActivityParams leisure = new ActivityParams(ActivityTypes.LEISURE);
			leisure.setTypicalDuration(4 * 3600);
			config.planCalcScore().addActivityParams(leisure);
			
			ActivityParams other = new ActivityParams(ActivityTypes.OTHER);
			other.setTypicalDuration(2 * 3600);
			config.planCalcScore().addActivityParams(other);
			
			ActivityParams shopping = new ActivityParams(ActivityTypes.SHOPPING);
			shopping.setTypicalDuration(1 * 3600);
			config.planCalcScore().addActivityParams(shopping);
			
			ActivityParams work = new ActivityParams(ActivityTypes.WORK);
			work.setTypicalDuration(8 * 3600);
			config.planCalcScore().addActivityParams(work);
			
			ActivityParams kindergarten = new ActivityParams(ActivityTypes.KINDERGARTEN);
			kindergarten.setTypicalDuration(3 * 3600);
			config.planCalcScore().addActivityParams(kindergarten);
			
			for(String mode : modes){
				
				ModeParams params = new ModeParams(mode);
				params.setConstant(0.0);
				params.setMarginalUtilityOfDistance(-0.0);
				params.setMarginalUtilityOfTraveling(-6.0);
				params.setMonetaryDistanceRate(0.0);
				set.addModeParams(params);
				
			}
			
		}
		
	}
	
	public static String assignPersonToGroup(Person person){
		return null;
	}
	
}