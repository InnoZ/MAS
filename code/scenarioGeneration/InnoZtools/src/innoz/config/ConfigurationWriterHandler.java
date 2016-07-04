package innoz.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import innoz.config.Configuration.AdminUnitEntry;

public class ConfigurationWriterHandler {

	private static String indent = "";
	
	public void startConfiguration(final BufferedWriter out) throws IOException{
		
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>");
		out.write("\n");
		out.write("<configuration>");
		out.write("\n");
		out.write("\n");
		indent = indent.concat("\t");
		
	}
	
	public void endConfiguration(final BufferedWriter out) throws IOException{
		
		indent = indent.replaceFirst("\t", "");
		out.write("</configuration>");
		
	}
	
	public void writeConfiguration(final Configuration configuration, final BufferedWriter out) throws IOException{
		
		out.write(indent);
		out.write("<areaSet k=\"surveyArea\">");
		indent = indent.concat("\t");
		out.write("\n");
		if(configuration.getSurveyAreaIds() != null){
		
			for(String s : configuration.getSurveyAreaIds().split(",")){
				
				AdminUnitEntry entry = configuration.getAdminUnitEntries().get(s);
				String id = entry.getId();
				Integer nHouseholds = entry.getNumberOfHouseholds();
				Integer networkDetail = entry.getNetworkDetail() != null ? entry.getNetworkDetail() : 6;
				out.write(indent);
				out.write("<adminUnit id =\"" + id + "\" numberOfHouseholds=\"" + nHouseholds + "\" networkDetail=\"" 
						+ networkDetail + "\" />");
				out.write("\n");
				
			}
			
		}
		indent = indent.replaceFirst("\t", "");
		out.write(indent);
		out.write("</areaSet>");
		out.write("\n");
		out.write("\n");
		
		out.write(indent);
		out.write("<areaSet k=\"vicinity\">");
		out.write("\n");
		indent = indent.concat("\t");
		if(configuration.getVicinityIds() != null){
		
			for(String s : configuration.getVicinityIds().split(",")){
			
				out.write(indent);
				AdminUnitEntry entry = configuration.getAdminUnitEntries().get(s);
				String id = entry.getId();
				Integer nHouseholds = entry.getNumberOfHouseholds();
				Integer networkDetail = entry.getNetworkDetail() != null ? entry.getNetworkDetail() : 4;
				out.write("<adminUnit id =\"" + id + "\" numberOfHouseholds=\"" + nHouseholds + "\" networkDetail=\"" 
						+ networkDetail + "\" />");
				out.write("\n");
				
			}
			
		}
		indent = indent.replaceFirst("\t", "");
		out.write(indent);
		out.write("</areaSet>");
		out.write("\n");
		out.write("\n");
		
		Map<String, String> params = configuration.getParams();
		Map<String, String> comments = configuration.getComments();
		
		for(Entry<String, String> param : params.entrySet()){
			
			String comment = comments.get(param.getKey());
			if(comment != null){
				out.write(indent);
				out.write("<!--" + comment + " -->");
				out.write("\n");
			}
			
			out.write(indent);
			out.write("<" + param.getKey() + " v=\"" + param.getValue() + "\"/>");
			out.write("\n");
			out.write("\n");
			
		}
		
		out.write("\n");
		
	}
	
}