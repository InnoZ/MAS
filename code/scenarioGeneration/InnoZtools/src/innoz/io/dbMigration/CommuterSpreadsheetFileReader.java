package innoz.io.dbMigration;

import innoz.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class CommuterSpreadsheetFileReader {

	public static void main(String args[]){
		
		try {
		
			new CommuterSpreadsheetFileReader().appendExcelSheetToDatabase("/home/dhosse/guiTest/pendlerdaten/krpend_01_0.xls",
					"reverse");
		
		} catch (EncryptedDocumentException | InvalidFormatException | IOException | InstantiationException |
				IllegalAccessException | ClassNotFoundException | SQLException | InterruptedException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public void createDatabaseTables() throws SQLException, InstantiationException, IllegalAccessException,
		ClassNotFoundException{
		
		Class.forName("org.postgresql.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/surveyed_mobility",
				"postgres", "postgres");
		
		if(connection != null){
			
			Statement statement = connection.createStatement();
			statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS commuters;");
			statement.executeUpdate("DROP TABLE IF EXISTS 2015_commuters;");
			statement.executeUpdate("DROP TABLE IF EXISTS 2015_reverse;");
			statement.executeUpdate("CREATE TABLE 2015_commuters(home_id character varying, home_name character varying,"
					+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
					+ "germans integer, foreigners integer, azubis integer);");
			statement.executeUpdate("CREATE TABLE 2015_reverse(home_id character varying, home_name character varying,"
					+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
					+ "germans integer, foreigners integer, azubis integer);");
			
			statement.close();
			
		}
		
		connection.close();
		
	}
	
	public void appendExcelSheetToDatabase(String file, String commuterType) throws EncryptedDocumentException,
		InvalidFormatException, IOException, InstantiationException, IllegalAccessException, ClassNotFoundException,
		SQLException, InterruptedException{
		
		Workbook workbook = WorkbookFactory.create(new File(file));
		ExcelExtractor extractor = new ExcelExtractor((HSSFWorkbook) workbook);
		extractor.setIncludeBlankCells(false);
		extractor.setFormulasNotResults(false);
		extractor.setIncludeSheetNames(false);
		String text = extractor.getText();
		extractor.close();
		
		
		Class.forName("org.postgresql.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/surveyed_mobility",
				"postgres", "postgres");
		
		if(connection != null){
			
			Statement statement = connection.createStatement();
			
			String fromId = null;
			String fromName = null;
			
			for(String line : text.split(TextUtils.NEW_LINE)){

				if(!line.equals(TextUtils.EMPTY)){

					//TODO update db table
					String[] lineParts = line.split(TextUtils.TAB);
					
					// from location included: length 10, else length 8
					if(lineParts.length == 10){
						
						fromId = lineParts[0];
						fromName = lineParts[1];
						
						String[] parts = new String[lineParts.length - 2];
						for(int i = 2; i < lineParts.length; i++){
							parts[i-2] = lineParts[i];
						}
						
						createNewEntry(statement, fromId, fromName, parts);
						
					} else if(lineParts.length == 8){
						
						createNewEntry(statement, fromId, fromName, lineParts);
						
					}
					
				}
				
			}
			
			statement.close();
			
		}
		
		connection.close();
		
	}
	
	private void createNewEntry(Statement statement, String fromId, String fromName, String[] lineParts) throws SQLException{
		
		statement.executeUpdate("INSERT INTO commuters.2015_commuters VALUES('" + fromId + "','" + fromName + "','"
				+ lineParts[0] + "','" + lineParts[1] + "','" + lineParts[2] + lineParts[3] + "','" + lineParts[4] + "','" 
				+ lineParts[5] + "','" + lineParts[6] + "','" + lineParts[7] + "');");
		
	}
	
}
