package com.innoz.toolbox.run;

import java.io.IOException;
import java.io.PrintWriter;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.ConfigurationUtils;
import com.innoz.toolbox.config.SshConnector;
import com.innoz.toolbox.run.controller.ScenarioGenerationController;
import com.jcraft.jsch.JSchException;

import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.FileNameCompleter;

/**
 * 
 * Starting point for the shell version of the scenario generation framework.
 * Currently, there are two ways to run the main method:
 * <ol>
 * <li> start the application with {@code java -cp InnoZscenarioGeneration.jar innoz.run.Runner}.
 * The user can execute (sub-)methods using predefined commands (see below).
 * <li> start the application like above but give a runtime argument (e.g. build-scenario and a
 * configuration file). This will execute the scenario generation process and exit the program
 * after it is finished with its task.
 * </ol>
 * 
 * @author dhosse
 *
 */
public class Runner {

	private Runner(){};
	
	public static void main(String args[]) throws IOException {
		
		// Create an empty configuration
		Configuration c = ConfigurationUtils.createConfiguration();
		
		boolean serverConnection = false;

		ConsoleReader reader = new ConsoleReader();
		reader.addCompleter(new FileNameCompleter());
		reader.setCompletionHandler(new CandidateListCompletionHandler());
		reader.setPrompt("> ");
		
		// If no runtime argument was given, start the infinite loop
		if(args.length == 0){
			
			PrintWriter writer = new PrintWriter(reader.getOutput());
			
			printWelcomeMessage(writer);
			printHelpStack(writer);
			
			String command;
			
			while((command = reader.readLine()) != null){
			
				try {
				
					// Evaluate which command was given by the user and execute it
					if(command.equals("quit") || command.equals("q")){
					
						// If the command was to exit the program, close the existing ssh connection
						// and everything else (e.g. the input stream).
						if(serverConnection){
							
							SshConnector.disconnect();
						
						}
						writer.println("> Goodbye");
						writer.close();
						reader.close();
						break;
					
					} else if(command.startsWith("build-scenario") || command.startsWith("bs")){
						
						ConfigurationUtils.loadConfiguration(command.split(" ")[1], c);
						
						if(!serverConnection && command.contains(" -c")){
							
							serverConnection = SshConnector.connectShell(c, reader);
							reader.setPrompt("> ");
							
						}
						
						SshConnector.setDbUserData(c, reader);
						
						reader.setPrompt("> ");
						
						new ScenarioGenerationController(c).run();
						
					} else if(command.equals("help") || command.equals("h")){
						
						printHelpStack(writer);
						
					} else if(command.equals("")){
						
						// Nothing to do
						
					} else if(command.equals("connect") || command.equals("c")){
						
						if(!serverConnection){
							
							serverConnection = SshConnector.connectShell(c, reader);
							reader.setPrompt("> ");
							
						} else {
	
							writer.println("> You already are connected to the MobilityDatahub!");
							writer.println("> Ignoring this command...");
							
						}
						
					} else if(command.equals("disconnect") || command.equals("d")){
						
						if(serverConnection){
							
							SshConnector.disconnect();
							serverConnection = false;
							
						} else {
							
							writer.println("> You are not connected to the MobilityDatahub yet!");
							writer.println("> Ignoring this command...");
							
						}
						
					} else {
						
						writer.println("> Unknown command '" + command + "'!");
						writer.println("> Enter h(elp) for usage information.");
						
					}

				} catch(JSchException e){
					
					e.printStackTrace();
					reader.setPrompt("> ");
					
				}
				
			}
			
		} else {
			
			if(args[0].equals("build-scenario") || args[0].equals("bs")){
				
				try {
				
					ConfigurationUtils.loadConfiguration(args[1], c);
					
					if(args[0].contains(" -c ")){
						
							SshConnector.connectShell(c, reader);
						
					}
					
					new ScenarioGenerationController(c).run();
					
					SshConnector.disconnect();
				
				} catch (JSchException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
	private static void printWelcomeMessage(PrintWriter writer){
		
		writer.println("> Welcome user.");
		writer.println("> This program can either be used for local development or you can connect to the MobilityDatahub.");
		
	}
	
	/**
	 * 
	 * Prints all possible commands this application can execute.
	 * 
	 */
	private static void printHelpStack(PrintWriter writer){
		
		writer.println(">");
		writer.println("> Usage:");
		writer.println("> build-scenario (bs) <path-to-file> : Build a new scenario based on the specifications in the given configuration file");
		writer.println("> connect (c)                        : Initiates a process to connect your computer with the MobilityDatahub server.");
		writer.println("> disconnect (d)                     : Closes an existing ssh connection to the MobilityDatahub.");
		writer.println("> quit (q)                           : Exits the program");
		writer.println("> write-table (wt) [options] <path>  : Writes the specified plans file into a database table.");
		writer.println("> options:");
		writer.println("> -attributes-file (-a)              : Path to a MATSim person attributes file. Tells the program to write a persons table.");
		writer.println("> -network-file (-nf)                : Path to a MATSim network file. Mandatory for creating the carsharing stations table.");
		writer.println("> -plans-file (-pf)                  : Path to a MATSim plans file. Tells the program to write a trips table.");
		writer.println("> -remote (-r)                       : Tells the database updater to write the table into a remote database (the MobilityDatabase).");
		writer.println("> -schema-name (-s)                  : Defines the schema name of the table.");
		writer.println("> -table-suffix (-t)                 : Defines a suffix for the tables to be created. This string is APPENDED to the kind of table that is being created (e.g. trips_<table-name>)");
		writer.println("> -vehicles-file (-vf)               : Path to a carsharing vehicles file. Tells the program to write a carsharing stations table.");
		writer.println(">");
		
	}
	
}
