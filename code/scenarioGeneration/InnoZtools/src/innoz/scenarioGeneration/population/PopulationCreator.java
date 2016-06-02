package innoz.scenarioGeneration.population;

import innoz.config.Configuration;
import innoz.io.database.MidDatabaseParser;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Distribution;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlan;
import innoz.scenarioGeneration.population.surveys.SurveyPlanActivity;
import innoz.scenarioGeneration.population.surveys.SurveyPlanElement;
import innoz.scenarioGeneration.population.surveys.SurveyPlanWay;
import innoz.scenarioGeneration.population.surveys.SurveyVehicle;
import innoz.scenarioGeneration.population.utils.PersonUtils;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.scenarioGeneration.vehicles.VehicleTypes;
import innoz.utils.GeometryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import org.matsim.vehicles.VehicleType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * This class generates an initial demand (MATSim population) for a given scenario. </br>
 * 
 * dhosse, 05/16:
 * At the moment, only demand generation from MiD survey data is supported. Possible additional / alternative
 * data sources would be:
 * <ul>
 * <li> other surveys (e.g. SrV, MoP)
 * <li> innoz tracks
 * </ul>
 * 
 * @author dhosse
 *
 */
public class PopulationCreator {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private final Random random = MatsimRandom.getLocalInstance();
	private final Geoinformation geoinformation;
	
	//Comparator that sorts households by their weights
	private Comparator<SurveyHousehold> householdComparator = new Comparator<SurveyHousehold>() {

		@Override
		public int compare(SurveyHousehold o1, SurveyHousehold o2) {
			return Double.compare(o1.getWeight(), o2.getWeight());
		
		}
		
	};
	
	private static final Logger log = Logger.getLogger(PopulationCreator.class);
	/////////////////////////////////////////////////////////////////////////////////////////
	

	//MEMBERS//////////////////////////////////////////////////////////////////////////////
	private static CoordinateTransformation transformation;
	static Distribution distribution;
	
	private Coord currentHomeLocation = null;
	private Coord currentMainActLocation = null;
	private SurveyPlanWay lastLeg = null;
	private Coord lastActCoord = null;
	private double c = 0d;
	private AdministrativeUnit currentHomeCell;
	private AdministrativeUnit currentMainActCell;
	private List<AdministrativeUnit> currentSearchSpace;
	private AdministrativeUnit lastActCell = null;
	/////////////////////////////////////////////////////////////////////////////////////////	

