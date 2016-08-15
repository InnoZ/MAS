package com.innoz.toolbox.utils.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import com.innoz.toolbox.utils.io.AbstractCsvReader;

public class ProcessBundeswehrSites {

	Map<String, BwSite> bwSites = new HashMap<>();
	QuadTree<CsStation> qT;
	CoordinateTransformation transformation2 = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:32633");
	CoordinateTransformation transformation3 = TransformationFactory.getCoordinateTransformation("EPSG:32633", "EPSG:4326");
	
	double minX = Double.POSITIVE_INFINITY;
	double minY = Double.POSITIVE_INFINITY;
	double maxX = Double.NEGATIVE_INFINITY;
	double maxY = Double.NEGATIVE_INFINITY;
	
	public static void main(String[] args) {

		new ProcessBundeswehrSites().run();
		
	}

	public void run(){
		
		String filebase = "/home/dhosse/00_Orga/bwfp/";
		String bwSites = "BwStandorte.csv";
		String bwCS = "BwCS.csv";
		String flinkster = "flinkster.csv";
		String miscCS = "cs_stations.csv";
		
		Set<Coord> visitedCoords = new HashSet<>();
		
		AbstractCsvReader reader1 = new AbstractCsvReader() {
			
			@Override
			public void handleRow(String[] line) {
				
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				
				if(Double.isFinite(x) && Double.isFinite(y)){
					
					if(x < minX){
						minX = x;
					}
					if(x > maxX){
						maxX = x;
					}
					if(y < minY){
						minY = y;
					}
					if(y > maxY){
						maxY = y;
					}
					
					String id = line[2];
					String name = line[3] + " " + line[4] + " " + line[5];
					String street = line[6];
					String postalCode = line[7];
					String city = line[8];
		
					Coord c = new Coord(x, y);
					
					if(!visitedCoords.contains(c)){
						ProcessBundeswehrSites.this.bwSites.put(id, new BwSite(id, name, street, postalCode, city, c));
						visitedCoords.add(c);
					}
					
				}
				
			}
			
		};
		
		reader1.read(filebase + bwSites);
		Coord topLeft = transformation2.transform(new Coord(0, 84));
		Coord bottomRight = transformation2.transform(new Coord(18,0));
		qT = new QuadTree<>(-3000000, -1000, bottomRight.getX(), topLeft.getY());
		
		AbstractCsvReader reader2 = new AbstractCsvReader(";",true) {
			
			@Override
			public void handleRow(String[] line) {
				
				if(!line[7].equals("")&&!line[7].equals(" ")){
					double x = Double.parseDouble(line[0].replace(",", "."));
					double y = Double.parseDouble(line[1].replace(",", "."));
					
					Coord result = transformation2.transform(new Coord(x, y));
					
					if(x >= 0 && x <= 18)
						qT.put(result.getX(), result.getY(), new CsStation("bw"));
				}
				
			}
		};
		
		reader2.read(filebase + bwCS);
		
		AbstractCsvReader reader3 = new AbstractCsvReader() {
			
			@Override
			public void handleRow(String[] line) {
				
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				
				Coord result = transformation2.transform(new Coord(x, y));
				
				if(x >= 0 && x <= 18)
					qT.put(result.getX(), result.getY(), new CsStation("flinkster"));
				
			}
		};
		
		reader3.read(filebase + flinkster);
		
		AbstractCsvReader reader4 = new AbstractCsvReader() {
			
			@Override
			public void handleRow(String[] line) {
				
				double x = Double.parseDouble(line[0]);
				double y = Double.parseDouble(line[1]);
				
				Coord result = transformation2.transform(new Coord(x, y));
				
				if(x >= 0 && x <= 18)
					qT.put(result.getX(), result.getY(), new CsStation("misc"));
				
			}
			
		};
		
		reader4.read(filebase + miscCS);
		
		for(BwSite bwSite : this.bwSites.values()){
			
			double x = bwSite.coord.getX();
			double y = bwSite.coord.getY();
			List<CsStation> closest = (List<CsStation>) this.qT.getDisk(x, y, 10000);

			if(closest != null){
				
				if(!closest.isEmpty()){
					
					for(CsStation s : closest){
						
						String o = s.operator;
						if(o.equals("bw")){
							bwSite.bwCounter++;
						} else if(o.equalsIgnoreCase("flinkster")){
							bwSite.flinksterCounter++;
						} else {
							bwSite.miscCounter++;
						}
						
					}
					
				}
				
			}
			
		}
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filebase + "bwStandorte_mit_Stationen.csv")));
		
			writer.write("id;name;street;postal_code;city;x;y;number_of_bw_carsharing_stations;number_of_flinkster_stations;number_of_other_stations");
			
			for(BwSite site : ProcessBundeswehrSites.this.bwSites.values()){
				
				Coord wgs84 = transformation3.transform(site.coord);
				
				writer.newLine();
				writer.write(site.id.replace("\"", "") + ";" + site.name.replace("\"", "") + ";" + site.street.replace("\"", "") + ";" +
						site.postalCode.replace("\"", "") + ";" + site.city.replace("\"", "") + ";" + wgs84.getX() + ";"
						+ wgs84.getY() + ";" + site.bwCounter + ";" + site.flinksterCounter + ";" + site.miscCounter);
				writer.flush();
				
			}
			
			writer.close();
		
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	static class BwSite{
		String id;
		String name;
		String street;
		String postalCode;
		String city;
		Coord coord;
		
		int bwCounter = 0;
		int flinksterCounter = 0;
		int miscCounter = 0;
		
		BwSite(String id, String name, String street, String postalCode, String city, Coord coord){
			this.id = id;
			this.name = name;
			this.street = street;
			this.postalCode = postalCode;
			this.city = city;
			this.coord = coord;
		}
	}
	
	static class CsStation{
		
		String operator;
		CsStation(String operator){
			this.operator = operator;
		}
		
	}
	
}