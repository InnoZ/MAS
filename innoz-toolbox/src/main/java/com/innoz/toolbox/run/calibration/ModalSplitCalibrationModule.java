package com.innoz.toolbox.run.calibration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.utils.analysis.LegModeDistanceDistribution;

public class ModalSplitCalibrationModule {

	public static void main(String args[]) {
		
		// java -cp bla.jar <config-path> <percentage>

		Config config = ConfigUtils.loadConfig(args[0]);
		
		// Run sample with factor defined by args[1]
		run(config, Double.parseDouble(args[1]));
		// Run full sample
		run(config, 1);
		
	}
	
	public static void run(final Config config, double sampleFactor) {
		
		modifyConfig(config, sampleFactor);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		int numberOfRuns = 1;
		
		if(sampleFactor < 1) {
			
			numberOfRuns = 5;
			
			// filter agents by attribute / read sample plans file
			System.out.println("Sample run!");
			final Random random = MatsimRandom.getRandom();
			scenario.getPopulation().getPersons().values().removeIf(p -> random.nextDouble() > sampleFactor);
			
		}
		
		///////////////////////////////////////////////////////////
		Controler controler = new Controler(scenario);
		
		LegModeDistanceDistribution lmdd = new LegModeDistanceDistribution();
		lmdd.init(scenario);
		lmdd.preProcessData();
		lmdd.postProcessData();
		ASCModalSplitCallibration asc = new ASCModalSplitCallibration(lmdd.getMode2Share());
		
		controler.addControlerListener(new RememberModeStats());

		double delta = Double.POSITIVE_INFINITY;
			
		for(int i = 0 ; i < numberOfRuns; i++) {
			
			System.out.println("Iteration " + i);
			
			controler.run();
			
			delta = asc.calculateDelta();
			if(delta <= 0.1) break;
			
			Map<String, Double> constants = asc.calculateModeConstants(config);
			
			for (Entry<String, Double> e : constants.entrySet()){

				if(!e.getKey().equals(TransportMode.walk) ) {

					ModeParams params = config.planCalcScore().getOrCreateModeParams(e.getKey());
					// keep one mode constant at 0 and adjust the others according to the constant
					params.addParam("constant", String.valueOf(e.getValue()));
					config.planCalcScore().addModeParams(params);
					
				}

			}
		
		}
		
	}
	
	private static void modifyConfig(final Config config, double sampleFactor) {
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.qsim().setFlowCapFactor(sampleFactor);
		config.qsim().setStorageCapFactor(sampleFactor);
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setWeight(0.2);
			config.strategy().addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
	}
	
}