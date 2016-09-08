package com.innoz.toolbox.populationForecast.populationCalc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
	
	public int getXLSCalcValues(int year, int gkzRow, String filepath, String ageGroup) throws IOException {
		
//		String file = filepath + "Z" + ageGroup + "_m.xls";
//		InputStream ExcelFileToRead = new FileInputStream(file);
//		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);		
//		XSSFSheet sheet = workbook.getSheetAt(0);
//		XSSFRow row = sheet.getRow(gkzRow);
//		XSSFCell cell = row.getCell(13 + year - 2009);
		
//		DC4 = InnoZ vorläufig = $'0-5_m'.R4+(SUM($'Z18-25_w'.BZ4+$'Z25-35_w'.BZ4+$'Z35-45_w'.BZ4)*$B$477*S$477/(45-18))+(BZ4*4/5)
//		int innoZvorl = pop0to5m +(SUM($'Z18-25_w'.BZ4+$'Z25-35_w'.BZ4+$'Z35-45_w'.BZ4)*$B$477*S$477/(45-18))+ diffInnoZvsBBSR;

		String file = filepath + ageGroup + "_m.xls";
		InputStream ExcelFileToRead = new FileInputStream(file);
		XSSFWorkbook  workbook = new XSSFWorkbook(ExcelFileToRead);
		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row = sheet.getRow(gkzRow);
		XSSFCell cell = row.getCell(17 + year - 2014);
		int pop0to5m = (int) cell.getNumericCellValue();
		

//		DC4+ (S$475*$'0-101'.BY4
//		int population = innoZvorl+ (S$475*$'0-101'.BY4);
		int population = pop0to5m;
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

