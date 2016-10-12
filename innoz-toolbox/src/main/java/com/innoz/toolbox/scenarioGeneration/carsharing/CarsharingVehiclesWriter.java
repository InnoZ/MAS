package com.innoz.toolbox.scenarioGeneration.carsharing;

public class CarsharingVehiclesWriter {

//	public void write(String pathname, Map<Coord, TwoWayEntry> roundtrip, Map<Coord, FFVehicleImpl> freefloating, String company){
//		
//		try {
//			
//			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(pathname)));
//			CarsharingVehiclesWriterHandler handler = new CarsharingVehiclesWriterHandler();
//			handler.writeXmlHead(writer);
//			handler.startCompany(writer, company);
//			
//			for(Entry<Coord, TwoWayEntry> entry : roundtrip.entrySet()){
//				
//				handler.writeTwoWayEntry(writer, entry);
//				
//			}
//			
//			for(Entry<Coord, FFVehicleImpl> entry : freefloating.entrySet()){
//				
//				handler.writeFreeFloatingEntry(writer, entry);
//				
//			}
//			
//			handler.endCompany(writer);
//			handler.end(writer);
//			
//			writer.flush();
//			writer.close();
//
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//			
//		}
//		
//	}
//	
//	static class CarsharingVehiclesWriterHandler extends DefaultHandler {
//		
//		private static String indent = "";
//		private static final String TAB = "\t";
//		private static final String NEWLINE = "\n";
//		
//		void writeXmlHead(BufferedWriter out) throws IOException{
//			
//			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
//			out.write(NEWLINE);
//			out.write("<companies>");
//			out.write(NEWLINE);
//			out.write(NEWLINE);
//			indent = indent.concat(TAB);
//			
//		}
//		
//		void startCompany(BufferedWriter out, String id) throws IOException {
//			
//			out.write(indent + "<company name=\"" + id + "\">");
//			out.write(NEWLINE);
//			indent = indent.concat(TAB);
//			
//		}
//		
//		void endCompany(BufferedWriter out) throws IOException {
//			
//			out.write("</company>");
//			out.write(NEWLINE);
//			indent = indent.replaceFirst(TAB, "");
//			
//		}
//		
//		void writeTwoWayEntry(BufferedWriter out, Entry<Coord, TwoWayEntry> entry) throws IOException {
//			
//			Coord coord = entry.getKey();
//			TwoWayEntry value = entry.getValue();
//			
//			out.write(indent + "<twoway id=\"" + value.id + "\" lat=\"" + coord.getX() + "\" lon=\"" + coord.getY() + "\">");
//			out.write(NEWLINE);
//			indent = indent.concat(TAB);
//			
//			for(CSVehicle v : value.vehicles){
//				
//				StationBasedVehicle vehicle = (StationBasedVehicle)v;
//				
//				out.write(indent + "<vehicle type=\"" + vehicle.getVehicleType() + "\" vehicleID=\"" + v.getVehicleId() + "\"/>");
//				out.write(NEWLINE);
//				
//			}
//			
//			indent = indent.replaceFirst(TAB, "");
//			out.write(indent + "</twoway>");
//			out.write(NEWLINE);
//			
//		}
//		
//		void writeFreeFloatingEntry(BufferedWriter out, Entry<Coord, FFVehicleImpl> entry) throws IOException {
//			
//			Coord coord = entry.getKey();
//			FFVehicleImpl value = entry.getValue();
//			
//			out.write(indent + "<freefloating id=\"" + value.getVehicleId() + "\" lat=\"" + coord.getX() + "\" lon=\"" +
//					coord.getY() + "\" type=\"" + value.getType() + "\"/>");
//			out.write(NEWLINE);
//			
//		}
//		
//		void writeOneWayEntry(BufferedWriter out, Entry<Coord, StationBasedVehicle> entry) throws IOException {
//			
//		}
//		
//		void end(BufferedWriter out) throws IOException{
//			
//			indent = indent.replaceFirst(TAB, "");
//			out.write("</companies>");
//			
//		}
//		
//	}
	
}