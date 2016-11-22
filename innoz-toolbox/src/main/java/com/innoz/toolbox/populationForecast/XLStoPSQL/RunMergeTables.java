package com.innoz.toolbox.populationForecast.XLStoPSQL;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class RunMergeTables {

	public static void main(String[] args) throws Exception  {
		
		String database = "mydb";
		String schema = "bbsrprognose.";
		String tablename = "populationdata";		
		
	    Connection c = null;
	    Statement stmt = null;
	    ResultSet rs;
	    
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
  	  	sql =	"CREATE TABLE " + schema + tablename + "("
	  	  		+ "AgeGroup char(50),"
  	  			+ "Land		integer,"
	  	  		+ "GKZ		integer,"
	  	  		+ "Name		char(50),"
	  	  		+ "Raumkategorie	integer";
	  	for (int year = 2009 ; year <= 2040; year++){
	  		sql = sql + ", year" + year + " integer";
	  	}
	  	sql = sql + ")";
  	  	System.out.println(sql);
  	  	stmt.executeUpdate(sql);
  	  	
  	  	// ArrayList including all ageGroups that are used in the bbsr forecast
		// 0-10 also exists but provides the same values as 0-5 + 5-10 . It is
		// not being imported
		ArrayList<String> ageGroupsArrayList = new ArrayList<String>();
		// list all tables in schema and add them to ArrayList ageGroupArrayList
		DatabaseMetaData md = c.getMetaData();
		
//		BBSR Data:
		rs = md.getTables(null, schema.replace(".", ""), null, new String[] { "TABLE" });
		while (rs.next()) {
			if (rs.getString(3).startsWith("agegroup") && !rs.getString(3).contains("z")){
				ageGroupsArrayList.add(rs.getString(3));
			}
		}	 	  	
  	  	
  	  	String columns = "Land, GKZ, Name";
  	  	for (int year=2009;year<=2040;year++) {
  	  		columns = columns + ", year" + year;
  	  	}
  	  	
  	  	for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
  	  		sql = 	"INSERT INTO " + schema + tablename 
  	  				+ " (" + columns + ", ageGroup)"
  	  				+ " SELECT " + columns + ",'" + ageGroupsArrayList.get(ii) + "'"
  	  				+ " FROM " + schema + ageGroupsArrayList.get(ii);
  	  		System.out.println(sql);
  	  		stmt.execute(sql);
  	  	}
  	  	
//  	year Data:
  	  	ageGroupsArrayList.clear();
		rs = md.getTables(null, schema.replace(".", ""), null, new String[] { "TABLE" });
		while (rs.next()) {
			if (rs.getString(3).startsWith("agegroup") && rs.getString(3).contains("z")){
				ageGroupsArrayList.add(rs.getString(3));
			}
		}
  	  	for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
  	  		if (ageGroupsArrayList.get(ii).contains("00to05") || ageGroupsArrayList.get(ii).contains("05to10") || ageGroupsArrayList.get(ii).contains("75to85") || ageGroupsArrayList.get(ii).contains("85to101")){	
  	  			sql = 	"INSERT INTO " + schema + tablename 
  	  	  				+ " (land, gkz, name, raumkategorie, year2010, year2011, year2012, ageGroup)"
  	  	  				+ " SELECT land, gkz, name, raumkategorie, year2010, year2011, year2012, '" + ageGroupsArrayList.get(ii) + "'"
  	  	  				+ " FROM " + schema + ageGroupsArrayList.get(ii);
  	  		} else {
  	  			sql = 	"INSERT INTO " + schema + tablename 
	  	  				+ " (land, gkz, name, raumkategorie, year2010, year2011, year2012, year2013, ageGroup)"
	  	  				+ " SELECT land, gkz, name, raumkategorie, year2010, year2011, year2012, year2013, '" + ageGroupsArrayList.get(ii) + "'"
	  	  				+ " FROM " + schema + ageGroupsArrayList.get(ii);
  	  		}
  	  		System.out.println(sql);
  	  		stmt.execute(sql);
  	  	}
  	  	
	}
}
