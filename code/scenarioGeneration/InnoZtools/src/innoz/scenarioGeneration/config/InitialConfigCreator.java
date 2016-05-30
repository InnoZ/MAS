package innoz.scenarioGeneration.config;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import innoz.config.Configuration;

public class InitialConfigCreator {
	
	public static Config create(final Configuration configuration){
		
		Config config = ConfigUtils.createConfig();

		config.network().setInputFile(configuration.getOutputDirectory() + "network.xml.gz");
		
		config.plans().setInputFile(configuration.getOutputDirectory() + "plans.xml.gz");
		
		config.qsim().setFlowCapFactor(configuration.getScaleFactor());
		
		return config;
		
	}

}
