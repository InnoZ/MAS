package com.innoz.toolbox.utils.misc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.utils.PsqlUtils;
import com.innoz.toolbox.utils.io.AbstractCsvReader;
import com.innoz.toolbox.utils.math.regression.ExpTrendLine;
import com.innoz.toolbox.utils.math.regression.LogTrendLine;
import com.innoz.toolbox.utils.math.regression.PolyTrendLine;
import com.innoz.toolbox.utils.math.regression.PowerTrendLine;
import com.innoz.toolbox.utils.math.regression.TrendLine;
import com.innoz.toolbox.utils.matsim.RecursiveStatsContainer;

public class RilCreateLeastSquares {

	private static final Logger log = Logger.getLogger(RilCreateLeastSquares.class);
	
	static final String RIL_DATABASE = "ril";
	static final int[] CATEGORIES = new int[]{100,300,1000,5000,10000,15000,20000,50000,1000000};

	static final int index2030 = 11;
	
	static final String FV = "fv";
	static final String NV = "nv";
	static final String FVO = "fv_fremd";
	static final String NVO = "nv_fremd";
	static final String GES = "ges";
	static final String STATION = "station";
	static final String YEAR = "verkehrs15";
	static final String TYP = "typ_name";
	static final String STATE= "state";
	
	enum RegMethod {
		
		Exp,
		Linear,
		Log,
		Poly,
		Power
		
	}
	
	static double[] xValues() {
		
		return Arrays.copyOf(new double[]{2006.0,2007.0,2008.0,2009.0,2010.0,2011.0,2012.0,2013.0,2014.0,2015.0,2016.0,2030.0}, 12);
		
	}
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, IOException {
		
		computeStationwise(RegMethod.Power);
		computeAggregates();
		
	}
	
	private static void computeStationwise(RegMethod m) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, IOException {
		
		Map<String, double[]> stationData = new HashMap<>();
		double[] x = xValues();
		
		Map<String, Station> stations2016 = new HashMap<>();

		AbstractCsvReader csv = new AbstractCsvReader("\t",true) {
			
			@Override
			public void handleRow(String[] line) {

				String station = line[0];
				String name = line[1];
				double ges = Double.parseDouble(line[14]);
				double factor = Double.parseDouble(line[18]);
				
				double n = Math.ceil(ges * (1 + factor));
				
				if(!stationData.containsKey(station)) {
					stationData.put(station, new double[x.length]);
					Station s = new Station();
					s.name = name;
					s.id = station;
					stations2016.put(station, s);
				}
				
				stationData.get(station)[index2030] = n;
				
			}
			
		};
		
		csv.read("/home/dhosse/01_Projects/GSP/Änderungsraten_2013-zu-2030_V2_fuer-InnoZ.csv");
		
		log.info("Read " + stationData.size() + " stations");
		
		String path = "/home/dhosse/01_Projects/GSP/Daten/20_Stationsdaten/for_import/";
		
		String[] files = {"Verkehrsdaten_2006_utf8.csv","Verkehrsdaten_2007_utf8.csv","Verkehrsdaten_2008_utf8.csv","Verkehrsdaten_2009_utf8.csv",
				"Verkehrsdaten_2010_utf8.csv","Verkehrsdaten_2011_utf8.csv","Verkehrsdaten_2012_utf8.csv","Verkehrsdaten_2013_utf8.csv",
				"Verkehrsdaten_2014_utf8.csv","Verkehrsdaten_2015_utf8.csv","Verkehrsdaten_2016_utf8.csv"};
		
		int p = 0;
		
		for(String s : files){
			
			int year = p;
			
			AbstractCsvReader reader = new AbstractCsvReader(";",true) {
				
				@Override
				public void handleRow(String[] line) {
					
					String id = line[0];
					String sbnNr = line[2];
					String sbn = line[3];
					String rbNr = line[4];
					String rb = line[5];
					String blNr = line[6];
					String bl = line[7];
					String katVs = line[8];
					String katSe = line[9];
					double n = Double.parseDouble(line[14]);

					if(stationData.containsKey(id)){
						
						stationData.get(id)[year] = n;
						
						Station station = stations2016.get(id);
						station.type = setType(n);
						station.sbn = sbn;
						station.sbnNr = sbnNr;
						station.rb = rb;
						station.rbNr = rbNr;
						station.bl = bl;
						station.blNr = blNr;
						station.katSe = katSe;
						station.katVs = katVs;
						
					} else {
						
//						log.error("Station " + name + " (" + id + ") has no counterpart!");
						
					}
					
				}
				
			};
			
			reader.read(path + s);
			
			p++;
			
		}
		
		Connection c = PsqlAdapter.createConnection(RIL_DATABASE);
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final", "daten_2008_regionstyp_final"
				, "daten_2009_regionstyp_final", "daten_2010_regionstyp_final", "daten_2011_regionstyp_final", "daten_2012_regionstyp_final"
				, "daten_2013_regionstyp_final", "daten_2014_regionstyp_final", "daten_2015_regionstyp_final"
				, "daten_2016_regionstyp_final"};
		
