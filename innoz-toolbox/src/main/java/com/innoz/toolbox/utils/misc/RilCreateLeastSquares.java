package com.innoz.toolbox.utils.misc;

import java.awt.BasicStroke;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.matsim.core.utils.charts.XYScatterChart;
import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.utils.PsqlUtils;
import com.innoz.toolbox.utils.io.AbstractCsvReader;
import com.innoz.toolbox.utils.math.PolynomialRegression;
import com.innoz.toolbox.utils.matsim.RecursiveStatsContainer;

public class RilCreateLeastSquares {

	static final String RIL_DATABASE = "ril";
	static final int[] CATEGORIES = new int[]{100,300,1000,5000,10000,15000,20000,50000,1000000};

	static final String FV = "fv";
	static final String NV = "nv";
	static final String FVO = "fv_fremd";
	static final String NVO = "nv_fremd";
	static final String GES = "ges";
	static final String STATION = "station";
	static final String V15 = "verkehrs15";
	static final String TYP = "typ_name";
	static final String STATE= "state";
	
	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, IOException {

//		compute();
		computeStationwise();
		
	}

	private static void compute() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
		SQLException, IOException {
		
		Connection c = PsqlAdapter.createConnection(RIL_DATABASE);
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		};
		
		Map<String, String> station2Category = new HashMap<>();
		Map<String, Integer> category2StationCount = new HashMap<>();
		
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
					
					if(!category2StationCount.containsKey(type)){
						category2StationCount.put(type, 0);
					}
					
