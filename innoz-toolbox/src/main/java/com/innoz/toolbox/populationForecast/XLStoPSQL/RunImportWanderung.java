package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class RunImportWanderung {

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
		String outputFolder = "/home/bmoehring/workspace/SchwartzbachData/CSV/";
		String filename;
		String database = "mydb";
		String schema = "bbsrprognose";
		String tablename;
		
		
	    Connection c = null;
	    Statement stmt = null;
	    
		Class.forName("org.postgresql.Driver");
  	  	c = DriverManager
			  .getConnection("jdbc:postgresql://localhost:5432/" + database,
			  "postgres", "postgres");
  	  	System.out.println("Opened database successfully");
  	  	stmt = c.createStatement();
  	  	String sql;
  	  	
//  		by ageGroup
  	  	filename = "WanderungAltersG";
  	  	tablename = schema + ".migrationbyagegroup";
  	  	sql = "DROP TABLE IF EXISTS " + tablename ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  tablename + "("
  	  			+ "ageGroup			char(20),"
  	  			+ "agegroupfactor	numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + tablename + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
//  	  	by Bundesland and Cluster
  	  	filename = "WanderungBLCluster";
  	  	tablename = schema + ".migrationbycluster";
  	  	sql = "DROP TABLE IF EXISTS " + tablename ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  tablename + "("
  	  			+ "Cluster			int,"
  	  			+ "Bundesland		char(50),"
  	  			+ "Kreisregion		char(50),"
  	  			+ "Kreistyp			numeric,"
  	  			+ "migration		numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + tablename + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
//  	  	Sterbetafel
  	  	filename = "Sterbetafel";
  	  	tablename = schema + ".deathsbyagegroup";
  	  	sql = "DROP TABLE IF EXISTS " + tablename ;
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	sql =	"CREATE TABLE " +  tablename + "("
  	  			+ "agegroup			char(50),"
  	  			+ "year1925			numeric,"
  	  			+ "year1933			numeric,"
  	  			+ "year1950			numeric,"
  	  			+ "year1961			numeric,"
  	  			+ "year1971			numeric,"
  	  			+ "year1987			numeric)";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	sql =	"COPY " + tablename + " FROM '" + outputFolder + filename + ".csv' CSV Header DELIMITER '\t'";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
	}

}
