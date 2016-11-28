package com.innoz.toolbox.populationForecast;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.innoz.toolbox.config.SshConnector;

public class RunCalculationWithLessQueries {
	
	static int calcYear = 2040;
	static int gkz = 11000;
	static String migrationScenario = "1";
	static String schema = "bbsrprognose.";
	static String populationDataTable = "populationdata";
	static String migrationTable = "migration";
	static String migrationByAgeGroupTable = "migrationbyagegroup";
	static String migrationByClusterTable = "migrationbycluster";
	static String mortalityTable = "deathsbyagegroup";
	static double totalFertilityRate = 1.5;
	static double boyQuotient = 0.513 * totalFertilityRate / (45 - 18);
	static double girlQuotient = (1 - 0.513)  * totalFertilityRate / (45 - 18);
	
	static Connection con = null;
	static Statement st = null;

	public static void main(String[] args) throws IOException, SQLException, Exception {

		// connect to postgreSQL database
		int localPort = 3200;
		String url = "jdbc:postgresql://localhost:"+localPort+"/population";
		
//		your mobility database username and password:
		String user = "";
		String password = "";
		
		SshConnector.connect(user, password, localPort, 5432);
		con = DriverManager.getConnection(url, user, password);
		st = con.createStatement();
		
		ResultSet rs;
		// ArrayList including all ageGroups that are used in the bbsr forecast
		// 0-10 also exists but provides the same values as 0-5 + 5-10 . It is
		// not being imported
		ArrayList<String> ageGroupsArrayList = new ArrayList<String>();
		String sql = "SELECT distinct agegroup FROM " + schema + populationDataTable;
		rs = st.executeQuery(sql);
		while (rs.next()) {
			if (rs.getString(1).startsWith("agegroup") && !rs.getString(1).contains("00to10") && !rs.getString(1).contains("75to101")){
				ageGroupsArrayList.add(rs.getString(1));
			}
		}
	    Collections.sort(ageGroupsArrayList, new Comparator<String>() {
	        @Override
	        public int compare(String s1, String s2) {
	            return s1.compareToIgnoreCase(s2);
	        }
	    });
		rs.close();

//		Create calculation map
		Map<String, HashMap<String, Integer>> calcMap = new HashMap<String, HashMap<String,Integer>>();
		for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
			HashMap<String,Integer> row = new HashMap<String, Integer>();
			calcMap.put(ageGroupsArrayList.get(ii), row);
		}
		
//		populationData: converts the ResultSet into a Map of Maps
		Map<String,HashMap<String, Object>> popMap = createMapFromSQL(populationDataTable, " WHERE gkz=" + gkz , "ageGroup");
		
//		migration: converts the ResultSet into a Map of Maps
		Map<String,HashMap<String,Object>> migrationMap = createMapFromSQL(migrationTable, " WHERE scenario='" + migrationScenario + "'", "scenario");

//		migrationFactorByAgeGroup: converts the ResultSet into a Map of Maps
		Map<String,HashMap<String,Object>> migrationFactorByAgeGroupMap = createMapFromSQL(migrationByAgeGroupTable, "", "ageGroup");
	
//		mortality: converts the ResultSet into a Map of Maps
		Map<String,HashMap<String,Object>> mortalityMap = createMapFromSQL(mortalityTable, "", "ageGroup");
		
//		Distribution of migration by population of 2013 for each gkz within its cluster and raumKategorie
		int blcluster = getBLCluster();
		int raumkategorie = getRaumkategorie();
		double migrationFactorByBLCluster 	= getMigrationFactorByBLCluster(blcluster, raumkategorie);
		double migrationFactorWithinCluster = getMigrationFactorWithinCluster(blcluster, raumkategorie, ageGroupsArrayList) ;
		System.out.println("blcluster: " + blcluster + " Kreistyp: " + raumkategorie + " migrationFactorWithinCluster: " + migrationFactorWithinCluster); 	
  		
//		kohortMap to put aging of agegroups 
		HashMap<String, Integer> kohortMap = new HashMap<String, Integer>();
		
