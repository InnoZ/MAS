package com.innoz.toolbox.populationForecast;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class RunCalculation {
	
	static int calcYear = 2040;
	static int gkz = 11000;
	static String schema = "bbsrprognose.";
	static String calcTable = schema + "aaagkz" + gkz + "year" + calcYear;
	static String migrationTable = "migration";
	static String migrationScenario = "1";
	static String migrationByAgeGroupTable = "migrationbyagegroup";
	static String migrationByClusterTable = "migrationbycluster";
	static String deathsTable = "deathsbyagegroup";
	static double boyQuotient = 0.513;
	static double totalFertilityRate = 1.5;
	
	static Connection con = null;
	static Statement st = null;

	public static void main(String[] args) throws IOException, SQLException {

		// connect to postgreSQL database
		
		ResultSet rs = null;
		String sql;

		String url = "jdbc:postgresql://localhost/mydb";
		String user = "postgres";
		String password = "postgres";

		con = DriverManager.getConnection(url, user, password);
		st = con.createStatement();
		
		// ArrayList including all ageGroups that are used in the bbsr forecast
		// 0-10 also exists but provides the same values as 0-5 + 5-10 . It is
		// not being imported
		ArrayList<String> ageGroupsArrayList = new ArrayList<String>();
		// list all tables in schema and add them to ArrayList ageGroupArrayList
		DatabaseMetaData md = con.getMetaData();
		rs = md.getTables(null, schema.replace(".", ""), null, new String[] { "TABLE" });
		while (rs.next()) {
			if (rs.getString(3).startsWith("agegroup") && !rs.getString(3).contains("00to10") && !rs.getString(3).contains("75to101")){
				ageGroupsArrayList.add(rs.getString(3));
			}
		}	
		
//		System.out.println("Number of ageGroups:  " + ageGroupsArrayList.size());
//		System.out.println(ageGroupsArrayList);
		
//		Create calculation table
		sql = "DROP TABLE IF EXISTS " + calcTable ;
  	  	st.executeUpdate(sql);
		System.out.println(sql);
		sql = "CREATE TABLE " + calcTable + " (agegroup       varchar(50)";
		for (int year = 2000 ; year <= calcYear ; year++){
			sql = sql + ", year" + year + " integer";
		}
		sql =	sql + ")";	
		System.out.println(sql);
		st.executeUpdate(sql);

//		adds a row for each agegroupZ to the calctable
		for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
			if (ageGroupsArrayList.get(ii).contains("z")){
				sql = "INSERT INTO " + calcTable + "(agegroup) VALUES ('" + ageGroupsArrayList.get(ii) + "')";
//				System.out.println(sql);
				st.execute(sql);
			}
		}
		
		int blcluster = getBLCluster();
		int raumkategorie = getRaumkategorie();
		
//		Distribution of migration by population of 2013 for each gkz within its cluster and raumKategorie
		double migrationFactorByBLCluster 	= getMigrationFactorByBLCluster(blcluster, raumkategorie);
		double migrationFactorWithinCluster = getMigrationFactorWithinCluster(blcluster, raumkategorie, ageGroupsArrayList) ;
		System.out.println("blcluster: " + blcluster + " Kreistyp: " + raumkategorie + " migrationFactorWithinCluster: " + migrationFactorWithinCluster); 	
  		
		int pop = 0;
		HashMap<String, Integer> kohortMap = new HashMap<String, Integer>();
		
//  	Calculation
		for (int year = 2013; year <= calcYear; year++){
			int migrationYear = getMigration(year); 
  			for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
  				String ageGroup = ageGroupsArrayList.get(ii);
  				String ageGroupBBSR = ageGroup.replace("z", "");
  				if (ageGroup.contains("z")){
  					
  					if (year == 2013){
  						
//  					ageGroups 00to05 and 05to10 are standardized by 00to10
  						if ( year == 2013 && (ageGroup.contains("z00to05") || ageGroup.contains("z05to10"))){
  							int popBefore 				= getPopFromSQL(year - 1, ageGroup);
  							int pop00to10;
  							int pop00to10Before;
  							if (ageGroup.endsWith("m")){
  	  							pop00to10 				= getPopFromSQL(year, "agegroupz00to10m");
  	  							pop00to10Before 			= getPopFromSQL(year - 1, "agegroupz00to10m");
  							}
  	  						else {
  	  							pop00to10 				= getPopFromSQL(year, "agegroupz00to10w");
  	  							pop00to10Before 			= getPopFromSQL(year - 1, "agegroupz00to10w");
  							}
//  						Is there a nicer way to do this?
//  							System.out.println(ageGroup + " " + popBefore + " * " + pop00to10  + " / " + pop00to10Before + " = " + pop);
  							pop = (int) Math.round((popBefore * 1.0 * pop00to10 / pop00to10Before));
  						}
  						else {  							
//  						ageGroups 75to85 and 85to101 are standardized by 75to101
  							if ( year == 2013 && (ageGroup.contains("z75to85") || ageGroup.contains("z85to101"))){
	  							int popBefore 			= getPopFromSQL(year - 1, ageGroup);
	  							int pop75to101;
	  							int pop75to101Before;
	  							if (ageGroup.endsWith("m")){
	  	  							pop75to101 			= getPopFromSQL(year, "agegroupz75to101m");
	  	  							pop75to101Before 	= getPopFromSQL(year - 1, "agegroupz75to101m");
	  							}
	  	  						else {
	  	  							pop75to101 			= getPopFromSQL(year, "agegroupz75to101w");
	  	  							pop75to101Before 	= getPopFromSQL(year - 1, "agegroupz75to101w");
	  							}
	  							pop = (int) Math.round((popBefore * 1.0 * pop75to101 / pop75to101Before));
  							}
  							else {
  				  				pop = getPopFromSQL(year, ageGroup);
  	  						}
  						}
  							
  					}
  					
//  				Not finished yet. deaths missing
  					else {
//  						System.out.println(kohortMap);
  						int bbsr = getPopFromSQL(year, ageGroupBBSR);
  						double innoZvorl;
  						int newBornKids = 0;
  						int kohortMovement = 0;
  						int diffInnoZBBSR 	= (int) Math.round(getPopFromCalculation(year - 1, ageGroup) - getPopFromSQL(year - 1, ageGroupBBSR));
//  					asumed that people older 55 start dying with a certain propability
  						double deathRate = 1.0;
  						if (ageGroup.contains("z00to05")){
  							int sumPotentialMothers = 
  									  (getPopFromCalculation(year -1, "agegroupz18to25w") - getPopFromSQL(year -1, "agegroup18to25w")) 
  									+ (getPopFromCalculation(year -1, "agegroupz25to35w") - getPopFromSQL(year -1, "agegroup25to35w")) 
  									+ (getPopFromCalculation(year -1, "agegroupz35to45w") - getPopFromSQL(year -1, "agegroup35to45w"));		
  							if (ageGroup.endsWith("m")){
  								newBornKids = (int) Math.round(sumPotentialMothers * boyQuotient * totalFertilityRate / (45 - 18));
  							}
  							else {
  								newBornKids = (int) Math.round(sumPotentialMothers * (1 - boyQuotient) * totalFertilityRate / (45 - 18));
  							}
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 5.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 4.0 / 5.0);
  						}
  						if (ageGroup.contains("z05to10")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z05to10", "z00to05"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 5.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 4.0 / 5.0);
  						}
  						if (ageGroup.contains("z10to18")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z10to18", "z05to10"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 8.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 7.0 / 8.0);
  						}
  						if (ageGroup.contains("z18to25")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z18to25", "z10to18"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 7.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 6.0 / 7.0);
  						}
  						if (ageGroup.contains("z25to35")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z25to35", "z18to25"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z35to45")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z35to45", "z25to35"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z45to55")){
  							kohortMovement = kohortMap.get(ageGroup.replace("z45to55", "z35to45"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z55to65")){
  							deathRate = getDeathRateFromSQL(ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z55to65", "z45to55"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z65to75")){
  							deathRate = getDeathRateFromSQL(ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z65to75", "z55to65"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z75to85")){
  							deathRate = getDeathRateFromSQL(ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z75to85", "z65to75"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z85to101")){
  							deathRate = getDeathRateFromSQL(ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z85to101", "z75to85"));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 14.0 / 15.0);
  						}
  						innoZvorl 			= (int) Math.round(bbsr + newBornKids + (diffInnoZBBSR + kohortMovement) * deathRate);
  						double migrationFactorByAgeGroup 	= getMigrationFactorByAgeGroup(ageGroup);
  						int migration = (int) Math.round(migrationYear * migrationFactorByAgeGroup * migrationFactorByBLCluster * migrationFactorWithinCluster );
  						pop = (int) (innoZvorl + migration);
//  						System.out.println("innoZvorl: " + innoZvorl + " bbsr: " + bbsr 
//  								+ " newBornKids: " + newBornKids + " diffInnoZBBSR: " + diffInnoZBBSR 
//  								+ " kohortMovement: " + kohortMovement + " deathRate: " + deathRate
//  								+ " migration: " + migration + " = " + migrationYear + " * " + migrationFactorByAgeGroup + " * " + migrationFactorByBLCluster + " * " + migrationFactorWithinCluster);
  					}
  	  				updateSQL(year, ageGroup, pop);
  				}

  			} 
  			
			printResult(year);
  		}
  	  	sql = "DROP TABLE IF EXISTS " + calcTable ;
  	  	st.executeUpdate(sql);
  	  	System.out.println("_____________________________________________________________________");
  	  	System.out.println(calcTable + " dropped successfully");
		con.close();
	}
	
