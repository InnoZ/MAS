package com.innoz.toolbox.scenarioGeneration.config;

import org.matsim.api.core.v01.TransportMode;
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
 * {@link com.innoz.toolbox.Controller.scenario().getConfig().Configuration}. The most important simulation parameters are set
 * to default values that should work for the start of a model calibration.
 * 
 * @author dhosse
 *
 */
public class InitialConfigCreator {

	// No instance!
	private InitialConfigCreator(){};
	
	public static void adapt() {
		
		// Network config group
		Controller.scenario().getConfig().network().setInputFile(Controller.configuration().misc().getOutputDirectory() + "network.xml.gz");
		
		// Plans config group
		Controller.scenario().getConfig().plans().setInputFile(Controller.configuration().misc().getOutputDirectory() + "plans.xml.gz");
		
		Controller.scenario().getConfig().plans().setInputPersonAttributeFile(Controller.configuration().misc().getOutputDirectory() + "personAttributes.xml.gz");
		
		Controller.scenario().getConfig().strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		// Set the last iteration to 100 (may be increased when enabling more replanning strategies or results show it is not enough)
		Controller.scenario().getConfig().controler().setLastIteration(100);
		
		// If households are used, adapt the parameters that define the usage in MATSim
		if(Controller.configuration().surveyPopulation().isUsingHouseholds()){
			Controller.scenario().getConfig().households().setInputFile(Controller.configuration().misc().getOutputDirectory() + "households.xml.gz");
		}
		
		Controller.scenario().getConfig().controler().setOutputDirectory(Controller.configuration().misc().getOutputDirectory() + "/output/");
			
		// Add activity types to the scoring parameters
		addBasicActivityParams();
		
		// Add scoring parameters for modes
		addModeScoringParams();
		
		// Add mode parameters
		addBasicModeRoutingParams();
		
		// QSim config group
		Controller.scenario().getConfig().qsim().setFlowCapFactor(Controller.configuration().scenario().getScaleFactor());
		Controller.scenario().getConfig().qsim().setStorageCapFactor(Controller.configuration().scenario().getScaleFactor());

		if(Controller.configuration().surveyPopulation().getVehicleType().equals(SurveyVehicleType.SURVEY)){
			
			Controller.scenario().getConfig().qsim().setVehiclesSource(VehiclesSource.fromVehiclesData);
			Controller.scenario().getConfig().vehicles().setVehiclesFile(Controller.configuration().misc().getOutputDirectory() + "vehicles.xml.gz");
			
		}
		
	}

	/**
	 * 
	 * Creates and adds default activity parameters for commonly used main activity types.
	 * 
	 * @param config
	 */
	private static void addBasicActivityParams(){

		ActivityParams education = new ActivityParams(ActivityTypes.EDUCATION);
		education.setTypicalDuration(6 * 3600);
		education.setOpeningTime(8 * 3600);
		education.setClosingTime(20 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(education);
		
		ActivityParams home = new ActivityParams(ActivityTypes.HOME);
		home.setTypicalDuration(12 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(home);
		
		ActivityParams leisure = new ActivityParams(ActivityTypes.LEISURE);
		leisure.setTypicalDuration(4 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(leisure);
		
		ActivityParams other = new ActivityParams(ActivityTypes.OTHER);
		other.setTypicalDuration(2 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(other);
		
		ActivityParams shopping = new ActivityParams(ActivityTypes.SHOPPING);
		shopping.setTypicalDuration(1 * 3600);
		shopping.setOpeningTime(8 * 3600);
		shopping.setClosingTime(20 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(shopping);
		
		ActivityParams work = new ActivityParams(ActivityTypes.WORK);
		work.setTypicalDuration(8 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(work);
		
		ActivityParams kindergarten = new ActivityParams(ActivityTypes.KINDERGARTEN);
		kindergarten.setTypicalDuration(3 * 3600);
		kindergarten.setOpeningTime(7 * 3600);
		kindergarten.setClosingTime(16 * 3600);
		Controller.scenario().getConfig().planCalcScore().addActivityParams(kindergarten);
		
	}

	/**
	 * 
	 * Creates and adds default mode routing parameters for the main modes.
	 * 
	 * @param config
	 */
	private static void addBasicModeRoutingParams(){
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.bike);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(15/3.6);
			Controller.scenario().getConfig().plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.other);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(30/3.6);
			Controller.scenario().getConfig().plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.pt);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeFreespeedFactor(2.0);
			Controller.scenario().getConfig().plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.ride);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(30/3.6);
			Controller.scenario().getConfig().plansCalcRoute().addModeRoutingParams(pars);
		}
		
		{
			ModeRoutingParams pars = new ModeRoutingParams(TransportMode.walk);
			pars.setBeelineDistanceFactor(1.3);
			pars.setTeleportedModeSpeed(4/3.6);
			Controller.scenario().getConfig().plansCalcRoute().addModeRoutingParams(pars);
		}
		
	}
	
	/**
	 * 
	 * Set mode scoring parameters to default values (as starting point for calibration).
	 * 
	 * @param config
	 */
	private static void addModeScoringParams() {
		
		String[] modes = new String[]{TransportMode.bike, TransportMode.car, TransportMode.other, TransportMode.pt, TransportMode.ride,
				TransportMode.walk};
		
		PlanCalcScoreConfigGroup planCalcScore = Controller.scenario().getConfig().planCalcScore();
		
		for(String mode : modes){
			
			ModeParams params = planCalcScore.getOrCreateModeParams(mode);
			params.setConstant(0.0);
			params.setMarginalUtilityOfDistance(0.0);
			params.setMarginalUtilityOfTraveling(0.0);
			params.setMonetaryDistanceRate(0.0);
			
		}
		
	}

}