	/**
	 * 
	 * Constructor.
	 * 
	 * @param geoinformation The geoinformation container.
	 */
	public PopulationCreator(final Geoinformation geoinformation){
		
		this.geoinformation = geoinformation;
		
	};
	
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
	public void run(Configuration configuration, Scenario scenario) throws NoSuchAuthorityCodeException,
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
	private void createDummyPopulation(Scenario scenario){
		
		log.info("Creating a dummy population without any preferences...");
		
		// From each administrative unit to each administrative unit, create a certain amount of commuters
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(Entry<String,AdministrativeUnit> fromEntry : d.getAdminUnits().entrySet()){
				
				for(Entry<String,AdministrativeUnit> toEntry : d.getAdminUnits().entrySet()){

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
		
	}
	
	/**
	 * 
	 * Creates a commuter population consisting of persons that perform only three activities during a simulated day:</br>
	 * <ol>
	 * <li>home
	 * <li>work
	 * <li>home
	 * </ol>
	 * 
	 * The home and work locations are chosen according to landuse data and a gravitation model.</br>
	 * 
	 * At the moment, this method is a stub and does nothing. dhosse 05/16
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario A Matsim scenario.
	 */
	private void createCommuterPopulation(Configuration configuration, Scenario scenario){
		
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
	
	/**
	 * 
	 * Creates a complete population (meaning: children, pupils, students, employees, pensioners etc.) from survey data.
	 * The activities are located according to landuse data and a gravitation model. 
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario A MATSim scenario.
	 */
	private void createCompletePopulation(Configuration configuration, Scenario scenario){
		
		// Run the survey data parser that stores all of the travel information
		MidDatabaseParser parser = new MidDatabaseParser();
		SurveyDataContainer container = new SurveyDataContainer(configuration);
		parser.run(configuration, container, this.geoinformation);
		
		// Initialize the disutilities for traveling from each cell to each other cell
		// to eventually get a gravitation model.
		distribution = new Distribution(scenario.getNetwork(), this.geoinformation, parser, transformation);
		
		// Choose the method for demand generation that has been specified in the configuration
		if(configuration.isUsingHouseholds()){
		
			createHouseholds(configuration, scenario, container);
			
		} else {
			
			createPersons(configuration, scenario, container);
			
		}
		
	}
	
	/**
	 * 
	 * Creates an initial demand at households level.
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario A MATSim scenario.
	 * @param parser The survey parser containing all of the information.
	 */
	private void createHouseholds(Configuration configuration, Scenario scenario, SurveyDataContainer container){

		int vehicleCounter = 0;
		
		// Get the MATSim population and initialize person attributes
		Population population = scenario.getPopulation();
		ObjectAttributes personAttributes = new ObjectAttributes();
		scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		// Sort the households by their weight (according to the survey data)
		List<SurveyHousehold> households = new ArrayList<>();
		households.addAll(container.getHouseholds().values());
		Collections.sort(households, householdComparator);
		
		// Choose a home cell for the household
		// Initialize a pseudo-random number and iterate over all administrative units.
		// Accumulate their weights and as soon as the random number is smaller or equal to the accumulated weight
		// pick the current admin unit.
//		for(int i = 0; i < configuration.getNumberOfHouseholds() * configuration.getScaleFactor(); i++){
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(int i = 0; i < d.getnHouseholds() * configuration.getScaleFactor(); i++){
				currentHomeCell = null;
				AdministrativeUnit au = null;
			double r = random.nextDouble() * this.geoinformation.getTotalWeightForLanduseKey(d.getId(), "residential");
			
			double r2 = 0.;

			int blandId = 0;
			int rtyp = 0;
			
			for(AdministrativeUnit admin : d.getAdminUnits().values()){
				
				r2 += admin.getWeightForKey("residential");
				
				if(r <= r2){
					
					au = admin;
					blandId = au.getBland();
					rtyp = au.getRegionType();
					break;
					
				}
				
			}
			
			// Choose a template household (weighted, same method as above)
			SurveyHousehold template = null;
			
			double accumulatedWeight = 0.;
			double rand = random.nextDouble() * container.getWeightForHouseholdsInState(blandId, rtyp);
			
			for(String hhId : container.getHouseholdsForState(blandId, rtyp)){
				
				SurveyHousehold hh = container.getHouseholds().get(hhId);
				
				if(hh != null){
					
					accumulatedWeight += hh.getWeight();
					if(accumulatedWeight >= rand){
						
						template = hh;
						break;
						
					}
					
				}
				
			}
			
			int nPersons = template.getNPersons();

			// Create a MATSim household
			Household household = new HouseholdImpl(Id.create(au.getId() + "_" + i, Household.class));
			household.setIncome(new IncomeImpl(template.getIncome(), IncomePeriod.month));
			((HouseholdImpl)household).setMemberIds(new ArrayList<Id<Person>>());

			// Set global home location for the entire household
			Coord homeLocation = null;

			// Go through all residential areas of the home cell and randomly choose one of them
			if(au.getLanduseGeometries().containsKey("residential")){
				
				double p = random.nextDouble() * au.getWeightForKey("residential");
				accumulatedWeight = 0.;
				
				for(Geometry g : au.getLanduseGeometries().get("residential")){
					
					accumulatedWeight += g.getArea();
					
					if(p <= accumulatedWeight){
						// Shoot the home location
						homeLocation = transformation.transform(GeometryUtils.shoot(g, random));
						currentHomeCell = au;
						break;
					}
					
				}
				
			} else {
			
				// If no residential areas exist within the home cell, shoot a random coordinate
				homeLocation = transformation.transform(GeometryUtils.shoot(au.getGeometry(), random));
				currentHomeCell = au;
				
			}

			// Create as many persons as were reported and add them to the household and the population
			for(int j = 0; j < nPersons; j++){
				
				String personId = template.getMemberIds().get(j);
				SurveyPerson templatePerson = container.getPersons().get(personId);
				
				Person person = createPerson(templatePerson, population, personAttributes, random.nextDouble(),
						population.getPersons().size(), homeLocation);
				
				// If the resulting MATSim person is not null, add it
				if(person != null){
					
					population.addPerson(person);
					household.getMemberIds().add(person.getId());
					
				}

			}
			
			// If we model non-generic cars, create all cars that were reported in the survey and add them to the household
			if(configuration.isUsingVehicles()){
				
				for(String vid : template.getVehicleIds()){
					
					SurveyVehicle v = container.getVehicles().get(vid);
					
					VehicleType type = VehicleTypes.getVehicleTypeForKey(v.getKbaClass(), v.getFuelType());
					
					if(!scenario.getVehicles().getVehicleTypes().containsKey(type.getId())){
						scenario.getVehicles().addVehicleType(type);
					}
					
					Vehicle vehicle = scenario.getVehicles().getFactory().createVehicle(Id.create(template.getId() + "_" + vid +
							"_" + v.getFuelType().name() + "_" + vehicleCounter, Vehicle.class), type);
					scenario.getVehicles().addVehicle(vehicle);
					vehicleCounter++;
					
					if(household.getVehicleIds() == null){
						
						((HouseholdImpl)household).setVehicleIds(new ArrayList<Id<Vehicle>>());
						
					}
					
					household.getVehicleIds().add(vehicle.getId());
					
				}
				
			}
			
			// Add the household to the scenario
			scenario.getHouseholds().getHouseholds().put(household.getId(), household);
			
		}
		}
		
	}
	
	/**
	 * 
	 * Creates an initial demand on person level. This means, it's not more detailed than {@link #createHouseholds(Configuration, Scenario, MidDatabaseParser)} 
	 * but completely ignores households and locates every person individually in the survey area.
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario The MATSim scenario.
	 * @param parser The survey parser.
	 */
	private void createPersons(Configuration configuration, Scenario scenario, SurveyDataContainer container){
		
		// Get the MATSim population and initialize person attributes
		Population population = scenario.getPopulation();
		ObjectAttributes personAttributes = new ObjectAttributes();
		scenario.addScenarioElement(PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		// TODO this is not final and most likely won't work like that /dhosse, 05/16
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(AdministrativeUnit au : d.getAdminUnits().values()){
				
				double personalRandom = random.nextDouble();
				
				Map<String,SurveyPerson> templatePersons = container.getPersons();
				SurveyPerson personTemplate = null;
				
				personTemplate = PersonUtils.getTemplate(templatePersons,
						personalRandom * PersonUtils.getTotalWeight(templatePersons.values()));
				
				//TODO: number of inhabitants for admin units
				for(int i = 0; i < 10000 * configuration.getScaleFactor(); i++){
					
					population.addPerson(createPerson(personTemplate, population, personAttributes, personalRandom, i, null));
					
				}
				
			}
			
		}
		
	}

	/**
	 * 
	 * Creates a MATSim person with an initial daily plan from a template taken from survey data.
	 * The activities and legs performed during a day are taken directly from the survey data.
	 * 
	 * @param personTemplate The survey person that is the template for the current MATSim person.
	 * @param population The MATSim population.
	 * @param personAttributes The person attributes container.
	 * @param personalRandom The random number for this person.
	 * @param i The index of the person (current number of persons in the population).
	 * @param homeCoord The home location.
	 * @return A MATSim person with an initial daily plan.
	 */
	@SuppressWarnings("deprecation")
	private Person createPerson(SurveyPerson personTemplate, Population population,
			ObjectAttributes personAttributes, double personalRandom, int i, Coord homeCoord) {

		// Initialize main act location, last leg, last act cell and the coordinate of the last activity as null to avoid errors...
		currentMainActLocation = null;
		lastLeg = null;
		lastActCoord = null;
		lastActCell = null;
		currentSearchSpace = null;
		
		// Create a new MATSim person and an empty plan
		Person person = population.getFactory().createPerson(Id.createPersonId(currentHomeCell.getId() + "_" + personTemplate.getId() + "_" + i));
		Plan plan = population.getFactory().createPlan();
		
		// Set the person's attributes (sex, age, employed, license, car availability) according to what was reported in the survey
		innoz.utils.matsim.PersonUtils.setSex(person, personTemplate.getSex());
		innoz.utils.matsim.PersonUtils.setAge(person, personTemplate.getAge());
		innoz.utils.matsim.PersonUtils.setEmployed(person, personTemplate.isEmployed());
		String carAvail = personTemplate.getCarAvailable() ? "always" : "never";
		innoz.utils.matsim.PersonUtils.setCarAvail(person, carAvail);
		String hasLicense = personTemplate.hasLicense() ? "yes" : "no";
		innoz.utils.matsim.PersonUtils.setLicence(person, hasLicense);
		
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_SEX, personTemplate.getSex());
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_AGE, personTemplate.getAge());
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_EMPLOYED, personTemplate.isEmployed());
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_CAR_AVAIL, carAvail);
		personAttributes.putAttribute(person.getId().toString(), PersonUtils.ATT_LICENSE, hasLicense);
		
		// Check if there are any plans for the person (if it is a mobile person)
		if(personTemplate.getPlans().size() > 0){

			// Select a template plan from the mid survey to create a matsim plan
			SurveyPlan templatePlan = null;
			
			// If there is only one plan, make it the template
			if(personTemplate.getPlans().size() < 2){
				
				templatePlan = personTemplate.getPlans().get(0);
				
			} else {

				// Otherwise, randomly draw a plan from the collection
				double planRandom = personalRandom * personTemplate.getWeightOfAllPlans();
				double accumulatedWeight = 0.;
				
				for(SurveyPlan p : personTemplate.getPlans()){
					
					accumulatedWeight += p.getWeigt();
					
					if(planRandom <= accumulatedWeight){
					
						templatePlan = p;
						break;
					
					}
					
				}
				
			}
			
			// Set the current home location
			currentHomeLocation = homeCoord != null ? homeCoord :
				transformation.transform(GeometryUtils.shoot(currentHomeCell.getGeometry(),random));
			lastActCoord = currentHomeLocation;

			// If there are at least two plan elements in the chosen template plan, generate the plan elements
			if(templatePlan.getPlanElements().size() > 1){
				
				// Locate the main activity according to the distribution that was computed before			
				String mainMode = templatePlan.getMainActIndex() > 0 ? 
						((SurveyPlanWay)templatePlan.getPlanElements().get(templatePlan.getMainActIndex()-1)).getMainMode() :
							((SurveyPlanWay)templatePlan.getPlanElements().get(1)).getMainMode();
				currentMainActCell = locateActivityInCell(templatePlan.getMainActType(), mainMode, personTemplate);
				
				currentMainActLocation = shootLocationForActType(currentMainActCell, templatePlan.getMainActType(),
						0d, templatePlan, mainMode, personTemplate);

				// To locate intermediate activities, create a search space and add the home and main activity cells
				currentSearchSpace = new ArrayList<>();
				currentSearchSpace.add(currentHomeCell);
				currentSearchSpace.add(currentMainActCell);
				
				c = CoordUtils.calcDistance(currentHomeLocation, currentMainActLocation);
				
				// Also, add all cells of which the sum of the distances between their centroid and the centroids of
				// the home and the main act cell is less than twice the distance between the home and the main activity location
//				List<AdministrativeUnit> adminUnits = new ArrayList<>();
//				adminUnits.addAll(this.geoinformation.getSurveyArea().values());
//				adminUnits.addAll(this.geoinformation.getVicinity().values());
				for(District d : this.geoinformation.getAdminUnits().values()){

					for(AdministrativeUnit au : d.getAdminUnits().values()){
						
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
					
				}
				
				// Create a MATSim plan element for each survey plan element and add them to the MATSim plan
				for(int j = 0; j < templatePlan.getPlanElements().size(); j++){
					
					SurveyPlanElement mpe = templatePlan.getPlanElements().get(j);
					
					if(mpe instanceof SurveyPlanActivity){
						
						plan.addActivity(createActivity(population, personTemplate, templatePlan, mpe));
						
					} else {
						
						plan.addLeg(createLeg(population, mpe));
						
					}
					
				}
				
			} else {
				
				// If there is only one plan element, create a 24hrs home activity
				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
				home.setMaximumDuration(24 * 3600);
				home.setStartTime(0);
				home.setEndTime(24 * 3600);
				plan.addActivity(home);
				
			}
			
		} else {
			
			// If there is no plan for the survey person, create a 24hrs home activity
			Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
			home.setMaximumDuration(24 * 3600);
			home.setStartTime(0);
			home.setEndTime(24 * 3600);
			plan.addActivity(home);
			
		}
		
		// In the end: add the person to the population
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		return person;
		
	}

	/**
	 * 
	 * Creates a MATSim leg from a survey way.
	 * 
	 * @param population The MATSim population
	 * @param mpe The survey plan element (in this case: way)
	 * @return
	 */
	private Leg createLeg(Population population,
			SurveyPlanElement mpe) {
		
		SurveyPlanWay way = (SurveyPlanWay)mpe;
		String mode = way.getMainMode();
		double departure = way.getStartTime();
		double ttime = way.getEndTime() - departure;
		
		Leg leg = population.getFactory().createLeg(mode);
		leg.setTravelTime(ttime);

		lastLeg = way;
		
		return leg;
		
	}

	/**
	 * 
	 * Randomly chooses an administrative unit in which an activity of a certain type should be located.
	 * The randomness depends on the distribution computed earlier, the activity type and the transport mode.
	 * 
	 * @param activityType The type of the activity to locate.
	 * @param mode The transport mode used to get to the activity.
	 * @param personTemplate The survey person.
	 * @return The administrative unit in which the activity most likely is located.
	 */
	private AdministrativeUnit locateActivityInCell(String activityType, String mode, SurveyPerson personTemplate){
		
		return locateActivityInCell(null, activityType, mode, personTemplate, 0d);
		
	}

	/**
	 * 
	 * Same method as {@link #locateActivityInCell(String, String, SurveyPerson)}, only that this method locates an activity of a sequence
	 * where the last activity and the distance traveled between the two locations is known.
	 * 
	 * @param fromId The identifier of the last administrative unit.
	 * @param activityType The type of the current activity.
	 * @param mode The transport mode used.
	 * @param personTemplate The survey person.
	 * @param distance The distance traveled between the last and the current activity.
	 * @return
	 */
	private AdministrativeUnit locateActivityInCell(String fromId, String activityType, String mode, SurveyPerson personTemplate, double distance){
		
		Set<String> modes = new HashSet<String>();
		
		if(mode != null){

			// If the person walked, it most likely didn't leave the last cell (to avoid very long walk legs)
			if(mode.equals(TransportMode.walk) && fromId != null){
				
				return this.geoinformation.getAdminUnitById(fromId);
				
			}
			
			// Add the transport mode used
			modes.add(mode);
			
		} else {
			
			// If the transport mode wasn't reported, consider all modes the person could have used
			modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt + "," + TransportMode.walk);
			
			if(personTemplate.getCarAvailable()){
			
				modes.add(TransportMode.ride);
				
				if(personTemplate.hasLicense()){
					
					modes.add(TransportMode.car);
					
				}
				
			}
			
		}
		
		AdministrativeUnit result = null;
		
		Map<String, Double> toId2Disutility = new HashMap<String, Double>();
		double sumOfWeights = 0d;
		
		// Set the search space to the person's search space if it's not null.
		// Else consider the whole survey area.
		List<AdministrativeUnit> adminUnits = null;
		if(currentSearchSpace != null){
			
			if(currentSearchSpace.size() > 0){
				
				adminUnits = currentSearchSpace;
				
			}
			
		}
		
		if(adminUnits == null){
			
			adminUnits = new ArrayList<AdministrativeUnit>();
			adminUnits.addAll(this.geoinformation.getSubUnits().values());
//			adminUnits.addAll(this.geoinformation.getVicinity().values());
			
		}
		
		// Go through all administrative units in the search space
		// Sum up the disutilities of all connections and map the entries for further work
		for(AdministrativeUnit au : adminUnits){
			
			if(fromId != null){
			
				if(distribution.getDistance(fromId, au.getId()) > distance / 1.3){
					
					continue;
					
				}
				
			}
			
			double disutility = Double.NEGATIVE_INFINITY;
				
			for(String m : modes){
				
				if(fromId != null){
					
					disutility = distribution.getDisutilityForActTypeAndMode(fromId, au.getId(), activityType, m);
					
				} else {
					
					disutility = distribution.getDisutilityForActTypeAndMode(currentHomeCell.getId(), au.getId(), activityType, m);
					
				}
				
				if(Double.isFinite(disutility)){
					
					toId2Disutility.put(au.getId(), disutility);
					sumOfWeights += disutility;
					
				}
				
			}
			
		}
		
		// Randomly choose a connection out of the search space
		double r = random.nextDouble() * sumOfWeights;
		double accumulatedWeight = 0d;
		
		for(Entry<String, Double> entry : toId2Disutility.entrySet()){
			
			accumulatedWeight += entry.getValue();
			if(r <= accumulatedWeight){
				result = this.geoinformation.getSubUnits().get(entry.getKey());
				if(result == null){
					result = this.geoinformation.getSubUnits().get(entry.getKey());
				}
				break;
			}
			
		}
		
		return result;
		
	}

	/**
	 * 
	 * Creates a MATSim activity from a survey activity.
	 * 
	 * @param population The MATSim population.
	 * @param personTemplate The survey person.
	 * @param mpe The survey plan element (in this case: activity)
	 * @return A MATSim activity.
	 */
	private Activity createActivity(Population population, SurveyPerson personTemplate, SurveyPlan templatePlan,
			SurveyPlanElement mpe) {

		AdministrativeUnit au = null;
		
		// Initialize the activity type and the start and end time
		SurveyPlanActivity act = (SurveyPlanActivity)mpe;
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

		// Check type of the next activity
		if(type.equals(ActivityTypes.HOME)){
			
			// If it's a home activity, set the location to the home coordinate
			coord = currentHomeLocation;
			au = currentHomeCell;
			
		} else if(act.getId() == templatePlan.getMainActId()){
			
			au = currentMainActCell;
			
			// If we already know the main act location, set it.
			// Else, shoot a new coordinate for the main act location.
			if(currentMainActLocation != null){
				
				coord = currentMainActLocation;
				
			} else {
				
				coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
				
				currentMainActLocation = coord;
				
			}
			
		} else {
			
			// If it's neither a home nor the main activity, locate the activity in any cell of the search space
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
		
		// Create a new activity
		Activity activity = population.getFactory().createActivityFromCoord(type, coord);
		activity.setStartTime(start);
		activity.setEndTime(end);
		
		// If the end time is set to zero (probably last activity) or after midnight, set it to midnight
		if(end == 0 || end > Time.MIDNIGHT){
		
			activity.setMaximumDuration(end - start + Time.MIDNIGHT);
			activity.setEndTime(Time.MIDNIGHT);
		
		} else{
			
			if(end - start < 900){
			
				// Set the activity duration to at least 1/4 hour if it's been reported shorter to avoid
				// extremely negative scores
				activity.setMaximumDuration(900);
			
			} else{
				
				// Set the maximum duration according to the survey data
				activity.setMaximumDuration(end - start);
				
			}
			
		}
		
		// Set the last coord and the last cell visited
		lastActCoord = activity.getCoord();
		lastActCell = au;
		
		return activity;
		
	}

	/**
	 * 
	 * Shoots a coordinate for the currently created activity.
	 * 
	 * @param au The administrative unit in which the activity should be located
	 * @param actType The type of the activity.
	 * @param distance The travel distance between the last and the current activity
	 * @param templatePlan The survey plan.
	 * @param mode The transport mode used to travel from last to current activity.
	 * @param personTemplate The survey person.
	 * @return A coordinate for the current activity.
	 */
	private Coord shootLocationForActType(AdministrativeUnit au, String actType, double distance,
			SurveyPlan templatePlan, String mode, SurveyPerson personTemplate) {

		if(mode != null){
			
			// Bound the maximum walk distance to avoid "endless" walk legs
//			if(mode.equals(TransportMode.walk) && distance > 2000){
//				
//				distance = 500 + random.nextInt(1001);
//				
//			}

		}
		
		// Divide the distance by the beeline distance factor and set boundaries for maximum and minimum distance traveled
		double d = distance / 1.3;
		double minFactor = 0.75;
		double maxFactor = 1.5;
		
		// Get all landuse geometries of the current activity type within the given administrative unit
		List<Geometry> closest = au.getLanduseGeometries().get(actType);
		
		// If there were any landuse geometries found, randomly choose one of the geometries.
		// Else pick the landuse geometry closest to the last activity coordinate.
		if(closest != null){
			
			if(!closest.isEmpty()){
				
				int index = random.nextInt(closest.size());
				Geometry area = closest.get(index);
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			} else {
				
				Geometry area = this.geoinformation.getQuadTreeForActType(actType).getClosest(lastActCoord.getX(), lastActCoord.getY());
				
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			}
		
		} else {
			
			closest = (List<Geometry>) this.geoinformation.getQuadTreeForActType(actType).getRing(lastActCoord.getX(),
					lastActCoord.getY(), d * minFactor, d * maxFactor);
			
			if(!closest.isEmpty()){
				
				int index = random.nextInt(closest.size());
				Geometry area = closest.get(index);
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			} else {
				
				Geometry area = this.geoinformation.getQuadTreeForActType(actType).getClosest(lastActCoord.getX(), lastActCoord.getY());
				
				return transformation.transform(GeometryUtils.shoot(area,random));
				
			}
			
		}
		
	}
	
}
