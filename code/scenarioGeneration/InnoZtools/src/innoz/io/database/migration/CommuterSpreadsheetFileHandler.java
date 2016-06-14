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

/**
 * 
 * A handler that reads in data from Excel commuter spreadsheets provided by the Bundesagentur für Arbeit ( 
 * <a href="http://statistik.arbeitsagentur.de/nn_31966/SiteGlobals/Forms/Rubrikensuche/Rubrikensuche_Suchergebnis_Form
 * .html?view=processForm&resourceId=210358&input_=&pageLocale=de&topicId=882788&region=&year_month=201506&year_month
 * .GROUP=1&search=Suchen">Source</a>) and writes it into the MobilityDatabase.
 * The reading procedure is provided by the Apache POI framework (see <a href="http://poi.apache.org/index.html">
 * http://poi.apache.org/index.html</a>).
 * 
 * @author dhosse
 *
 */
public class CommuterSpreadsheetFileHandler {

	private static final Logger log = Logger.getLogger(CommuterSpreadsheetFileHandler.class);
	
	private Connection connection;
	
	/**
	 * 
	 * Example main method for the usage of this class.
	 * 
	 * @param args
	 * @throws JSchException
	 */
	public static void main(String args[]) throws JSchException{
		
		try {
	
			log.info("Parsing xls commuter data to write it into MobilityDatabase...");
			
			// Create an empty configuration
			Configuration configuration = ConfigurationUtils.createConfiguration();
			
			// Try to establish an ssh tunnel to the MobilityDatabase server
			if(SshConnector.connect(configuration)){

				// Create a new handler object
				CommuterSpreadsheetFileHandler handler = new CommuterSpreadsheetFileHandler();
				
				// Instantiate a postgresql driver and establish a connection to the remote database
				Class.forName("org.postgresql.Driver").newInstance();
				handler.connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() +
						"/surveyed_mobility", configuration.getDatabaseUsername(), configuration.getDatabasePassword());
				
				// If the connection could be established, proceed
				if(handler.connection != null){
					
					String path = "/run/user/1009/gvfs/smb-share:domain=INNOZ,server=192.168.0.3,share=gisdata,user=dhosse/"
							+ "MOBILITYDATA/Pendlerdaten_Arbeitsagentur/xls/";

					// Create all the schemata and tables needed
					handler.createDatabaseTables();
					// Read one spreadsheet per state and write the data into the database
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
				
				// After everything is done, close the connection and exit
				handler.connection.close();
				
				log.info("Done.");
				
			}
			
			SshConnector.disconnect();
			
		} catch (EncryptedDocumentException | InvalidFormatException | IOException | InstantiationException |
				IllegalAccessException | ClassNotFoundException | SQLException | InterruptedException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * Creates all schemata and tables needed for the commuter data.
	 * 
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public void createDatabaseTables() throws SQLException, InstantiationException, IllegalAccessException,
		ClassNotFoundException{
		
		log.info("Creating schemata and tables...");
		
		// Create a new statement to execute the following queries
		Statement statement = connection.createStatement();
		
		// Create the commuter schema if it doesn't exist already
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS commuters;");
		// Drop all existing tables
		statement.executeUpdate("DROP TABLE IF EXISTS commuters.\"2015_commuters\";");
		statement.executeUpdate("DROP TABLE IF EXISTS commuters.\"2015_reverse\";");
		// Create new empty tables
		statement.executeUpdate("CREATE TABLE commuters.\"2015_commuters\"(home_id character varying, home_name character varying,"
				+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
				+ "germans integer, foreigners integer, azubis integer);");
		statement.executeUpdate("CREATE TABLE commuters.\"2015_reverse\"(home_id character varying, home_name character varying,"
				+ "work_id character varying, work_name character varying, amount integer, men integer, women integer,"
				+ "germans integer, foreigners integer, azubis integer);");
		
		// Exit
		statement.close();
		
	}
	
	/**
	 * 
	 * Reads in data from an Excel spreadsheet and writes it into a database. The method can distinguish between
	 * 'commuter' and 'reverse commuter' relations.
	 * 
	 * @param file The input spreadsheet file
	 * @throws EncryptedDocumentException
	 * @throws InvalidFormatException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public void appendExcelSheetToDatabase(String file) throws EncryptedDocumentException, InvalidFormatException, IOException,
		InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, InterruptedException {
		
		log.info("Handling file " + file + "...");
		
		// Apache POI part
		// Create a new workbook and an extractor the retrieve the data
		Workbook workbook = WorkbookFactory.create(new File(file));
		ExcelExtractor extractor = new ExcelExtractor((HSSFWorkbook) workbook);
		// Make the extractor ignore blank cells, formulas and sheet names
		extractor.setIncludeBlankCells(false);
		extractor.setFormulasNotResults(false);
		extractor.setIncludeSheetNames(false);
		// Retrieve the text
		String text = extractor.getText();
		// Close the extractor when it's done
		extractor.close();
		// Apache POI end
		
		// Create a new statement to execute the following queries
		Statement statement = connection.createStatement();
		
		// Initialize some important fields
		String fromId = null;
		String fromName = null;
		String table = null;
		Set<String> keys = new HashSet<String>();
		
		// Iterate over all lines of the spreadsheed text
		for(String line : text.split(TextUtils.NEW_LINE)){

			// If the line is empty, skip it
			if(!line.isEmpty()){

				// Set the table name according to the type of relations we are about to parse.
				// Also, reset the keys set.
				if(line.contains("Auspendler")){
					
					table = "2015_reverse";
					keys.clear();
					
				} else if(line.contains("Einpendler")){
					
					table = "2015_commuters";
					keys.clear();
					
				}
				
				// Split the current line into its individual cells
				String[] lineParts = line.split(TextUtils.TAB);
				
				// If the line contains only two cells, it's probably a new relation
				if(lineParts.length == 2){
					
					fromId = lineParts[0];
					fromName = lineParts[1];
					
					keys.clear();
					
				} else if(lineParts.length == 8){
					
					// Create a new entry for the current od pair
					createNewEntry(statement, table, keys, fromId, fromName, lineParts);
					
				}
				
			}
			
		}
		
		// After everything's finished, close the statement
		statement.close();
		
	}
	
	/**
	 * 
	 * Creates a new origin-destination (OD) pair for the current identifiers.
	 * The data (i.e. origin, destination, amount of travelers) is directly written into the specified database table.
	 * 
	 * @param statement The postgreSQL statement that executes the current queries.
	 * @param table The database table the current data should be written into.
	 * @param keys Set of the already 'visited' ids.
	 * @param fromId The current origin's id.
	 * @param fromName The current origin's name.
	 * @param lineParts The cells of the current line.
	 * @throws SQLException
	 */
	private void createNewEntry(Statement statement, String table, Set<String> keys, String fromId, String fromName,
			String[] lineParts) throws SQLException {
		
		// To prevent duplicate keys store them inside the key set and only parse the current cells, if
		// we haven't been there before
		if(!keys.contains(lineParts[0])){
			
			String toId = null;
			String toName = null;
			int amount = getNumber(lineParts[2]);
			int men = getNumber(lineParts[3]);
			int women = getNumber(lineParts[4]);
			int germans = getNumber(lineParts[5]);
			int foreigners = getNumber(lineParts[6]);
			int azubis = getNumber(lineParts[6]);
			
			// If we are parsing a 'commuter' relation, the first two columns contain the work location (destination).
			// Thus, we have to switch from and to id.
			if(table.equals("2015_commuters")){
				
				toId = new String(fromId);
				toName = new String(fromName);
				fromId = lineParts[0];
				fromName = lineParts[1];
				
			} else {
				
				// If we are parsing a 'reverse commuter' relation, we only have to set the to id and name.
				toId = lineParts[0];
				toName = lineParts[1];
				
			}
			
			// Update the database table
			statement.executeUpdate("INSERT INTO commuters.\"" + table + "\" VALUES('" + fromId + "','" + fromName + "','"
					+ toId + "','" + toName + "','" + amount + "','" + men + "','" + women + "','" + germans + "','"
					+ foreigners + "','" + azubis + "');");
			
			// Add the current destination's id to the already visited keys to avoid duplicates.
			keys.add(lineParts[0]);
			
		}
		
	}
	
	/**
	 * 
	 * Handles a string that represents a number. In case, the string doesn't hold a numeric value, 0 is returned. 
	 * 
	 * @param numberString The string representation of a number.
	 * @return Numeric value of the string.
	 */
	private int getNumber(String numberString){
		
		if(numberString.equals("-") || numberString.equals("*")){
			
			return 0;
			
		} else {
			
			return Integer.parseInt(numberString.replace(",", ""));
			
		}
		
	}
	
}
