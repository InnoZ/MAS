package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RunImportWanderung {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		String outputFolder = "/home/bmoehring/workspace/SchwartzbachData/CSV/";
		String filename;
		String dbName = "mydb";
		String dbSchema = "bbsrprognose";
		String dbTable;
		
		
	    Connection c = null;
	    Statement stmt = null;
	    
		Class.forName("org.postgresql.Driver");
  	  	c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + dbName,
			  "postgres", "postgres");
  	  	System.out.println("Opened dbName successfully");
  	  	stmt = c.createStatement();
  	  	String sql;
  	  	
//  		by ageGroup
  	  	filename = "WanderungAltersG";
  	  	dbTable = dbSchema + ".migrationbyagegroup";
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
  	  	
//  	  	by Bundesland and Cluster
  	  	filename = "WanderungBLCluster";
  	  	dbTable = dbSchema + ".migrationbycluster";
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
  	  	
//  	  	Sterbetafel
  	  	filename = "Sterbetafel";
  	  	dbTable = dbSchema + ".mortalitybyagegroup";
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
