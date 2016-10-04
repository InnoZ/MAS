package com.innoz.toolbox.io.database.migration;

import java.io.BufferedWriter;
import java.io.IOException;

import org.matsim.core.utils.io.IOUtils;

import com.innoz.toolbox.utils.io.AbstractCsvReader;

class Utilities {
	
	static StringBuilder builder = new StringBuilder();
	
	static void readWriteCallaBike() throws IOException{
		
		BufferedWriter writer = IOUtils.getBufferedWriter("/home/dhosse/Downloads/HACKATHON_BOOKING_CALL_A_BIKE_UTF8_failures.csv");
		
		AbstractCsvReader reader = new AbstractCsvReader(";",true) {
			
			@Override
			public void handleRow(String[] line) {

				if(line.length > 48){
					int i = 0;
					for(String s : line){
						i++;
						if(i == line.length){
							builder.append(s);
						} else {
							builder.append(s + ";");
						}
					}
					try {
						
						writer.write(builder.toString());
						writer.newLine();
						
					} catch (IOException e) {
						e.printStackTrace();
					}
					builder = new StringBuilder();
				}
			
			}
			
		};
		
		reader.read("/home/dhosse/Downloads/HACKATHON_BOOKING_CALL_A_BIKE_UTF8.csv");

		writer.flush();
		writer.close();
		
	}

}
