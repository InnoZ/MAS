package com.innoz.toolbox.populationForecast.populationCalc;

import java.io.IOException;

public class RunPopulationForecast {

	public static void main(String[] args) throws IOException {

		int calcYear = 2014;
//		gkz isn't used so far
		int gkz = 1001;
		String filepath = "/home/bmoehring/workspace/SchwartzbachData/Excel/";
		
//		checks if calculation year is valid (only 2009 - 2040)
		if (calcYear < 2040 && calcYear > 2008){
			PopulationCalculator popCalc = new PopulationCalculator (calcYear, gkz, filepath);
			System.out.println("done");
		}
		else {
			System.out.println("invalid Calculation Year.");
		}
	}
}
