package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RunImportParams {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		String outputFolder = "/home/bmoehring/workspace/SchwartzbachData/CSV/";
		String filename;
		String dbName = "mydb";
		String dbSchema = "bbsrprognose.";
		String dbTable;
		
		
	    Connection c = null;
	    Statement stmt = null;
	    
		Class.forName("org.postgresql.Driver");
  	  	c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + dbName,
			  "postgres", "postgres");
  	  	System.out.println("Opened " + dbName + " successfully");
  	  	stmt = c.createStatement();
  	  	String sql;
  	  	
//  	  	migration
  	  	dbTable = dbSchema + "migration";
  	  	sql = "DROP TABLE IF EXISTS " + dbTable ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " + dbTable + " ("
  	  			+ "scenario			character varying";
  	    for (int year = 2000 ; year <= 2040; year++){
  	    	sql = sql + ", year" + year + " integer";
  	    }
  	    sql = sql + ")";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql = "INSERT INTO " + dbTable 
  	  			+ " (scenario, year2014, year2015) VALUES "
  	  			+ " (1,250000,100000)";
  	  	System.out.println(sql);
  	  	stmt.execute(sql);
  	  	
//  		migration by ageGroup
  	  	filename = "WanderungAltersG";
  	  	dbTable = dbSchema + "migrationbyagegroup";
  	  	sql = "DROP TABLE IF EXISTS " + dbTable ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  dbTable + "("
  	  			+ "ageGroup			character varying,"
  	  			+ "agegroupfactor	numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + dbTable + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
//  	  	migration by Bundesland and Cluster
  	  	filename = "WanderungBLCluster";
  	  	dbTable = dbSchema + "migrationbycluster";
  	  	sql = "DROP TABLE IF EXISTS " + dbTable ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  dbTable + "("
  	  			+ "Cluster			int,"
  	  			+ "Bundesland		character varying,"
  	  			+ "Kreisregion		character varying,"
  	  			+ "Kreistyp			numeric,"
  	  			+ "migration		numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + dbTable + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
//  	  	mortality rates
  	  	filename = "Sterbetafel";
  	  	dbTable = dbSchema + "mortalitybyagegroup";
  	  	sql = "DROP TABLE IF EXISTS " + dbTable ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  dbTable + "("
  	  			+ "agegroup			character varying,"
  	  			+ "year1925			numeric,"
  	  			+ "year1933			numeric,"
  	  			+ "year1950			numeric,"
  	  			+ "year1961			numeric,"
  	  			+ "year1971			numeric,"
  	  			+ "year1987			numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + dbTable + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
	}

}
