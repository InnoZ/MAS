package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.io.File;
import java.util.ArrayList;

public class RunImport {

	public static void main(String[] args) 
    {
		
		/*
		RunImport.java converts xls files to csv and imports them into an postgreSQL database
		FileInputStream of the complete xlsx file is too big for the laptops in use. 
		The following link provides a macro to split all sheets to single workbooks.
		http://superuser.com/questions/561923/how-can-one-split-an-excel-xlsx-file-that-contains-multiple-sheets-into-sep
		Split the workbook and adjust the filepaths before running RunImport.java 
		*/
		
		
		String inputFolder = "/home/bmoehring/workspace/SchwartzbachData/Excel/";
		String outputFolder = "/home/bmoehring/workspace/SchwartzbachData/CSV/";
		String filename;
		String database = "mydb";
		String schema = "bbsrprognose";
		
//		ArrayList including all ageGroups that are used in the bbsr forecast
//		0-10 also exists but provides the same values as 0-5 + 5-10 . It is not being imported
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
		
		XLStoCSV converter = new XLStoCSV();
		CSVtoPostgreSQL importer = new CSVtoPostgreSQL();
		
		for (int ii = 0; ii < ageGroupsArrayList.size() ; ii++){
			
			filename = ageGroupsArrayList.get(ii) + "_m";
            converter.xls(inputFolder, outputFolder, filename);
            importer.csv(outputFolder, filename, database, schema);
            
			filename = ageGroupsArrayList.get(ii) + "_w";
			converter.xls(inputFolder, outputFolder, filename);
			importer.csv(outputFolder, filename, database, schema);
		}
    }
	

}
