package playground.dhosse.scenarios.generic.utils;

import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;

public class RunUtils {

	public static void addBasicStrategySettings(StrategyConfigGroup strategy, String[] subpopulations) {
		
		for(String subpopulation : subpopulations){
			
			StrategySettings expBeta = new StrategySettings();
			expBeta.setStrategyName("ChangeExpBeta");
			expBeta.setDisableAfter(-1);
			expBeta.setWeight(0.8);
			expBeta.setSubpopulation(subpopulation);
			strategy.addStrategySettings(expBeta);
			
			StrategySettings reroute = new StrategySettings();
			reroute.setStrategyName("ReRoute");
			reroute.setWeight(0.2);
			reroute.setSubpopulation(subpopulation);
			strategy.addStrategySettings(reroute);
			
		}
		
		strategy.setFractionOfIterationsToDisableInnovation(0.8);
		
	}
	
	public static void addSubtourModeChoiceSettings(StrategyConfigGroup strategy, double weight){
		
		StrategySettings smc = new StrategySettings();
		smc.setStrategyName("SubtourModeChoice");
		smc.setWeight(weight);
		strategy.addStrategySettings(smc);
		
	}
	
}
