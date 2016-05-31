package innoz.scenarioGeneration.config;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;

import innoz.config.Configuration;
import innoz.scenarioGeneration.utils.ActivityTypes;

public class InitialConfigCreator {
	
	public static Config create(final Configuration configuration){
		
		// Create a new MATSim configuration
		Config config = ConfigUtils.createConfig();

		// Network config group
		config.network().setInputFile(configuration.getOutputDirectory() + "network.xml.gz");
		
		// Plans config group
		config.plans().setInputFile(configuration.getOutputDirectory() + "plans.xml.gz");
		config.plans().setInputPersonAttributeFile(configuration.getOutputDirectory() + "personAttributes.xml.gz");
	
		// If households are used, adapt the parameters that define the usage in MATSim
		if(configuration.isUsingHouseholds()){
			
			config.scenario().setUseHouseholds(true);
			config.households().setInputFile(configuration.getOutputDirectory() + "households.xml.gz");
			
		}
		
		// Add activity types to the scoring parameters
		addBasicActivityParams(config);
		
		// QSim config group
		config.qsim().setFlowCapFactor(configuration.getScaleFactor());
		config.qsim().setStorageCapFactor(configuration.getScaleFactor());

		// If non-generic vehicles are used, adapt the parameters that define the usage in MATSim
		if(configuration.isUsingVehicles()){
			
			config.scenario().setUseVehicles(true);
			config.qsim().setUseDefaultVehicles(false);
			config.vehicles().setVehiclesFile(configuration.getOutputDirectory() + "vehicles.xml.gz");
			
		}
		
		return config;
		
	}
	
	private static void addBasicActivityParams(Config config){

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
		
	}

}
