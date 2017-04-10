package com.innoz.toolbox.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParameterSet;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParams;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeScoringFunctionFactory;

/**
 * 
 * Entry point for a minimal execution of the MATSim controler. To execute it, just run the main method.
 * No additional settings are made aside from the settings in the config file given as the only argument.
 * 
 * @author dhosse
 *
 */
public class RunMatsim {

	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig(args[0]);
		
		//
		MobilityAttitudeConfigGroup ma = new MobilityAttitudeConfigGroup();
		ma.setSubpopulationAttribute("mobilityAttitude");
		ma.setScaleFactor(1d);
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("convBike");
			MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
			params.setMode(TransportMode.car);
			params.setOffset(-1.0);
			pars.addModeParams(params);
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup(null);
			ma.addModeParams(pars);
		}
		
		config.addModule(ma);
		
		
		
//		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
//		
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(-1);
//			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta.name());
//			stratSets.setSubpopulation("convBike");
//			stratSets.setWeight(1.0);
//			config.strategy().addStrategySettings(stratSets);
//		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(1.0);
			config.strategy().addStrategySettings(stratSets);
		}
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(-1);
//			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
//			stratSets.setSubpopulation(null);
//			stratSets.setWeight(0.1);
//			config.strategy().addStrategySettings(stratSets);
//		}
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(-1);
//			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
//			stratSets.setSubpopulation("convBike");
//			stratSets.setWeight(0.2);
//			config.strategy().addStrategySettings(stratSets);
//		}
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.2);
			config.strategy().addStrategySettings(stratSets);
		}
//		{
//			StrategySettings stratSets = new StrategySettings();
//			stratSets.setDisableAfter(-1);
//			stratSets.setStrategyName(DefaultStrategy.TimeAllocationMutator_ReRoute.name());
//			stratSets.setSubpopulation(null);
//			stratSets.setWeight(0.1);
//			config.strategy().addStrategySettings(stratSets);
//		}
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("/home/dhosse/scenarios/test/output/");
		config.controler().setLastIteration(0);
		config.qsim().setEndTime(30*3600);

		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		
		controler.addOverridingModule(new AbstractModule() {
			
			@Override
			public void install() {
				bindScoringFunctionFactory().to(MobilityAttitudeScoringFunctionFactory.class);
			}
		});
		controler.run();
		
	}
	
}