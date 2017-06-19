package com.innoz.scenarios.osnabrück;

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

public class OsMain {

	static String filebase = "/home/dhosse/scenarios/mobilityAttitude/osnabrueck/";
	
	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig(filebase + "config.xml.gz");

		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		addStrategySettings(config);
		addMobilityAttitudeConfigGroup(config);
		
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
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
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
		ma.setScaleFactor(10d);
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("tradCar");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.5509808129);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-0.6728124661);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-1.3728579165);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("flexCar");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.6380481272);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.1034697375);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(-0.2605919306);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("urbanPt");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.7808252647);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(-1.6880941473);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.700257666);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("convBike");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.582954545);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.5985309311);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.3420721502);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("envtPtBike");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(-0.5953667551);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.4392845925);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.5825576487);
				pars.addModeParams(params);
			}
			
			
			ma.addModeParams(pars);
		}
		
		{
			MobilityAttitudeModeParameterSet pars = new MobilityAttitudeModeParameterSet();
			pars.setAttitudeGroup("multiOpt");
			
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.car);
				params.setOffset(0.3446035305);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.bike);
				params.setOffset(0.3974286492);
				pars.addModeParams(params);
			}
			{
				MobilityAttitudeModeParams params = new MobilityAttitudeModeParams();
				params.setMode(TransportMode.pt);
				params.setOffset(0.2730661675);
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