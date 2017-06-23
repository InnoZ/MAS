package com.innoz.scenarios.osnabrück;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.carsharing.config.CarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.FreeFloatingConfigGroup;
import org.matsim.contrib.carsharing.config.OneWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.TwoWayCarsharingConfigGroup;
import org.matsim.contrib.carsharing.runExample.RunCarsharing;
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
		
		Config config = ConfigUtils.loadConfig("/home/bmoehring/scenarios/osnabrueck/03404_2017/config.xml.gz");
//				,
//				new CarsharingConfigGroup(), new OneWayCarsharingConfigGroup(), new TwoWayCarsharingConfigGroup(), new FreeFloatingConfigGroup());
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(false);
		
		config.vspExperimental().setWritingOutputEvents(true);
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(1);
		
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
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ChangeTripMode.name());
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
		{
			ModeParams params = new ModeParams(TransportMode.ride);
			params.setConstant(0d);
			params.setMarginalUtilityOfTraveling(0d);
			planCalcScore.addModeParams(params);
		}
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
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		config.plansCalcRoute().setInsertingAccessEgressWalk(true);

		Controler controler = new Controler(scenario);
		
//		RunCarsharing.installCarSharing(controler);
		
		controler.addControlerListener(new RememberModeStats());
		Map<String, Double> modalSplitGoal = new HashMap<String, Double>();
		modalSplitGoal.put(TransportMode.car, 	0.41);
		modalSplitGoal.put(TransportMode.pt, 	0.16);
		modalSplitGoal.put(TransportMode.walk, 	0.19);
		modalSplitGoal.put(TransportMode.bike, 	0.12);
		modalSplitGoal.put(TransportMode.other, 0.0);
		modalSplitGoal.put(TransportMode.ride, 	0.12);
		ASCModalSplitCallibration asc = new ASCModalSplitCallibration(modalSplitGoal);
		double delta = Double.POSITIVE_INFINITY;
			
		while (delta > 1) {
			
			controler.run();
			
			for (String mode : ASCModalSplitCallibration.getModalSplitGoal().keySet()){
				
				double c = config.planCalcScore().getOrCreateModeParams(mode).getConstant();
				ModeParams params = config.planCalcScore().getOrCreateModeParams(mode);
				params.setConstant(asc.calculateModeConstant(mode, c));
				config.planCalcScore().addModeParams(params);
				
			}
			
			delta = asc.calculateDelta();
		
		}
		
	}
	
}