		for(String s : tables) {
			
			String query = new PsqlUtils.PsqlStringBuilder("SELECT", "public", s).variables("nr,station,typ_name").build();
			
			ResultSet result = statement.executeQuery(query);
			
			while(result.next()){

				String id = Integer.toString(result.getInt("nr"));
				String regionType = result.getString("typ_name");
				String stationName = result.getString("station");
				
				Station station = stations2016.get(id);
				
				if(station != null && station.name.equals(stationName)) {
					
					station.regionType = regionType;
					
				}
				
			}
			
			result.close();
			
		}
		
		statement.close();
		c.close();
		
		Set<Station> newStations = new HashSet<>();
		
		for(Station s : stations2016.values()) {
			if(s.regionType == null || s.regionType.equals("")) newStations.add(s);
		}
		Connection geoConnection = PsqlAdapter.createConnection("geodata");
		
		Statement stmt = geoConnection.createStatement();

		for(Station s : newStations) {
			
			String blNr = s.blNr.length() < 2 ? "0" + s.blNr : s.blNr;
			String name = s.name;

			while(s.ags.isEmpty() && name.length() > 0) {
				
				ResultSet set = stmt.executeQuery("SELECT ags,name FROM opengeodb_locations WHERE name LIKE '" + name + "%' AND ags LIKE '" +
							blNr + "%' LIMIT 1;");
				
				while(set.next()) {
					
					s.ags = set.getString("ags");
					
				}
				
				set.close();
				
				name = name.substring(0, name.length() - 1);
				
			}
			
			if(s.ags != null && !s.ags.isEmpty()) {

				ResultSet set2 = stmt.executeQuery("SELECT ags,typ_bez FROM bbsr_region_types WHERE ags LIKE '" + s.ags + "%' OR (gem_name"
						+ " LIKE '" + name + "' AND ags LIKE '" + s.ags.substring(0, 2) + "%');");
				
				while(set2.next()) {
					
					s.regionType = set2.getString("typ_bez");
					
				}
				
				set2.close();
				
			}
			
		}
		
		stmt.close();
		
		geoConnection.close();
		
		AbstractCsvReader ds100Reader = new AbstractCsvReader(";", true) {
			
			@Override
			public void handleRow(String[] line) {
				
				String id = line[2];
				Station station = stations2016.get(id);
				if(station != null) {
					station.ds100 = line[4];
				}
				
			}
		};
		
		ds100Reader.read("/home/dhosse/01_Projects/GSP/Daten/DBSuS-Uebersicht_Bahnhoefe-Stand2016-07.csv");
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/trends_" + m.name() + ".csv");
		out.write("Bhf Nr;Bahnhof;Sbn Nr;Sbn;Rb Nr;Rb;Bl Nr;Bundesland;Kat Vs;Kat Se;DS 100;Raumtyp;Reisendenzahlengruppe;Cluster;Reisende 2006;Reisende 2007;Reisende 2008;"
				+ "Reisende 2009;Reisende 2010;Reisende 2011;Reisende2012;Reisende 2013;Reisende 2014;Reisende 2015;Reisende 2016;Prognose 2026;Prognose 2027;"
				+ "Prognose 2028;Prognose 2029;Prognose 2030;Prognose 2031;Prognose 2032;Bestimmtheitsmaß Prognose");
		out.flush();
		
