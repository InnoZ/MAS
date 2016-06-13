package innoz.run;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;

import java.awt.EventQueue;
import java.io.Console;

import com.jcraft.jsch.JSchException;

public class MobilityDatabaseMain {

	private static Configuration c;
	
	public static void main(String args[]){
		
		try {
			
//			Runtime.getRuntime().exec("clear");
			
			c = ConfigurationUtils.createConfiguration();
			
			boolean alive = false;
			
			if(SshConnector.connectShell(c)){
				alive = true;
			}
			
			System.out.println("> Welcome user!");
			
			Console console = System.console();
			
			while(alive){
				
				System.out.print("> ");
				String command = console.readLine();
				
				if(command.equals("exit") || command.equals("e")){
				
					SshConnector.disconnect();
					alive = false;
					System.out.println("> Goodbye!");
				
				} else if(command.startsWith("sg")){
					
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
		
		} catch (JSchException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private static void printHelpStack(){
		
		System.out.println("> ");
		System.out.println("> Usage:");
		System.out.println("> e(xit)           : Quits the program");
		System.out.println("> sg <path-to-file>: Generate a new scenario based on the specifications in the given configuration file");
		System.out.println("> ");
		
	}
	
}
