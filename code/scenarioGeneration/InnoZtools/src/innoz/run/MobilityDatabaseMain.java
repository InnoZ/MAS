package innoz.run;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;

import java.util.Scanner;

import com.jcraft.jsch.JSchException;

public class MobilityDatabaseMain {

	public static void main(String args[]){
		
		try {
			
			Configuration c = ConfigurationUtils.createConfiguration();
			
			boolean alive = false;
			
			if(SshConnector.connectShell(c)){
				alive = true;
			}
			
			if(args.length == 0){

				System.out.println("> Welcome user!");
				
				Scanner scanner = new Scanner(System.in);
				
				while(alive){
					
					System.out.print("> ");
					String command = scanner.nextLine();
					
					if(command.equals("exit") || command.equals("e")){
					
						SshConnector.disconnect();
						alive = false;
						scanner.close();
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
				
			} else {
				
				if(args[0].equals("sg")){
					
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
	
	private static void printHelpStack(){
		
		System.out.println("> ");
		System.out.println("> Usage:");
		System.out.println("> e(xit)           : Quits the program");
		System.out.println("> sg <path-to-file>: Generate a new scenario based on the specifications in the given configuration file");
		System.out.println("> ");
		
	}
	
}
