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
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParameterSet;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup.MobilityAttitudeModeParams;
import com.innoz.toolbox.matsim.scoring.MobilityAttitudeScoringFunctionFactory;
import com.innoz.toolbox.scenarioGeneration.population.mobilityAttitude.MobilityAttitudeGroups;

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
		
		for (int ii = 0 ; ii < MobilityAttitudeGroups.subpops.length ; ii++)
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup(MobilityAttitudeGroups.subpops[ii]);
			MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
			params.setMode(TransportMode.car);
			params.setOffset(MobilityAttitudeGroups.getAttitude(MobilityAttitudeGroups.subpops[ii], TransportMode.car));
			pars.addModeParams(params);
			ma.addModeParams(pars);
			
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setSubpopulation(MobilityAttitudeGroups.subpops[ii]);
			stratSets.setWeight(1.0);
			config.strategy().addStrategySettings(stratSets);
		}
		
		config.addModule(ma);
		
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setOutputDirectory("/home/bmoehring/scenarios/osnabrueck/03404_2017/output/");
		config.controler().setLastIteration(10);
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