//	gets the migration data for the population forecast. raw data needs to be updated. so far only 2014 and 2015 implemented
	private static int getMigration(int year) throws SQLException {
		String sql = "SELECT year" + year + " FROM " + schema + migrationTable + " WHERE scenario = '" + migrationScenario + "'";
		ResultSet rs = st.executeQuery(sql);
//		System.out.println(sql);
		int migrationYear = 0;
		while (rs.next()){
			migrationYear = rs.getInt(1);
		}
		return migrationYear;
	}

//	deathRate is actually the survival rate telling the ratio of people of an ageGroup that will still be allive in the following year
	private static double getDeathRateFromSQL(String ageGroup, int year) throws SQLException {
		int ageGroupMaxYear = Integer.parseInt(ageGroup.substring(13, 15));
		int deathRateCalcYear = year - ageGroupMaxYear;
		String deathRateCalcYearColumn = "";
		if (deathRateCalcYear >= 1987){
			deathRateCalcYearColumn = "year1987";
		} else if (deathRateCalcYear >= 1971){
			deathRateCalcYearColumn = "year1971";
		} else if (deathRateCalcYear >= 1961){
			deathRateCalcYearColumn = "year1961";
		} else if (deathRateCalcYear >= 1950){
			deathRateCalcYearColumn = "year1950";
		} else if (deathRateCalcYear >= 1933){
			deathRateCalcYearColumn = "year1933";
		} else {
			deathRateCalcYearColumn = "year1925";			
		}
		double deathRate = 1;
		String sql = "SELECT " + deathRateCalcYearColumn + " FROM " + schema + deathsTable + " WHERE agegroup= '" + ageGroup + "'  ";
		ResultSet rs = st.executeQuery(sql);
//		System.out.println(sql);
		while (rs.next()){
			deathRate = rs.getDouble(1);
		}
		return deathRate;
	}
	