  	  	st.close();
		con.close();
		SshConnector.disconnect();
		
//  	Calculation
		int pop = 0;
		System.out.println();
		for (int year = 2013; year <= calcYear; year++){
			System.out.println(year);
			int migrationYear = getMigration(migrationMap, year); 
  			for (int ii = 0; ii < ageGroupsArrayList.size(); ii++){
  				String ageGroup = ageGroupsArrayList.get(ii);
  				String ageGroupBBSR = ageGroup.replace("z", "");
  				if (ageGroup.contains("z")){
  					if (year == 2013){
//  					ageGroups 00to05 and 05to10 are standardized by 00to10
  						if ( year == 2013 && (ageGroup.contains("z00to05") || ageGroup.contains("z05to10"))){
  							int popBefore 				= getPopFromBBSR(popMap, year - 1, ageGroup);
  							int pop00to10;
  							int pop00to10Before;
  							if (ageGroup.endsWith("m")){
  	  							pop00to10 				= getPopFromBBSR(popMap, year, "agegroupz00to10m");
  	  							pop00to10Before 			= getPopFromBBSR(popMap, year - 1, "agegroupz00to10m");
  							}
  	  						else {
  	  							pop00to10 				= getPopFromBBSR(popMap, year, "agegroupz00to10w");
  	  							pop00to10Before 			= getPopFromBBSR(popMap, year - 1, "agegroupz00to10w");
  							}
//  						Is there a nicer way to do this?
//  							System.out.println(ageGroup + " " + popBefore + " * " + pop00to10  + " / " + pop00to10Before + " = " + pop);
  							pop = (int) Math.round((popBefore * 1.0 * pop00to10 / pop00to10Before));
  						}
//  					ageGroups 75to85 and 85to101 are standardized by 75to101
  						else if ( year == 2013 && (ageGroup.contains("z75to85") || ageGroup.contains("z85to101"))){							
  							int popBefore 			= getPopFromBBSR(popMap, year - 1, ageGroup);
  							int pop75to101;
  							int pop75to101Before;
  							if (ageGroup.endsWith("m")){
  	  							pop75to101 			= getPopFromBBSR(popMap, year, "agegroupz75to101m");
  	  							pop75to101Before 	= getPopFromBBSR(popMap, year - 1, "agegroupz75to101m");
  							}
  	  						else {
  	  							pop75to101 			= getPopFromBBSR(popMap, year, "agegroupz75to101w");
  	  							pop75to101Before 	= getPopFromBBSR(popMap, year - 1, "agegroupz75to101w");
  							}
  							pop = (int) Math.round((popBefore * 1.0 * pop75to101 / pop75to101Before));
  						}
  						else {
  				  				pop = getPopFromBBSR(popMap, year, ageGroup);
  	  					}	
  					}
  					
  					else {
//  						System.out.println(kohortMap);
  						int bbsr = getPopFromBBSR(popMap, year, ageGroupBBSR);
  						double innoZvorl;
  						int newBornKids = 0;
  						int kohortMovement = 0;
  						int diffInnoZBBSR 	= getPopFromCalcMap(year - 1, ageGroup, calcMap) - getPopFromBBSR(popMap, year - 1, ageGroupBBSR);
//  					asumed that people older 55 start dying with a certain propability
  						double mortalityRate = 1.0;
  						if (ageGroup.contains("z00to05")){
  							int sumPotentialMothers = 
  									  (getPopFromCalcMap(year -1, "agegroupz18to25w", calcMap) - getPopFromBBSR(popMap, year -1, "agegroup18to25w")) 
  									+ (getPopFromCalcMap(year -1, "agegroupz25to35w", calcMap) - getPopFromBBSR(popMap, year -1, "agegroup25to35w")) 
  									+ (getPopFromCalcMap(year -1, "agegroupz35to45w", calcMap) - getPopFromBBSR(popMap, year -1, "agegroup35to45w"));		
  							if (ageGroup.endsWith("m")){
  								newBornKids = (int) Math.round(sumPotentialMothers * boyQuotient);
  							}
  							else {
  								newBornKids = (int) Math.round(sumPotentialMothers * girlQuotient);
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
//  							all mortality from previous ages are summarized to the people leaving ageGroup 45to55
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR * getmortality(mortalityMap, ageGroup, year) / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z55to65")){
  							mortalityRate = getmortality(mortalityMap, ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z55to65", "z45to55"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z65to75")){
  							mortalityRate = getmortality(mortalityMap, ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z65to75", "z55to65"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z75to85")){
  							mortalityRate = getmortality(mortalityMap, ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z75to85", "z65to75"));
  							kohortMap.put(ageGroup, (int)Math.round(diffInnoZBBSR / 10.0));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 9.0 / 10.0);
  						}
  						if (ageGroup.contains("z85to101")){
  							mortalityRate = getmortality(mortalityMap, ageGroup, year);
  							kohortMovement = kohortMap.get(ageGroup.replace("z85to101", "z75to85"));
  							diffInnoZBBSR = (int) Math.round(diffInnoZBBSR * 14.0 / 15.0);
  						}
  						innoZvorl 			= (int) Math.round(bbsr + newBornKids + (diffInnoZBBSR + kohortMovement) * mortalityRate);
  						double migrationFactorByAgeGroup 	= getMigrationFactorByAgeGroup(migrationFactorByAgeGroupMap, ageGroup);
  						int migration = (int) Math.round(migrationYear * migrationFactorByAgeGroup * migrationFactorByBLCluster * migrationFactorWithinCluster );
  						pop = (int) (innoZvorl + migration);
//  						System.out.println("innoZvorl: " + innoZvorl + " bbsr: " + bbsr 
//  								+ " newBornKids: " + newBornKids + " diffInnoZBBSR: " + diffInnoZBBSR 
//  								+ " kohortMovement: " + kohortMovement + " mortalityRate: " + mortalityRate
//  								+ " migration: " + migration + " = " + migrationYear + " * " + migrationFactorByAgeGroup + " * " + migrationFactorByBLCluster + " * " + migrationFactorWithinCluster);
  					}
  	  				updateCalcMap(year, ageGroup, pop, calcMap);
  				}
  			} 		
			printResult(year, calcMap, ageGroupsArrayList);
  		}
	}
	
