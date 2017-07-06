package com.innoz.toolbox.matsim.examples;

import java.util.Random;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.modeAvailability.ModeAvailabilityConfigGroup;
import com.innoz.toolbox.matsim.modeAvailability.ModeAvailabilityModule;

public class AvailableModesExample {

	public static void main(String args[]) {
		
		Config config = ConfigUtils.loadConfig("/home/dhosse/scenarios/intermodalExample/config.xml",
				new ModeAvailabilityConfigGroup());
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		
		ModeAvailabilityConfigGroup macg = (ModeAvailabilityConfigGroup) config.getModules().get(ModeAvailabilityConfigGroup.GROUP_NAME);
		macg.setRestrictedModes("car,bike");
		
		Scenario scenario = ScenarioUtils.loadScenario(config);

		final Random random = MatsimRandom.getRandom();
		scenario.getPopulation().getPersons().values().stream().forEach(p -> {
			if(random.nextDouble() <= 0.6) {
				p.getCustomAttributes().put("bikeAvail", true);
			}
		});
		
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new ModeAvailabilityModule());
		controler.run();
		
	}
	
}