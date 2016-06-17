package innoz.run;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.run.controller.DatabaseUpdaterControler;
import innoz.run.controller.ScenarioGenerationController;

import java.io.IOException;
import java.util.Scanner;

import com.jcraft.jsch.JSchException;

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

	public static void main(String args[]) throws IOException{
		
		try {
			
			// Create an empty configuration
			Configuration c = ConfigurationUtils.createConfiguration();
			
			boolean alive = false;
			
			// If a network connection to the remote server could be established,
			// proceed with the execution
			if(SshConnector.connectShell(c)){
				alive = true;
			}

			// If no runtime argument was given, start the infinite loop
			if(args.length == 0){
				
				System.out.println("> Welcome user!");
				
				Scanner scanner = new Scanner(System.in);
				
				while(alive){
					
					System.out.print("> ");
					String command = scanner.nextLine();
					
					// Evaluate which command was given by the user and execute it
					if(command.equals("quit") || command.equals("q")){
					
						// If the command was to exit the program, close the existing ssh connection
						// and everything else (e.g. the input stream).
						SshConnector.disconnect();
						alive = false;
						scanner.close();
						System.out.println("> Goodbye!");
					
					} else if(command.startsWith("build-scenario") || command.startsWith("bs")){
						
						ConfigurationUtils.loadConfiguration(command.split(" ")[1], c);
						
						new ScenarioGenerationController(c).run();
						
						c.reset();
						
					} else if(command.equals("help") || command.equals("h")){
						
						printHelpStack();
						
					} else if(command.equals("")){
						
						// Nothing to do
						
					} else if(command.startsWith("write-table") || command.startsWith("wt")){
						
						String inputPlansFile = null;
						boolean writePersons = false;
						
						String[] parts = command.split(" ");
						
						int i = 0;
						
						for(String part : parts){
						
							if(part.startsWith("-")){
								
								if(part.equals("-schema-name") || part.equals("-s")){
									
									ConfigurationUtils.set(c, "databaseSchemaName", parts[i + 1]);
									
								} else if(part.equals("-table-name") || part.equals("-t")){
									
									ConfigurationUtils.set(c, "tripsTableName", parts[i + 1]);
									
								} else if(part.equals("-remote") || part.equals("-r")){
									
									ConfigurationUtils.set(c, "intoMobilityDatahub", true);
									
								} else if(part.equals("-write-persons") || part.equals("-p")){
									
									writePersons = true;
									
								}
								
							} else {
								
								if(!part.equals("write-table") || !part.equals("wt")){
									
									inputPlansFile = part;
									
								}
								
							}
							
							i++;
							
						}
						
						if(inputPlansFile != null){
							
							new DatabaseUpdaterControler(c, inputPlansFile, writePersons).run();
							
						} else {
							
							System.err.println("No plans file specified! Aborting...");
							
						}
						
						c.reset();
						
					} else {
						
						System.out.println("> Unknown command '" + command + "'!");
						System.out.println("> Enter h(elp) for usage information.");
						
					}
					
				}
				
			} else {
				
				if(args[0].equals("build-scenario") || args[0].equals("bs")){
					
					ConfigurationUtils.loadConfiguration(args[1], c);
					
					new ScenarioGenerationController(c).run();
					
					SshConnector.disconnect();
					alive = false;
					
				}
				
			}
			
		} catch (JSchException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * Prints all possible commands this application can execute.
	 * 
	 */
	private static void printHelpStack(){
		
		System.out.println("> ");
		System.out.println("> Usage:");
		System.out.println("> build-scenario (bs) <path-to-file> : Build a new scenario based on the specifications in the given configuration file");
		System.out.println("> quit (q)                           : Exits the program");
		System.out.println("> write-tables [options] (wt) <path>  : Writes the specified plans file into a database table.");
		System.out.println("> options:");
		System.out.println("> -write-persons (-p)                : Writes a table containing the person data of the given plans file (default is 'false').");
		System.out.println("> -remote (-r)                       : Tells the database updater to write the table into a remote database (the MobilityDatabase).");
		System.out.println("> -schema-name (-s)                  : Defines the schema name of the table.");
		System.out.println("> -table-name (-t)                   : Defines the name of the table.");
		System.out.println("> ");
		
	}
	
}
