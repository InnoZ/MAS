package Mathfunctions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;

import countTraffic.Counts;

public class Calculator {
	
	private double scalingfactor = 10;
	
	/**
	 * scale from person to agent
	 * @param d number of persons 
	 * @return number of agents
	 */
	public double scale(double d){
		return d/this.scalingfactor;
	}
	/**
	 * scale from agents to persons
	 * @param d number of agents 
	 * @return number of persons
	 */
	public int scaleReverse(double d){
		return (int) (d*this.scalingfactor);

	}
	
	/**
	 * @param a
	 * @param absoluteDifference
	 * @return n2 as % of n1
	 */
	private double percentage(double a, double absoluteDifference) {
		return Math.abs(roundTo2DecimalPlaces((100.0 / a) * absoluteDifference));
	}

	/**
	 * @param a
	 * @param b
	 * @return (a-b) as percentage of a
	 */
	public double relativeDifference(double a, double b){
		double absoluteDifference = a - b;
		return percentage(a, absoluteDifference);	
	}
	
	private double roundTo2DecimalPlaces(double inputNumber){
		return Math.round(inputNumber*100)/100.0;
	}
	
	/**
	 * @param inputvalues
	 * @return arithmetic mean of all given inputvalues
	 */
	public double arithmeticMean(ArrayList<Double> inputvalues){
		double sum = 0;
		for (Double input : inputvalues) {
			sum += input;
		}
		double mean = sum/(inputvalues.size());
		return roundTo2DecimalPlaces(mean);
	}
	
	public double arithmeticMeanInteger(ArrayList<Integer> inputvalues) {
		int sum = 0;
		for (Integer input : inputvalues) {
			sum += input;
		}
		double mean = (double)sum/(double)(inputvalues.size());
		return roundTo2DecimalPlaces(mean);
	}
	/**
	 * @param inputvalues
	 * @param arithmeticMean
	 * @return variance of given inputvalues
	 */
	public double variance(ArrayList<Double> inputvalues, double arithmeticMean){
		double quadSum = 0;
		for (Double input : inputvalues) {
			quadSum += Math.pow( (input - arithmeticMean), 2);
		}
		double variance = quadSum/(inputvalues.size());
		return roundTo2DecimalPlaces(variance);
	}
	/**
	 * @param variance
	 * @return standard deviation of given variance
	 */
	public double standardDeviaion(double variance){
		double deviation = Math.sqrt(variance);
		return roundTo2DecimalPlaces(deviation);	
	}
	
	public int sumOverIntegerMapValues(Map<Id, Integer> volume){
		int currentValue;
		int sum = 0;
		for (Entry<Id, Integer> countEntry : volume.entrySet()) {
			currentValue = countEntry.getValue();
			sum += currentValue;
		}
		return sum;		
	}
	
	public int sumOverCountsMapValues(Map<Id, Counts> volume){
		Counts currentValue;
		int sum = 0;
		for (Entry<Id, Counts> countEntry : volume.entrySet()) {
			currentValue = countEntry.getValue();
			sum += currentValue.getTotal();
		}
		return sum;		
	}
}
