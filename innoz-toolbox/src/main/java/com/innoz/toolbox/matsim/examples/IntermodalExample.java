package com.innoz.toolbox.matsim.examples;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.av.intermodal.router.VariableAccessTransitRouterModule;
import org.matsim.contrib.av.intermodal.router.config.VariableAccessConfigGroup;
import org.matsim.contrib.av.intermodal.router.config.VariableAccessModeConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

public class IntermodalExample {

	public static void main(String args[]) {
		
		Config config = ConfigUtils.loadConfig("/home/dhosse/scenarios/intermodalExample/config.xml");
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		{
			ModeParams params = new ModeParams("pedelec");
			config.planCalcScore().addModeParams(params);
		}
		
		{
			ModeRoutingParams params = new ModeRoutingParams();
			params.setMode("pedelec");
			params.setTeleportedModeSpeed(25/3.6);
			params.setBeelineDistanceFactor(1.3);
			config.plansCalcRoute().addModeRoutingParams(params);
		}
		{
			ModeRoutingParams params = new ModeRoutingParams();
			params.setMode(TransportMode.walk);
			params.setTeleportedModeSpeed(4/3.6);
			params.setBeelineDistanceFactor(1.3);
			config.plansCalcRoute().addModeRoutingParams(params);
		}
		{
			ModeRoutingParams params = new ModeRoutingParams();
			params.setMode(TransportMode.bike);
			params.setTeleportedModeSpeed(12/3.6);
			params.setBeelineDistanceFactor(1.3);
			config.plansCalcRoute().addModeRoutingParams(params);
		}
		
		VariableAccessConfigGroup vacg = new VariableAccessConfigGroup();
		{
			VariableAccessModeConfigGroup vamcg = new VariableAccessModeConfigGroup();
			vamcg.setDistance(2000);
			vamcg.setTeleported(true);
			vamcg.setMode(TransportMode.bike);
			vacg.setAccessModeGroup(vamcg);
		}
		{
			VariableAccessModeConfigGroup vamcg = new VariableAccessModeConfigGroup();
			vamcg.setDistance(500);
			vamcg.setTeleported(true);
			vamcg.setMode(TransportMode.walk);
			vacg.setAccessModeGroup(vamcg);
		}
		{
			VariableAccessModeConfigGroup vamcg = new VariableAccessModeConfigGroup();
			vamcg.setDistance(5000);
			vamcg.setTeleported(true);
			vamcg.setMode("pedelec");
			vacg.setAccessModeGroup(vamcg);
		}
		
		config.addModule(vacg);
		
		
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8);
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new VariableAccessTransitRouterModule());
		controler.run();
		
	}
	
}