//	gets the Raumkategorie of a certain gkz
	private static int getRaumkategorie() throws SQLException {
		String sql = "SELECT raumkategorie FROM " + schema + "agegroupz00to05m WHERE gkz= '" + gkz + "'  ";
//		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		int raumkategorie = 0;
		while (rs.next()){
			raumkategorie = rs.getInt(1);
		}
		return raumkategorie;
	}

	private static int getBLCluster() throws SQLException {
		String sql = "SELECT land FROM " + schema + "agegroupz00to05m WHERE gkz= '" + gkz + "'  ";
	//	System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		int blcluster = 0;
		while (rs.next()){
			blcluster = rs.getInt(1);
		}
	//	Schleswig-Holstein, Niedersachsen and Bremen are categorized by 3
		if (blcluster == 1 || blcluster == 3 || blcluster == 4){
			blcluster = 3;
		}
	//	Rheinland-Pfalz and Saarland are categorized by 7
		if (blcluster == 7 || blcluster == 10){
			blcluster = 7;
		}
	//	Brandenburg and Mecklenburg-Vorpommern are categorized by 12
		if (blcluster == 12 || blcluster == 13){
			blcluster = 12;
		}
	//	Sachsen, Sachsen-Anhalt and Thüringen are categorized by 14
		if (blcluster == 14 || blcluster == 15 || blcluster == 16){
			blcluster = 14;
		}
			return blcluster;
	}

//	gets last year's calculated value
	private static int getPopFromCalculation (int year, String ageGroup) throws SQLException{
		String sql = "SELECT year" + (year) + " FROM " + calcTable + " "
				+ "WHERE agegroup= '" + ageGroup + "'  ";
//		System.out.println(ageGroup);
		ResultSet rs = st.executeQuery(sql);
		int pop = 0;
		while (rs.next()){
				pop = rs.getInt(1);
//				System.out.println(pop); 
			}
		return pop;
	}
	
