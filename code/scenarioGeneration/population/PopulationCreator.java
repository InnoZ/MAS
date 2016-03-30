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
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.population.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.HouseholdsWriterV10;
import org.matsim.households.Income.IncomePeriod;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.population.io.commuters.CommuterDataElement;
import playground.dhosse.scenarios.generic.population.io.commuters.CommuterFileReader;
import playground.dhosse.scenarios.generic.population.io.mid.MiDActivity;
import playground.dhosse.scenarios.generic.population.io.mid.MiDHousehold;
import playground.dhosse.scenarios.generic.population.io.mid.MiDParser;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPerson;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlan;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlanElement;
import playground.dhosse.scenarios.generic.population.io.mid.MiDWay;
import playground.dhosse.scenarios.generic.utils.ActivityTypes;
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
			case commuter:	createCommuterPopulation(configuration, scenario);
							break;
			case complete:	createCompletePopulation(configuration, scenario);
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
	
	private static void createCommuterPopulation(Configuration configuration, Scenario scenario){
		
		if(!configuration.getReverseCommuterFile().equals(null) &&
				!configuration.getCommuterFile().equals(null)){
			
			CommuterFileReader cReader = new CommuterFileReader();
			cReader.read(configuration.getReverseCommuterFile(), true);
			cReader.read(configuration.getCommuterFile(), false);

			Population population = scenario.getPopulation();
			
			for(Entry<String, CommuterDataElement> entry : cReader.getCommuterRelations().entrySet()){
				
				String homeId = entry.getValue().getFromId();
				String workId = entry.getValue().toString();
				
				Geometry homeCell = Geoinformation.getGeometries().get(homeId);
				Geometry workCell = Geoinformation.getGeometries().get(workId);
				
				for(int i = 0; i < entry.getValue().getCommuters(); i++){
					
					createOneCommuter(entry.getValue(), population, homeId, workId, homeCell, workCell, i);
					
				}
				
			}
			
		} else {
			
			log.error("Population type was set to " + configuration.getPopulationType().name() + 
					" but no input file was defined!");
			log.warn("No population will be created.");
			
		}
		
	}
	
	private static void createCompletePopulation(Configuration configuration, Scenario scenario){
		
		MiDParser parser = new MiDParser();
		parser.run(configuration);
		
		//TODO further working w/ MiD data and so on
		
		writeOutput(parser, configuration);
		
	}
	
	private static void createOneCommuter(CommuterDataElement element, Population population,
			String homeId, String workId, Geometry homeCell, Geometry workCell, int i){
		
		Person person = population.getFactory().createPerson(Id.createPersonId(homeId + "-" +
				workId + "_" + i));
		Plan plan = population.getFactory().createPlan();
		
		Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME,
				transformation.transform(GeometryUtils.shoot(homeCell)));
		home.setEndTime(6 * 3600);
		plan.addActivity(home);
		
		Leg leg = population.getFactory().createLeg(TransportMode.car);
		plan.addLeg(leg);
		
		Activity work = population.getFactory().createActivityFromCoord(ActivityTypes.WORK,
				transformation.transform(GeometryUtils.shoot(workCell)));
		work.setMaximumDuration(9 * 3600);
		plan.addActivity(work);
		
		Leg returnLeg = population.getFactory().createLeg(TransportMode.car);
		plan.addLeg(returnLeg);
		
		Activity home2 = population.getFactory().createActivityFromCoord(ActivityTypes.HOME,
				transformation.transform(GeometryUtils.shoot(homeCell)));
		plan.addActivity(home2);
		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		population.addPerson(person);
		
	}
	
	private static void writeOutput(MiDParser parser, Configuration configuration){
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		
		for(MiDPerson person : parser.getPersons().values()){
			
			Person p = scenario.getPopulation().getFactory().createPerson(Id.createPersonId(person.getId()));
			PersonUtils.setAge(p, person.getAge());
			PersonUtils.setCarAvail(p, Boolean.toString(person.getCarAvailable()));
			PersonUtils.setEmployed(p, person.isEmployed());
			PersonUtils.setLicence(p, Boolean.toString(person.isHasLicense()));
			PersonUtils.setSex(p, Integer.toString(person.getSex()));
			
			for(MiDPlan plan : person.getPlans()){
				
				Plan pl = scenario.getPopulation().getFactory().createPlan();
				
				for(MiDPlanElement element : plan.getPlanElements()){
					
					if(element instanceof MiDActivity){
						
						MiDActivity act = (MiDActivity)element;
						
						Activity activity = scenario.getPopulation().getFactory().createActivityFromCoord(
								act.getActType(), new Coord(0.0d, 0.0d));
						activity.setStartTime(act.getStartTime());
						activity.setEndTime(act.getEndTime());
						pl.addActivity(activity);
						
					} else{
						
						MiDWay way = (MiDWay)element;
						
						Leg leg = scenario.getPopulation().getFactory().createLeg(way.getMainMode());
						leg.setDepartureTime(way.getStartTime());
						leg.setTravelTime(way.getEndTime() - way.getStartTime());
						pl.addLeg(leg);
						
					}
					
				}
				
				p.addPlan(pl);
				
			}
			
			if(!p.getPlans().isEmpty()){
				scenario.getPopulation().addPerson(p);
			}
			
		}
		
		new PopulationWriter(scenario.getPopulation()).write(configuration.getWorkingDirectory() +
				"/matsimInput/plansFromMiD.xml.gz");
		
		if(configuration.isUsingHouseholds()){

			for(MiDHousehold household : parser.getHouseholds().values()){
			
				Household hh = scenario.getHouseholds().getFactory().createHousehold(
						Id.create(household.getId(), Household.class));
				
				for(String pid : household.getMemberIds()){
					
					Id<Person> personId = Id.createPersonId(pid);
					
					if(scenario.getPopulation().getPersons().containsKey(personId)){
					
						((HouseholdImpl)hh).getMemberIds().add(personId);
						
					}
					
				}
				
				hh.setIncome(scenario.getHouseholds().getFactory().createIncome(household.getIncome(),
						IncomePeriod.month));
				
				if(!hh.getMemberIds().isEmpty()){
					scenario.getHouseholds().getHouseholds().put(hh.getId(), hh);
				}
				
			}
			
			new HouseholdsWriterV10(scenario.getHouseholds()).writeFile(configuration.getWorkingDirectory() +
					"/matsimInput/hhFromMiD.xml.gz");
			
		}
		
	}
	
}
