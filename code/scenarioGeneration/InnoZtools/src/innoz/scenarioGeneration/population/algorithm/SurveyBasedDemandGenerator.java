package innoz.scenarioGeneration.population.algorithm;

import innoz.config.Configuration;
import innoz.io.database.SurveyDatabaseParser;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.Distribution;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.mobilityAttitude.MobilityAttitudeGroups;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlan;
import innoz.scenarioGeneration.population.surveys.SurveyPlanActivity;
import innoz.scenarioGeneration.population.surveys.SurveyPlanElement;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;
import innoz.scenarioGeneration.population.surveys.SurveyVehicle;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.scenarioGeneration.vehicles.VehicleTypes;
import innoz.utils.GeometryUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.misc.Time;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.Income.IncomePeriod;
import org.matsim.households.IncomeImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import com.vividsolutions.jts.geom.Geometry;

public class SurveyBasedDemandGenerator extends DemandGenerationAlgorithm {

	public SurveyBasedDemandGenerator(final Geoinformation geoinformation,
			final CoordinateTransformation transformation) {

		super(geoinformation, transformation);
		
	}

	@Override
	public void run(Scenario scenario, Configuration configuration) {

		createCompletePopulation(configuration, scenario);
		
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
		SurveyDatabaseParser parser = new SurveyDatabaseParser();
		SurveyDataContainer container = new SurveyDataContainer(configuration);
		parser.run(configuration, container, this.geoinformation);
		
		// Initialize the disutilities for traveling from each cell to each other cell
		// to eventually get a gravitation model.
		this.distribution = new Distribution(scenario.getNetwork(), this.geoinformation, parser,
				this.transformation);
		
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

		// Get the MATSim population and initialize person attributes
		Population population = scenario.getPopulation();
		ObjectAttributes personAttributes = scenario.getPopulation().getPersonAttributes();
//		scenario.addScenarioElement(innoz.scenarioGeneration.population.utils.PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		// Sort the households by their weight (according to the survey data)
		List<SurveyHousehold> households = new ArrayList<>();
		households.addAll(container.getHouseholds().values());
		Collections.sort(households, this.householdComparator);
		
		// Choose a home cell for the household
		// Initialize a pseudo-random number and iterate over all administrative units.
		// Accumulate their weights and as soon as the random number is smaller or equal to the accumulated weight
		// pick the current admin unit.
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(int i = 0; i < d.getnHouseholds() * configuration.getScaleFactor(); i++){
				
				this.currentHomeCell = chooseAdminUnitInsideDistrict(d, ActivityTypes.HOME);
				
				int rtyp = this.currentHomeCell.getRegionType();
				
				// Choose a template household (weighted, same method as above)
				SurveyHousehold template = null;
				
				double accumulatedWeight = 0.;
				double rand = this.random.nextDouble() * container.getWeightForHouseholdsInRegionType(rtyp);
				
				for(String hhId : container.getHouseholdsForRegionType(rtyp)){
					
					SurveyHousehold hh = container.getHouseholds().get(hhId);
					
					if(hh != null){
						
						accumulatedWeight += hh.getWeight();
						if(accumulatedWeight >= rand){
							
							template = hh;
							break;
							
						}
						
					}
					
				}
				
				// Create a MATSim household
				Household household = new HouseholdImpl(Id.create(this.currentHomeCell.getId() + "_" + i,
						Household.class));
				household.setIncome(new IncomeImpl(template.getIncome(), IncomePeriod.month));
				((HouseholdImpl)household).setMemberIds(new ArrayList<Id<Person>>());
	
				// Set global home location for the entire household
				Coord homeLocation = null;
	
				// Go through all residential areas of the home cell and randomly choose one of them
				if(this.currentHomeCell.getLanduseGeometries().containsKey(ActivityTypes.HOME)){
					
					homeLocation = chooseActivityCoordInAdminUnit(this.currentHomeCell, ActivityTypes.HOME);
					
				} else {
				
					// If no residential areas exist within the home cell, shoot a random coordinate
					homeLocation = this.transformation.transform(GeometryUtils.shoot(
							this.currentHomeCell.getGeometry(), this.random));
					
				}
	
				// Create as many persons as were reported and add them to the household and the population
				for(int j = 0; j < template.getNPersons(); j++){
					
					String personId = template.getMemberIds().get(j);
					SurveyPerson templatePerson = container.getPersons().get(personId);
					
					Person person = createPerson(configuration, templatePerson, population, personAttributes, this.random.nextDouble(),
							population.getPersons().size(), homeLocation, container, household.getIncome().getIncome());
					
					// If the resulting MATSim person is not null, add it
					if(person != null){
						
						population.addPerson(person);
						household.getMemberIds().add(person.getId());
						
					}
	
				}
				
				// If we model non-generic cars, create all cars that were reported in the survey and add them to the household
				if(configuration.isUsingVehicles()){
					
					createSurveyVehicles(scenario, container, template,
							household);
					
				}
				
				// Add the household to the scenario
				scenario.getHouseholds().getHouseholds().put(household.getId(), household);
				
			}

		}
		
	}

	private void createSurveyVehicles(Scenario scenario, SurveyDataContainer container, SurveyHousehold template,
			Household household) {
		
		int vehicleCounter = 0;
		
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
		scenario.addScenarioElement(innoz.scenarioGeneration.population.utils.PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		// TODO this is not final and most likely won't work like that /dhosse, 05/16
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(AdministrativeUnit au : d.getAdminUnits().values()){
				
				double personalRandom = this.random.nextDouble();
				
				Map<String,SurveyPerson> templatePersons = container.getPersons();
				SurveyPerson personTemplate = null;
				
				personTemplate = innoz.scenarioGeneration.population.utils.PersonUtils.getTemplate(templatePersons,
						personalRandom * innoz.scenarioGeneration.population.utils.PersonUtils.getTotalWeight(templatePersons.values()));
				
				//TODO: number of inhabitants for admin units
				for(int i = 0; i < 10000 * configuration.getScaleFactor(); i++){
					
					Coord homeCoord = chooseActivityCoordInAdminUnit(au, ActivityTypes.HOME);
					population.addPerson(createPerson(configuration, personTemplate, population, personAttributes, personalRandom, i, homeCoord, container, 0d));
					
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
	private Person createPerson(Configuration configuration, SurveyPerson personTemplate, Population population,
			ObjectAttributes personAttributes, double personalRandom, int i, Coord homeCoord, SurveyDataContainer container, double hhIncome) {

		// Initialize main act location, last leg, last act cell and the coordinate of the last activity as null to avoid errors...
		this.currentMainActLocation = null;
		this.lastLeg = null;
		this.lastActCoord = null;
		this.lastActCell = null;
		this.currentSearchSpace = null;
		
		// Create a new MATSim person and an empty plan
		Person person = population.getFactory().createPerson(Id.createPersonId(this.currentHomeCell.getId() + "_" + personTemplate.getId() + "_" + i));
		Plan plan = population.getFactory().createPlan();
		
		// Set the person's attributes (sex, age, employed, license, car availability) according to what was reported in the survey
		PersonUtils.setSex(person, personTemplate.getSex());
		PersonUtils.setAge(person, personTemplate.getAge());
		PersonUtils.setEmployed(person, personTemplate.isEmployed());
		String carAvail = personTemplate.hasCarAvailable() ? "always" : "never";
		PersonUtils.setCarAvail(person, carAvail);
		String hasLicense = personTemplate.hasLicense() ? "yes" : "no";
		PersonUtils.setLicence(person, hasLicense);
		
		personAttributes.putAttribute(person.getId().toString(),
				innoz.scenarioGeneration.population.utils.PersonUtils.ATT_SEX, personTemplate.getSex());
		personAttributes.putAttribute(person.getId().toString(),
				innoz.scenarioGeneration.population.utils.PersonUtils.ATT_AGE, personTemplate.getAge());
		personAttributes.putAttribute(person.getId().toString(),
				innoz.scenarioGeneration.population.utils.PersonUtils.ATT_EMPLOYED, personTemplate.isEmployed());
		personAttributes.putAttribute(person.getId().toString(),
				innoz.scenarioGeneration.population.utils.PersonUtils.ATT_CAR_AVAIL, carAvail);
		personAttributes.putAttribute(person.getId().toString(),
				innoz.scenarioGeneration.population.utils.PersonUtils.ATT_LICENSE, hasLicense);
		if(personTemplate.isCarsharingUser()){
			personAttributes.putAttribute(person.getId().toString(), "OW_CARD", "true");
			personAttributes.putAttribute(person.getId().toString(), "RT_CARD", "true");
			personAttributes.putAttribute(person.getId().toString(), "FF_CARD", "true");
		}
		
		if(configuration.isUsingMobilityAttitudeGroups()){
			String mag = MobilityAttitudeGroups.assignPersonToGroup(person, random,
					hhIncome, personAttributes);
			if(mag != null){
				personAttributes.putAttribute(person.getId().toString(), "mobilityAttitude", mag);
			}
		}
		
		// Check if there are any plans for the person (if it is a mobile person)
		if(personTemplate.getPlans().size() > 0){

			// Select a template plan from the mid survey to create a matsim plan
			SurveyPlan planTemplate = null;
			
			// If there is only one plan, make it the template
			if(personTemplate.getPlans().size() < 2){
				
				planTemplate = personTemplate.getPlans().get(0);
				
			} else {

				// Otherwise, randomly draw a plan from the collection
				double planRandom = personalRandom * personTemplate.getWeightOfAllPlans();
				double accumulatedWeight = 0.;
				
				for(SurveyPlan p : personTemplate.getPlans()){
					
					accumulatedWeight += p.getWeigt();
					
					if(planRandom <= accumulatedWeight){
					
						planTemplate = p;
						break;
					
					}
					
				}
				
			}
			
			// Set the current home location
			this.currentHomeLocation = homeCoord != null ? homeCoord :
				this.transformation.transform(GeometryUtils.shoot(this.currentHomeCell.getGeometry(),
						this.random));
			this.lastActCoord = this.currentHomeLocation;

			// If there are at least two plan elements in the chosen template plan, generate the plan elements
			if(planTemplate.getPlanElements().size() > 1){
				
				// Distribute activities in the survey area (or its vicinity) according to the distribution matrix
				List<String> cellIds = distributeActivitiesInCells(personTemplate, planTemplate, container);
				
				int actIndex = 0;
				
				// Create a MATSim plan element for each survey plan element and add them to the MATSim plan
				for(int j = 0; j < planTemplate.getPlanElements().size(); j++){
					
					SurveyPlanElement mpe = planTemplate.getPlanElements().get(j);
					
					if(mpe instanceof SurveyPlanActivity){
						
						plan.addActivity(createActivity(population, personTemplate, planTemplate, mpe, cellIds.get(actIndex)));
						
						actIndex++;
						
					} else {
						
						plan.addLeg(createLeg(population, mpe));
						
					}
					
				}
				
			} else {
				
				// If there is only one plan element, create a 24hrs home activity
				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
				home.setMaximumDuration(24 * 3600);
//				home.setStartTime(0);
//				home.setEndTime(24 * 3600);
				plan.addActivity(home);
				
			}
			
		} else {
			
			// If there is no plan for the survey person, create a 24hrs home activity
			Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
			home.setMaximumDuration(24 * 3600);
//			home.setStartTime(0);
//			home.setEndTime(24 * 3600);
			plan.addActivity(home);
			
		}
		
		// In the end: add the person to the population
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		return person;
		
	}
	
	private List<String> distributeActivitiesInCells(final SurveyPerson person, final SurveyPlan plan, SurveyDataContainer container){
		
		// Locate the main activity according to the distribution that was computed before
		String mainMode = plan.getMainActIndex() > 0 ? 
				((SurveyPlanTrip)plan.getPlanElements().get(plan.getMainActIndex()-1)).getMainMode() :
					((SurveyPlanTrip)plan.getPlanElements().get(plan.getMainActIndex()+1)).getMainMode();
		this.currentMainActCell = locateActivityInCell(plan.getMainActType(), mainMode, person);
		
		double distance = container.getModeStatsContainer().get(mainMode).getMean();
		
		this.currentMainActLocation = shootLocationForActType(this.currentMainActCell,
				plan.getMainActType(), distance, plan, mainMode, person);

		// To locate intermediate activities, create a search space and add the home and main activity cells
		this.currentSearchSpace = new ArrayList<>();
		this.currentSearchSpace.add(this.currentHomeCell);
		this.currentSearchSpace.add(this.currentMainActCell);
		
		c = CoordUtils.calcEuclideanDistance(this.currentHomeLocation, this.currentMainActLocation);
		
		// Also, add all cells of which the sum of the distances between their centroid and the centroids of
		// the home and the main act cell is less than twice the distance between the home and the main activity location
		for(District d : this.geoinformation.getAdminUnits().values()){

			for(AdministrativeUnit au : d.getAdminUnits().values()){
				
				double a = CoordUtils.calcEuclideanDistance(this.transformation.transform(
						MGC.point2Coord(this.currentHomeCell.getGeometry().getCentroid())),
						this.transformation.transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				double b = CoordUtils.calcEuclideanDistance(this.transformation.transform(
						MGC.point2Coord(this.currentMainActCell.getGeometry().getCentroid())),
						this.transformation.transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				
				if(a + b < 2 * c){
					
					this.currentSearchSpace.add(au);
					
				}
				
			}
			
		}
		
		List<String> cellList = new ArrayList<String>();
		
		AdministrativeUnit lastTo = this.currentHomeCell;
		SurveyPlanTrip lastWay = null;
		
		for(SurveyPlanElement pe : plan.getPlanElements()){
			
			if(pe instanceof SurveyPlanActivity){
				
				AdministrativeUnit next = null;

				SurveyPlanActivity act = (SurveyPlanActivity)pe;
				
				if(act.getActType().equals(ActivityTypes.HOME)){
					
					next = this.currentHomeCell;
					
				} else if(act.getId() == plan.getMainActId()){
					
					next = this.currentMainActCell;
					
				} else {
					
					if(lastWay != null){
						next = locateActivityInCell(lastTo.getId(), act.getActType(),
								lastWay.getMainMode(), person);
					}
					
				}
				
				if(next == null){
					
					next = lastTo;
					
				}
				
				cellList.add(next.getId());
				lastTo = next;
				next = null;
				
			} else {
				
				lastWay = (SurveyPlanTrip)pe;
				
			}
			
		}
		
		return cellList;
		
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
		
		SurveyPlanTrip way = (SurveyPlanTrip)mpe;
		String mode = way.getMainMode();
		double departure = way.getStartTime();
		double ttime = way.getEndTime() - departure;
		
		Leg leg = population.getFactory().createLeg(mode);
		leg.setTravelTime(ttime);

		this.lastLeg = way;
		
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
		
		return locateActivityInCell(null, activityType, mode, personTemplate);
		
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
	private AdministrativeUnit locateActivityInCell(String fromId, String activityType, String mode, SurveyPerson personTemplate){
		
		Set<String> modes = new HashSet<String>();
		
		if(fromId == null){
			fromId = this.currentHomeCell.getId();
		}
		
		if(mode != null){

			// If the person walked, it most likely didn't leave the last cell (to avoid very long walk legs)
			if(mode.equals(TransportMode.walk) && fromId != null){
				
				return this.geoinformation.getAdminUnitById(fromId);
				
			}
			
			// Add the transport mode used
			modes.add(mode);
			
		} else {
			
			// If the transport mode wasn't reported, consider all modes the person could have used
			modes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.pt + ","
					+ TransportMode.walk);
			
			if(personTemplate.hasCarAvailable()){
			
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
		if(this.currentSearchSpace != null){
			
			if(this.currentSearchSpace.size() > 0){
				
				adminUnits = this.currentSearchSpace;
				
			}
			
		}
		
		if(adminUnits == null){
			
			adminUnits = new ArrayList<AdministrativeUnit>();
			adminUnits.addAll(this.geoinformation.getSubUnits().values());
			
		}
		
		// Go through all administrative units in the search space
		// Sum up the disutilities of all connections and map the entries for further work
		for(AdministrativeUnit au : adminUnits){
			
			double disutility = Double.NEGATIVE_INFINITY;
				
			for(String m : modes){
				
				if(fromId != null){
					
					disutility = this.distribution.getDisutilityForActTypeAndMode(fromId, au.getId(),
							activityType, m);
					
				} else {
					
					disutility = this.distribution.getDisutilityForActTypeAndMode(
							this.currentHomeCell.getId(), au.getId(), activityType, m);
					
				}
				
				if(Double.isFinite(disutility)){
					
					toId2Disutility.put(au.getId(), disutility);
					sumOfWeights += disutility;
					
				}
				
			}
			
		}
		
		// Randomly choose a connection out of the search space
		double r = this.random.nextDouble() * sumOfWeights;
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
			SurveyPlanElement mpe, String cellId) {

		AdministrativeUnit au = this.geoinformation.getAdminUnitById(cellId);
		
		// Initialize the activity type and the start and end time
		SurveyPlanActivity act = (SurveyPlanActivity)mpe;
		String type = act.getActType();
		double start = act.getStartTime();
		double end = act.getEndTime();
		
		double distance = 0.;
		String mode = null;
		if(this.lastLeg != null){
			distance = this.lastLeg.getTravelDistance();
			mode = this.lastLeg.getMainMode();
		}
		if(this.lastActCoord == null){
			
			this.lastActCoord = this.currentHomeLocation;
			
		}

		Coord coord = null;

		// Check type of the next activity
		if(type.equals(ActivityTypes.HOME)){
			
			// If it's a home activity, set the location to the home coordinate
			coord = this.currentHomeLocation;
			au = this.currentHomeCell;
			
		} else if(act.getId() == templatePlan.getMainActId()){
			
			au = this.currentMainActCell;
			
			// If we already know the main act location, set it.
			// Else, shoot a new coordinate for the main act location.
			if(this.currentMainActLocation != null){
				
				coord = this.currentMainActLocation;
				
			} else {
				
				coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
				
				this.currentMainActLocation = coord;
				
			}
			
		} else {
			
			// If it's neither a home nor the main activity, locate the activity in any cell of the search space
			if(this.lastActCell == null) this.lastActCell = this.currentHomeCell;
//			au = locateActivityInCell(lastActCell.getId(), type, mode, personTemplate,distance);
			
			if(au == null) au = this.lastActCell;
			
			double a = 0;
			double b = 0;
			
			int cnt = 0;
			
			do{
			
				cnt++;
				coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
				a = CoordUtils.calcEuclideanDistance(this.currentHomeLocation, coord);
				b = CoordUtils.calcEuclideanDistance(this.currentMainActLocation, coord);
			
			} while(a + b > 2 * c && cnt < 20);
				
		}
		
		// Create a new activity
		Activity activity = population.getFactory().createActivityFromCoord(type.split("_")[0], coord);
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
		this.lastActCoord = activity.getCoord();
		this.lastActCell = au;
		
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

//		if(mode != null){
//			
//			// Bound the maximum walk distance to avoid "endless" walk legs
//			if(mode.equals(TransportMode.walk) && distance > 2000){
//				
//				distance = 500 + random.nextInt(1001);
//				
//			}
//
//		}
		
		// Divide the distance by the beeline distance factor and set boundaries for maximum and minimum distance traveled
		double d = distance / 1.3;
		double minFactor = 0.75;
		double maxFactor = 1.5;
		
		// Get all landuse geometries of the current activity type within the given administrative unit
		List<Geometry> closest = (List<Geometry>) this.geoinformation.getQuadTreeForActType(actType).getRing
				(this.lastActCoord.getX(), this.lastActCoord.getY(), d * minFactor, d * maxFactor);
		
		// If there were any landuse geometries found, randomly choose one of the geometries.
		// Else pick the landuse geometry closest to the last activity coordinate.
		if(closest != null){
			
			if(!closest.isEmpty()){
				
				double w = 0;
				for(Geometry g : closest){
					w+=g.getArea();
				}
				
				double shootingRandom = this.random.nextDouble() * w;
				double accumulatedWeight = 0.0d;
				Geometry area = null;
				
				for(Geometry g : closest){
					
					accumulatedWeight += g.getArea();
					if(shootingRandom <= accumulatedWeight){
						area = g;
						break;
					}
					
				}
				
				
				return this.transformation.transform(GeometryUtils.shoot(area, this.random));
				
			} else {
				
				Geometry area = this.geoinformation.getQuadTreeForActType(actType)
						.getClosest(this.lastActCoord.getX(), this.lastActCoord.getY());
				
				return this.transformation.transform(GeometryUtils.shoot(area, this.random));
				
			}
		
		} else {
			
			closest = au.getLanduseGeometries().get(actType);
			
			if(!closest.isEmpty()){
				
				double shootingRandom = this.random.nextDouble() * au.getWeightForKey(actType);
				double accumulatedWeight = 0.0d;
				Geometry area = null;
				
				for(Geometry g : closest){
					
					accumulatedWeight += g.getArea();
					if(shootingRandom <= accumulatedWeight){
						area = g;
						break;
					}
					
				}
				
				return this.transformation.transform(GeometryUtils.shoot(area, this.random));
				
			} else {
				
				Geometry area = this.geoinformation.getQuadTreeForActType(actType).getClosest(
						this.lastActCoord.getX(), this.lastActCoord.getY());
				
				return this.transformation.transform(GeometryUtils.shoot(area, this.random));
				
			}
			
		}
		
	}

}