	private static void updateCalcMap (int year, String ageGroup, int pop, Map<String, HashMap<String, Integer>> calcMap){
		calcMap.get(ageGroup).put("year"+year, pop);
		
	}
	
	private static int getPopFromCalcMap (int year, String ageGroup, Map<String, HashMap<String, Integer>> calcMap) {
		int pop;
		try {
			pop = (int) calcMap.get(ageGroup).get("year" + year);
		} catch (NullPointerException getPopFromCalcException){
			System.out.println("calcMap not woring!!!");
			pop = 0;
		}
		return pop;
	}
	
//	gets the population value with a certain year, ageGroup, and gkz
	private static int getPopFromBBSR (Map<String,HashMap<String,Object>> popMap, int year, String ageGroup){
		int pop;
		try {
			pop = (int) popMap.get(ageGroup).get("year" + year);
		} catch (NullPointerException getPopFromBBSRException){
			System.out.println("calcMap not woring!!!");
			pop = 0;
		}
		return pop;
	}
	
	private static void printResult(int year, Map<String, HashMap<String, Integer>> calcMap, ArrayList<String> ageGroupsArrayList) throws SQLException{
		int pop = 0;
//		from Map
		for (int ii = 0 ; ii < ageGroupsArrayList.size() ; ii++){
			if (ageGroupsArrayList.get(ii).contains("z")){
				System.out.println(ageGroupsArrayList.get(ii) + ": " + calcMap.get(ageGroupsArrayList.get(ii)).get("year" + year));
				pop = pop +  calcMap.get(ageGroupsArrayList.get(ii)).get("year" + year); 
			}
		}
		System.out.println(year + ": " + pop + " inhabitants");
		System.out.println("_____________________________________________________________________");
	}
	
//	gets the migration data for the population forecast. raw data needs to be updated. so far only 2014 and 2015 implemented
	private static int getMigration(Map<String,HashMap<String,Object>> migrationMap, int year){
		int migrationYear;
		try {
			migrationYear = (int) migrationMap.get(migrationScenario).get("year" + year);
		} catch (NullPointerException getMigrationException){
			migrationYear = 0;
		}
		return migrationYear;
	}

//	mortalityRate is actually the survival rate telling the ratio of people of an ageGroup that will still be allive in the following year
	private static double getmortality(Map<String,HashMap<String,Object>> mortalityMap, String ageGroup, int year){
		int ageGroupMaxYear = Integer.parseInt(ageGroup.substring(13, 15));
		int mortalityRateCalcYear = year - ageGroupMaxYear;
		String mortalityRateCalcYearColumn = "";
		if (mortalityRateCalcYear >= 1987){
			mortalityRateCalcYearColumn = "year1987";
		} else if (mortalityRateCalcYear >= 1971){
			mortalityRateCalcYearColumn = "year1971";
		} else if (mortalityRateCalcYear >= 1961){
			mortalityRateCalcYearColumn = "year1961";
		} else if (mortalityRateCalcYear >= 1950){
			mortalityRateCalcYearColumn = "year1950";
		} else if (mortalityRateCalcYear >= 1933){
			mortalityRateCalcYearColumn = "year1933";
		} else {
			mortalityRateCalcYearColumn = "year1925";			
		}
		double mortalityRate;
		try {
			mortalityRate = ((BigDecimal) mortalityMap.get(ageGroup).get(mortalityRateCalcYearColumn)).doubleValue();
		} catch (NullPointerException getmortalityException){
			mortalityRate = 1.0;
		}
		return mortalityRate;
	}
	
//	gets the agegroup's amount of all movements as a factor 
	private static double getMigrationFactorByAgeGroup(Map<String,HashMap<String,Object>> migrationFactorByAgeGroupMap, String ageGroup){
		double migrationFactorByAgeGroup;
		try {
			migrationFactorByAgeGroup = ((BigDecimal) migrationFactorByAgeGroupMap.get(ageGroup).get("agegroupfactor")).doubleValue();
		} catch (NullPointerException getMigrationFactorByAgeGroupException){
			migrationFactorByAgeGroup = 0;
		}
		return migrationFactorByAgeGroup;
	}
	
//	gets the Raumkategorie of a certain gkz
	private static int getRaumkategorie() throws SQLException {
		String sql = "SELECT raumkategorie FROM " + schema + populationDataTable + " WHERE agegroup='agegroupz00to05m' AND gkz= '" + gkz + "'  ";
//		System.out.println(sql);
		ResultSet rs = st.executeQuery(sql);
		int raumkategorie = 0;
		while (rs.next()){
			raumkategorie = rs.getInt(1);
		}
//		System.out.println("Raumkategorie: " + raumkategorie);
		return raumkategorie;
	}

