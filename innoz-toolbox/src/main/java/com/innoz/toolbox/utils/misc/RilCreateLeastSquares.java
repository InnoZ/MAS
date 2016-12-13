package com.innoz.toolbox.utils.misc;

import java.awt.BasicStroke;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.matsim.core.utils.charts.XYScatterChart;
import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.config.PsqlAdapter;
import com.innoz.toolbox.utils.matsim.RecursiveStatsContainer;

public class RilCreateLeastSquares {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, IOException {

		compute();
		computeAggregates();
		computeStationwise();
		
	}
	
	private static void compute() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
		SQLException, IOException {
		
		Connection c = PsqlAdapter.createConnection("ril");
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		};
		
		int[] categories = new int[]{100,300,1000,5000,10000,15000,20000,50000,1000000};
		
		Map<String, String> station2Category = new HashMap<>();
		Map<String, Integer> category2StationCount = new HashMap<>();
		
		ResultSet set = statement.executeQuery("SELECT state,fv,nv,fv_fremd,nv_fremd,station,ges,verkehrs15,typ_name from "
				+ "daten_2016_regionstyp_final;");
		
		while(set.next()){
			
			int fv = set.getInt("fv");
			int nv = set.getInt("nv");
			int fvo = set.getInt("fv_fremd");
			int nvo = set.getInt("nv_fremd");
			
			if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){
				
				float n = set.getFloat("ges");
				String catType = null;
				
				for(int cat : categories){
					
					if(n <= cat){
						catType = Integer.toString(cat);
						break;
					}
					
				}
				
				String type = set.getString("typ_name") + "_" + catType;
				String station = set.getString("station");
				
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
			
			ResultSet result = statement.executeQuery("SELECT state,station,fv,nv,fv_fremd,nv_fremd,ges,verkehrs15,typ_name from " + table + ";");
			
			int year = 0;
			
			while(result.next()){
				
				int fv = result.getInt("fv");
				int nv = result.getInt("nv");
				int fvo = result.getInt("fv_fremd");
				int nvo = result.getInt("nv_fremd");
				
				String state = result.getString("state");
				
				if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){
					
					year = result.getInt("verkehrs15");

					float n = result.getFloat("ges");
					String station = result.getString("station");
					
					if(station2Category.containsKey(station)){
						
						String type = station2Category.get(station);

						if(!type2Container.containsKey(state)){
							type2Container.put(state, new HashMap<>());
						}
						
						if(!type2Container.containsKey(type)){
							type2Container.put(type, new HashMap<>());
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
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/timeline.csv");

		int order = 6;
		
		String t = "state;type";
		for(int i = 0; i < order + 1; i++){
			t += ";median_a" + Integer.toString(i);
		}
		for(int i = 0; i < order + 1; i++){
			t += ";avg_a" + Integer.toString(i);
		}
		t += ";nStations";
		
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

					double[] medianData = Regression.getPolynomialRegression(new XYSeriesCollection(
							median.get(key).get(entry.getKey())), 0, order);
					double[] avgData = Regression.getPolynomialRegression(new XYSeriesCollection(
							avg.get(key).get(entry.getKey())), 0, order);

					out.newLine();
					String line = key != null ? key : "";
					line += ";" + entry.getKey();
					for(int i = 0; i < medianData.length - 1; i++){
						
						line += ";" + medianData[i];
						
					}
					for(int i = 0; i < avgData.length - 1; i++){
						
						line += ";" + avgData[i];
						
					}
					
					line += ";" + nEntries;
					
					out.write(line);
					
					double[] medianX = new double[11];
					double[] medianY = new double[11];
					
					float f = 2006f;
					int j = 2006;
					
					while(f < 2017){

						medianX[j-2006] = f;
						
						for(int i = 0; i <= order; i++){
							
							medianY[j-2006] += avgData[i] * Math.pow(f, i);
							
						}
						
						f += 1f;
						j++;
						
					}

					chart.addSeries("Trend", medianX, medianY);
					((XYPlot)chart.getChart().getPlot()).setRenderer(0, new XYLineAndShapeRenderer(true,true));
					
					double[] x = new double[entry.getValue().getItems().size()];
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
					
					String outFile = "/home/dhosse/01_Projects/GSP/types/" + key + "_" + entry.getKey() + ".png";
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
		
		Connection c = PsqlAdapter.createConnection("ril");
		Statement statement = c.createStatement();
		
		String[] tables = new String[]{"daten_2006_regionstyp_final", "daten_2007_regionstyp_final",
				"daten_2008_regionstyp_final", "daten_2009_regionstyp_final", "daten_2010_regionstyp_final",
				"daten_2011_regionstyp_final", "daten_2012_regionstyp_final", "daten_2013_regionstyp_final",
				"daten_2014_regionstyp_final", "daten_2015_regionstyp_final", "daten_2016_regionstyp_final"
		};
		
		Map<String, XYSeries> stationData = new HashMap<String, XYSeries>();
		
		for(String table : tables){
		
			ResultSet result = statement.executeQuery("SELECT station,fv,nv,fv_fremd,nv_fremd,ges,verkehrs15 from " + table + ";");
			
			while(result.next()){
				
				int fv = result.getInt("fv");
				int nv = result.getInt("nv");
				int fvo = result.getInt("fv_fremd");
				int nvo = result.getInt("nv_fremd");
				
				if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){

					String name = result.getString("station");
					int year = result.getInt("verkehrs15");
					int n = result.getInt("ges");
					
					if(!stationData.containsKey(name)) stationData.put(name, new XYSeries(""));
					
					stationData.get(name).add(year, n);
					
				}
				
			}
			
			result.close();

		}
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/stationwise.csv");
		out.write("name;a0;a1");
		
		for(Entry<String, XYSeries> entry : stationData.entrySet()){
			
			XYSeries xy = entry.getValue();
			
			if(xy.getItems().size() >= 2){

				double[] reg = Regression.getOLSRegression(new XYSeriesCollection(xy),0);
				
				out.newLine();
							
				out.write(entry.getKey() + ";" + reg[0] + ";" + reg[1]);
				
			}
			
		}
		
		out.close();
		
		statement.close();
		c.close();
		
	}

	private static void computeAggregates()
			throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
		
		Connection c = PsqlAdapter.createConnection("ril");
		
		Statement statement = c.createStatement();

		String[] tables = new String[]{"daten_eng_verfl_0_to_100", "daten_eng_verfl_10001_to_15000",
				"daten_eng_verfl_1001_to_5000", "daten_eng_verfl_101_to_300", "daten_eng_verfl_15001_to_20000",
				"daten_eng_verfl_20001_to_50000", "daten_eng_verfl_301_to_1000", "daten_eng_verfl_50000_to_1000000",
				"daten_eng_verfl_5001_to_10000", "daten_ergaenz_0_to_100", "daten_ergaenz_10001_to_15000",
				"daten_ergaenz_1001_to_5000", "daten_ergaenz_101_to_300", "daten_ergaenz_15001_to_20000",
				"daten_ergaenz_20001_to_50000", "daten_ergaenz_301_to_1000", "daten_ergaenz_50000_to_1000000",
				"daten_ergaenz_5001_to_10000", "daten_gem_aus_gros_0_to_100", "daten_gem_aus_gros_10001_to_15000",
				"daten_gem_aus_gros_1001_to_5000", "daten_gem_aus_gros_101_to_300", "daten_gem_aus_gros_15001_to_20000",
				"daten_gem_aus_gros_20001_to_50000", "daten_gem_aus_gros_301_to_1000", "daten_gem_aus_gros_50000_to_1000000",
				"daten_gem_aus_gros_5001_to_10000", "daten_weit_verfl_0_to_100", "daten_weit_verfl_10001_to_15000",
				"daten_weit_verfl_1001_to_5000", "daten_weit_verfl_101_to_300", "daten_weit_verfl_15001_to_20000",
				"daten_weit_verfl_20001_to_50000", "daten_weit_verfl_301_to_1000", "daten_weit_verfl_50000_to_1000000",
				"daten_weit_verfl_5001_to_10000", "daten_zentrum_0_to_100", "daten_zentrum_10001_to_15000",
				"daten_zentrum_1001_to_5000", "daten_zentrum_101_to_300", "daten_zentrum_15001_to_20000",
				"daten_zentrum_20001_to_50000", "daten_zentrum_301_to_1000", "daten_zentrum_50000_to_1000000",
				"daten_zentrum_5001_to_10000"};

		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/aggregates.csv");
		out.write("category;median_a0;median_a1;avg_a0;avg_a1;Anzahl Datenpunkte");
		
		for(String table : tables){
			
			ResultSet result = statement.executeQuery("SELECT * FROM " + table + ";");
			XYSeries medianSeries = new XYSeries("");
			XYSeries avgSeries = new XYSeries("");
			
			int nDatapoints = 0;

			while(result.next()){
				
				int year = result.getInt("year");
				float median = result.getFloat("median_ges");
				float avg = result.getFloat("avg_ges");
				nDatapoints = result.getInt("count");
				
				medianSeries.add(year, median);
				avgSeries.add(year, avg);
				
			}
			
			result.close();
			
			XYSeriesCollection medianData = new XYSeriesCollection(medianSeries);
			XYSeriesCollection avgData = new XYSeriesCollection(avgSeries);
			double[] median_as = Regression.getOLSRegression(medianData, 0);
			double[] avg_as = Regression.getOLSRegression(avgData, 0);
			
			out.newLine();
			out.write(table + ";" + median_as[0] + ";" + median_as[1] + ";" + avg_as[0] + ";" + avg_as[1] + ";" + nDatapoints);
			
		}
		
		out.close();
		
		statement.close();
		c.close();
	}

}