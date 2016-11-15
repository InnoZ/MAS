package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLStoCSV {
	
	public static void xls(String inputFolder, String outputFolder, String filename) {
		
        File inputFile = new File(inputFolder + filename + ".xls");
        File outputFile = new File(outputFolder + filename + ".csv");
        
        int columnFrom = 0;
        int columnTo = 44;
        
//      Zensusdata only for 2009 until 2013
        if (filename.startsWith("Z")){
        	columnFrom = 0;
        	columnTo = 18;
        	if (filename.contains("00-05")||filename.contains("05-10")||filename.contains("75-85")||filename.contains("85-101")){
        		columnTo = 17;
        	}
        }
        
		// For storing data into CSV files
        StringBuffer data = new StringBuffer();
        try 
        {
        FileOutputStream fos = new FileOutputStream(outputFile);

        // Get the workbook object for XLS file
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(inputFile));
        // Get first sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);
        Cell cell;
        Row row;

        // Iterate through each rows from first sheet
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) 
        {
                row = rowIterator.next();
                
                if (row.getRowNum()>1 && row.getRowNum()<416){
	                // For each row, iterate through each columns
	                Iterator<Cell> cellIterator = row.cellIterator();
	                while (cellIterator.hasNext()) 
	                {
	                        cell = cellIterator.next();
	                        if (cell.getColumnIndex() >= columnFrom && cell.getColumnIndex() < columnTo) {
	                        	
		                        switch (cell.getCellType()) 
		                        {
		                        case Cell.CELL_TYPE_BOOLEAN:
		                                data.append(cell.getBooleanCellValue() + ";");
		                                break;
		                                
		                        case Cell.CELL_TYPE_FORMULA:
		                            switch (cell.getCachedFormulaResultType()) {
		                                case Cell.CELL_TYPE_STRING:
		                                	data.append(cell.getRichStringCellValue().getString()+ ";");
		                                    break;
		                                case Cell.CELL_TYPE_NUMERIC:
		                                    data.append((int) cell.getNumericCellValue()+ ";");
		                                    break;
		                            }
		                            break;
		                                
		                        case Cell.CELL_TYPE_NUMERIC:
		                                data.append((int) cell.getNumericCellValue() + ";");
		                                break;
		                                
		                        case Cell.CELL_TYPE_STRING:
		                                data.append(cell.getStringCellValue() + ";");
		                                break;
		
		                        case Cell.CELL_TYPE_BLANK:
		                                data.append("0" + ";");
		                                break;
		                        
		                        default:
		                                data.append(cell + ";");
		                        }
	                        }
	                }
//	                deletes the last semicolon of a row in the stream before line break. 
//	                otherwise another column will be recognized by postgreSQL Driver
	                data.deleteCharAt(data.length()-1);
	                data.append('\n'); 
                }
        }

        fos.write(data.toString().getBytes());
        fos.close();
        System.out.println(inputFile.toString() + "  transformed to csv");
        }
        catch (FileNotFoundException e) 
        {
                e.printStackTrace();
        }
        catch (IOException e) 
        {
                e.printStackTrace();
        }
        }
}