	private static int getBLCluster() throws SQLException {
		String sql = "SELECT land FROM " + schema + populationDataTable + " WHERE agegroup='agegroupz00to05m' AND gkz= '" + gkz + "'  ";
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
//		System.out.println("Bundeslandcluster: " + blcluster);
		return blcluster;
	}
	
//	gets the migrationFactor by blcluster and raumKategorie
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
		sql = "SELECT SUM(year2013) "
				+ "FROM " + schema + populationDataTable + " "
				+ "WHERE (" + conditionBLCluster + ") "
					+ "AND (" + conditionRaumKategorie + ") "
					+ "AND agegroup LIKE '%z%' ";
		ResultSet rs = st.executeQuery(sql);
		while (rs.next()){
			pop2 = rs.getInt(1);
		}
		sql = "SELECT SUM(year2013) "
				+ "FROM " + schema + populationDataTable + " "
				+ "WHERE gkz = " + gkz
					+ " AND agegroup LIKE '%z%' ";
		rs = st.executeQuery(sql);
		while (rs.next()){
			pop1 = rs.getInt(1);
		}
		double migrationFactorWithinCluster = pop1 * 1.0 / pop2;		
		System.out.println("migrationFactorWithinCluster: " + pop2 + " / " + pop1 + " = " + migrationFactorWithinCluster);
		return migrationFactorWithinCluster;
	}
	
	private static Map<String,HashMap<String, Object>> createMapFromSQL(String table, String condition, String rowIndex) throws SQLException{
//		populationData: converts the ResultSet into a Map of Maps
		String sql = "SELECT * FROM " + schema + table + condition;
		ResultSet rs = st.executeQuery(sql);
		ResultSetMetaData rsmd = rs.getMetaData();
	    int columns = rsmd.getColumnCount();
		Map<String,HashMap<String, Object>> map = new HashMap<String, HashMap<String,Object>>();
		while (rs.next()) {
	        HashMap<String, Object> row = new HashMap<String, Object>(columns);
	        for(int ii=1; ii<=columns; ++ii) {
	            if (rs.getObject(ii) == null){
	        		row.put(rsmd.getColumnName(ii),0);
	        	} else {
	        		row.put(rsmd.getColumnName(ii), rs.getObject(ii));
	        	}
	        }
	        map.put(rs.getObject(rowIndex).toString(), row);
	    }
		rs.close();
		return map;
	}
}