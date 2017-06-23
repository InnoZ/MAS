package com.innoz.toolbox.run.callibration;

import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.TransportMode;

public class ASCModalSplitCallibration {
	
	private static Map<String, Double> modalSplitGoal;
	private static Map<String, Double> modalSplitLatest;
	
	public ASCModalSplitCallibration(Map<String, Double> modalSplit){
		
		ASCModalSplitCallibration.modalSplitGoal = modalSplit;
		
	}
	
	public double calculateModeConstant(String mode, Double cModeOld){
		
		double cModeNew;;
		
//		try {
			cModeNew = cModeOld - Math.log(
					ASCModalSplitCallibration.modalSplitLatest.get(mode) 
					/ ASCModalSplitCallibration.modalSplitGoal.get(mode));
//		} catch (Exception e){
//			cModeNew = cModeOld;
//		}
		
		return cModeNew;
	}
	
	public double calculateDelta(){
		double delta = 0;
		
		for(Entry<String, Double> e : modalSplitGoal.entrySet()){
			delta += Math.abs(e.getValue() - ASCModalSplitCallibration.modalSplitLatest.get(e.getKey()));
		}
		
		return delta;
	}
	
	public static void updateModalSplit(Map<String, Double> modalSplit){
		ASCModalSplitCallibration.modalSplitLatest = modalSplit;
	}

	public static Map<String, Double> getModalSplitGoal() {
		return modalSplitGoal;
	}
	
	

}
