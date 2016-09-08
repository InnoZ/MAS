package com.innoz.toolbox.populationForecast.populationCalc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PopulationCalculator {

	public PopulationCalculator(int calcYear, int gkz, String filepath) throws IOException {
//		String filename = filepath + "Z0-5_m.xls";
//		String filename = "BV-Vergleich ROP InnoZ.xlsx";
//		String filename = "120521_BBSR_BevInnoZ_100k.xls";
		
//		XLSXHandler xlsx = new XLSXHandler();
		
//		xlsx.readXLSXFile(filename);
//		xlsx.getXLSAdress(filename, gkz, calcYear);
//		System.out.println(xlsx.getRow(filename, gkz));
		
		
		ArrayList<String> ageGroupsArrayList = new ArrayList<String>();
		ageGroupsArrayList.add("0-5");
		ageGroupsArrayList.add("5-10");
		ageGroupsArrayList.add("10-18");
		ageGroupsArrayList.add("18-25");
		ageGroupsArrayList.add("25-35");
		ageGroupsArrayList.add("35-45");
		ageGroupsArrayList.add("45-55");
		ageGroupsArrayList.add("55-65");
		ageGroupsArrayList.add("65-75");
		ageGroupsArrayList.add("75-85");
		ageGroupsArrayList.add("85-101");
		
//		test
		System.out.println("Number of ageGroups:  " + ageGroupsArrayList.size());
		
//		need to add method to determine row due to gkz
		int gkzRow = 3;
		
//		creates filepath iterating over all ageGroups
//		for (int i = 0; i<ageGroupsArrayList.size(); i++) {
//		System.out.println("Z" + ageGroupsArrayList.get(i) + "_m  :  " + xlsx.getXLSValue(year, gkzRow, filepath + "Z" + ageGroupsArrayList.get(i) + "_m.xls"));
//		}
		



////		creates a HashMap for all male AgeGroups
//		HashMap ageGroupsM = new HashMap();
		
//		creates a map for each ageGroup inside a map for each year (so far only male)
		HashMap<Integer, HashMap<String, Integer>> populationMap = new HashMap<Integer, HashMap<String, Integer>>();
		for (int year = 2012; year < (calcYear + 1); year++){
			HashMap<String, Integer> ageGroupsM = new HashMap<String, Integer>();
			populationMap.put(year, ageGroupsM);
			for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
				populationMap.get(year).put(ageGroupsArrayList.get(ii), calcPopulation(populationMap, year, gkzRow, filepath, ageGroupsArrayList.get(ii)));
			}		
		};
		
		System.out.println(populationMap.values());
			
	}
	
//	method that decides which calculationmethod to use by year and ageGroup
	public int calcPopulation(HashMap<Integer, HashMap<String, Integer>> populationMap, int year, int gkzRow, String filepath, String ageGroup) throws IOException{
		XLSXHandler xlsx = new XLSXHandler();
		int pop = 0;
		if (year<2014) {
//			ageGroups 0-5 and 5-10 are adjusted to the change of ageGroup 0-10
			if (year == 2013 && (ageGroup == "0-5"|| ageGroup == "5-10")){
				pop = (int) Math.round(((double) populationMap.get(2012).get(ageGroup) / (double) xlsx.getXLSValue(2012, gkzRow, filepath, "0-10")) * (double) xlsx.getXLSValue(2013, gkzRow, filepath, "0-10"));
			}
//			all ageGroups > 10 years aren't calculated. Raw data is being used
			else {
				pop = xlsx.getXLSValue(year, gkzRow, filepath, ageGroup);
			}
		}
		
//		calculation for all ageGroups beginning in 2014	
		if (year == 2014) {
			pop = xlsx.getXLSCalcValues(year, gkzRow, filepath, ageGroup);
//			System.out.println("test  " + year);
		}
		
//		if (year > 2013) {
//			pop = 
//		}
		System.out.println( year + "  " + ageGroup + "  " + pop);
		return pop;
	}

}
