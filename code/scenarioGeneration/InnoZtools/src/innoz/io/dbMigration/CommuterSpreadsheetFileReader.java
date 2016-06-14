package innoz.io.dbMigration;

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
			
			for(String line : text.split("\n")){

				//TODO update db table
				
			}
			
			statement.close();
			
		}
		
		connection.close();
		
	}
	
}