					int count = category2StationCount.get(type);
					category2StationCount.put(type, count + 1);
					
					
				}
				
			}
			
		}
		
		Map<String, Map<String,XYSeries>> avg = new HashMap<>();
		Map<String, Map<String,XYSeries>> median = new HashMap<>();
		Map<String, Map<String,XYSeries>> type2Plot = new HashMap<>();
		
		for(String table : tables){
			
			Map<String, Map<String,RecursiveStatsContainer>> type2Container = new HashMap<>();
			
			ResultSet result = statement.executeQuery(
					PsqlUtils.createSelectStatement("state,station,fv,nv,nv_fremd,ges,verkehrs15,typ_name", table));
			
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
		
		csv.read("/home/dhosse/01_Projects/GSP/types/Änderungsraten_2013-zu-2030_fuer-InnoZ.csv");
		
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
				
				System.out.println(entry.getKey() + ": " + entry.getValue().getMedian());
				
			}
			
		}
		
		int order = 4;
		String stateOrAgg = "aggregated";
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/types/new/" + stateOrAgg + "/order" + order + "/timeline.csv");
		
		String t = "state;type";
		for(int i = 0; i < order + 1; i++){
			t += ";median_a" + Integer.toString(i);
		}
		
		t+= ";median_r2";
		
		for(int i = 0; i < order + 1; i++){
			t += ";avg_a" + Integer.toString(i);
		}
		t += ";avg_r2;nStations";
		
		out.write(t);
		
		for(String key : type2Plot.keySet()){
			
			Map<String, XYSeries> map = type2Plot.get(key);
			
			for(Entry<String,XYSeries> entry : map.entrySet()){

				int nEntries = entry.getValue().getItems().size() / 11;

				if(nEntries > 4){
					
					String[] s = entry.getKey().split("_");
					String title = s[0] + ", " + getTitle(s[1]);
					
					if(key != null) title += ", " + key;
					
					XYScatterChart chart = new XYScatterChart(title, "Jahr", "Anzahl Reisende");

					double[] x = new double[11];
					double[] yMedian = new double[11];
					double[] yAvg = new double[11];
					
					for(int i = 0; i < 11; i++) {
						
						x[i] = median.get(key).get(entry.getKey()).getDataItem(i).getXValue() - 2006;
						yMedian[i] = median.get(key).get(entry.getKey()).getDataItem(i).getYValue();
						yAvg[i] = avg.get(key).get(entry.getKey()).getDataItem(i).getYValue();
						
					}
					
					PolynomialRegression median_reg = new PolynomialRegression(x, yMedian, order);
					PolynomialRegression avg_reg = new PolynomialRegression(x, yAvg, order);
					
//					double[] medianData = Regression.getPolynomialRegression(new XYSeriesCollection(
//							median.get(key).get(entry.getKey())), 0, order);
//					double[] avgData = Regression.getPolynomialRegression(new XYSeriesCollection(
//							avg.get(key).get(entry.getKey())), 0, order);

					out.newLine();
					String line = key != null ? key : "";
					line += ";" + entry.getKey();
					for(int i = 0; i < median_reg.degree()+1; i++){
						
						line += ";" + median_reg.beta(i);
						
					}
					
					line += ";" + median_reg.R2();
					
					for(int i = 0; i < avg_reg.degree()+1; i++){
						
						line += ";" + avg_reg.beta(i);
						
					}
					
					line += ";" + + avg_reg.R2() + ";" + nEntries;
					
					out.write(line);
					
					double[] medianX = new double[25];
					double[] medianY = new double[25];
					
					float f = 2006f;
					int j = 2006;
					
					while(f < 2031){

						medianX[j-2006] = f;
						
						medianY[j-2006] = median_reg.predict(f-2006);
						
						f += 1f;
						j++;
						
					}

					chart.addSeries("Trend", medianX, medianY);
					((XYPlot)chart.getChart().getPlot()).setRenderer(0, new XYLineAndShapeRenderer(true,true));
					
					x = new double[entry.getValue().getItems().size()];
					double[] y = new double[entry.getValue().getItems().size()];
					
					int ii = 0;
					
					for(Object d : entry.getValue().getItems()){
						
						XYDataItem i = (XYDataItem)d;
						
						x[ii] = i.getXValue();
						y[ii] = i.getYValue();
						
						ii++;
							
					}
					
					XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true,true);
					renderer.setSeriesStroke(0, new BasicStroke(2.0f));
					renderer.setSeriesStroke(1, new BasicStroke(0.0f));
					((XYPlot)chart.getChart().getPlot()).setRenderer(renderer);
					
					chart.addSeries("Rohdaten", x, y);
					((XYPlot)chart.getChart().getPlot()).setRenderer(1, new XYLineAndShapeRenderer(false,true));
					
					String state = key != null ? key+ "_" : "";
					
					String outFile = "/home/dhosse/01_Projects/GSP/types/new/" + stateOrAgg + "/order" + order + "/" + state + entry.getKey() + ".png";
					chart.saveAsPng(outFile, 800, 600);
					
				}
				
			}
			
		}
		
		out.close();
		
		statement.close();
		c.close();
		
	}
	
	private static String getTitle(String s){
		
		if(s.equals("100")){
			
			return "0 - 100";
		} else if(s.equals("300")){
			return "101 - 301";
		} else if(s.equals("1000")){
			return "301 - 1.000";
		} else if(s.equals("5000")){
			return "1.001 - 5.000";
		} else if(s.equals("10000")){
			return "5.001 - 10.000";
		} else if(s.equals("15000")){
			return "10.001 - 15.000";
		} else if(s.equals("20000")){
			return "15.001 - 20.000";
		} else if(s.equals("50000")){
			return "20.001 - 50.000";
		} else{
			return "> 50.001";
		}
		
	}
	
	private static void computeStationwise() throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, IOException {
		
		Connection c = PsqlAdapter.createConnection(RIL_DATABASE);
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		};
		
		Map<String, XYSeries> stationData = new ConcurrentHashMap<String, XYSeries>();
		
		Set<String> stations2016 = new HashSet<>();

		AbstractCsvReader csv = new AbstractCsvReader("\t",true) {
			
			@Override
			public void handleRow(String[] line) {

				String station = line[1];
				double ges = Double.parseDouble(line[14]);
				double factor = Double.parseDouble(line[18]);
				double n = Math.ceil(ges * (1 + factor));
				
				if(!stationData.containsKey(station)) {
					stationData.put(station, new XYSeries(""));
				}
				
				stationData.get(station).add(2030, n);
				
			}
			
		};
		
		csv.read("/home/dhosse/01_Projects/GSP/types/Änderungsraten_2013-zu-2030_fuer-InnoZ.csv");
		
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
						stationData.get(name).add(year, n);
					}
					
				}
				
			}
			
			result.close();

		}
		
		int order = 4;
		int endYear = 2030;
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/types/stationwise_noOrder.csv");
		out.write("name");
		out.write(";r2;daten_2006;daten_2007;daten_2008;daten_2009;daten_2010;daten_2011;daten_2012;daten_2013;daten_2014;daten_2015;"
				+ "daten_2016;daten2030");
		for(int i = 2006; i < 2031; i++) {
			out.write(";formel_" + Integer.toString(i));
		}
				
		for(Entry<String, XYSeries> entry : stationData.entrySet()){
			
			XYSeries xy = entry.getValue();

			int nElements = entry.getValue().getItemCount() - 1;
//			order = entry.getValue().getItemCount()-1;
			
			if(nElements < 1 || nElements < order) continue;

			double[] x = new double[nElements + 1];
			double[] y = new double[nElements + 1];
			
			XYDataItem first = (XYDataItem)xy.getItems().get(0);
			
			for(int i = 0; i < nElements + 1; i++) {
				
				XYDataItem item = (XYDataItem)xy.getItems().get(i);
				x[i] = item.getXValue() - first.getXValue();
				y[i] = item.getYValue();
			}
			
			PolynomialRegression reg = new PolynomialRegression(x, y, order);
				
				out.newLine();
				
				out.write(entry.getKey() + ";" + reg.R2());
				
				int start = 2006;
				StringBuilder dataStringBuilder = new StringBuilder();

				for(Object o : entry.getValue().getItems()) {
					
					XYDataItem item = (XYDataItem)o;
					
					if(item.getXValue() == start) {
						dataStringBuilder.append(";" + item.getYValue());
					} else {
						dataStringBuilder.append(";0");
					}
					start++;
					if(start == 2017){
						start = 2030;
					}
					
				}
				
				for(int i = start; i <= endYear; i++) {
					dataStringBuilder.append(";0");
				}
				
				out.write(dataStringBuilder.toString());
				
				for(int i = 2006; i <= endYear; i++) {
					
					double v = reg.predict(i-2006);
					out.write(";" + v);
					
				}
				
				out.flush();
				
				
		}
		
		if(!stationData.keySet().containsAll(stations2016)) {
			System.err.println("Not all stations that exist today are present in the output!");
		}
		
		out.close();
		
		statement.close();
		c.close();
		
	}

}