package playground.dhosse.scenarios.generic.population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.misc.Time;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.Income.IncomePeriod;
import org.matsim.households.IncomeImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.vehicles.Vehicle;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.population.io.mid.MiDActivity;
import playground.dhosse.scenarios.generic.population.io.mid.MiDHousehold;
import playground.dhosse.scenarios.generic.population.io.mid.MiDParser;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPerson;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlan;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlanElement;
import playground.dhosse.scenarios.generic.population.io.mid.MiDWay;
import playground.dhosse.scenarios.generic.utils.ActivityTypes;
import playground.dhosse.scenarios.generic.utils.AdministrativeUnit;
import playground.dhosse.scenarios.generic.utils.Distribution;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.utils.GeometryUtils;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * This class generates an initial demand (MATSim population) for a given scenario. </br>
 * 
 * dhosse, 05/16:
 * At the moment, only demand generation from MiD survey data is supported. Possible additional / alternative
 * data sources would be:
 * <ul>
 * <li> other surveys
 * <li> innoz tracks
 * </ul>
 * 
 * @author dhosse
 *
 */
public class PopulationCreator {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	static final Random random = MatsimRandom.getLocalInstance();
	
	static Comparator<MiDHousehold> householdComparator = new Comparator<MiDHousehold>() {

		@Override
		public int compare(MiDHousehold o1, MiDHousehold o2) {
			return Double.compare(o1.getWeight(), o2.getWeight());
		
		}
		
	};
	
	static Comparator<Geometry> geometryComparator = new Comparator<Geometry>() {

		@Override
		public int compare(Geometry o1, Geometry o2) {

			return Double.compare(o1.getArea(), o2.getArea());
		
		}
		
	};
	
	private static final Logger log = Logger.getLogger(PopulationCreator.class);
	/////////////////////////////////////////////////////////////////////////////////////////
	

	//MEMBERS//////////////////////////////////////////////////////////////////////////////
	private static CoordinateTransformation transformation;
	static Distribution distribution;
	
	private static Coord currentHomeLocation = null;
	private static Coord currentMainActLocation = null;
	private static MiDWay lastLeg = null;
	private static Coord lastActCoord = null;
	static double c = 0d;
	static AdministrativeUnit currentHomeCell;
	static AdministrativeUnit currentMainActCell;
	static List<AdministrativeUnit> currentSearchSpace;
	static AdministrativeUnit lastActCell = null;
	
