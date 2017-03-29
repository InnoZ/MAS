package com.innoz.toolbox.utils.misc;

import java.awt.BasicStroke;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.matsim.core.utils.charts.XYScatterChart;
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
		
		computeAggregates(RegMethod.Power);
		computeStationwise(RegMethod.Power);
		
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
//					String name = line[1];
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
			
			ResultSet result = statement.executeQuery(
					PsqlUtils.createSelectStatement("nr,station,typ_name", s));
			
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
		
		Set<String> newStations = new HashSet<>();
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/trends_" + m.name() + ".csv");
		out.write("Bhf Nr;Bahnhof;Sbn Nr;Sbn;Rb Nr;Rb;Bl Nr;Bundesland;Kat Vs;Kat Se;Cluster;Reisende 2006;Reisende 2007;Reisende 2008;"
				+ "Reisende 2009;Reisende 2010;Reisende 2011;Reisende2012;Reisende 2013;Reisende 2014;Reisende 2015;Reisende 2016;Prognose 2027;"
				+ "Prognose 2028;Prognose 2029;Prognose 2030;Prognose 2031;Prognose 2032;Bestimmtheitsmaß Prognose");
//		out.write("station;name;cluster;2016;2027;2028;2029;2030;2031;2032;r2");
		out.flush();
		
		for(Entry<String, double[]> entry : stationData.entrySet()) {

			String id = entry.getKey();
			
			Station station = stations2016.get(id);
			
			String name = station.name;
			
			String regionType = station.regionType != null ? station.regionType : "_";
			String type = station.type != null ? station.type : setType(entry.getValue()[index2030]);
			
			String cluster = regionType + "," + type;
			
			double[] y = entry.getValue();
			
			Set<Integer> indicesToRemove = new HashSet<>();
			
			for(int i = 0; i < x.length; i++) {
				
				if(y[i] == 0) {
					
					indicesToRemove.add(i);
					
				}
				
			}
			
			double[] xValues = new double[x.length-indicesToRemove.size()];
			double[] yValues = new double[y.length-indicesToRemove.size()];
			
			int j = 0;
			
			for(int i = 0; i < x.length; i++) {
				
				if(y[i] != 0) {
					
					xValues[j] = x[i];
					yValues[j] = y[i];
					
					if(yValues[j] < 0) {
						
						// The least value has to be '1' to avoid NaNs during trend line generation
						
						if(j > 0) {
						
							double yBefore = y[j-1];
							
							if(j < y.length - 1) {
								
								double yAfter = y[j+1];
								
								if(yBefore > 0 && yAfter > 0) {

									yValues[j] = (yBefore + yAfter) / 2;
									
								}
								
							} else {
								
								yValues[j] = yBefore;
								
							}
							
						} else {
							
							yValues[j] = 1;
							
						}
						
					}
					
				} else j--;
				
				j++;
				
			}
			
			if(xValues.length > 2) {

				if(m.equals(RegMethod.Poly) && xValues.length < 10) {
					
					continue;
					
				}
				
				TrendLine t = getRegressionMethod(m);
				t.setValues(yValues, xValues);
				
				appendDataToCsv(out, station, cluster, y, t);
				
			} else {
				
				newStations.add(name);
				
			}
			
		}
		
		out.flush();
		out.close();
		
		BufferedWriter out2 = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/newStations.csv");
		out2.write("station");
		for(String name : newStations) {
			out2.newLine();
			out2.write(name);
		}
		out2.flush();
		out2.close();
		
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
	
	private static void appendDataToCsv(BufferedWriter writer, Station station, String type, double[] yValues, TrendLine t) throws IOException {

//		out.write("Bhf Nr;Bahnhof;Sbn Nr;Sbn;Rb Nr;Rb;Bl Nr;Bundesland;Kat Vs;Kat Se;Reisende 2006;Reisende 2007;Reisende 2008;Reisende 2009;"
//				+ "Reisende 2010;Reisende 2011;Reisende2012;Reisende 2013;Reisende 2014;Reisende 2015;Reisende 2016;Prognose 2027;Prognose 2028;"
//				+ "Prognose 2029;Prognose 2030;Prognose 2031;Prognose 2032;Bestimmtheitsmaß Prognose");
		
		writer.newLine();
		
		writer.write(station.id + ";" + station.name + ";" + station.sbnNr + ";" + station.sbn + ";" + station.rbNr + ";" + station.rb + ";"
				+ station.blNr + ";" + station.bl + ";" + station.katVs + ";" + station.katSe + ";" + type);
		
		for(int i = 0; i < 11; i++) {
			
			writer.write(";" + yValues[i]);
			
		}
		
		for(int i = 2027; i < 2033; i++) {
			
			writer.write(";" + Math.ceil(t.predict(i)));
			
		}
		
		writer.write(";" + t.getRSquare());
		
		writer.flush();
		
	}
	
	private static void saveDataAsPng(TrendLine t, double[] xData, double[] yData, String filename) {
		
		double[] x = createXData();
		double[] y = new double[27];
		
		for(int i = 2006; i < 2033; i++) {
			
			y[i-2006] = 0;
			
		}
		
		for(int i = 0; i < yData.length-1; i++) {
			
			y[i] = yData[i];
			
		}
		
		y[24] = yData[yData.length-1];
		
		double[] medianY = new double[27];
		
		for(int i = 2006; i < 2033; i++) {
			
			medianY[i-2006] = t.predict(i);
		}
		
		XYScatterChart chart = new XYScatterChart("", "Jahr", "Anzahl Reisende");
		chart.addSeries("Trend", x, medianY);
		((XYPlot)chart.getChart().getPlot()).setRenderer(0, new XYLineAndShapeRenderer(true,true));
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,true);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(0.0f));
		((XYPlot)chart.getChart().getPlot()).setRenderer(renderer);
		
		chart.addSeries("Rohdaten", x, y);
		((XYPlot)chart.getChart().getPlot()).setRenderer(1, new XYLineAndShapeRenderer(false,true));
		
		String outFile = filename;
		chart.saveAsPng(outFile, 800, 600);
		
	}
	
	private static double[] createXData() {
		
		double[] x = new double[27];
		
		for(int i = 2006; i < 2033; i++) {
			
			x[i-2006] = i;
			
		}
		
		return x;
		
	}
	
	private static void computeAggregates(RegMethod m) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	SQLException, IOException {
	
		Connection c = PsqlAdapter.createConnection(RIL_DATABASE);
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		};
		
		Map<String, String> station2Category = new HashMap<>();
		Map<String, List<String>> category2Station = new HashMap<>();
		
		ResultSet set = statement.executeQuery(PsqlUtils.createSelectStatement("fv,nv,fv_fremd,nv_fremd,station,ges,verkehrs15,typ_name",
				"daten_2016_regionstyp_final"));
		
		while(set.next()){
			
			int fv = set.getInt(FV);
			int nv = set.getInt(NV);
			int fvo = set.getInt(FVO);
			int nvo = set.getInt(FVO);
			
			if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){
				
				float n = set.getFloat(GES);
				String catType = null;
				
				for(int cat : CATEGORIES){
					
					if(n <= cat){
						catType = Integer.toString(cat);
						break;
					}
					
				}
				
				String type = set.getString(TYP) + "_" + catType;
				String station = set.getString(STATION);
				
				if(!type.contains("null")){
					
					station2Category.put(station, type);
					
					if(!category2Station.containsKey(type)){
						category2Station.put(type, new ArrayList<String>());
					}
					
					category2Station.get(type).add(station);
					
					
				}
				
			}
			
		}
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/category2stations.csv");
		
		writer.write("category;station_name");
		
		for(String category : category2Station.keySet()) {
			
			for(String stationName : category2Station.get(category)) {
				
				writer.newLine();
				writer.write(category + ";" + stationName);
				writer.flush();
				
			}
			
		}
		
		writer.close();
		
		Map<String, Map<String,XYSeries>> avg = new HashMap<>();
		Map<String, Map<String,XYSeries>> median = new HashMap<>();
		Map<String, Map<String,XYSeries>> type2Plot = new HashMap<>();
		
		for(String table : tables){
			
			Map<String, Map<String,RecursiveStatsContainer>> type2Container = new HashMap<>();
			
			ResultSet result = statement.executeQuery(
					PsqlUtils.createSelectStatement("state,station,fv,nv,fv_fremd,nv_fremd,ges,verkehrs15,typ_name", table));
			
			int year = 0;
			
			while(result.next()){
				
				int fv = result.getInt(FV);
				int nv = result.getInt(NV);
				int fvo = result.getInt(FVO);
				int nvo = result.getInt(NVO);
				
				String state = null;
				
				if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){
					
					year = result.getInt("verkehrs15");
	
					float n = result.getFloat("ges");
					String station = result.getString("station");
					
					if(station2Category.containsKey(station)){
						
						String type = station2Category.get(station);
	
						if(!type2Container.containsKey(state)){
							type2Container.put(state, new HashMap<>());
						}
						
						if(!type2Container.get(state).containsKey(type)){
							type2Container.get(state).put(type, new RecursiveStatsContainer());
						}
						
						if(!type2Plot.containsKey(state)){
							type2Plot.put(state, new HashMap<>());
						}
						
						if(!type2Plot.get(state).containsKey(type)){
							type2Plot.get(state).put(type, new XYSeries(""));
						}
						
						type2Plot.get(state).get(type).add(year, n);
						type2Container.get(state).get(type).handleNewEntry(n);
						
					}
					
				}
				
			}
			
			result.close();
			
			for(String key : type2Container.keySet()){
				
				for(Entry<String, RecursiveStatsContainer> entry : type2Container.get(key).entrySet()){
					
					if(!avg.containsKey(key)){
						
						avg.put(key, new HashMap<>());
						median.put(key, new HashMap<>());
						
					}
					
					if(!avg.get(key).containsKey(entry.getKey())){
						
						avg.get(key).put(entry.getKey(), new XYSeries(""));
						median.get(key).put(entry.getKey(), new XYSeries(""));
						
					}
					
					avg.get(key).get(entry.getKey()).add(year, entry.getValue().getMean());
					median.get(key).get(entry.getKey()).add(year, entry.getValue().getMedian());
					
				}
				
			}
			
		}
		
		Map<String, Map<String,RecursiveStatsContainer>> type2Container = new HashMap<>();
		int year = 2030;
		
		AbstractCsvReader csv = new AbstractCsvReader("\t",true) {
			
			@Override
			public void handleRow(String[] line) {
	
				String state = null;
				
				String station = line[1];
				double ges = Double.parseDouble(line[14]);
				double factor = Double.parseDouble(line[18]);
				double n = ges * (1 + factor);
				
				if(station2Category.containsKey(station)){
					
					String type = station2Category.get(station);
	
					if(!type2Container.containsKey(state)){
						type2Container.put(state, new HashMap<>());
					}
					
					if(!type2Container.get(state).containsKey(type)){
						type2Container.get(state).put(type, new RecursiveStatsContainer());
					}
					
					if(!type2Plot.containsKey(state)){
						type2Plot.put(state, new HashMap<>());
					}
					
					if(!type2Plot.get(state).containsKey(type)){
						type2Plot.get(state).put(type, new XYSeries(""));
					}
					
					type2Plot.get(state).get(type).add(year, n);
					type2Container.get(state).get(type).handleNewEntry(n);
					
				}
				
			}
			
		};
		
		csv.read("/home/dhosse/01_Projects/GSP/Änderungsraten_2013-zu-2030_V2_fuer-InnoZ.csv");
		
		for(String key : type2Container.keySet()){
			
			for(Entry<String, RecursiveStatsContainer> entry : type2Container.get(key).entrySet()){
				
				if(!avg.containsKey(key)){
					
					avg.put(key, new HashMap<>());
					median.put(key, new HashMap<>());
					
				}
				
				if(!avg.get(key).containsKey(entry.getKey())){
					
					avg.get(key).put(entry.getKey(), new XYSeries(""));
					median.get(key).put(entry.getKey(), new XYSeries(""));
					
				}
				
				avg.get(key).get(entry.getKey()).add(year, entry.getValue().getMean());
				median.get(key).get(entry.getKey()).add(year, entry.getValue().getMedian());
				
			}
			
		}
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/Documentation/timeline_" + m.name() + ".csv");
		
		String t = "state;type";
		for(int i = 2027; i < 2033; i++){
			t += ";" + Integer.toString(i);
		}
		
		t+= ";r2;nStations";
		
		out.write(t);
		
		for(Entry<String, Map<String, XYSeries>> entry : avg.entrySet()) {
			
			String key = entry.getKey();
			
			for(Entry<String, XYSeries> entry2 : entry.getValue().entrySet()) {
				
				int nEntries = entry2.getValue().getItemCount();
				
				double[] x = new double[nEntries];
				double[] y = new double[nEntries];
				Set<Integer> indicesToRemove = new HashSet<>();
				
				for(int i = 0; i < nEntries; i++) {
					
					x[i] = (double)entry2.getValue().getX(i);
					y[i] = (double)entry2.getValue().getY(i);
					
					if(y[i] == 0) {
						
						indicesToRemove.add(i);
						
					}
					
				}
				
				double[] xValues = new double[x.length-indicesToRemove.size()];
				double[] yValues = new double[y.length-indicesToRemove.size()];
				
				int j = 0;
				
				for(int i = 0; i < x.length; i++) {
					
					if(y[i] != 0) {
						
						xValues[j] = x[i];
						yValues[j] = y[i];
						
					} else j--;
					
					j++;
					
				}
				
				TrendLine trend = getRegressionMethod(m);
				trend.setValues(yValues, xValues);
				
				out.newLine();
				String line = key != null ? key : "";
				line += ";" + entry2.getKey();
				for(int i = 2027; i < 2033; i++){
					
					line += ";" + trend.predict(i);
					
				}
				
				line += ";" + trend.getRSquare() + ";" + category2Station.get(entry2.getKey()).size();
				
				out.write(line);
				out.flush();
			
				saveDataAsPng(trend, xValues, yValues, "/home/dhosse/01_Projects/GSP/charts/" + entry2.getKey() + ".png");
				
			}
		
		}
		
		out.close();
		
		statement.close();
		c.close();
	
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
		
	}

}