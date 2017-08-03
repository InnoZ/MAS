package com.innoz.toolbox.run.calibration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.core.config.Config;

/**
 * 
 * A module for automatic calibration of modal split via alternative specific constants for transport modes.
 * The approach is to define a goal for the modal split and then iteratively calculate new ASCs for the modes via 
 * <code>c_i+1 = c_i - log(p_mode_i / p_mode_0)</code> until the delta between the goal and the last simulated modal split is
 * less than a defined minimum.
 * 
 * @author bsmoehring
 *
 */
public class ASCModalSplitCallibration {
	
	// MEMBERS //////////////////////////////////
	private static Map<String, Double> modalSplitGoal;
	private static Map<String, Double> modalSplitLatest;
	/////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param modalSplit The modal split that should eventually be approximated by the calibration. The map
	 * consists of key: mode string and value: percentage of modal split (with respect to legs) for the mode.
	 */
	public ASCModalSplitCallibration(Map<String, Double> modalSplit) {
		
		ASCModalSplitCallibration.modalSplitGoal = modalSplit;
		
	}
	
	/**
	 * 
	 * Calculates the new ASCs for all modes.
	 * 
	 * @param config The MATSim config that is used for this simulation.
	 * @return A map of key: mode string and value: double value for the mode constant (ASC)
	 */
	public HashMap<String, Double> calculateModeConstants(Config config) {
		
		// Initialize the constants map		
		HashMap<String, Double> constants = new HashMap<String, Double>();
		
		// Iterate over all modes that were simulated
		for (String mode : ASCModalSplitCallibration.getModalSplitGoal().keySet()){
			
			// Get the old ASC from the config
			double cModeOld = config.planCalcScore().getOrCreateModeParams(mode).getConstant();
			double cModeNew = 0.0;
			
			try {	
				
				// Calculate the new ASC
				cModeNew = cModeOld - Math.log(
						ASCModalSplitCallibration.modalSplitLatest.get(mode) 
						/ ASCModalSplitCallibration.modalSplitGoal.get(mode));
				
			} catch (Exception e) {
				
				// Possible exception: Mode wasn't used by any person in the last iteration
				// Use the old ASC in that case
				cModeNew = cModeOld;
				
			}
			
			constants.put(mode, cModeNew);
			
		}
		
		return constants;
	}
	
	/**
	 * 
	 * Calculates the error of the last simulated modal split compared to the calibration goal. 
	 * 
	 * @return A double value of the error.
	 */
	public double calculateDelta() {
		
		double delta = 0;
		
		// Iterate over all modes and compare the simulated values with the actual ones in the goal
		for(Entry<String, Double> e : ASCModalSplitCallibration.modalSplitGoal.entrySet()){
			
			double d;
			
			try {
				
				d = Math.abs(e.getValue() - ASCModalSplitCallibration.modalSplitLatest.get(e.getKey()));
		
			} catch (NullPointerException exception) {
				
				d = Math.abs(e.getValue());
				
			}

			// Add the calculated delta to the error sum
			delta += d;
			
		}
		
		return delta/ 100;
	}
	
	/**
	 * 
	 * Setter for the last simulated modal split map.
	 * 
	 */
	public static void updateModalSplit(Map<String, Double> modalSplit) {
		
		ASCModalSplitCallibration.modalSplitLatest = modalSplit;
		
	}

	/**
	 * 
	 * Getter for the modal split map.
	 * 
	 * @return The modal split that was produced by the last finished mobility simulation.
	 */
	public static Map<String, Double> getModalSplitGoal() {
		return modalSplitGoal;
	}
	
	

}
