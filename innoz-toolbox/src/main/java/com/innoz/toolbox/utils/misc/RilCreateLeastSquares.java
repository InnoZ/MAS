package com.innoz.toolbox.utils.misc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
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
		
		Map<String, XYSeries> avg = new HashMap<>();
		Map<String, XYSeries> median = new HashMap<>();
		
		for(String table : tables){
			
			Map<String, RecursiveStatsContainer> type2Container = new HashMap<>();
			
			ResultSet result = statement.executeQuery("SELECT fv,nv,fv_fremd,nv_fremd,ges,verkehrs15,typ_name from " + table + ";");
			
			int year = 0;
			
			while(result.next()){
				
				int fv = result.getInt("fv");
				int nv = result.getInt("nv");
				int fvo = result.getInt("fv_fremd");
				int nvo = result.getInt("nv_fremd");
				
				if(fv >= 0 && nv >= 0 && fvo >= 0 && nvo >= 0){
					
					year = result.getInt("verkehrs15");

					float n = result.getFloat("ges");
					String catType = null;
					
					for(int cat : categories){
						
						if(n <= cat){
							catType = Integer.toString(cat);
							break;
						}
						
					}
					
					String type = result.getString("typ_name") + "_" + catType;
					
					if(!type.contains("null")){

						if(!type2Container.containsKey(type)){
							type2Container.put(type, new RecursiveStatsContainer());
						}
						
						type2Container.get(type).handleNewEntry(n);
						
					}
					
				}
				
			}
			
			for(Entry<String, RecursiveStatsContainer> entry : type2Container.entrySet()){
				
				if(!avg.containsKey(entry.getKey())){
					
					avg.put(entry.getKey(), new XYSeries(""));
					median.put(entry.getKey(), new XYSeries(""));
					
				}
				
				avg.get(entry.getKey()).add(year, entry.getValue().getMean());
				median.get(entry.getKey()).add(year, entry.getValue().getMedian());
				
			}

			result.close();
			
		}
		
		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/01_Projects/GSP/timeline.csv");
		
		out.write("type;median_a0;median_a1;avg_a0;avg_a1");
		
		Set<String> keys = avg.keySet();
		
		for(String key : keys){
			
			double[] medianData = Regression.getOLSRegression(new XYSeriesCollection(median.get(key)), 0);
			double[] avgData = Regression.getOLSRegression(new XYSeriesCollection(avg.get(key)), 0);
			
			out.newLine();
			out.write(key + ";" + medianData[0] + ";" + medianData[1] + ";" + avgData[0] + ";" + avgData[1]);
			
		}
		
		out.close();
		
		statement.close();
		c.close();
		
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