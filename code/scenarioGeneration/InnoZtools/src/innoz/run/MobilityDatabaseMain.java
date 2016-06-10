package innoz.run;

import innoz.config.Configuration;
import innoz.config.ConfigurationUtils;
import innoz.config.SshConnector;

import java.awt.EventQueue;
import java.io.Console;
import java.io.IOException;

import com.jcraft.jsch.JSchException;

public class MobilityDatabaseMain {

	public static void main(String args[]){
		
		try {
			
			Runtime.getRuntime().exec("clear");
			
			SshConnector.connect(ConfigurationUtils.createConfiguration());
			
			System.out.println("Welcome user!");
			
			Console console = System.console();
			
			boolean alive = true;
			
			while(alive){
				
				System.out.print(">");
				String command = console.readLine();
				
				if(command.equals("exit")){
				
					SshConnector.disconnect();
					alive = false;
					System.out.println("Goodbye!");
				
				} else if(command.startsWith("sg")){
					
					Configuration configuration = ConfigurationUtils.createConfiguration();
					ConfigurationUtils.loadConfiguration(command.split(" ")[1], configuration);
					
					EventQueue.invokeLater(new ScenarioGenerationController(configuration));
					
				} else if(command.equals("help")){
					
					printHelpStack();
					
				}
				
			}
		
		} catch (IOException | JSchException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private static void printHelpStack(){
		
		System.out.println("exit             : Quits the program");
		System.out.println("sg <path-to-file>: Generate a new scenario based on the specifications in the given configuration file");
		
	}
	
}