		for(Entry<String, double[]> entry : stationData.entrySet()) {

			String id = entry.getKey();
			
			Station station = stations2016.get(id);
			
			String regionType = station.regionType != null ? station.regionType : "_";
			String type = station.type != null ? station.type : setType(entry.getValue()[index2030]);
			
			String cluster = "";
			
			if(!regionType.equals("_") && !regionType.equals("")) {
				cluster = regionType + ", " + type; 
			}
			
			double[] y = entry.getValue();
			
			
			
			Map<Double, Double> values = new TreeMap<>();
			Map<Double, Double> origValues = new TreeMap<>();
			
			for(int i = 0; i < x.length; i++) {
				
				origValues.put(x[i], y[i]);
				if(y[i] < 0) {
					origValues.put(x[i], 0d);
				}
				
				if(y[i] > 0) {
					
					values.put(x[i], y[i]);
					
				} else {
					
					if(x[i] == 2030) {
						
						values.put(2030d, 1d);
						
					}
					
				}
				
			}
			
			if(values.size() > 2) {

				if(m.equals(RegMethod.Poly) && values.size() < 10) {
					
					continue;
					
				}
				
				TrendLine t = getRegressionMethod(m);
				
				double[] xValues = new double[values.size()];
				double[] yValues = new double[values.size()];
				
				int i = 0;
				
				for(Entry<Double, Double> valEntry : values.entrySet()) {
					
					xValues[i] = valEntry.getKey();
					yValues[i] = valEntry.getValue();
					i++;
					
				}
				
				t.setValues(yValues, xValues);
				
				appendDataToCsv(out, station, cluster, origValues, t);
				
			}
			
		}
		
