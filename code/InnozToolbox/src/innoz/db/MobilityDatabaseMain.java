package innoz.db;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.population.io.mid.MiDParser;

public class MobilityDatabaseMain {

	public static void main(String args[]){
		
		if(args.length > 0){

			Configuration configuration = new Configuration(args[0]);
			
			MiDParser parser = new MiDParser();
			parser.run(configuration);
			
		} else {
			
			throw new RuntimeException("You must pass a configuration file as runtime argument!");
			
		}
		
	}
	
}