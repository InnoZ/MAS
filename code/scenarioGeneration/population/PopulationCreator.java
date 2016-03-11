package playground.dhosse.scenarios.generic.population;

import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.population.io.commuters.CommuterFileReader;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.utils.GeometryUtils;

import com.vividsolutions.jts.geom.Geometry;


public class PopulationCreator {
	
	private static final Logger log = Logger.getLogger(PopulationCreator.class);
	
	private static CoordinateTransformation transformation;
	
	public static void run(Configuration configuration){
		
		log.info("Selected type of population: " + configuration.getPopulationType().name());
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		transformation = TransformationFactory.getCoordinateTransformation(
				TransformationFactory.WGS84, configuration.getCrs());
		
		switch(configuration.getPopulationType()){
		
			case dummy: 	createDummyPopulation(scenario);
							break;
			case commuter:	createCommuterPopulation(configuration);
							break;
			case complete:	createCompletePopulation(configuration);
							break;
			default: 		break;
		
		}
		
	}
	
	private static void createDummyPopulation(Scenario scenario){
		
		for(Entry<String,Geometry> fromEntry : Geoinformation.getGeometries().entrySet()){
			
			for(Entry<String,Geometry> toEntry : Geoinformation.getGeometries().entrySet()){

				for(int i = 0; i < 1000; i++){

					Person person = scenario.getPopulation().getFactory().createPerson(Id.createPersonId(
							fromEntry.getKey() + "_" + toEntry.getKey() + "-" + i));
					Plan plan = scenario.getPopulation().getFactory().createPlan();
					
					Coord homeCoord = transformation.transform(GeometryUtils.shoot(fromEntry.getValue()));
					Coord workCoord = transformation.transform(GeometryUtils.shoot(toEntry.getValue()));
					
					Activity home = scenario.getPopulation().getFactory().createActivityFromCoord("home",
							homeCoord);
					home.setEndTime(7 * 3600);
					plan.addActivity(home);
					
					Leg leg = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
					plan.addLeg(leg);
					
					Activity work = scenario.getPopulation().getFactory().createActivityFromCoord("work",
							workCoord);
					work.setEndTime(18 * 3600);
					plan.addActivity(work);
					
					Leg leg2 = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
					plan.addLeg(leg2);
					
					Activity home2 = scenario.getPopulation().getFactory().createActivityFromCoord("home",
							homeCoord);
					plan.addActivity(home2);
					
					person.addPlan(plan);
					person.setSelectedPlan(plan);
					
					scenario.getPopulation().addPerson(person);
					
				}
				
			}
			
		}
		
	}
	
	private static void createCommuterPopulation(Configuration configuration){
		
		if(!configuration.getReverseCommuterFile().equals(null) &&
				!configuration.getCommuterFile().equals(null)){
			
			CommuterFileReader cReader = new CommuterFileReader();
			cReader.read(configuration.getReverseCommuterFile(), true);
			cReader.read(configuration.getCommuterFile(), false);
			
		} else {
			
			log.error("Population type was set to " + configuration.getPopulationType().name() + 
					" but no input file was defined!");
			log.warn("No population will be created.");
			
		}
		
	}
	
	private static void createCompletePopulation(Configuration configuration){
		
	}
	
}
