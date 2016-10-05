package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class CSVtoPostgreSQL {
   public static void csv(String outputFolder, String filename, String database, String schema) {
      Connection c = null;
      Statement stmt = null;
      try {
    	  Class.forName("org.postgresql.Driver");
    	  c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + database,
			  "postgres", "postgres");
    	  System.out.println();
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
    	  sql =	"CREATE TABLE " +  tableName + "("
    	  		+ "Land		integer,"
    	  		+ "GKZ		integer,"
    	  		+ "Name		char(50)";
    	  for (int year = 2000 ; year < 2041; year++){
    		  sql = sql + ", year" + year + " integer";
    	  }
    	  sql = sql + ")";
		  System.out.println(sql);
    	  stmt.executeUpdate(sql);
    	      	  
//    	  Copy Values from CSV to PostgreSQL
    	  sql =	"COPY " + tableName + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER ';'";
    	  stmt.executeUpdate(sql);
       	  System.out.println(sql);
    	  
    	  stmt.close();
    	  c.close();
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      
   }
}