	static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}
	/////////////////////////////////////////////////////////////////////////////////////////	
	
	// No instance!
	private PopulationCreator(){};
	
	/**
	 * 
	 * This is the "main method" of the demand generation process.
	 * According to what type of population was defined in the given configuration (one of: {@code dummy}, {@code commuter},
	 * {@code complete}), an initial demand is created and added to the MATSim scenario.
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario The MATsim scenario eventually containing all of the information about network, demand etc.
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 */
	public static void run(Configuration configuration, Scenario scenario) throws NoSuchAuthorityCodeException,
		FactoryException {
		
		log.info("Creating population for MATSim scenario...");
		log.info("Selected type of population: " + configuration.getPopulationType().name());

		// Create the coordinate transformation for all of the geometries
		// This could also be done by just passing the auth id strings, but doing it this way suppresses
		// warnings.
		CoordinateReferenceSystem from = CRS.decode("EPSG:4326", true);
		CoordinateReferenceSystem to = CRS.decode(configuration.getCrs(), true);
		transformation = TransformationFactory.getCoordinateTransformation(
				from.toString(), to.toString());

		// Choose the demand generation method according to what type of population was defined in the configuration
		switch(configuration.getPopulationType()){
		
			case dummy: 	createDummyPopulation(scenario);
							break;
			case commuter:	createCommuterPopulation(configuration, scenario);
							break;
			case complete:	createCompletePopulation(configuration, scenario);
							break;
			default: 		break;
		
		}
		
		log.info("...done.");
		
	}
	
	/**
	 * 
	 * Creates a "dummy" population, meaning:
	 * <ul>
	 * <li> No input data of any kind (e.g. surveys, tracks) is used to generate travel chains
	 * <li> No person attributes are created (e.g. age, employment, car ownership, ...)
	 * <li> The transport mode of all the persons is chosen randomly
	 * <li> The activity locations are chosen randomly within the survey area
	 * </ul>
	 * 
	 * The resulting initial demand contains of persons whose plans only consist of three activities (home-work-home)
	 * and connecting legs.
	 * 
	 * @param scenario The MATsim scenario eventually containing all of the information about network, demand etc.
	 */
	private static void createDummyPopulation(Scenario scenario){
		
		log.info("Creating a dummy population without any preferences...");
		
		// From each administrative unit to each administrative unit, create a certain amount of commuters
		for(Entry<String,AdministrativeUnit> fromEntry : Geoinformation.getAdminUnits().entrySet()){
			
			for(Entry<String,AdministrativeUnit> toEntry : Geoinformation.getAdminUnits().entrySet()){

				//TODO maybe make the max number configurable...
				for(int i = 0; i < 1000; i++){

					// Create a new person and an empty plan
					Person person = scenario.getPopulation().getFactory().createPerson(Id.createPersonId(
							fromEntry.getKey() + "_" + toEntry.getKey() + "-" + i));
					Plan plan = scenario.getPopulation().getFactory().createPlan();
					
					// Shoot the activity coords (home activity located inside of the FROM admin unit,
					// work activity inside of the TO admin unit)
					Coord homeCoord = transformation.transform(GeometryUtils.shoot(fromEntry.getValue()
							.getGeometry(),random));
					Coord workCoord = transformation.transform(GeometryUtils.shoot(toEntry.getValue()
							.getGeometry(),random));
					
					// Create activities and legs and add them to the plan
					Activity home = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
							homeCoord);
					home.setEndTime(7 * 3600);
					plan.addActivity(home);
					
					Leg leg = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
					plan.addLeg(leg);
					
					Activity work = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.WORK,
							workCoord);
					work.setEndTime(18 * 3600);
					plan.addActivity(work);
					
					Leg leg2 = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
					plan.addLeg(leg2);
					
					Activity home2 = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
							homeCoord);
					plan.addActivity(home2);
					
					// Add the plan to the current person and make it the selected one
					person.addPlan(plan);
					person.setSelectedPlan(plan);

					// Add the current person to the population
					scenario.getPopulation().addPerson(person);
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * @param configuration
	 * @param scenario
	 */
	private static void createCommuterPopulation(Configuration configuration, Scenario scenario){
		
//		if(!configuration.getReverseCommuterFile().equals(null) &&
//				!configuration.getCommuterFile().equals(null)){
//			
//			CommuterFileReader cReader = new CommuterFileReader();
//			cReader.read(configuration.getReverseCommuterFile(), true);
//			cReader.read(configuration.getCommuterFile(), false);
//
//			Population population = scenario.getPopulation();
//			
//			for(Entry<String, CommuterDataElement> entry : cReader.getCommuterRelations().entrySet()){
//				
//				String homeId = entry.getValue().getFromId();
//				String workId = entry.getValue().toString();
//				
//				Geometry homeCell = Geoinformation.getAdminUnits().get(homeId).getGeometry();
//				Geometry workCell = Geoinformation.getAdminUnits().get(workId).getGeometry();
//				
//				for(int i = 0; i < entry.getValue().getCommuters(); i++){
//					
//					createOneCommuter(entry.getValue(), population, homeId, workId, homeCell, workCell, i);
//					
//				}
//				
//			}
//			
//		} else {
//			
//			log.error("Population type was set to " + configuration.getPopulationType().name() + 
//					" but no input file was defined!");
//			log.warn("No population will be created.");
//			
//		}
		
	}
	
	private static void createCompletePopulation(Configuration configuration, Scenario scenario){
		
		MiDParser parser = new MiDParser();
		parser.run(configuration);
		
		distribution = new Distribution(scenario.getNetwork(), parser, transformation);
		
		if(configuration.isUsingHouseholds()){
		
			createHouseholds(configuration, scenario, parser);
			
		} else {
			
			createPersons(configuration, scenario, parser);
			
		}
		
	}
	
	private static void createHouseholds(Configuration configuration, Scenario scenario, MiDParser parser){
		
		Population population = scenario.getPopulation();
		ObjectAttributes personAttributes = new ObjectAttributes();
		scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		//TODO number of households...
		//nhh hb 308705
		//nhh gap 36531
		//berlin: 1966000
		//dessau-rosslau: 45106
		//os: 84218
		
		int[] hhDistribution = new int[8];

		List<MiDHousehold> households = new ArrayList<>();
		households.addAll(parser.getHouseholds().values());
		Collections.sort(households, householdComparator);
		
		//TODO number of households in db table...
		for(int i = 0; i < configuration.getNumberOfHouseholds(); i++){

			currentHomeCell = null;
			AdministrativeUnit au = null;
			double r = random.nextDouble() * Geoinformation.getTotalWeightForLanduseKey("residential");
			
			double r2 = 0.;
			
			for(AdministrativeUnit admin : Geoinformation.getAdminUnits().values()){
				
				r2 += admin.getWeightForKey("residential");
				if(r <= r2){
					au = admin;
					break;
				}
				
			}
			
			MiDHousehold template = null;

			double accumulatedWeight = 0.;
			double rand = random.nextDouble() * parser.getSumOfHouseholdWeights();
			
			for(MiDHousehold hh : households){
				
				accumulatedWeight += hh.getWeight();
				if(accumulatedWeight >= rand){
					template = hh;
					hhDistribution[hh.getNPersons()-1]++;
					break;
				}
				
			}
			
			int nPersons = template.getNPersons();

			Household household = new HouseholdImpl(Id.create(au.getId() + "_" + i, Household.class));
			household.setIncome(new IncomeImpl(template.getIncome(), IncomePeriod.month));
			((HouseholdImpl)household).setMemberIds(new ArrayList<Id<Person>>());

			//set global home location for the entire household
			Coord homeLocation = null;

			if(au.getLanduseGeometries().containsKey("residential")){
				
				double p = random.nextDouble() * au.getWeightForKey("residential");
				accumulatedWeight = 0.;
				
				for(Geometry g : au.getLanduseGeometries().get("residential")){
					
					accumulatedWeight += g.getArea();
					
					if(p <= accumulatedWeight){
						homeLocation = transformation.transform(GeometryUtils.shoot(g, random));
						currentHomeCell = au;
						break;
					}
					
				}
				
			} else {
				
				homeLocation = transformation.transform(GeometryUtils.shoot(au.getGeometry(), random));
				currentHomeCell = au;
				
			}

			//create as many persons as were reported and add them to the household
			for(int j = 0; j < nPersons; j++){
				
				String personId = template.getMemberIds().get(j);
				MiDPerson templatePerson = parser.getPersons().get(personId);
				
				Person person = createPerson(templatePerson, population, personAttributes, random.nextDouble(),
						population.getPersons().size(), homeLocation);
				if(person != null){
					population.addPerson(person);
					household.getMemberIds().add(person.getId());
				}

			}
			
			//only if we model non-generic cars
			if(configuration.isUsingCars()){

				for(int k = 0; k < template.getNCars(); k++){
					
					Vehicle vehicle = scenario.getVehicles().getFactory().createVehicle(Id.create(template.getId() +
							"_v_" + k, Vehicle.class), null);
					scenario.getVehicles().addVehicle(vehicle);
					household.getVehicleIds().add(vehicle.getId());
					
				}
				
			}
			
			scenario.getHouseholds().getHouseholds().put(household.getId(), household);
			
		}
		
	}
	
	private static void createPersons(Configuration configuration, Scenario scenario, MiDParser parser){
		
		Population population = scenario.getPopulation();
		ObjectAttributes personAttributes = new ObjectAttributes();
		scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
			
			double personalRandom = random.nextDouble();
			
			Map<String,MiDPerson> templatePersons = parser.getPersons();
			MiDPerson personTemplate = null;
			
			personTemplate = PersonUtils.getTemplate(templatePersons,
					personalRandom * PersonUtils.getTotalWeight(templatePersons.values()));
			
			//TODO: number of inhabitants for admin units
			for(int i = 0; i < 10000; i++){
				
				population.addPerson(createPerson(personTemplate, population, personAttributes, personalRandom, i, null));
				
			}
			
		}
		
	}

	@SuppressWarnings("deprecation")
	private static Person createPerson(MiDPerson personTemplate, Population population,
			ObjectAttributes personAttributes, double personalRandom, int i, Coord homeCoord) {

		currentMainActLocation = null;
		lastLeg = null;
		lastActCoord = null;

//		if(population.getPersons().size() > 13){
//			System.out.println();
//		}
		
		Person person = population.getFactory().createPerson(Id.createPersonId(currentHomeCell.getId() + "_" + personTemplate.getId() + "_" + i));
		Plan plan = population.getFactory().createPlan();
		
		//TODO employed, car avail, license
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_SEX, personTemplate.getSex());
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_AGE, personTemplate.getAge());
		
		playground.dhosse.utils.PersonUtils.setAge(person, personTemplate.getAge());
		playground.dhosse.utils.PersonUtils.setEmployed(person, personTemplate.isEmployed());
		String carAvail = personTemplate.getCarAvailable() ? "always" : "never";
		playground.dhosse.utils.PersonUtils.setCarAvail(person, carAvail);
		String hasLicense = personTemplate.hasLicense() ? "yes" : "no";
		playground.dhosse.utils.PersonUtils.setLicence(person, hasLicense);
		
		if(personTemplate.getPlans().size() > 0){

			//select a template plan from the mid survey to create a matsim plan
			MiDPlan templatePlan = null;
			
			//if there is only one plan, make it the template
			if(personTemplate.getPlans().size() < 2){
				
				templatePlan = personTemplate.getPlans().get(0);
				
			} else {

				//otherwise, randomly draw a plan from the collection
				double planRandom = personalRandom * personTemplate.getWeightOfAllPlans();
				double accumulatedWeight = 0.;
				
				for(MiDPlan p : personTemplate.getPlans()){
					
					accumulatedWeight += p.getWeigt();
					
					if(planRandom <= accumulatedWeight){
					
						templatePlan = p;
						break;
					
					}
					
				}
				
			}
			
			currentHomeLocation = homeCoord != null ? homeCoord :
				transformation.transform(GeometryUtils.shoot(currentHomeCell.getGeometry(),random));
			lastActCoord = currentHomeLocation;

			if(templatePlan.getPlanElements().size() > 1){
				
			//////////////////////////////////////////////////////////////////////
							
			String mainMode = templatePlan.getMainActIndex() > 0 ? 
					((MiDWay)templatePlan.getPlanElements().get(templatePlan.getMainActIndex()-1)).getMainMode() :
						((MiDWay)templatePlan.getPlanElements().get(1)).getMainMode();
			currentMainActCell = locateActivityInCell(templatePlan.getMainActType(), mainMode, personTemplate);
			
			currentMainActLocation = shootLocationForActType(currentMainActCell, templatePlan.getMainActType(),
					0d, templatePlan, mainMode, personTemplate);
			
			currentSearchSpace = new ArrayList<>();
			currentSearchSpace.add(currentHomeCell);
			currentSearchSpace.add(currentMainActCell);
			
			c = CoordUtils.calcDistance(currentHomeLocation, currentMainActLocation);
			
			for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
				
				double a = CoordUtils.calcDistance(transformation.transform(
						MGC.point2Coord(currentHomeCell.getGeometry().getCentroid())),
						transformation.transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				double b = CoordUtils.calcDistance(transformation.transform(
						MGC.point2Coord(currentMainActCell.getGeometry().getCentroid())),
						transformation.transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				
				if(a + b < 2 * c){
					
					currentSearchSpace.add(au);
					
				}
				
			}
			
			lastActCell = null;
				
				for(int j = 0; j < templatePlan.getPlanElements().size(); j++){
					
					MiDPlanElement mpe = templatePlan.getPlanElements().get(j);
					
					if(mpe instanceof MiDActivity){
						
						plan.addActivity(createActivity(population, personTemplate, plan, templatePlan, mpe));
						
					} else {
						
						plan.addLeg(createLeg(population, plan, mpe));
						
					}
					
				}
				
			} else {
				
				// Create a 24hrs home activity
				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
				home.setMaximumDuration(24 * 3600);
				home.setStartTime(0);
				home.setEndTime(24 * 3600);
				plan.addActivity(home);
				
			}
			
		} else {
			
			// Create a 24hrs home activity
			Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
			home.setMaximumDuration(24 * 3600);
			home.setStartTime(0);
			home.setEndTime(24 * 3600);
			plan.addActivity(home);
			
		}
		
		//in the end: add the person to the population
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		return person;
		
	}

	private static Leg createLeg(Population population, Plan plan,
			MiDPlanElement mpe) {
		
		MiDWay way = (MiDWay)mpe;
		String mode = way.getMainMode();
		double departure = way.getStartTime();
		double ttime = way.getEndTime() - departure;
		
		Leg leg = population.getFactory().createLeg(mode);
//		leg.setDepartureTime(departure);
		leg.setTravelTime(ttime);

		lastLeg = way;
		
		return leg;
		
	}
	
	private static AdministrativeUnit locateActivityInCell(String activityType, String mode, MiDPerson personTemplate){
		
//		if(mode.equals(TransportMode.walk)) return currentHomeCell;
		
		AdministrativeUnit result = null;
		
		Map<String, Double> toId2Disutility = new HashMap<String, Double>();
		double sumOfWeights = 0d;
		
		for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
			
			double disutility = Double.NEGATIVE_INFINITY;
			
			if(mode != null){
				
				disutility = distribution.getDisutilityForActTypeAndMode(currentHomeCell.getId(), au.getId(), activityType, mode);
				
				if(Double.isFinite(disutility)){
					
					toId2Disutility.put(au.getId(), disutility);
					sumOfWeights += disutility;
					
				}
				
			} else {
				
				Set<String> modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt + "," + TransportMode.walk);
				
				if(personTemplate.getCarAvailable()){
					modes.add(TransportMode.ride);
					if(personTemplate.hasLicense()){
						modes.add(TransportMode.car);
					}
				}
				
				for(String m : modes){
					
					disutility = distribution.getDisutilityForActTypeAndMode(currentHomeCell.getId(), au.getId(), activityType, m);
					
					if(Double.isFinite(disutility)){
						
						toId2Disutility.put(au.getId(), disutility);
						sumOfWeights += disutility;
						
					}
					
				}
				
			}
			
		}
		
		double r = random.nextDouble() * sumOfWeights;
		double accumulatedWeight = 0d;
		
		for(Entry<String, Double> entry : toId2Disutility.entrySet()){
			
			accumulatedWeight += entry.getValue();
			if(r <= accumulatedWeight){
				result = Geoinformation.getAdminUnits().get(entry.getKey());
				break;
			}
			
		}
		
		return result;
		
	}
	
	private static AdministrativeUnit locateActivityInCell(String fromId, String activityType, String mode, MiDPerson personTemplate, double distance){
		
		if(mode != null){

			if(mode.equals(TransportMode.walk)) return Geoinformation.getAdminUnits().get(fromId);
			
		}
		
		AdministrativeUnit result = null;
		
		Map<String, Double> toId2Disutility = new HashMap<String, Double>();
		double sumOfWeights = 0d;
		
		List<AdministrativeUnit> adminUnits = null;
		if(currentSearchSpace != null){
			if(currentSearchSpace.size() > 0){
				adminUnits = currentSearchSpace;
			}
		}
		if(adminUnits == null){
			adminUnits = (List<AdministrativeUnit>) Geoinformation.getAdminUnits().values();
		}
		
		for(AdministrativeUnit au : adminUnits){
			
			if(distribution.getDistance(fromId, au.getId()) > distance / 1.3){
				continue;
			}
			
			double disutility = Double.NEGATIVE_INFINITY;
			
			if(mode != null){
				
				disutility = distribution.getDisutilityForActTypeAndMode(fromId, au.getId(), activityType, mode);
				
				if(Double.isFinite(disutility)){
					
					toId2Disutility.put(au.getId(), disutility);
					sumOfWeights += disutility;
					
				}
				
			} else {
				
				Set<String> modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt + "," + TransportMode.walk);
				
				if(personTemplate.getCarAvailable()){
					modes.add(TransportMode.ride);
					if(personTemplate.hasLicense()){
						modes.add(TransportMode.car);
					}
				}
				
				for(String m : modes){
					
					disutility = distribution.getDisutilityForActTypeAndMode(fromId, au.getId(), activityType, m);
					
					if(Double.isFinite(disutility)){
						
						toId2Disutility.put(au.getId(), disutility);
						sumOfWeights += disutility;
						
					}
					
				}
				
			}
			
		}
		
		double r = random.nextDouble() * sumOfWeights;
		double accumulatedWeight = 0d;
		
		for(Entry<String, Double> entry : toId2Disutility.entrySet()){
			
			accumulatedWeight += entry.getValue();
			if(r <= accumulatedWeight){
				result = Geoinformation.getAdminUnits().get(entry.getKey());
				break;
			}
			
		}
		
		return result;
		
	}

	private static Activity createActivity(Population population, MiDPerson personTemplate, Plan plan, MiDPlan templatePlan,
			MiDPlanElement mpe) {

		AdministrativeUnit au = null;
		
		MiDActivity act = (MiDActivity)mpe;
		String type = act.getActType();
		double start = act.getStartTime();
		double end = act.getEndTime();
		
		double distance = 0.;
		String mode = null;
		if(lastLeg != null){
			distance = lastLeg.getTravelDistance();
			mode = lastLeg.getMainMode();
		}
		if(lastActCoord == null){
			
			
			lastActCoord = currentHomeLocation;
			
		}

		Coord coord = null;

		//check type of the next activity
		if(type.equals(ActivityTypes.HOME)){
			
			//set home location
			coord = currentHomeLocation;
			au = currentHomeCell;
			
		} else if(act.getId() == templatePlan.getMainActId()){
			
			au = currentMainActCell;
			
			//if we already know the main act location, set it
			//else, shoot a new coordinate for the main act location
			if(currentMainActLocation != null){
				
				coord = currentMainActLocation;
				
			} else {
				
				coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
				
				currentMainActLocation = coord;
				
			}
			
		} else {
			
			if(lastActCell == null) lastActCell = currentHomeCell;
			au = locateActivityInCell(lastActCell.getId(), type, mode, personTemplate,distance);
			
			if(au == null) au = lastActCell;
			
			double a = 0;
			double b = 0;
			
			int cnt = 0;
			do{
			
				cnt++;
				coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
				a = CoordUtils.calcDistance(currentHomeLocation, coord);
				b = CoordUtils.calcDistance(currentMainActLocation, coord);
			
			} while(a + b > 2 * c && cnt < 10);
				
		}
		
		Activity activity = population.getFactory().createActivityFromCoord(type, coord);
		activity.setStartTime(start);
		activity.setEndTime(end);
		
		if(end == 0 || end > Time.MIDNIGHT){
		
			activity.setMaximumDuration(end - start + Time.MIDNIGHT);
			activity.setEndTime(Time.MIDNIGHT);
		
		} else{
			
			if(end - start < 900){
			
				//set the activity duration to at least 1/4 hour
				activity.setMaximumDuration(900);
			
			} else{
				
				activity.setMaximumDuration(end - start);
				
			}
			
		}
		
		lastActCoord = activity.getCoord();
		lastActCell = au;
		
		return activity;
		
	}

	static int cnt = 0;
	static int cntNoClosest = 0;
	
	private static Coord shootLocationForActType(AdministrativeUnit au, String actType, double distance,
			MiDPlan templatePlan, String mode, MiDPerson personTemplate) {

		if(mode != null){
			if(mode.equals(TransportMode.walk) && distance > 2000){
				distance = 500 + random.nextInt(1001);
			}
		}
		
		double d = distance / 1.3; //divide the distance by the beeline distance factor
		double minFactor = 0.75;
		double maxFactor = 1.25;
		
		//TODO widerstandskurve statt d aus survey
		List<Geometry> closest = new ArrayList<>();
		
		if(au.equals(currentMainActCell)){
			
			closest = currentMainActCell.getLanduseGeometries().get(actType);
			
			if(closest != null){

				int index = random.nextInt(closest.size());
				Geometry area = closest.get(index);
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			}
			
		}
		
		closest = au.getLanduseGeometries().get(actType);
		
		if(closest != null){
			
			if(!closest.isEmpty()){
				
				cnt++;
				
				int index = random.nextInt(closest.size());
				Geometry area = closest.get(index);
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			} else {
				
				cntNoClosest++;
				
				Geometry area = Geoinformation.getQuadTreeForActType(actType).getClosest(lastActCoord.getX(), lastActCoord.getY());
				
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			}
		
		} else {
			
			closest = (List<Geometry>) Geoinformation.getQuadTreeForActType(actType).getRing(lastActCoord.getX(),
					lastActCoord.getY(), d * minFactor, d * maxFactor);
			
			if(!closest.isEmpty()){
				
				int index = random.nextInt(closest.size());
				Geometry area = closest.get(index);
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			} else {
				
				cntNoClosest++;
				
				Geometry area = Geoinformation.getQuadTreeForActType(actType).getClosest(lastActCoord.getX(), lastActCoord.getY());
				
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			}
			
		}
		
	}
	
}
