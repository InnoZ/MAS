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
		
		Connection c = PsqlAdapter.createConnection(RIL_DATABASE);
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{
				
				"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		
		};

		Map<String, double[]> stationData = new HashMap<>();
		double[] x = xValues();
		
		Set<String> stations2016 = new HashSet<>();

		AbstractCsvReader csv = new AbstractCsvReader("\t",true) {
			
			@Override
			public void handleRow(String[] line) {

				String station = line[1];
				double ges = Double.parseDouble(line[14]);
				double factor = Double.parseDouble(line[18]);
				double n = Math.ceil(ges * (1 + factor));
				
				if(!stationData.containsKey(station)) {
					stationData.put(station, new double[x.length]);
				}
				
				stationData.get(station)[index2030] = n;
				
			}
			
		};
		
		csv.read("/home/dhosse/01_Projects/GSP/Änderungsraten_2013-zu-2030_V2_fuer-InnoZ.csv");
		
		for(String table : tables){
		
			ResultSet result = statement.executeQuery(
					PsqlUtils.createSelectStatement("station,fv,nv,fv_fremd,nv_fremd,ges,verkehrs15", table));
			
			while(result.next()){
				
				int fv = result.getInt("fv");
				int nv = result.getInt("nv");
				int fvo = result.getInt("fv_fremd");
				int nvo = result.getInt("nv_fremd");
				String name = result.getString("station");
				int year = result.getInt("verkehrs15");
				
				if(year == 2013) {
					stations2016.add(name);
				}
				
				if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){

					int n = result.getInt("ges");
					
					if(stationData.containsKey(name)){
						stationData.get(name)[year-2006] = (double) n;
					}
					
				}
				
			}
			
			result.close();

		}
		
		statement.close();
		c.close();
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/charts/trends.csv");
		out.write("station;2027;2028;2029;2030;2031;2032;r2");
		out.flush();
		
		for(Entry<String, double[]> entry : stationData.entrySet()) {
			
			String name = entry.getKey();
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
					
				} else j--;
				
				j++;
				
			}
			
			if(xValues.length > 2) {

				TrendLine t = getRegressionMethod(m);
				t.setValues(yValues, xValues);
				
				appendDataToCsv(out, name, t);
				
			}
			
		}
		
		out.flush();
		out.close();
		
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
				
			}
		
		} catch (InstantiationException | IllegalAccessException e) {
			
			e.printStackTrace();
			
		}
		
		return null;
		
	}
	
	private static void appendDataToCsv(BufferedWriter writer, String name, TrendLine t) throws IOException {

		writer.newLine();
		
		writer.write(name);
		
		for(int i = 2027; i < 2033; i++) {
			
			writer.write(";" + t.predict(i));
			
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
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/charts/category2stations.csv");
		
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
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/charts/timeline.csv");
		
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

}