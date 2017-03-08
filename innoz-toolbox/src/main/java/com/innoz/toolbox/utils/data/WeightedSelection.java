package com.innoz.toolbox.utils.data;

import java.util.List;
import java.util.stream.Collectors;

import com.innoz.toolbox.scenarioGeneration.utils.Weighted;

public class WeightedSelection {

	/**
	 *
	 * Weighted random choice method. The objects in the list must implement the {@link Weighted} interface in order to make this work.
	 * According to the randomly chosen double, an object from the list is taken and returned.
	 * 
	 * @param list List of weighted objects.
	 * @param randomNumber
	 * @return
	 */
	public static Object choose(List<? extends Weighted> list, double randomNumber) {
		
		double totalWeight = getTotalWeight(list);
		double indexWeight = randomNumber * totalWeight;
		double sum = 0d;
		int index = 0;
		
		while(sum < indexWeight) {
			
			sum += list.get(index++).getWeight();
			
		}
		
		return list.get(index - 1);
		
	}
	
	private static double getTotalWeight(List<? extends Weighted> list) {
		
		return list.stream().map(p -> p.getWeight()).collect(Collectors.summarizingDouble(Double::doubleValue)).getSum();
		
	}
	
}