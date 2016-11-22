package com.innoz.energy.example;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.energy.config.EnergyConsumptionConfigGroup;
import com.innoz.energy.control.EnergyConsumptionModule;

public class RunExample {

	//TODO create example scenario in resources folder
	public static void main(String args[]){
		
		Config config = ConfigUtils.loadConfig("/home/dhosse/scenarios/berlin/config.xml.gz");
		EnergyConsumptionConfigGroup aec = new EnergyConsumptionConfigGroup();
		aec.addActivityEneryConsumptionParams("home", 1.0);
		aec.addActivityEneryConsumptionParams("work", 2.0);
		aec.addActivityEneryConsumptionParams("other", 0.5);
		aec.addActivityEneryConsumptionParams("education", 0.75);
		aec.addActivityEneryConsumptionParams("leisure", 4.0);
		aec.addLegEnergyConsumptionParams(TransportMode.car, 0.04);
		config.addModule(aec);
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.overwriteExistingFiles);
		config.controler().setLastIteration(0);
		config.controler().setOutputDirectory("/home/dhosse/scenarios/berlin/output/");
		
		config.qsim().setEndTime(30*3600);
		
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new EnergyConsumptionModule());
		controler.run();
		
	}
	
}