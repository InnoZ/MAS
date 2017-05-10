package com.innoz.scenarios.osnabrück;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
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

public class OsMain {

	static String filebase = "/home/dhosse/scenarios/mobilityAttitude/osnabrueck/";
	
	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig(filebase + "config.xml.gz");

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.controler().setLastIteration(10);
		
		addStrategySettings(config);
		addMobilityAttitudeConfigGroup(config);
		
		new ConfigWriter(config).write(filebase + "configWithMA.xml");
		
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
	
	private static void addStrategySettings(Config config) {
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
		}
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.2);
			config.strategy().addStrategySettings(stratSets);
		}
		
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setDisableAfter(-1);
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setSubpopulation(null);
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
		
	}
	
	private static void addMobilityAttitudeConfigGroup(Config config) {
		
		MobilityAttitudeConfigGroup ma = new MobilityAttitudeConfigGroup();
		ma.setSubpopulationAttribute("mobilityAttitude");
		ma.setScaleFactor(1d);
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("tradCar");
			
			double betaCar = 0.5509808129;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-0.6728124661 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-1.3728579165 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("flexCar");
			
			double betaCar = 0.6380481272;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.1034697375 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-0.2605919306 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("urbanPt");
		
			double betaCar = -0.7808252647;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-1.6880941473 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.700257666 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("convBike");
			
			double betaCar = -0.582954545;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.5985309311 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.3420721502 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("envtPtBike");
			
			double betaCar = -0.5953667551;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.4392845925 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.5825576487 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("multiOpt");
			
			double betaCar = 0.3446035305;
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.0);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.3974286492 - betaCar);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.2730661675 - betaCar);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup(null);
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("none");
			ma.addModeParams(pars);
		}
		
		config.addModule(ma);
		
	}
	
}