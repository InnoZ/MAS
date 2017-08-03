package com.innoz.scenarios.osnabrück;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.run.calibration.ASCModalSplitCallibration;
import com.innoz.toolbox.run.calibration.RememberModeStats;

public class OsMain {

	public static void main(String args[]){
		
//		INPUT configurationcd
		Config config = ConfigUtils.loadConfig("/home/bmoehring/scenarios/osnabrueck/03404_2017/config.xml.gz");
//		INPUT MODAL SPLIT HERE:
		Map<String, Double> modalSplitGoal = new HashMap<String, Double>();
		modalSplitGoal.put(TransportMode.bike, 	0.12);
		modalSplitGoal.put(TransportMode.car, 	0.55);
		modalSplitGoal.put(TransportMode.pt, 	0.07);
		modalSplitGoal.put(TransportMode.walk, 	0.24);
		String holdMode = TransportMode.car;
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(false);
		
		config.vspExperimental().setWritingOutputEvents(true);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(50);
		
		StrategyConfigGroup strategy = config.strategy();
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setWeight(0.7);
			strategy.addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setWeight(0.2);
			strategy.addStrategySettings(stratSets);
		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setWeight(0.1);
			strategy.addStrategySettings(stratSets);
		}

		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Controler controler = new Controler(scenario);
		
		controler.addControlerListener(new RememberModeStats());

		ASCModalSplitCallibration asc = new ASCModalSplitCallibration(modalSplitGoal);
		double delta = Double.POSITIVE_INFINITY;
			
		for(int i = 0 ; i < 10; i++) {
			
			System.out.println("Iteration " + i);
			
			controler.run();
			
			delta = asc.calculateDelta();
			if(delta < 0.1) break;
			
			Map<String, Double> constants = asc.calculateModeConstants(config);
			
			for (Entry<String, Double> e : constants.entrySet()){
				
				ModeParams params = config.planCalcScore().getOrCreateModeParams(e.getKey());
				// keep one mode constant at 0 and adjust the others according to the constant
				params.addParam("constant", String.valueOf(e.getValue()-constants.get(holdMode)));
				config.planCalcScore().addModeParams(params);
				
			}
		
		}
		
		
	}
	
}