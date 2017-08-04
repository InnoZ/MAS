package com.innoz.toolbox.run.calibration;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy.OverwriteFileSetting;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultSelector;
import org.matsim.core.replanning.strategies.DefaultPlanStrategiesModule.DefaultStrategy;
import org.matsim.core.scenario.ScenarioUtils;

import com.innoz.toolbox.utils.analysis.LegModeDistanceDistribution;

/**
 * 
 * The "outside loop" of the model calibration process. The inner loop consists of MATSim runs of the specific scenario,  the outer
 * loop checks the termination criteria of the calibration (convergence of modal split / route choice reached OR number of maximum
 * runs reached).
 * 
 * @author dhosse
 *
 */
public class ModalSplitCalibrationModule {

	public static void main(String args[]) {
		
		// Load the config from the location defined in the runtime arguments
		Config config = ConfigUtils.loadConfig(args[0]);
		
		// Run sample with factor defined by args[1]
		run(config, Double.parseDouble(args[1]));
		// Run full sample
		run(config, 1);
		
	}
	
	/**
	 * This methods first sets up the MATSim scenario. The set up includes a check if the run is defined to be a sample run (defined
	 * by the sample factor, any numerical value greater than 0 and less or equal than 1) and conditionally scales down the population
	 * size and network capacities. As a second step, the MATSim controller runs the simulation. Finally, the convergence of the
	 * calibration parameters (i.e. deviation of modal split and later route choice compared to real world measurements) is checked,
	 * i.e. if the error between the simulated and the real-world values is less than a defined tolerance. If so, the execution stops
	 * and the model is considered calibrated, otherwise the execution continues.
	 * 
	 * @param config The MATSim config that defines the scenario to be run by this method.
	 * @param sampleFactor The "scale factor" for traffic demand and supply. Must be a non-null numerical value (0,1]
	 */
	public static void run(final Config config, double sampleFactor) {
		
		// Modify the config with some necessary changes and load the scenario
		modifyConfig(config, sampleFactor);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		
		int numberOfRuns = 1;

		// Check if we want to run a sample or a full population
		if(sampleFactor < 1) {
			
			// Samples need to be calibrated in potentially many runs
			numberOfRuns = 5;
			
			// Filter agents to scale down the sample
			final Random random = MatsimRandom.getRandom();
			scenario.getPopulation().getPersons().values().removeIf(p -> random.nextDouble() > sampleFactor);
			
		}
		
		///////////////////////////////////////////////////////////
		
		Controler controler = new Controler(scenario);
		
		// Compute the initial modal split (goal) and pass it to the calibration class
		LegModeDistanceDistribution lmdd = new LegModeDistanceDistribution();
		lmdd.init(scenario);
		lmdd.preProcessData();
		lmdd.postProcessData();
		ASCModalSplitCallibration asc = new ASCModalSplitCallibration(lmdd.getMode2Share());
		
		// Add a controler listener that passes the modal split of the last simulation to the calibration class
		controler.addControlerListener(new RememberModeStats());

		// The error produced by the simulation
		double delta = Double.POSITIVE_INFINITY;
		
		// Outer loop of the simulation:
		// Run as long as the maximum number of runs isn't reached
		for(int i = 0 ; i < numberOfRuns; i++) {
			
			// The MATSim run
			controler.run();
			
			// Calculate the error with respect to the mode choice
			delta = asc.calculateDelta();
			// Second termination criterion: Error is inside tolerance
			if(delta <= 0.1) break;
			
			adaptModeConstants(config, asc.calculateModeConstants(config));
		
		}
		
	}

	/**
	 * 
	 * Sets up some basic config settings to make the outer loop calibration process work.
	 * 
	 * @param config The MATSim config that defines the scenario to be run by this method.
	 * @param sampleFactor The "scale factor" for traffic demand and supply. Must be a non-null numerical value (0,1] 
	 */
	private static void modifyConfig(final Config config, double sampleFactor) {
		
		config.controler().setOverwriteFileSetting(OverwriteFileSetting.deleteDirectoryIfExists);
		config.qsim().setFlowCapFactor(sampleFactor);
		config.qsim().setStorageCapFactor(sampleFactor);
		
		// Disable innovation after 80% of the simulation
		config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8);
		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
		
		// Replanning strategies
		// Choose existing plan
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultSelector.ChangeExpBeta);
			stratSets.setWeight(0.7);
			config.strategy().addStrategySettings(stratSets);
		}
		// Route choice
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.ReRoute.name());
			stratSets.setWeight(0.2);
			config.strategy().addStrategySettings(stratSets);
		}
		// Mode choice
		{
			StrategySettings stratSets = new StrategySettings();
			stratSets.setStrategyName(DefaultStrategy.SubtourModeChoice.name());
			stratSets.setWeight(0.1);
			config.strategy().addStrategySettings(stratSets);
		}
		
	}
	
	/**
	 * 
	 * Adapts the alternative specific constants (ASC) in the MATSim config for the next outer iteration.
	 * There is always one mode (most common: walk) that keeps its ASC to calibrate all other modes "relative" to the fixed one.
	 * 
	 * @param config The MATSim config.
	 * @param constants The new ASCs for the simulated modes. These parameters are written into the config.
	 */
	private static void adaptModeConstants(final Config config, Map<String, Double> constants) {
		
		// Pass the new mode constants to the MATSim config
		for (Entry<String, Double> e : constants.entrySet()){

			if(!e.getKey().equals(TransportMode.walk) ) {

				ModeParams params = config.planCalcScore().getOrCreateModeParams(e.getKey());
				// keep one mode constant at 0 and adjust the others according to the constant
				params.addParam("constant", String.valueOf(e.getValue()));
				config.planCalcScore().addModeParams(params);
				
			}

		}
		
	}
	
}