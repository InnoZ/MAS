package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CSVtoPostgreSQL {
   public static void csv(String outputFolder, String filename, String database, String schema) {
      Connection c = null;
      Statement stmt = null;
      try {
    	  Class.forName("org.postgresql.Driver");
    	  c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + database,
			  "postgres", "postgres");
    	  System.out.println("Opened database successfully");
    	  stmt = c.createStatement();
    	  String sql;
    	  
//    	  sql table can't use characters as "-" and "_"
//		  filename can not start with a number
		  String tableName;
		  tableName = schema + ".ageGroup" + filename.replace("-", "to");
		  tableName = tableName.replace("_", "");
		  System.out.println(tableName);

//    	  for test: delete the old table if already existing
    	  sql = "DROP TABLE IF EXISTS " + tableName ;
    	  stmt.executeUpdate(sql);
    	  
//    	  Create Table with currently 44 columns
//    	  if Zensus-Data only 17 columns
    	  int lastColumn = 2040;
    	  if (tableName.contains("Z")){
    		  lastColumn = 2013; 
    		  if (tableName.contains("00to05")){
    			  lastColumn = 2012;
    		  }
    	  }
    	  sql =	"CREATE TABLE " +  tableName + "("
    	  		+ "Land		integer,"
    	  		+ "GKZ		integer,"
    	  		+ "Name		char(50)";
    	  if (tableName.contains("Z")){
    		  sql = sql + ", Raumkategorie int";
    	  }
    	  for (int year = 2000 ; year <= lastColumn; year++){
    		  sql = sql + ", year" + year + " integer";
    	  }
    	  sql = sql + ")";
		  System.out.println(sql);
    	  stmt.executeUpdate(sql);
    	      	  
//    	  Copy Values from CSV to PostgreSQL
    	  sql =	"COPY " + tableName + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER ';'";
    	  stmt.executeUpdate(sql);
       	  System.out.println(sql);
       	  System.out.println();
    	  
    	  stmt.close();
    	  c.close();
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      
   }
}
