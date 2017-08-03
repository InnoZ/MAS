package com.innoz.toolbox.scenarioGeneration.config;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;

import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyVehicleType;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

/**
 * 
 * Creates a MATSim config file. The input file paths are set to the output directory set in the 
 * {@link com.innoz.toolbox.config.Configuration}. The most important simulation parameters are set
 * to default values that should work for the start of a model calibration.
 * 
 * @author dhosse
 *
 */
public class InitialConfigCreator {

	// No instance!
	private InitialConfigCreator(){};
	
	public static void adapt(final Config config) {
		
		// Network config group
		config.network().setInputFile(Controller.configuration().misc().getOutputDirectory() + "network.xml.gz");
		
		// Plans config group
		config.plans().setInputFile(Controller.configuration().misc().getOutputDirectory() + "plans.xml.gz");
		
		config.plans().setInputPersonAttributeFile(Controller.configuration().misc().getOutputDirectory() + "personAttributes.xml.gz");
		
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
	
		// If households are used, adapt the parameters that define the usage in MATSim
		config.households().setInputFile(Controller.configuration().misc().getOutputDirectory() + "households.xml.gz");
			
		// Add activity types to the scoring parameters
		addBasicActivityParams(config);
		
		// Add scoring parameters for modes
		addModeScoringParams(config);
		
		// Add mode parameters
		addBasicModeRoutingParams(config);
		
		// QSim config group
		config.qsim().setFlowCapFactor(Controller.configuration().scenario().getScaleFactor());
		config.qsim().setStorageCapFactor(Controller.configuration().scenario().getScaleFactor());

		if(Controller.configuration().surveyPopulation().getVehicleType().equals(SurveyVehicleType.SURVEY)){
			
			config.qsim().setVehiclesSource(VehiclesSource.fromVehiclesData);
			config.vehicles().setVehiclesFile(Controller.configuration().misc().getOutputDirectory() + "vehicles.xml.gz");
			
		}
		
	}

	/**
	 * 
	 * Creates and adds default activity parameters for commonly used main activity types.
	 * 
	 * @param config
	 */
	private static void addBasicActivityParams(Config config){

		ActivityParams education = new ActivityParams(ActivityTypes.EDUCATION);
		education.setTypicalDuration(6 * 3600);
		education.setOpeningTime(8 * 3600);
		education.setClosingTime(20 * 3600);
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
		shopping.setOpeningTime(8 * 3600);
		shopping.setClosingTime(20 * 3600);
		config.planCalcScore().addActivityParams(shopping);
		
		ActivityParams work = new ActivityParams(ActivityTypes.WORK);
		work.setTypicalDuration(8 * 3600);
		config.planCalcScore().addActivityParams(work);
		
		ActivityParams kindergarten = new ActivityParams(ActivityTypes.KINDERGARTEN);
		kindergarten.setTypicalDuration(3 * 3600);
		kindergarten.setOpeningTime(7 * 3600);
		kindergarten.setClosingTime(16 * 3600);
		config.planCalcScore().addActivityParams(kindergarten);
		
	}

	/**
	 * 
	 * Creates and adds default mode routing parameters for the main modes.
	 * 
	 * @param config
	 */
	private static void addBasicModeRoutingParams(Config config){
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.bike);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(15/3.6);
			config.plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.other);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(30/3.6);
			config.plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.pt);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeFreespeedFactor(2.0);
			config.plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.ride);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(30/3.6);
			config.plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.walk);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(4/3.6);
			config.plansCalcRoute().addModeRoutingParams(pars);
		}
		
	}
	
	/**
	 * 
	 * Set mode scoring parameters to default values (as starting point for calibration).
	 * 
	 * @param config
	 */
	private static void addModeScoringParams(Config config) {
		
		String[] modes = new String[]{TransportMode.bike, TransportMode.car, TransportMode.other, TransportMode.pt, TransportMode.ride,
				TransportMode.walk};
		
		PlanCalcScoreConfigGroup planCalcScore = config.planCalcScore();
		
		for(String mode : modes){
			
			ModeParams params = planCalcScore.getOrCreateModeParams(mode);
			params.setConstant(0.0);
			params.setMarginalUtilityOfDistance(0.0);
			params.setMarginalUtilityOfTraveling(0.0);
			params.setMonetaryDistanceRate(0.0);
			
		}
		
	}

}