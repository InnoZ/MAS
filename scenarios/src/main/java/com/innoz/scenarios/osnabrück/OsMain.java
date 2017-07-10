package com.innoz.scenarios.osnabrück;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.run.callibration.ASCModalSplitCallibration;
import com.innoz.toolbox.run.callibration.RememberModeStats;

public class OsMain {

	public static void main(String args[]){
		
//		INPUT configurationcd
		Config config = ConfigUtils.loadConfig("/home/bmoehring/scenarios/osnabrueck/03404_2017/config.xml.gz");
//		INPUT MODAL SPLIT HERE:
		Map<String, Double> modalSplitGoal = new HashMap<String, Double>();
		modalSplitGoal.put(TransportMode.bike, 	0.12);
		modalSplitGoal.put(TransportMode.car, 	0.55);
		modalSplitGoal.put(TransportMode.pt, 	0.07);
//		modalSplitGoal.put(TransportMode.ride, 	0.13);
		modalSplitGoal.put(TransportMode.walk, 	0.24);
//		modalSplitGoal.put(TransportMode.other, 0.0);
//		Transport.Mode to fix and keep with constant=0 HERE:
		String holdMode = TransportMode.car;
		
//				,
//				new CarsharingConfigGroup(), new OneWayCarsharingConfigGroup(), new TwoWayCarsharingConfigGroup(), new FreeFloatingConfigGroup());
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(false);
		
		config.vspExperimental().setWritingOutputEvents(true);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(50);
		
//		config.qsim().setMainModes(Arrays.asList(TransportMode.car, "freefloating_vehicle",
//				"twoway_vehicle", "oneway_vehicle"));
		
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
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setStrategyName(DefaultStrategy.ChangeTripMode.name());
//			stratSets.setWeight(0.1);
//			strategy.addStrategySettings(stratSets);
//		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setWeight(0.1);
			strategy.addStrategySettings(stratSets);
		}

//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setStrategyName("CarsharingSubtourModeChoiceStrategy");
//			stratSets.setWeight(0.05);
//			strategy.addStrategySettings(stratSets);
//		}
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setStrategyName("RandomTripToCarsharingStrategy");
//			stratSets.setWeight(0.05);
//			strategy.addStrategySettings(stratSets);
//		}
		
		PlanCalcScoreConfigGroup planCalcScore = config.planCalcScore();
//		{
//			ModeParams params = new ModeParams(TransportMode.ride);
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("access_walk_tw");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("access_walk_ff");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("egress_walk_tw");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("egress_walk_ff");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("twoway_vehicle");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
//		{
//			ModeParams params = new ModeParams("freefloating_vehicle");
//			params.setConstant(0d);
//			params.setMarginalUtilityOfTraveling(0d);
//			planCalcScore.addModeParams(params);
//		}
		
//		config.plansCalcRoute().setInsertingAccessEgressWalk(true);
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Controler controler = new Controler(scenario);
		
		controler.addControlerListener(new RememberModeStats());

//		RunCarsharing.installCarSharing(controler);

		ASCModalSplitCallibration asc = new ASCModalSplitCallibration(modalSplitGoal);
		double delta = Double.POSITIVE_INFINITY;
		int run = 1;
		Map<Integer, Double> deltas = new HashMap<Integer, Double>();
			
		while (delta > 0.01 && run <= 100) {
			
			config.controler().setOutputDirectory("/home/bmoehring/scenarios/osnabrueck/03404_2017/output" + run);
			
			controler.run();
			
			Map<String, Double> constants = asc.calculateModeConstants(config);
			
			for (Entry<String, Double> e : constants.entrySet()){
				
				ModeParams params = config.planCalcScore().getOrCreateModeParams(e.getKey());
				// keep one mode constant at 0 and adjust the others according to the constant
				params.addParam("constant", String.valueOf(e.getValue()-constants.get(holdMode)));
				config.planCalcScore().addModeParams(params);
				
			}
			
			delta = asc.calculateDelta();
			deltas.put(run, delta);
			//have at least five runs to 'gain knowledge and experience'
			if(run <= 5) delta = 1;
			run++;
			
			System.out.println(run + " Delta: " + delta);
			System.out.println(config.planCalcScore().getModes());
		
		}
		
		for (Entry<Integer, Double> e : deltas.entrySet()){
			System.out.println(e.getValue());
			
		}
		
		
	}
	
}