//	gets the population value with a certain year, ageGroup, and gkz
	private static int getPopFromSQL (int year, String ageGroup) throws SQLException{
		String sql = "SELECT year" + year + " FROM " + schema + ageGroup + " "
				+ "WHERE gkz=" + gkz + "  ";
//		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		int pop = 0;
		while (rs.next()){
				pop = rs.getInt(1);
//				System.out.println("getPopFromSQL: " + year + " " + ageGroup + " " + pop); 
			}
		return pop;
	}
	
//	gets the agegroup's amount of all movements as a factor 
	private static double getMigrationFactorByAgeGroup (String ageGroup) throws SQLException{
		String sql = "SELECT agegroupfactor FROM " + schema + "migrationByAgeGroup "
				+ "WHERE agegroup= '" + ageGroup + "' ";
//		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		double migrationFactorByAgeGroup = 0;
		while (rs.next()){
			migrationFactorByAgeGroup = rs.getDouble(1);
			}
		return migrationFactorByAgeGroup;
	}
	
	private static double getMigrationFactorByBLCluster(int blcluster, int raumKategorie) throws SQLException {
		double kreistyp;
		if (raumKategorie == 3 || raumKategorie == 4){
			kreistyp = 3.5;
		} else {
			kreistyp = raumKategorie;
		}
		String sql = "SELECT migration FROM " + schema + "migrationbycluster "
				+ "WHERE cluster= '" + blcluster + "'  AND kreistyp= '" + kreistyp + "' ";
//		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		double migrationFactorByBLCluster = 0;
		while (rs.next()){
			migrationFactorByBLCluster = rs.getDouble(1);
			}
		return migrationFactorByBLCluster;
	}

	private static void updateSQL(int year, String ageGroup, int pop) throws SQLException{
		String sql = "UPDATE " + calcTable + " SET year" + year + " = " + pop + " "
				+ "WHERE agegroup = '" + ageGroup + "'";
		st.execute(sql);
//		System.out.println(sql);
	}
	
	private static void printResult(int year) throws SQLException{
//		result
		System.out.println("_____________________________________________________________________");
		System.out.println(year);
		String sql = "SELECT agegroup, year" + year + " FROM " + calcTable;
		ResultSet rs = st.executeQuery(sql);
		int pop = 0;
		while (rs.next()){
			System.out.println(rs.getString(1) + " " + rs.getInt(2));;		
		}
		sql = "SELECT SUM(year" + year + ") FROM " + calcTable;
		rs = st.executeQuery(sql);
		pop = 0;
		while (rs.next()){
			pop = rs.getInt(1);		
		}
		System.out.println(year + ": " + pop + " inhabitants");
	}
	
	private static double getMigrationFactorWithinCluster(int blcluster, int raumKategorie, ArrayList<String> ageGroupsArrayList) throws SQLException{
		String sql;
		int pop1 = 0;
		int pop2 = 0;
		String conditionBLCluster;
		switch (blcluster) {
			case 3:		conditionBLCluster = " land = 1 OR land = 3";
					break;
			case 7:		conditionBLCluster = " land = 7 OR land = 10";
					break;
			case 12:	conditionBLCluster = " land = 12 OR land = 13";
					break;
			case 14:	conditionBLCluster = " land = 14 OR land = 15 OR land = 16";
					break;
			default: 	conditionBLCluster = " land = " + blcluster;
		}
		String conditionRaumKategorie = " raumkategorie = " + raumKategorie;
		if (raumKategorie == 3 || raumKategorie == 4){
			conditionRaumKategorie = " raumkategorie = 3 OR raumkategorie = 4 ";
		}
		
		for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
			String ageGroup = ageGroupsArrayList.get(ii);
			if ( ageGroupsArrayList.get(ii).contains("z")){
				sql = "SELECT SUM(year2012) FROM " + schema + ageGroup + " WHERE (" + conditionBLCluster + ") AND (" + conditionRaumKategorie + ")";
//				System.out.println(sql);
				ResultSet rs = st.executeQuery(sql);
				int result = 0;
				while (rs.next()){
					result = rs.getInt(1);
				}
				pop1 = pop1 + result;
				
				sql = "SELECT year2012 FROM " + schema + ageGroup + " WHERE gkz = " + gkz;
//				System.out.println(sql);
				rs = st.executeQuery(sql);
				result = 0;
				while (rs.next()){
					result = rs.getInt(1);
				}
				pop2 = pop2 + result;
			}
		}
		double migrationFactorWithinCluster = pop2 * 1.0 / pop1;
		System.out.println("migrationFactorWithinCluster: " + pop2 + " / " + pop1 + " = " + migrationFactorWithinCluster);
		return migrationFactorWithinCluster;
	}
	
}

