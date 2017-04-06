package com.innoz.toolbox.run;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.matsim.scoring.MobilityAttitudeConfigGroup;
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
		ma.setSubpopulationAttribute("subpopulation");
		ma.setScaleFactor(1d);
		
		{
			MobilityAttitudeModeParams pars = new MobilityAttitudeModeParams();
			pars.setAttitudeGroup("test");
			pars.setOffsetForMode(TransportMode.car, 1.0);
			pars.setOffsetForMode(TransportMode.walk, 2);
			ma.getModeParams().put(pars.getAttitudeGroup(), pars);
		}
		{
			MobilityAttitudeModeParams pars = new MobilityAttitudeModeParams();
			pars.setAttitudeGroup(null);
			pars.setOffsetForMode(TransportMode.car, -1000.0);
			ma.getModeParams().put(pars.getAttitudeGroup(), pars);
		}
		
		config.addModule(ma);
		//
		
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