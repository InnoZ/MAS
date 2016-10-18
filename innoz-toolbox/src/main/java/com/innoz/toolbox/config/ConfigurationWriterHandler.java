package com.innoz.toolbox.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.innoz.toolbox.config.Configuration.AdminUnitEntry;

public class ConfigurationWriterHandler {

	private static String indent = "";
	private static final String TAB = "\t";
	private static final String NEWLINE = "\n";
	
	public void startConfiguration(final BufferedWriter out) throws IOException{
		
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
		out.write(NEWLINE);
		out.write("<configuration>");
		out.write(NEWLINE);
		out.write(NEWLINE);
		indent = indent.concat(TAB);
		
	}
	
	public void endConfiguration(final BufferedWriter out) throws IOException{
		
		indent = indent.replaceFirst(TAB, "");
		out.write("</configuration>");
		
	}
	
	public void writeConfiguration(final Configuration configuration, final BufferedWriter out) throws IOException{
		
		out.write(indent);
		out.write("<areaSet k=\"surveyArea\">");
		indent = indent.concat(TAB);
		out.write(NEWLINE);
		if(configuration.getSurveyAreaIds() != null){
		
			for(String s : configuration.getSurveyAreaIds().split(",")){
				
				AdminUnitEntry entry = configuration.getAdminUnitEntries().get(s);
				String id = entry.getId();
				Integer nHouseholds = entry.getNumberOfHouseholds();
				Integer networkDetail = entry.getNetworkDetail() != null ? entry.getNetworkDetail() : 6;
				out.write(indent);
				out.write("<adminUnit id =\"" + id + "\" numberOfHouseholds=\"" + nHouseholds + "\" networkDetail=\"" 
						+ networkDetail + "\" />");
				out.write(NEWLINE);
				
			}
			
		}
		indent = indent.replaceFirst(NEWLINE, "");
		out.write(indent);
		out.write("</areaSet>");
		out.write(NEWLINE);
		out.write(NEWLINE);
		
		out.write(indent);
		out.write("<areaSet k=\"vicinity\">");
		out.write(NEWLINE);
		indent = indent.concat(TAB);
		if(configuration.getVicinityIds() != null){
		
			for(String s : configuration.getVicinityIds().split(",")){
			
				out.write(indent);
				AdminUnitEntry entry = configuration.getAdminUnitEntries().get(s);
				String id = entry.getId();
				Integer nHouseholds = entry.getNumberOfHouseholds();
				Integer networkDetail = entry.getNetworkDetail() != null ? entry.getNetworkDetail() : 4;
				out.write("<adminUnit id =\"" + id + "\" numberOfHouseholds=\"" + nHouseholds + "\" networkDetail=\"" 
						+ networkDetail + "\" />");
				out.write(NEWLINE);
				
			}
			
		}
		indent = indent.replaceFirst(TAB, "");
		out.write(indent);
		out.write("</areaSet>");
		out.write(NEWLINE);
		out.write(NEWLINE);
		
//		Map<String, String> params = configuration.getParams();
//		Map<String, String> comments = configuration.getComments();
//		
//		for(Entry<String, String> param : params.entrySet()){
//			
//			String comment = comments.get(param.getKey());
//			if(comment != null){
//				out.write(indent);
//				out.write("<!--" + comment + " -->");
//				out.write(NEWLINE);
//			}
//			
//			out.write(indent);
//			out.write("<" + param.getKey() + " v=\"" + param.getValue() + "\"/>");
//			out.write(NEWLINE);
//			out.write(NEWLINE);
//			
//		}
//		
//		out.write(NEWLINE);
		
	}
	
}