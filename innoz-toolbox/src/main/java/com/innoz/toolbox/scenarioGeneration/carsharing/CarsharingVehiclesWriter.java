package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.contrib.carsharing.vehicles.FFVehicleImpl;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.matsim.core.utils.io.IOUtils;
import org.xml.sax.helpers.DefaultHandler;

import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.FFEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.TwoWayEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.VehicleEntry;

public class CarsharingVehiclesWriter {

	public void write(Map<CSVehicle, Link> vehicleLocations, String pathname) {
		
		BufferedWriter writer = IOUtils.getBufferedWriter(pathname);
		CarsharingVehiclesWriterHandler handler = new CarsharingVehiclesWriterHandler();
		try {
			
		
			handler.writeXmlHead(writer);
			
			handler.startCompany(writer, "teilauto");
			
			processVehicles(vehicleLocations, "twoway").entrySet().forEach(entry -> {
				
				try {
					
					handler.writeTwoWayEntry(writer, entry);
					
				} catch (IOException e) {

					e.printStackTrace();
					
				}
				
			});
			
			processVehicles(vehicleLocations, "freefloating").entrySet().forEach(entry -> {
				
				try {
				
					handler.writeFreeFloatingEntry(writer, entry);
				
				} catch (IOException e) {

					e.printStackTrace();
					
				}
				
			});
			
			handler.endCompany(writer);
			
			handler.end(writer);
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private Map<Coord, VehicleEntry> processVehicles(Map<CSVehicle, Link> vehicleLocations, String csType) {
		
		Map<Coord, VehicleEntry> entryMap = new HashMap<>();
		
		vehicleLocations.entrySet().stream().filter(entry -> entry.getKey().getCsType().equals(csType)).forEach(entry -> {
			
			Coord c = entry.getValue().getCoord();
			
			if(csType.equals("twoway")) {

				StationBasedVehicle vehicle = (StationBasedVehicle)entry.getKey();

				if(!entryMap.containsKey(c)) {
					
					entryMap.put(c, new TwoWayEntry(vehicle.getStationId()));
				}
				
				TwoWayEntry tw = (TwoWayEntry) entryMap.get(c);
				tw.vehicles.add(vehicle);
				
			} else if(csType.equals("freefloating")) {
				
				FFVehicleImpl vehicle = (FFVehicleImpl) entry.getKey();
				
				if(!entryMap.containsKey(c)) {
					entryMap.put(c, new FFEntry(vehicle.getVehicleId(), c, "car"));
				}
				
			}
			
		});
		
		return entryMap;
		
	}
	
	public void write(String pathname, Map<String, Map<Coord, VehicleEntry>> vehicles){
		
		try {
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathname)));
			CarsharingVehiclesWriterHandler handler = new CarsharingVehiclesWriterHandler();
			handler.writeXmlHead(writer);
			
			for(Entry<String, Map<Coord, VehicleEntry>> entry : vehicles.entrySet()) {
				
				handler.startCompany(writer, entry.getKey());
				
				for(Entry<Coord, VehicleEntry> vehicleEntry : entry.getValue().entrySet()) {
					
					if(vehicleEntry.getValue() instanceof TwoWayEntry) {
						
						handler.writeTwoWayEntry(writer, vehicleEntry);
						
					} else {
						
						handler.writeFreeFloatingEntry(writer, vehicleEntry);
						
					}
					
				}
				
				handler.endCompany(writer);
				
			}
			
			handler.end(writer);
			
			writer.flush();
			writer.close();

		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	static class CarsharingVehiclesWriterHandler extends DefaultHandler {
		
		private static String indent = "";
		private static final String TAB = "\t";
		private static final String NEWLINE = "\n";
		
		void writeXmlHead(BufferedWriter out) throws IOException{
			
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			out.write(NEWLINE);
			out.write("<!DOCTYPE companies SYSTEM \"CarsharingStationsV2.dtd\">");
			out.newLine();
			out.write("<companies>");
			out.write(NEWLINE);
			out.write(NEWLINE);
			indent = indent.concat(TAB);
			
		}
		
		void startCompany(BufferedWriter out, String id) throws IOException {
			
			out.write(indent + "<company name=\"" + id + "\">");
			out.write(NEWLINE);
			indent = indent.concat(TAB);
			
		}
		
		void endCompany(BufferedWriter out) throws IOException {
			
			out.write("</company>");
			out.write(NEWLINE);
			indent = indent.replaceFirst(TAB, "");
			
		}
		
		void writeTwoWayEntry(BufferedWriter out, Entry<Coord, VehicleEntry> entry) throws IOException {
			
			Coord coord = entry.getKey();
			TwoWayEntry value = (TwoWayEntry)entry.getValue();
			
			out.write(indent + "<twoway id=\"" + value.id + "\" x=\"" + coord.getX() + "\" y=\"" + coord.getY() + "\">");
			out.write(NEWLINE);
			indent = indent.concat(TAB);
			
			for(CSVehicle v : value.vehicles){
				
				out.write(indent + "<vehicle type=\"" + v.getType() + "\" vehicleID=\"" + v.getVehicleId() + "\"/>");
				out.write(NEWLINE);
				
			}
			
			indent = indent.replaceFirst(TAB, "");
			out.write(indent + "</twoway>");
			out.write(NEWLINE);
			
		}
		
		void writeFreeFloatingEntry(BufferedWriter out, Entry<Coord, VehicleEntry> entry) throws IOException {
			
			Coord coord = entry.getKey();
			FFEntry value = (FFEntry)entry.getValue();
			
			out.write(indent + "<freefloating id=\"" + value.id + "\" x=\"" + coord.getX() + "\" y=\"" +
					coord.getY() + "\" type=\"" + value.vehicleType + "\"/>");
			out.write(NEWLINE);
			
		}
		
		void writeOneWayEntry(BufferedWriter out, Entry<Coord, StationBasedVehicle> entry) throws IOException {
			
		}
		
		void end(BufferedWriter out) throws IOException{
			
			indent = indent.replaceFirst(TAB, "");
			out.write("</companies>");
			
		}
		
	}
	
}