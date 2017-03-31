package com.innoz.toolbox.scenarioGeneration.config;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.QSimConfigGroup.VehiclesSource;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.VehicleType;
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
	
	private InitialConfigCreator(){};
	
	public static Config create(final Configuration configuration){
		
		// Create a new MATSim configuration
		Config config = ConfigUtils.createConfig();

		// Network config group
		config.network().setInputFile(configuration.misc().getOutputDirectory() + "network.xml.gz");
		
		// Plans config group
		config.plans().setInputFile(configuration.misc().getOutputDirectory() + "plans.xml.gz");
		
		config.plans().setInputPersonAttributeFile(configuration.misc().getOutputDirectory() + "personAttributes.xml.gz");
		
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
	
		// If households are used, adapt the parameters that define the usage in MATSim
		config.households().setInputFile(configuration.misc().getOutputDirectory() + "households.xml.gz");
			
		// Add activity types to the scoring parameters
		addBasicActivityParams(config);
			
		// Add mode parameters
		addBasicModeParams(config);
		
		// QSim config group
		config.qsim().setFlowCapFactor(configuration.scenario().getScaleFactor());
		config.qsim().setStorageCapFactor(configuration.scenario().getScaleFactor());

		if(configuration.surveyPopulation().getVehicleType().equals(VehicleType.SURVEY)){
			
			config.qsim().setVehiclesSource(VehiclesSource.fromVehiclesData);
			config.vehicles().setVehiclesFile(configuration.misc().getOutputDirectory() + "vehicles.xml.gz");
			
		}
		
		return config;
		
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
	 * Creates and adds default mode parameters for the main modes.
	 * 
	 * @param config
	 */
	private static void addBasicModeParams(Config config){
		
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

}