		out.flush();
		out.close();
		
	}
	
	static String doubleArrayToString(double[] array) {
		
		StringBuffer b = new StringBuffer();
		
		for(double d : array) {
			
			b.append(Double.toString(d));
			
		}
		
		return b.toString();
		
	}
	
	private static String setType(double nPassengers) {
		
		if(nPassengers <= 100) {
			
			return "unter 100 Fahrgäste";
			
		} else if(nPassengers <= 300) {
			
			return "101 - 300 Fahrgäste";
			
		} else if(nPassengers <= 1001) {
			
			return "301 - 1.000 Fahrgäste";
			
		} else if(nPassengers <= 5000) {
			
			return "1.001 - 5.000 Fahrgäste";
			
		} else if(nPassengers <= 15000) {
			
			return "5.001 - 15.000 Fahrgäste";
			
		} else if(nPassengers <= 20000) {
			
			return "15.001 - 20.000 Fahrgäste";
			
		} else if(nPassengers <= 50000) {
			
			return "20.001 - 50.000 Fahrgäste";
			
		} else {
			
			return "50.000 und mehr Fahrgäste";
			
		}
		
	}
	
	private static TrendLine getRegressionMethod(RegMethod m) {
		
		try {
		
			if(m.equals(RegMethod.Exp)) {
			
				return ExpTrendLine.class.newInstance();

			} else if(m.equals(RegMethod.Linear)) {
				
				return PolyTrendLine.class.newInstance();
				
			} else if(m.equals(RegMethod.Log)) {
				
				return LogTrendLine.class.newInstance();
				
			} else if(m.equals(RegMethod.Power)) {
				
				return PowerTrendLine.class.newInstance();
				
			} else if(m.equals(RegMethod.Poly)) {
				
				return new PolyTrendLine(4);
				
			}
		
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e) {
			
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	private static void appendDataToCsv(BufferedWriter writer, Station station, String type, Map<Double,Double> values, TrendLine t)
			throws IOException {

		writer.newLine();
		
		String[] typeParts = type.split(", ").length > 1 ? type.split(", ") : new String[]{"",""};
		
		writer.write(station.id + ";" + station.name + ";" + station.sbnNr + ";" + station.sbn + ";" + station.rbNr + ";" + station.rb + ";"
				+ station.blNr + ";" + station.bl + ";" + station.katVs + ";" + station.katSe + ";" + station.ds100 + ";" + typeParts[0]
				+ ";" + typeParts[1] + ";" + type);
		
		for(int i = 2006; i < 2017; i++) {
			
			Double d = new Double(i);
			
			if(values.get(d) != null) {

				writer.write(";" + values.get(new Double(i)));

			} else {
				
				writer.write(";0");
				
			}
			
		}
		
		for(int i = 2026; i < 2033; i++) {

			if(i >= 2030) {
				
				if(values.get(2030d) == 0d) {
					
					writer.write(";0");
					
				} else {
					
					writer.write(";" + Math.ceil(t.predict(i)));
					
				}
				
			} else {

				writer.write(";" + Math.ceil(t.predict(i)));
				
			}
			
		}
		
		writer.write(";" + t.getRSquare());
		
		writer.flush();
		
	}
	
	private static void computeAggregates() throws IOException {
	
		Map<String, TreeMap<Integer, RecursiveStatsContainer>> cluster2Passengers = new HashMap<>();
		
		AbstractCsvReader reader = new AbstractCsvReader(";", true) {
			
			@Override
			public void handleRow(String[] line) {
				
				String cluster = line[13];
				
				if(!cluster.isEmpty()) {
					
					if(!cluster2Passengers.containsKey(cluster)) {
						
						cluster2Passengers.put(cluster, new TreeMap<>());
						
					}
					
					Map<Integer, RecursiveStatsContainer> map = cluster2Passengers.get(cluster);
					
					int year = 2006;
					
					for(int i = 14; i < 31; i++) {
						
						if(!map.containsKey(year)) {
							
							map.put(year, new RecursiveStatsContainer());
							
						}
						
						double n = Double.parseDouble(line[i]);
						if(Double.isNaN(n) || n < 0) {
							year++;
							continue;
						}
						
						map.get(year).handleNewEntry(n);
						
						year++;
						
						if(year == 2017) year = 2026;
						
					}
					
				}
				
			}
			
		};
		
		reader.read("/home/dhosse/01_Projects/GSP/Documentation/trends_Power.csv");
		
		writeAvgOutput(cluster2Passengers);
		writeMedianOutput(cluster2Passengers);
	
	}
	
	static void writeAvgOutput(Map<String, TreeMap<Integer, RecursiveStatsContainer>> cluster2Passengers) throws IOException {
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/Trendprognose_Cluster_Mittelwert.csv");
		
		out.write("Cluster;Reisende 2006;Reisende 2007;Reisende 2008;Reisende 2009;Reisende 2010;Reisende 2011;Reisende 2012;Reisende 2013;"
				+ "Reisende 2014;Reisende 2015;Reisende 2016;Prognose 2026;Prognose 2027;Prognose 2028;Prognose 2029;Prognose 2030;Prognose 2031;Prognose 2032");
		
		out.newLine();
		
		for(String cluster : cluster2Passengers.keySet()) {
			
			StringBuffer writeOut = new StringBuffer(cluster);
			
			for(RecursiveStatsContainer container : cluster2Passengers.get(cluster).values()) {
				
				writeOut.append(";");
				writeOut.append(Math.ceil(container.getMean()));
				
			}
			
			out.write(writeOut.toString());
			out.newLine();
			out.flush();
			
		}
		
		out.close();
		
	}
	
	static void writeMedianOutput(Map<String, TreeMap<Integer, RecursiveStatsContainer>> cluster2Passengers) throws IOException {
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/Trendprognose_Cluster_Median.csv");
		
		out.write("Cluster;Reisende 2006;Reisende 2007;Reisende 2008;Reisende 2009;Reisende 2010;Reisende 2011;Reisende 2012;Reisende 2013;"
				+ "Reisende 2014;Reisende 2015;Reisende 2016;Prognose 2026;Prognose 2027;Prognose 2028;Prognose 2029;Prognose 2030;Prognose 2031;Prognose 2032");
		
		out.newLine();
		
		for(String cluster : cluster2Passengers.keySet()) {
			
			StringBuffer writeOut = new StringBuffer(cluster);
			
			for(RecursiveStatsContainer container : cluster2Passengers.get(cluster).values()) {
				
				writeOut.append(";");
				writeOut.append(Math.ceil(container.getMedian()));
				
			}
			
			out.write(writeOut.toString());
			out.newLine();
			out.flush();
			
		}
		
		out.close();
		
	}
	
	static class Station {
		
		String id = "";
		String name = "";
		String sbnNr = "";
		String sbn = "";
		String rbNr = "";
		String rb = "";
		String blNr = "";
		String bl = "";
		String katVs = "";
		String katSe = "";
		String type = "";
		String regionType = "";
		String ags = "";
		String ds100 = "";
		
		private Station(){};
		
	}

}