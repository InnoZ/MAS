package innoz.run;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;
import innoz.run.controller.ScenarioGenerationController;

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

	public static void main(String args[]){
		
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
		System.out.println("> ");
		
	}
	
}
