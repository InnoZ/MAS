package com.innoz.toolbox.io.database.migration;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.ConfigurationUtils;
import com.innoz.toolbox.config.PsqlAdapter;
import com.innoz.toolbox.config.SshConnector;
import com.jcraft.jsch.JSchException;

public class DatabaseDocumentation {

	final static String HTML_HEADER = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";

	final static String HTML = "html";
	final static String HEAD = "head";
	final static String BODY = "body";
	
	public static void main(String args[]){
		
		try {
			generate();
		} catch (JSchException | IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static void generate() throws JSchException, IOException{
		
		String[] databases = new String[]{"electric_mobility","geodata","modalyzer","shared_mobility",
				"simulated_mobility","surveyed_mobility"};
		
		Configuration configuration = ConfigurationUtils.createConfiguration();
		
		Map<String, Map<String,List<TableEntry>>> db2Schemas2Tables = new HashMap<>();
		
		SshConnector.connect(configuration);
		
		for(String database : databases){
			
			db2Schemas2Tables.put(database, new HashMap<>());

			try {
				
				Connection connection = PsqlAdapter.createConnection(configuration, database);
				
				String query = "SELECT * FROM pg_catalog.pg_tables ORDER BY SCHEMANAME, TABLENAME";
				Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery(query);
				
				while(results.next()){
					
					String schemaname = results.getString("schemaname");
					String tablename = results.getString("tablename");
					
					if(!schemaname.equals("pg_catalog") && !schemaname.equals("information_schema")){

						if(!db2Schemas2Tables.get(database).containsKey(schemaname)){
							
							db2Schemas2Tables.get(database).put(schemaname, new ArrayList<>());

						}
						
						db2Schemas2Tables.get(database).get(schemaname).add(new TableEntry(tablename));
						
					}
					
				}
				
				results.close();
				
				for(Entry<String, List<TableEntry>> entries : db2Schemas2Tables.get(database).entrySet()){
					
					for(TableEntry e : entries.getValue()){
						
						query = "SELECT column_name,data_type FROM information_schema.columns WHERE table_name='"
								+ e.tablename + "';";
						
						results = statement.executeQuery(query);
						
						while(results.next()){
							
							String columnname = results.getString("column_name");
							String datatype = results.getString("data_type");
							
							e.columns2Datatypes.put(columnname, datatype);
							
						}
						
					}
					
				}
				
				statement.close();
				connection.close();
				
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
				
				e.printStackTrace();
				
			}
			
		}

		writeHtmlFiles(db2Schemas2Tables);
		
		SshConnector.disconnect();
		
	}
	
	static void writeHtmlFiles(Map<String, Map<String, List<TableEntry>>> db2Schemas2Tables){
		
		try {
			
			writeIndexHtml(db2Schemas2Tables);
		
		} catch (IOException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	static void writeIndexHtml(Map<String, Map<String, List<TableEntry>>> db2Schemas2Tables) throws IOException{
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/00_Orga/Datahub/index.html");
		
		writer.write(HTML_HEADER);
		writer.newLine();
		writer.write("<html>");
		writer.newLine();
		writer.write("<head>");
		writer.newLine();
		writer.write("<title>MobilityDataHub Documentation</title>");
		writer.newLine();
		writer.write("</head>");
		writer.newLine();
		writer.write("<body>");
		writer.newLine();
		writer.write("<b>List of databases with schemas and tables</b>");
		writer.newLine();
		writer.write("<ol>");
		
		for(Entry<String, Map<String, List<TableEntry>>> entry : db2Schemas2Tables.entrySet()){
			
			writer.newLine();
			writer.write("<li>" + entry.getKey());
			writer.newLine();
			writer.write("<ol>");
			
			for(Entry<String, List<TableEntry>> entry2 : db2Schemas2Tables.get(entry.getKey()).entrySet()){
				
				writer.newLine();
				writer.write("<li>" + entry2.getKey());
				writer.newLine();
				writer.write("<ol>");
				
				for(TableEntry s : entry2.getValue()){
					
					writer.newLine();
					writer.write("<li><a href=\"./" + s.tablename + ".html\">" + s.tablename + "</a>");
					writeTableHtml(entry.getKey(), entry2.getKey(), s);
					
				}

				writer.newLine();
				writer.write("</ol>");
				
			}
			
			writer.newLine();
			writer.write("</ol>");
			
		}
		
		writer.write("</ol>");
		writer.newLine();
		writer.write("</body>");
		writer.newLine();
		writer.write("</html>");
		writer.flush();
		writer.close();
		
	}
	
	static void writeTableHtml(String dbName, String schemaName, TableEntry entry) throws IOException{
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/00_Orga/Datahub/" + entry.tablename + ".html");
		
		writer.write(HTML_HEADER);
		
		writer.newLine();
		writer.write("<html>");
		writer.newLine();
		writer.write("<head>");
		writer.newLine();
		writer.write("<title>" + schemaName + "." + entry.tablename + " in " + dbName + "</title>");
		writer.newLine();
		writer.write("<style>");
		writer.newLine();
		writer.write("table, th, td {");
		writer.newLine();
		writer.write("border: 1px solid; border-collapse: collapse;");
		writer.newLine();
		writer.write("}");
		writer.newLine();
		writer.write("caption{");
		writer.newLine();
		writer.write("font-size:16pt;");
		writer.newLine();
		writer.write("font-weight:bold;");
		writer.newLine();
		writer.write("}");
		writer.newLine();
		writer.write("</style>");
		writer.newLine();		
		writer.write("</head>");
		writer.newLine();
		writer.write("<body>");
		writer.newLine();
		
		writer.write("<table style=\"width:100%\">");
		writer.newLine();
		writer.write("<caption>" + schemaName + "." + entry.tablename + " in " + dbName + "</caption>");
		writer.newLine();
		writer.write("<tr>");
		writer.newLine();
		writer.write("<th>column_name</th>");
		writer.newLine();
		writer.write("<th>data_type</th>");
		writer.newLine();
		writer.write("</tr>");
		
		for(Entry<String, String> e : entry.columns2Datatypes.entrySet()){
			
			writer.newLine();
			writer.write("<tr>");
			writer.newLine();
			writer.write("<td>" + e.getKey() + "</td>");
			writer.newLine();
			writer.write("<td>" + e.getValue() + "</td>");
			writer.newLine();
			writer.write("</tr>");
			
		}
		
		writer.newLine();
		writer.write("</body>");
		writer.newLine();
		
		writer.write("</html>");
		writer.flush();
		writer.close();
		
	}
	
	static class TableEntry{
		
		String tablename;
		Map<String,String> columns2Datatypes = new HashMap<>();
		
		TableEntry(String tablename){
			
			this.tablename = tablename;
			
		}
		
	}
	
}