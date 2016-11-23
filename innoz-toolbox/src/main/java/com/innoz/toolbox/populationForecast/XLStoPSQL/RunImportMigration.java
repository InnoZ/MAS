package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RunImportMigration {

	public static void main(String[] args) throws SQLException, ClassNotFoundException {
		
		String database = "mydb";
		String schema = "bbsrprognose.";
		String tablename = "migration";
		
		
	    Connection c = null;
	    Statement stmt = null;
	    
		Class.forName("org.postgresql.Driver");
  	  	c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + database,
			  "postgres", "postgres");
  	  	System.out.println("Opened database successfully");
  	  	stmt = c.createStatement();
  	  	String sql;
  	  	
  	  	sql = "DROP TABLE IF EXISTS " + schema + tablename ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"CREATE TABLE " + schema + tablename + " ("
  	  			+ "scenario			character varying";
  	    for (int year = 2000 ; year <= 2040; year++){
  	    	sql = sql + ", year" + year + " integer";
  	    }
  	    sql = sql + ")";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql = "INSERT INTO " + schema + tablename 
  	  			+ " (scenario, year2014, year2015) VALUES "
  	  			+ " (1,250000,100000)";
  	  	System.out.println(sql);
  	  	stmt.execute(sql);
	}

}
