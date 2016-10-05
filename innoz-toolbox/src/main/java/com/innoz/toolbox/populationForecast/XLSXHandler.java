package com.innoz.toolbox.populationForecast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class XLSXHandler {
	

//	public static void readXLSXFile(String filename) throws IOException
//	{
//		InputStream ExcelFileToRead = new FileInputStream(filename);
//		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);
//		
//		XSSFSheet sheet = workbook.getSheetAt(0); 
//		XSSFRow row; 
//		XSSFCell cell; 
//
//		Iterator<Row> rows = sheet.rowIterator();
//
//		while (rows.hasNext())
//		{
//			row=(XSSFRow) rows.next();
//			Iterator<Cell> cells = row.cellIterator();
//			while (cells.hasNext())
//			{
//				cell=(XSSFCell) cells.next();
//		
//				if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING)
//				{
//					System.out.print(cell.getStringCellValue()+" ");
//				}
//				else if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC)
//				{
//					System.out.print(cell.getNumericCellValue()+" ");
//				}
//				else
//				{
//					//U Can Handel Boolean, Formula, Errors
//				}
//			}
//			System.out.println();
//		}
//	}
	
	public int getXLSValue(int year, int gkzRow, String filepath, String ageGroup) throws IOException{
		String file = filepath + "Z" + ageGroup + "_m.xls";
		InputStream ExcelFileToRead = new FileInputStream(file);
		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);		
		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row = sheet.getRow(gkzRow);
		int coloumn = (13 + year - 2009);
		XSSFCell cell = row.getCell(coloumn);
		int population = (int) cell.getNumericCellValue();
		workbook.close();
		ExcelFileToRead.close();
		return population;
	}
	
	public int getXLSCalcValues(int year, int gkzRow, String filepath, String ageGroup, HashMap<Integer, HashMap<String, Integer>> populationMap) throws IOException {
		String file;
		InputStream ExcelFileToRead;
		XSSFWorkbook  workbook;
		XSSFSheet sheet;
		XSSFRow row;
		XSSFCell cell;
//		DC4 = InnoZ vorläufig = $'0-5_m'.R4+(SUM($'Z18-25_w'.BZ4+$'Z25-35_w'.BZ4+$'Z35-45_w'.BZ4)*$B$477*S$477/(45-18))+(BZ4*4/5)
//		int innoZvorl = popm + fromLowerCohort*(1/5)+ diffInnoZvsBBSR*(4/5);

//		get bbsr data for calculating year
		file = filepath + ageGroup + "_m.xls";
		ExcelFileToRead = new FileInputStream(file);
		workbook = new XSSFWorkbook(ExcelFileToRead);
		sheet = workbook.getSheetAt(0);
		row = sheet.getRow(gkzRow);
		cell = row.getCell(17 + year - 2014);
		int popm = (int) cell.getNumericCellValue();
		
//		gets bbsr data for the year before the calculating year
		cell = row.getCell(16 + year - 2014);
		int bbsr2013 = (int) cell.getNumericCellValue();
		System.out.println("TEST   BBSR2013:  " + bbsr2013);
//		gets the population data for the year before the calculating year
		int pop2013 = populationMap.get(year - 1).get(ageGroup);
		System.out.println("TEST    pop2013:  " + pop2013);
		int diffInnoZvsBBSR = pop2013 - bbsr2013;
		System.out.println(diffInnoZvsBBSR);
		
//		movement of cohorts
		int fromLowerCohort = 0;
		if (ageGroup == "0-5"){
////			birthrate NEEDS TO BE CHANGED FOR MAP F
//			int potentialPregnantWomen = populationMap.get(year - 1).get("18-25") + populationMap.get(year - 1).get("25-35") + populationMap.get(year - 1).get("35-45");
//			fromLowerCohort = potentialPregnantWomen
		}
		else {
//			fromLowerCohort = populationMap.get(year - 1).get(ageGroupBefore);
		}
		
//		DC4+ (S$475*$'0-101'.BY4
//		int population = innoZvorl+ (S$475*$'0-101'.BY4);
		int population = popm + fromLowerCohort*(1/5) + diffInnoZvsBBSR*(4/5);
		workbook.close();
		ExcelFileToRead.close();
		return population;
	}
	
	
	
////iterating over rows to find matching value gkz	
//	public XSSFRow getRow(String filename, int gkz) throws IOException{
//		InputStream ExcelFileToRead = new FileInputStream(filename);
//		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);		
//		XSSFSheet sheet = workbook.getSheetAt(0);
//		XSSFRow row = null;
//		XSSFCell cell;
//		Iterator<Row> rows = sheet.rowIterator();
//		while (rows.hasNext())
//		{	
//			row = (XSSFRow) rows.next();
//			cell = row.getCell(1);
//			checkForMatchingCells(cell, gkz);
//		}
//		workbook.close();
//		return row;
//	}

//	public static void getXLSAdress(String filename, int gkz, int year) throws IOException {
//		InputStream ExcelFileToRead = new FileInputStream(filename);
//		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);		
//		XSSFSheet sheet = workbook.getSheetAt(0);
//		XSSFRow row;
//		XSSFCell cell;	
//		
//		Iterator<Row> rows = sheet.rowIterator();
//		while (rows.hasNext())
//		{	
//			row = (XSSFRow) rows.next();
//			cell = row.getCell(1);
//			checkForMatchingCells(cell, gkz);
//		}
//		
//		row = workbook.getSheetAt(0).getRow(2);
//		Iterator<Cell> cells = row.cellIterator();		
//		while (cells.hasNext())
//		{
//			cell=(XSSFCell) cells.next();
//			checkForMatchingCells(cell, year);
//		}	
//		
//		workbook.close();
//	}
	
	public static void checkForMatchingCells(XSSFCell cell, int x){
		if (cell != null){ 		
			if(cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC && cell.getNumericCellValue() == x)
			{	
				System.out.println(cell.getAddress().getRow() + " " + cell.getAddress().getColumn() + " " + cell.getNumericCellValue() );
			}
		}		
	}



}

