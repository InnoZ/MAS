package com.innoz.toolbox.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.innoz.toolbox.config.groups.ConfigurationGroup;

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
		
		writeConfigurationGroup(configuration.misc(), out);
		writeConfigurationGroup(configuration.psql(), out);
		writeConfigurationGroup(configuration.scenario(), out);
		writeConfigurationGroup(configuration.surveyPopulation(), out);
		
	}
	
	private void writeConfigurationGroup(ConfigurationGroup configuration, BufferedWriter out) throws IOException{
		
		out.write(indent);
		out.write("<" + configuration.groupName + ">");
		out.write(NEWLINE);
		out.write(NEWLINE);

		indent = indent.concat(TAB);
		
		Map<String, String> params = configuration.getParams();
		Map<String, String> comments = configuration.getComments();
		
		for(Entry<String, String> param : params.entrySet()){
			
			String comment = comments.get(param.getKey());
			if(comment != null){
				out.write(indent);
				out.write("<!-- " + comment + " -->");
				out.write(NEWLINE);
			}
			
			out.write(indent);
			out.write("<" + param.getKey() + " v=\"" + param.getValue() + "\"/>");
			out.write(NEWLINE);
			out.write(NEWLINE);
			
		}
		
		Map<String, ConfigurationGroup> paramSets = configuration.getParameterSets();
		
		for(ConfigurationGroup set : paramSets.values()){
			
			writeConfigurationGroup(set, out);
			
		}
		
		indent = indent.replaceFirst(TAB, "");
		
		out.write(indent);
		out.write("</" + configuration.groupName + ">");
		out.write(NEWLINE);
		out.write(NEWLINE);
		
	}
	
}