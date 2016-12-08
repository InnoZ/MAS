package com.innoz.toolbox.utils.matsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides arithmetic mean, standard deviation, and Min/Max.
 * 
 * @author aneumann
 *
 */
public final class RecursiveStatsContainer {

	private double numberOfEntries = Double.NaN;
	private double arithmeticMean;
	private double min = Double.POSITIVE_INFINITY;
	private double max = Double.NEGATIVE_INFINITY;
	private double tempVar;
	private List<Double> entries = new ArrayList<>();

	public void handleNewEntry(double entry){
		
		entries.add(entry);
		
		if (entry < this.min) {
			this.min = entry;
		}
		
		if (entry > this.max) {
			this.max = entry;
		}
		
		// new entries n + 1
		double meanEntry_n_1;
		double tempVarEntry_n_1;

		if(Double.isNaN(this.numberOfEntries)){
			// initialize
			this.numberOfEntries = 0;
			this.arithmeticMean = 0;
			this.tempVar = 0;
		}

		// calculate the exact mean and variance

		// calculate new mean
		meanEntry_n_1 =  (this.numberOfEntries * this.arithmeticMean + entry) / (this.numberOfEntries + 1);

		if (this.numberOfEntries == 0) {
			tempVarEntry_n_1 = 0;
		} else {
			tempVarEntry_n_1 = this.tempVar + (this.numberOfEntries + 1) / (this.numberOfEntries) * (meanEntry_n_1 - entry) * (meanEntry_n_1 - entry);
		}

		this.numberOfEntries++;

		// store em away
		this.arithmeticMean = meanEntry_n_1;
		this.tempVar = tempVarEntry_n_1;
	}

	public double getStdDev() {
		if (this.numberOfEntries > 1){
			return Math.sqrt(1.0/(this.numberOfEntries - 1.0) * this.tempVar);
		}			
		return Double.NaN;
	}

	public double getMean() {
		return this.arithmeticMean;
	}
	
	public int getNumberOfEntries(){
		return (int) this.numberOfEntries;
	}
	
	public double getMin(){
		return this.min;
	}
	
	public double getMax(){
		return this.max;
	}
	
	public double getMedian() {

		Collections.sort(this.entries);
		
		if(this.numberOfEntries % 2 == 0){
			
			return this.entries.get((int)(this.numberOfEntries) / 2);
			
		} else {
			
			return (this.entries.get((int)(this.numberOfEntries - 1) / 2)
					+ this.entries.get((int)this.numberOfEntries / 2)) / 2;
			
		}
		
	}
	
}
