package innoz.io.database.migration;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.jcraft.jsch.JSchException;

public class CommuterSpreadsheetFileHandler {

	private static final Logger log = Logger.getLogger(CommuterSpreadsheetFileHandler.class);
	
	private Connection connection;
	
	public static void main(String args[]) throws JSchException{
		
		try {
	
			log.info("Parsing xls commuter data to write it into MobilityDatabase...");
			
			Configuration configuration = ConfigurationUtils.createConfiguration();
			
			if(SshConnector.connect(configuration)){

				CommuterSpreadsheetFileHandler handler = new CommuterSpreadsheetFileHandler();
				
				Class.forName("org.postgresql.Driver").newInstance();
				handler.connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() +
						"/surveyed_mobility", configuration.getDatabaseUsername(), configuration.getDatabasePassword());
				
				if(handler.connection != null){
					
					String path = "/run/user/1009/gvfs/smb-share:domain=INNOZ,server=192.168.0.3,share=gisdata,user=dhosse/"
							+ "MOBILITYDATA/Pendlerdaten_Arbeitsagentur/xls/";

					handler.createDatabaseTables();
					handler.appendExcelSheetToDatabase(path + "krpend_01_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_02_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_03_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_04_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_05_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_06_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_07_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_08_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_09_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_10_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_11_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_12_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_13_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_14_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_15_0.xls");
					handler.appendExcelSheetToDatabase(path + "krpend_16_0.xls");
					
				}
				
				handler.connection.close();
				
				log.info("Done.");
				
			}
			
			SshConnector.disconnect();
			
		} catch (EncryptedDocumentException | InvalidFormatException | IOException | InstantiationException |
				IllegalAccessException | ClassNotFoundException | SQLException | InterruptedException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public void createDatabaseTables() throws SQLException, InstantiationException, IllegalAccessException,
		ClassNotFoundException{
		
		log.info("Creating schemata and tables...");
		
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS commuters;");
		statement.executeUpdate("DROP TABLE IF EXISTS commuters.\"2015_commuters\";");
		statement.executeUpdate("DROP TABLE IF EXISTS commuters.\"2015_reverse\";");
		statement.executeUpdate("CREATE TABLE commuters.\"2015_commuters\"(home_id character varying, home_name character varying,"
				+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
				+ "germans integer, foreigners integer, azubis integer);");
		statement.executeUpdate("CREATE TABLE commuters.\"2015_reverse\"(home_id character varying, home_name character varying,"
				+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
				+ "germans integer, foreigners integer, azubis integer);");
		
		statement.close();
		
	}
	
	public void appendExcelSheetToDatabase(String file) throws EncryptedDocumentException, InvalidFormatException, IOException,
		InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InterruptedException {
		
		log.info("Handling file " + file + "...");
		
		Workbook workbook = WorkbookFactory.create(new File(file));
		ExcelExtractor extractor = new ExcelExtractor((HSSFWorkbook) workbook);
		extractor.setIncludeBlankCells(false);
		extractor.setFormulasNotResults(false);
		extractor.setIncludeSheetNames(false);
		String text = extractor.getText();
		extractor.close();
		
		Statement statement = connection.createStatement();
		
		String fromId = null;
		String fromName = null;
		String table = null;
		Set<String> keys = new HashSet<String>();
		
		for(String line : text.split(TextUtils.NEW_LINE)){

			if(!line.isEmpty()){

				if(line.contains("Auspendler")){
					
					table = "2015_reverse";
					keys.clear();
					
				} else if(line.contains("Einpendler")){
					
					table = "2015_commuters";
					keys.clear();
					
				}
				
				String[] lineParts = line.split(TextUtils.TAB);
				
				if(lineParts.length == 2){
					
					fromId = lineParts[0];
					fromName = lineParts[1];
					
					keys.clear();
					
				} else if(lineParts.length == 8){
					
					createNewEntry(statement, table, keys, fromId, fromName, lineParts);
					
				}
				
			}
			
		}
		
		statement.close();
		
	}
	
	private void createNewEntry(Statement statement, String table, Set<String> keys, String fromId, String fromName,
			String[] lineParts) throws SQLException {
		
		// To prevent duplicate keys store them inside the key set
		if(!keys.contains(lineParts[0])){
			
			String toId = null;
			String toName = null;
			int amount = getNumber(lineParts[2]);
			int men = getNumber(lineParts[3]);
			int women = getNumber(lineParts[4]);
			int germans = getNumber(lineParts[5]);
			int foreigners = getNumber(lineParts[6]);
			int azubis = getNumber(lineParts[6]);
			
			if(table.equals("2015_commuters")){
				
				toId = new String(fromId);
				toName = new String(fromName);
				fromId = lineParts[0];
				fromName = lineParts[1];
				
			} else {
				
				toId = lineParts[0];
				toName = lineParts[1];
				
			}
			
			statement.executeUpdate("INSERT INTO commuters.\"" + table + "\" VALUES('" + fromId + "','" + fromName + "','"
					+ toId + "','" + toName + "','" + amount + "','" + men + "','" + women + "','" + germans + "','"
					+ foreigners + "','" + azubis + "');");
			
			keys.add(lineParts[0]);
			
		}
		
	}
	
	private int getNumber(String numberString){
		
		if(numberString.equals("-") || numberString.equals("*")){
			
			return 0;
			
		} else {
			
			return Integer.parseInt(numberString.replace(",", ""));
			
		}
		
	}
	
}
