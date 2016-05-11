package playground.dhosse.scenarios.generic;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.scenario.ScenarioUtils;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.io.ParseException;

import playground.dhosse.scenarios.generic.network.NetworkCreatorFromPsql;
import playground.dhosse.scenarios.generic.population.PopulationCreator;
import playground.dhosse.scenarios.generic.utils.Geoinformation;

public class ScenarioBuilder {

	private static final Logger log = Logger.getLogger(ScenarioBuilder.class);
	
	//is not meant to be instantiated
	private ScenarioBuilder(){}
	
	public static void main(String args[]){
		
		if(args.length < 1){
			
			throw new RuntimeException("No configuration given. Scenario generation aborts...");
			
		}
		
		Configuration configuration = new Configuration(args[0]);
		
		new File(configuration.getWorkingDirectory()).mkdirs();
		
		try {
			
			OutputDirectoryLogging.initLoggingWithOutputDirectory(configuration.getWorkingDirectory());
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		log.info("########## Creating a scenario from parameters given in configuration file " + args[0]);
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		log.info("########## network generation");
		
		NetworkCreatorFromPsql networkCreator = new NetworkCreatorFromPsql(scenario, configuration);
		try {
			networkCreator.create();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException | ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//TODO: change network creator to use osm db
//		NetworkCreator.main(new String[]{
//				configuration.getCrs(),
//				configuration.getWorkingDirectory() + configuration.getOsmFile(),
//				configuration.getWorkingDirectory() + "network.xml.gz"
//				});
		
		log.info("########## creating transit schedule (not implemented yet)");
		//TODO pt (make it optional?)
		
		log.info("########## loading administrative borders");
		//TODO load admin borders
		//TODO load adjacency matrix
		Set<String> filterIds = new HashSet<>();
		
		//first, add the survey area id(s)
		for(String id : configuration.getSurveyAreaIds()){
			
			filterIds.add(id);
			
		}
		
		try {
			
			Geoinformation.readGeodataFromDatabase(configuration, filterIds, scenario);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		log.info("########## demand generation");
		
		if(configuration.getPopulationType() != null){
			
			try {
				PopulationCreator.run(configuration, scenario);
			} catch (FactoryException e) {
				e.printStackTrace();
			}
			
		} else {
			
			log.warn("Population type was not defined.");
			log.warn("No population will be created.");
			
		}
		
		log.info("########## Scenario created!");
		
		OutputDirectoryLogging.closeOutputDirLogging();
		
	}
	
}
