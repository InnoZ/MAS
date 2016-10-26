package com.innoz.toolbox.populationForecast;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RunCalculation {
	
	static int calcYear = 2014;
	static int gkz = 1001;
	static String schema = "bbsrprognose.";
	static String calcTable = schema + "aaagkz" + gkz + "year" + calcYear;
	static double boyQuotient = 0.513;
	static double totalFertilityRate = 1.5;
	static int migration2014 = 250000;
	
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
		rs = md.getTables(null, "bbsrprognose", null, new String[] { "TABLE" });
		while (rs.next()) {
			if (rs.getString(3).startsWith("agegroup") && !rs.getString(3).contains("0to10") && !rs.getString(3).contains("75to101")){
				ageGroupsArrayList.add(rs.getString(3));
			}
		}		
		System.out.println("Number of ageGroups:  " + ageGroupsArrayList.size());
		
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
		
		// get blcluster
		sql = "SELECT land FROM " + schema + "agegroupz0to5m WHERE gkz= '" + gkz + "'  ";
//		System.out.println(sql);
		rs = st.executeQuery(sql);
		int blcluster = 0;
		while (rs.next()){
			blcluster = rs.getInt(1);
		}
//		Schleswig-Holstein, Niedersachsen and Bremen are categorized by 3
		if (blcluster == 1 || blcluster == 3 || blcluster == 4){
			blcluster = 3;
		}
//		Rheinland-Pfalz and Saarland are categorized by 7
		if (blcluster == 7 || blcluster == 10){
			blcluster = 7;
		}
//		Brandenburg and Mecklenburg-Vorpommern are categorized by 12
		if (blcluster == 12 || blcluster == 13){
			blcluster = 12;
		}
//		Sachsen, Sachsen-Anhalt and Thüringen are categorized by 14
		if (blcluster == 14 || blcluster == 15 || blcluster == 16){
			blcluster = 14;
		}
		sql = "SELECT raumkategorie FROM " + schema + "agegroupz0to5m WHERE gkz= '" + gkz + "'  ";
//		System.out.println(sql);
		rs = st.executeQuery(sql);
		int raumKategorie = 0;
		while (rs.next()){
			raumKategorie = rs.getInt(1);
		}
		
//		Distribution of migration by population of 2013 for each gkz within its cluster and raumKategorie
		double migrationFactorWithinCluster = getMigrationFactorWithinCluster(blcluster, raumKategorie, ageGroupsArrayList) ;
		System.out.println("blcluster: " + blcluster + " Kreistyp: " + raumKategorie + " migrationFactorWithinCluster: " + migrationFactorWithinCluster); 	
  		
		int pop = 0;
		
		
//  	Calculation
		for (int year = 2013; year <= calcYear; year++){
  			for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
  				String ageGroup = ageGroupsArrayList.get(ii);
  				String ageGroupBBSR = ageGroup.replace("z", "");
  				if (ageGroup.contains("z")){
  					
  					if (year >= 2009 && year <=2013){
  						
//  					ageGroups 0to5 and 5to10 are standardized by 0to10
  						if ( year == 2013 && (ageGroup.contains("z0to5") || ageGroup.contains("z5to10"))){
  							int popBefore 				= getPopFromSQL(year - 1, ageGroup);
  							int pop0to10;
  							int pop0to10Before;
  							if (ageGroup.endsWith("m")){
  	  							pop0to10 				= getPopFromSQL(year, "agegroupz0to10m");
  	  							pop0to10Before 			= getPopFromSQL(year - 1, "agegroupz0to10m");
  							}
  	  						else {
  	  							pop0to10 				= getPopFromSQL(year, "agegroupz0to10w");
  	  							pop0to10Before 			= getPopFromSQL(year - 1, "agegroupz0to10w");
  							}
//  						Is there a nicer way to do this?
  							pop = (int) Math.round((popBefore * 1.0 * pop0to10 / pop0to10Before));
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
	//  						Is there a nicer way to do this?
	  							pop = (int) Math.round((popBefore * 1.0 * pop75to101 / pop75to101Before));
  							}
  							else {
  				  				pop = getPopFromSQL(year, ageGroup);
  	  						}
  						}
  							
  					}
  					
//  				Not finished yet
  					if (year >= 2014){
//  						DC4 + (S$475*$'0-101'.BY4)
//  						s475 = S476*$'0-101'.S$488 
//  						s476 = =$Wanderung_AltersG.$AG$9
//  						'0-101'.BY4=(R4/R$493)*$W_Anteile_BLblcluster.$O$491
//  						
  						int bbsr = getPopFromSQL(year, ageGroupBBSR);
  						double innoZvorl;
  						int newBornKids = 0;
  						if (ageGroup.contains("0to5")){
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
  						}
  						int diffInnoZBBSR 	= (int) Math.round(getPopFromCalculation(year - 1, ageGroup) - getPopFromSQL(year - 1, ageGroupBBSR));
  						innoZvorl 			= (int) Math.round(bbsr + newBornKids + (diffInnoZBBSR * 4.0 / 5.0));
//  						System.out.println(year + " " + ageGroup + " pop: " + pop + " diffInnoZBBSR: " + diffInnoZBBSR + " newBornKids: " + newBornKids + " innoZvorl: " + innoZvorl + " lastCalcPop: " + getPopFromCalculation(year, ageGroup, st) + " popBBSR: " + bbsr + " bbsrold: " + getPopFromSQL(year - 1, ageGroupBBSR, st));
//  						adding latest foreign movements (S$475*$'0-101'.BY4)
//  						'0-101'.BY4 = (R4/R$493)*$W_Anteile_BLblcluster.$O$491
  						double migrationFactorByAgeGroup 	= getMigrationFactorByAgeGroup(ageGroupBBSR);
  						double migrationFactorByBLCluster 	= getMigrationFactorByBLCluster(blcluster, raumKategorie);
  						int migration = (int) Math.round(migration2014 * migrationFactorByAgeGroup * migrationFactorByBLCluster * migrationFactorWithinCluster );
  						pop = (int) (innoZvorl + migration);
  					}
  	  				updateSQL(year, ageGroup, pop);
  				}

  			} 
  			
//  			Check if the calculation works
			pop = getPopSumOfYear(year);
			System.out.println(year + " : " + pop);
			System.out.println("_____________________________________________________________________");
  		}
  		
//  	  	sql = "DROP TABLE IF EXISTS " + calcTable ;
//  	  	st.executeUpdate(sql);
		con.close();
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
//				System.out.println(pop); 
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
		System.out.println(sql); 
	}
	
	private static int getPopSumOfYear(int year) throws SQLException{
		String sql = "SELECT SUM(year" + year + ") FROM " + calcTable;
		ResultSet rs = st.executeQuery(sql);
		int pop = 0;
		while (rs.next()){
			pop = rs.getInt(1);		
		}
		return pop;
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
			if ( ageGroupsArrayList.get(ii).contains("z")){
				sql = "SELECT SUM(year2012) FROM " + schema + ageGroupsArrayList.get(ii) + " WHERE (" + conditionBLCluster + ") AND (" + conditionRaumKategorie + ")";
//				System.out.println(sql);
				ResultSet rs = st.executeQuery(sql);
				int result = 0;
				while (rs.next()){
					result = rs.getInt(1);
				}
				pop1 = pop1 + result;
				
				sql = "SELECT year2012 FROM " + schema + ageGroupsArrayList.get(ii) + " WHERE gkz = " + gkz;
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

