package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.misc.Time;
import org.matsim.facilities.ActivityFacility;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.Income.IncomePeriod;
import org.matsim.households.IncomeImpl;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.ActivityLocationsType;
import com.innoz.toolbox.io.database.SurveyDatabaseParserV2;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.Landuse;
import com.innoz.toolbox.scenarioGeneration.geoinformation.landuse.ProxyFacility;
import com.innoz.toolbox.scenarioGeneration.population.OriginDestinationData;
import com.innoz.toolbox.scenarioGeneration.population.mobilityAttitude.MobilityAttitudeGroups;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlan;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanActivity;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanElement;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanTrip;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyVehicle;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.scenarioGeneration.vehicles.VehicleTypes;
import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.data.Tree.Node;
import com.innoz.toolbox.utils.data.WeightedSelection;
import com.vividsolutions.jts.geom.Geometry;

public class SurveyBasedDemandGenerator extends DemandGenerationAlgorithm {

	public SurveyBasedDemandGenerator() {}

	@Override
	public void run(String ids) {

		createCompletePopulation(ids);
		
	}
	
	/**
	 * 
	 * Creates a complete population (meaning: children, pupils, students, employees, pensioners etc.) from survey data.
	 * The activities are located according to landuse data and a gravitation model. 
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario A MATSim scenario.
	 */
	private void createCompletePopulation(String ids){
		
		// Run the survey data parser that stores all of the travel information
		SurveyDatabaseParserV2 parser = new SurveyDatabaseParserV2();
		SurveyDataContainer container = SurveyDataContainer.getInstance();
		container.init();
		parser.run(CollectionUtils.stringToSet(ids));
		
		// Choose the method for demand generation that has been specified in the configuration
		if(Controller.configuration().surveyPopulation().isUsingHouseholds()){
		
			createHouseholds(ids);
			
		} else {
			
			createPersons(ids);
			
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
	private void createHouseholds(String ids){

		// Get the MATSim population and initialize person attributes
		Population population = Controller.scenario().getPopulation();
		ObjectAttributes personAttributes = Controller.scenario().getPopulation().getPersonAttributes();
//		scenario.addScenarioElement(innoz.scenarioGeneration.population.utils.PersonUtils.PERSON_ATTRIBUTES, personAttributes);
		
		// Sort the households by their weight (according to the survey data)
		List<SurveyHousehold> households = new ArrayList<>();
		households.addAll(SurveyDataContainer.getInstance().getHouseholds().values());
		
		// Choose a home cell for the household
		// Initialize a pseudo-random number and iterate over all administrative units.
		// Accumulate their weights and as soon as the random number is smaller or equal to the accumulated weight
		// pick the current admin unit.
		for(String s : ids.split(",")){

			AdministrativeUnit d = Geoinformation.getInstance().getAdminUnit(s).getData();
			
			for(int i = 0; i < d.getNumberOfHouseholds() * Controller.configuration().scenario().getScaleFactor(); i++){
				
				this.currentHomeCell = chooseAdminUnit(d, ActivityTypes.HOME);
				
				int rtyp = this.currentHomeCell.getRegionType();
				
				// Choose a template household (weighted, same method as above)
				List<SurveyHousehold> hhInRegion = new ArrayList<>();
				households.stream().filter(p -> SurveyDataContainer.getInstance()
						.getHouseholdsForRegionType(rtyp).contains(p.getId())).forEach(e -> hhInRegion.add(e));
				SurveyHousehold template = (SurveyHousehold) WeightedSelection.choose(hhInRegion, this.random.nextDouble());
				
				// Create a MATSim household
				Household household = new HouseholdImpl(Id.create(this.currentHomeCell.getId() + "_" + i,
						Household.class));
				household.setIncome(new IncomeImpl(template.getIncome(), IncomePeriod.month));
				((HouseholdImpl)household).setMemberIds(new ArrayList<Id<Person>>());
	
				// Set global home location for the entire household
				Coord homeLocation = null;
				ActivityFacility homeFacility = null;
	
				// Go through all residential areas of the home cell and randomly choose one of them
				if(this.currentHomeCell.getLanduseGeometries().containsKey(ActivityTypes.HOME)){
					
					if(Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){
						
						homeFacility = chooseActivityFacilityInAdminUnit(this.currentHomeCell, ActivityTypes.HOME);
						homeLocation = Geoinformation.getTransformation().transform(homeFacility.getCoord());
						
					} else {
						
						homeLocation = chooseActivityCoordInAdminUnit(this.currentHomeCell, ActivityTypes.HOME);
						
					}
					
				} else {
				
					// If no residential areas exist within the home cell, shoot a random coordinate
					homeLocation = Geoinformation.getTransformation().transform(GeometryUtils.shoot(
							this.currentHomeCell.getGeometry(), this.random));
					
				}
	
				// Create as many persons as were reported and add them to the household and the population
				for(int j = 0; j < template.getNPersons(); j++){
					
					String personId = template.getMemberIds().get(j);
					SurveyPerson templatePerson = SurveyDataContainer.getInstance().getPersons().get(personId);
					
					Person person = createPerson(templatePerson, population, personAttributes, this.random.nextDouble(),
							population.getPersons().size(), homeLocation, homeFacility, household.getIncome().getIncome());
					
					// If the resulting MATSim person is not null, add it
					if(person != null){
						
						population.addPerson(person);
						household.getMemberIds().add(person.getId());
						
					}
	
				}
				
				// If we model non-generic cars, create all cars that were reported in the survey and add them to the household
				if(Controller.configuration().surveyPopulation().getVehicleType().equals(com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyVehicleType.SURVEY)){
					
					createSurveyVehicles(SurveyDataContainer.getInstance(), template, household);
					
				}
				
				// Add the household to the scenario
				Controller.scenario().getHouseholds().getHouseholds().put(household.getId(), household);
				
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
	private void createPersons(String ids){
		
		// Get the MATSim population and initialize person attributes
		Population population = Controller.scenario().getPopulation();
		ObjectAttributes personAttributes = population.getPersonAttributes();
		
		for(String s : ids.split(",")){

			AdministrativeUnit d = Geoinformation.getInstance().getAdminUnit(s).getData();
			
			for(Entry<String, Integer> entry : d.getPopulationMap().entrySet()) {
				
//				counts person created in this age group
				int agegroupPersonsCreated = 0;
				int agegroupPersonsToCreate = (int)(entry.getValue() * Controller.configuration().scenario().getScaleFactor());
				
//				gets sex and age boundaries of the current ageGroup as ageFrom and ageTo from the populationmap's key which contains all information in a string
				String y = entry.getKey().substring(entry.getKey().length()-1);
				if (y.equals("w")) {
					y="f";
				}
				String sex = y;
				int ageFrom = Integer.parseInt(entry.getKey().substring(8, 10));	
				int x = Integer.parseInt(entry.getKey().substring(12, 14));	
//				for ageGroup85to101 the String consists of 16 characters
				if (entry.getKey().length()==16){
					x = Integer.parseInt(entry.getKey().substring(12, 15));
				}	
				int ageTo = x;
				
//				method to summarize weights of current agegroup (entry)
				List<SurveyPerson> surveyPersonsInAgeGroup = new ArrayList<>();
				SurveyDataContainer.getInstance().getPersons().values().stream().filter(p -> 
				p.getAge() >= ageFrom && 
				p.getAge() < ageTo && 
				p.getSex().equals(sex) && 
				p.getRegionType().equals(d.getRegionType()))
				.forEach(p -> surveyPersonsInAgeGroup.add(p));
				
				double weightOfSurveyPersonsInAgeGroup = surveyPersonsInAgeGroup.stream().map(SurveyPerson::getWeight).collect(Collectors.summarizingDouble(Double::doubleValue)).getSum();
				
				for (SurveyPerson p : surveyPersonsInAgeGroup){

//						defining how often a template should be used by multiplying the template's weight within its agegroup with the scaled population
						int personDuplicates = (int) Math.ceil(agegroupPersonsToCreate * (p.getWeight() / weightOfSurveyPersonsInAgeGroup));
					
					for (int ii = 0; ii < personDuplicates; ii++){
					
						SurveyPerson personTemplate = null;
						
						// Choose a template person (weighted, same method as above)
						if (p != null) {
							
							personTemplate = p;
							
						}
						
						this.currentHomeCell = chooseAdminUnit(d, ActivityTypes.HOME);
						
						// Set global home location for the entire household
						Coord homeLocation = null;
						ActivityFacility homeFacility = null;
						
						// Go through all residential areas of the home cell and randomly choose one of them
//						if(this.currentHomeCell.getLanduseGeometries().containsKey(ActivityTypes.HOME)){
							
						if (Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){
							
							homeFacility = chooseActivityFacilityInAdminUnit(this.currentHomeCell, ActivityTypes.HOME);
							homeLocation = Geoinformation.getTransformation().transform(homeFacility.getCoord());
							
						} else {
							
							homeLocation = chooseActivityCoordInAdminUnit(this.currentHomeCell, ActivityTypes.HOME);
							
						}
							
//						}
						
						Person person = createPerson(personTemplate, population, personAttributes, this.random.nextDouble(),
								population.getPersons().size(), homeLocation, homeFacility, 0.0);
						
						// If the resulting MATSim person is not null, add it
						if(person != null){
							
							population.addPerson(person);
							
							agegroupPersonsCreated++;
							
						}
						
					}
					
//					ends creating persons if the required amount has been reached
					if (agegroupPersonsCreated >= agegroupPersonsToCreate) break;
						
				}
				
			}
			
			log.info(population.getPersons().size() + " persons created..." );
			
		}
		
	}

	int vehicleCounter = 0;
	
	private void createSurveyVehicles(SurveyDataContainer container, SurveyHousehold template,
			Household household) {
		
		for(String vid : template.getVehicleIds()){
			
			SurveyVehicle v = container.getVehicles().get(vid);
			
			VehicleType type = VehicleTypes.getVehicleTypeForKey(v.getKbaClass(), v.getFuelType());
			
			if(type != null){

				if(!Controller.scenario().getVehicles().getVehicleTypes().containsKey(type.getId())){
					Controller.scenario().getVehicles().addVehicleType(type);
				}
				
				Vehicle vehicle = Controller.scenario().getVehicles().getFactory().createVehicle(Id.create(vid +
						"_" + v.getFuelType().name() + "_" + vehicleCounter, Vehicle.class), type);
				Controller.scenario().getVehicles().addVehicle(vehicle);
				vehicleCounter++;
				
				if(household.getVehicleIds() == null){
					
					((HouseholdImpl)household).setVehicleIds(new ArrayList<Id<Vehicle>>());
					
				}
				
				household.getVehicleIds().add(vehicle.getId());
				
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
	private Person createPerson(SurveyPerson personTemplate, Population population, ObjectAttributes personAttributes,
			double personalRandom, int i, Coord homeCoord, ActivityFacility homeFacility, double hhIncome) {

		// Initialize main act location, last leg, last act cell and the coordinate of the last activity as null to avoid errors...
		this.currentMainActLocation = null;
		this.lastLeg = null;
		this.lastActCoord = null;
		this.lastActCell = null;
		this.currentSearchSpace = null;
		this.currentMainActFacility = null;
		
//		// Create a new MATSim person and an empty plan
		Person person = population.getFactory().createPerson(Id.createPersonId(this.currentHomeCell.getId() + "_" + personTemplate.getId() + "_" + i));
		
		Plan plan = population.getFactory().createPlan();
		
		// Set the person's attributes (sex, age, employed, license, car availability) according to what was reported in the survey
		// no need for this anymore: ??
		PersonUtils.setSex(person, personTemplate.getSex());
		PersonUtils.setAge(person, personTemplate.getAge());
		PersonUtils.setEmployed(person, personTemplate.isEmployed());
		String carAvail = personTemplate.hasCarAvailable() ? "always" : "never";
		PersonUtils.setCarAvail(person, carAvail);
		String hasLicense = personTemplate.hasLicense() ? "yes" : "no";
		PersonUtils.setLicence(person, hasLicense);
		String mobilityAttitude = MobilityAttitudeGroups.getMobilityAttitudeForAge(personTemplate.getAge());
		
		personAttributes.putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_SEX, personTemplate.getSex());
		personAttributes.putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_AGE, personTemplate.getAge());
		personAttributes.putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_EMPLOYED, personTemplate.isEmployed());
		personAttributes.putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_CAR_AVAIL, carAvail);
		personAttributes.putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_LICENSE, hasLicense);
		personAttributes.putAttribute(person.getId().toString(), 
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_BIKE_AVAIL, personTemplate.hasBikeAvailable());
		personAttributes.putAttribute(person.getId().toString(), 
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_MOBILITYATTITUDE, mobilityAttitude);
		if(personTemplate.isCarsharingUser()){
			personAttributes.putAttribute(person.getId().toString(), "OW_CARD", "true");
			personAttributes.putAttribute(person.getId().toString(), "RT_CARD", "true");
			personAttributes.putAttribute(person.getId().toString(), "FF_CARD", "true");
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
				planTemplate = (SurveyPlan) WeightedSelection.choose(personTemplate.getPlans(), personalRandom);
				
			}
			
			// Set the current home location
			this.currentHomeLocation = homeCoord != null ? homeCoord :
				OriginDestinationData.chooseHomeLocationFromGrid(currentHomeCell);
			this.lastActCoord = this.currentHomeLocation;

			// If there are at least two plan elements in the chosen template plan, generate the plan elements
			if(planTemplate.getPlanElements().size() > 1){
				
				// Distribute activities in the survey area (or its vicinity) according to the distribution matrix
				List<String> cellIds = distributeActivitiesInCells(personTemplate, planTemplate, SurveyDataContainer.getInstance());
				
				int actIndex = 0;
				
				// Create a MATSim plan element for each survey plan element and add them to the MATSim plan
				for(int j = 0; j < planTemplate.getPlanElements().size(); j++){
					
					SurveyPlanElement planElement = planTemplate.getPlanElements().get(j);
					
					if(planElement instanceof SurveyPlanActivity){
						
						Activity act = createActivity(population, personTemplate, planTemplate, planElement, cellIds.get(actIndex), SurveyDataContainer.getInstance());
						if(act == null){
							plan.getPlanElements().remove(plan.getPlanElements().size()-1);
							break;
						}
							
						plan.addActivity(act);
						actIndex++;
						
					} else {
						
						plan.addLeg(createLeg(population, planElement));
						
					}
					
				}
				
			} else {
				
				// If there is only one plan element, create a 24hrs home activity
				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
				
				if(Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){

					home.setFacilityId(homeFacility.getId());
					
				}
				
				home.setMaximumDuration(24 * 3600);
				home.setStartTime(0);
				home.setEndTime(24 * 3600);
				plan.addActivity(home);
				
			}
			
		} else {
			
			// If there is only one plan element, create a 24hrs home activity
			Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
			
			if(Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){

				home.setFacilityId(homeFacility.getId());
				
			}
			
			home.setMaximumDuration(24 * 3600);
			home.setStartTime(0);
			home.setEndTime(24 * 3600);
			plan.addActivity(home);
			
		}
		
		// In the end: add the person to the population
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		mutateActivityEndTimes(plan);
		
		return person;
		
	}
	
	private List<String> distributeActivitiesInCells(final SurveyPerson person, final SurveyPlan plan, SurveyDataContainer container){
		
		// Locate the main activity according to the distribution that was computed before
		String mainMode = plan.getMainActIndex() > 0 ? 
				((SurveyPlanTrip)plan.getPlanElements().get(plan.getMainActIndex()-1)).getMainMode() :
					((SurveyPlanTrip)plan.getPlanElements().get(plan.getMainActIndex()+1)).getMainMode();
		
		this.currentSearchSpace = new HashSet<>();
				
		this.currentMainActCell = locateActivityInCell(plan.getMainActType(), mainMode, person);
		
		double distance = container.getModeStatsContainer().get(mainMode).getMean();
		
		//TODO
		this.currentMainActLocation = 
				shootLocationForActType(this.currentMainActCell,
				plan.getMainActType(), distance, plan, mainMode, person);
		
		if(Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){
			
			this.currentMainActFacility = chooseActivityFacilityInAdminUnit(this.currentMainActCell, plan.getMainActType());
			
		}

		// To locate intermediate activities, create a search space and add the home and main activity cells
		this.currentSearchSpace.add(this.currentHomeCell);
		this.currentSearchSpace.add(this.currentMainActCell);
		c = CoordUtils.calcEuclideanDistance(this.currentHomeLocation, this.currentMainActLocation);
		
		// Also, add all cells of which the sum of the distances between their centroid and the centroids of
		// the home and the main act cell is less than twice the distance between the home and the main activity location
		for(Node<AdministrativeUnit> d : Geoinformation.getInstance().getAdminUnits()){

			for(Node<AdministrativeUnit> node : d.getChildren()){
				
				AdministrativeUnit au = node.getData();
				
				double a = CoordUtils.calcEuclideanDistance(Geoinformation.getTransformation().transform(
						MGC.point2Coord(this.currentHomeCell.getGeometry().getCentroid())),
						Geoinformation.getTransformation().transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				double b = CoordUtils.calcEuclideanDistance(Geoinformation.getTransformation().transform(
						MGC.point2Coord(this.currentMainActCell.getGeometry().getCentroid())),
						Geoinformation.getTransformation().transform(MGC.point2Coord(au.getGeometry().getCentroid())));
				
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
				
				if(act.getActType().equals(ActivityTypes.HOME)){// || act.isInHomeCell()){
					
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
					this.lastActCell = lastTo;
					
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
	 * Creates a MATSim leg from a survey trip.
	 * 
	 * @param population The MATSim population
	 * @param mpe The survey plan element (in this case: way)
	 * @return
	 */
	private Leg createLeg(Population population,
			SurveyPlanElement mpe) {
		
		SurveyPlanTrip trip = (SurveyPlanTrip)mpe;
		String mode = trip.getMainMode();
		
//		double departure = way.getStartTime();
//		double ttime = way.getEndTime() - departure;
		
		Leg leg = population.getFactory().createLeg(mode);
//		leg.setTravelTime(ttime);

		this.lastLeg = trip;
		
		return leg;
		
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
			SurveyPlanElement mpe, String cellId, SurveyDataContainer container) {

		AdministrativeUnit au = Geoinformation.getInstance().getAdminUnit(cellId).getData();
		
		// Initialize the activity type and the start and end time
		SurveyPlanActivity act = (SurveyPlanActivity)mpe;
		String type = act.getActType();
		if(type.equals(ActivityTypes.EDUCATION) && personTemplate.getAge() > 18){
			type = ActivityTypes.UNIVERSITY;
		}
		double start = act.getStartTime();
		double end = act.getEndTime();
		
		double distance = 0.;
		String mode = null;
		if(this.lastLeg != null){
			distance = this.lastLeg.getTravelTime() * container.getModeStatsContainer().get(this.lastLeg.getMainMode()).getMean();
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
			
			if(au == null) au = this.lastActCell;
			
//			double a = 0;
//			double b = 0;
//			
//			int cnt = 0;
			
//			do{
			
//				cnt++;
			coord = shootLocationForActType(au, type, distance, templatePlan, mode, personTemplate);
//				a = CoordUtils.calcEuclideanDistance(this.currentHomeLocation, coord);
//				b = CoordUtils.calcEuclideanDistance(this.currentMainActLocation, coord);
			
//			} while(a + b > 2 * c && cnt < 20);
				
		}
		
		// Create a new activity
		Activity activity = population.getFactory().createActivityFromCoord(type.split("_")[0], coord);
		activity.setStartTime(start);
		activity.setMaximumDuration(end - start);
		activity.setEndTime(end);
		if(Geoinformation.getInstance().getLanduseType().equals(ActivityLocationsType.FACILITIES)){
			activity.setFacilityId(((ProxyFacility)Geoinformation.getInstance().getLanduseOfType(type)
					.getClosest(coord.getX(), coord.getY())).get().getId());
		}
		
		if(end - start < 900){
			
			// Set the activity duration to at least 1/4 hour if it's been reported shorter to avoid
			// extremely negative scores
			activity.setMaximumDuration(900);
			activity.setEndTime(start + 900);
			
		} else{
				
			// Set the maximum duration according to the survey data
			activity.setMaximumDuration(end - start);
			
		}
		
		// If the end time is set to zero (probably last activity) or after midnight, set it to midnight
		if(end == 0 || end > Time.MIDNIGHT){
			
			activity.setEndTime(Time.MIDNIGHT);
			activity.setMaximumDuration(Time.MIDNIGHT - start);
					
		}
		
		if(start > Time.MIDNIGHT){
			
			return null;
			
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

		// Divide the distance by the beeline distance factor and set boundaries for maximum and minimum distance traveled
		double d = distance / 1.3;
		double minFactor = 0.75;
		double maxFactor = 1.25;
		
		if(Geoinformation.getInstance().getLanduseOfType(actType) == null) {
			actType = actType.split("_")[0];
		}
		
		List<Landuse> candidates = au.getLanduseGeometries().get(actType);
		
		// If there were any landuse geometries found, randomly choose one of the geometries.
		// Else pick the landuse geometry closest to the last activity coordinate.
		if(candidates != null && !candidates.isEmpty()){
			
			Geometry area = ((Landuse)WeightedSelection.choose(candidates, this.random.nextDouble())).getGeometry();
			
			return Geoinformation.getTransformation().transform(GeometryUtils.shoot(area, this.random));
		
		} else {
			
			// Get all landuse geometries of the current activity type within the given administrative unit
			List<Landuse> closest = (List<Landuse>) Geoinformation.getInstance().getLanduseOfType(actType).getRing
						(this.lastActCoord.getX(), this.lastActCoord.getY(), d * minFactor, d * maxFactor);

			if(closest != null && !closest.isEmpty()) {
				Geometry area = ((Landuse)WeightedSelection.choose(closest, this.random.nextDouble())).getGeometry();
				
				return Geoinformation.getTransformation().transform(GeometryUtils.shoot(area, this.random));
				
			} else {
				
				Coord newCoord = (GeometryUtils.shoot(lastActCoord, d * minFactor, d * maxFactor, random));
				
				return newCoord;
				
			}

		}
		
	}

}