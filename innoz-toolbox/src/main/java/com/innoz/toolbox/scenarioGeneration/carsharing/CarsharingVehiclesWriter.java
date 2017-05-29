package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.xml.sax.helpers.DefaultHandler;

import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.FFEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.TwoWayEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.VehicleEntry;

public class CarsharingVehiclesWriter {

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
			
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
			out.write(NEWLINE);
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
			
			out.write(indent + "<twoway id=\"" + value.id + "\" lat=\"" + coord.getX() + "\" lon=\"" + coord.getY() + "\">");
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
			
			out.write(indent + "<freefloating id=\"" + value.id + "\" lat=\"" + coord.getX() + "\" lon=\"" +
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