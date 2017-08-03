package com.innoz.toolbox.run.calibration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;

public class ASCModalSplitCallibration {
	
	private static Map<String, Double> modalSplitGoal;
	private static Map<String, Double> modalSplitLatest;
	
	public ASCModalSplitCallibration(Map<String, Double> modalSplit){
		
		ASCModalSplitCallibration.modalSplitGoal = modalSplit;
		
	}
	
	public HashMap<String, Double> calculateModeConstants(Config config){
		
		HashMap<String, Double> constants = new HashMap<String, Double>();
		
		for (String mode : ASCModalSplitCallibration.getModalSplitGoal().keySet()){
			
			double cModeOld = config.planCalcScore().getOrCreateModeParams(mode).getConstant();
			double cModeNew;
			
			if(ASCModalSplitCallibration.modalSplitGoal.get(mode)==0.0){
				cModeNew = 0.0;
			} else {
			
				try {	
					cModeNew = cModeOld - Math.log(
							ASCModalSplitCallibration.modalSplitLatest.get(mode) 
							/ ASCModalSplitCallibration.modalSplitGoal.get(mode));
				} catch (Exception e){
					cModeNew = cModeOld;
				}
			}
			constants.put(mode, cModeNew);
		}
		
		return constants;
	}
	
	public double calculateDelta(){
		double delta = 0;
		
		for(Entry<String, Double> e : ASCModalSplitCallibration.modalSplitGoal.entrySet()){
			
			double d;
			try {
				
				d = Math.abs(e.getValue() - ASCModalSplitCallibration.modalSplitLatest.get(e.getKey()));
		
			} catch (NullPointerException exception) {
				
				d = Math.abs(e.getValue());
				
			}
			delta += d;
			
			System.out.println(delta + " " + e.getKey() + " " + e.getValue());
			
			
		}
		
		return delta;
	}
	
	public static void updateModalSplit(Map<String, Double> modalSplit){
		
		ASCModalSplitCallibration.modalSplitLatest = modalSplit;
		System.out.println(ASCModalSplitCallibration.modalSplitLatest);
		
	}

	public static Map<String, Double> getModalSplitGoal() {
		return modalSplitGoal;
	}
	
	

}
