package simulationWithFacilitiesForMID_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.utils.objectattributes.ObjectAttributes;

import Mathfunctions.Calculator;

/**
 * @author yasemin a class for generating initial plans for all agents belonging
 *         to a MATSim-population which allready exists in the scenario. The
 *         input-file has to be a table in csv-format with the columns personId,
 *         tripID, mode, activityType, distance, activityDuration, startTime,
 *         endTime; Here the personIds from the input-table have to be identical
 *         to the personIds of the given population (the population is generated
 *         by the class CreatePopulation.java).
 * 
 *         ErrorHandling: startTime/endTime == NULL is intercepted by
 *         eliminating the appropriate trip. persons for whom there is missing
 *         too much information about all trips are removed from the population.
 *         If an activityType (travelpurpose) or tripdistance is missing, then
 *         they will be generated independently of the information of the
 *         inputtable.
 *
 */
public class CreateDemandWithMID_Data {
	Scenario scenario;

	private String tripsFile = "./input/CensusAndTravelsurveys/MID/travelsurvey.csv";

	private ObjectAttributes personsHomeLocations;
	private ObjectAttributes personsActDurDesire = new ObjectAttributes();
	private ObjectAttributes personsCS_CardExistence = new ObjectAttributes();

	public ObjectAttributes getPersonsCS_CardExistence() {
		return personsCS_CardExistence;
	}

	public ObjectAttributes getPersonsActDurDesire() {
		return personsActDurDesire;
	}

	private Random random = new Random(3838494);
	private Calculator calc = new Calculator();
	private ArrayList<Id> errorPersons;
	private Map<Id, Integer> workFacilityCapacities = new HashMap<Id, Integer>();

	private QuadTree<ActivityFacility> shopFacilitiesTree;
	private QuadTree<ActivityFacility> leisureFacilitiesTree;
	private QuadTree<ActivityFacility> educationFacilitiesTree;
	private QuadTree<ActivityFacility> workFacilitiesTree;
	private int noFacilitiesFoundCounter = 0;
	private int PersonsRemovedFromPopulationCounter = 0;

	private final static Logger log = Logger
			.getLogger(CreateDemandWithMID_Data.class);

	public void run(Scenario scenario, ObjectAttributes personHomeLocations,
			ArrayList<Id> errorPersons, Map<Id, Integer> workFacilityCapacities) {
		this.scenario = scenario;
		this.personsHomeLocations = personHomeLocations;
		this.errorPersons = errorPersons;
		this.workFacilityCapacities = workFacilityCapacities;
		this.init();
		this.createPlans();
		printInformationProcessInformation();
	}

	private void init() {
		/*
		 * Build quad trees for assigning facility locations
		 */
		this.shopFacilitiesTree = this.createActivitiesTree("shop", this.scenario);
		this.leisureFacilitiesTree = this.createActivitiesTree("leisure",
				this.scenario);
		this.educationFacilitiesTree = this.createActivitiesTree("education",
				this.scenario);
		this.workFacilitiesTree = this.createActivitiesTree("work", this.scenario);
	}

	private void printInformationProcessInformation() {
		System.out
				.println("\n PROCESS INFORMATION "
						+ "\n Number of cancelled trips because of no suitable facility was found: "
						+ this.noFacilitiesFoundCounter
						+ "\n Number of persons removed from population because of incomplete input-data: "
						+ this.PersonsRemovedFromPopulationCounter + "\n");
	}

	/**
	 * @param activityType
	 * @return QuadTree that contains all facilities for the specified
	 *         input-activityType
	 */
	private QuadTree<ActivityFacility> getFacilitesTree(String activityType) {
		QuadTree<ActivityFacility> facilitiesTree = null;

		int type = activityType.charAt(0);
		switch (type) { // e = 101, l = 108, s = 115, w = 119

		case 101:
			facilitiesTree = this.educationFacilitiesTree;
			break;
		case 108:
			facilitiesTree = this.leisureFacilitiesTree;
			break;
		case 115:
			facilitiesTree = this.shopFacilitiesTree;
			break;
		case 119:
			facilitiesTree = this.workFacilitiesTree;
			break;
		default:
			break;
		}
		return facilitiesTree;
	}

	/**
	 * @param activityType
	 *          at destination
	 * @param distance
	 *          optimal distance from coordStart to destination
	 * @param coordStart
	 *          center of the area in which we are looking for facilities
	 * @return List of all facilities which have the specified activityType and
	 *         lie in the area between two concentric circles around coordStart
	 *         with radii 08.*distance and 1.2*distance.
	 * @throws NullPointerException
	 *           , if the facilitiesTree for the specified activityType is null.
	 */
	private ArrayList<ActivityFacility> findFacilitiesInCertainArea(
			String activityType, double distance, Coord coordStart)
			throws NullPointerException {

		QuadTree<ActivityFacility> facilitiesTree = getFacilitesTree(activityType);
		ArrayList<ActivityFacility> facilities = new ArrayList<ActivityFacility>();

		if (facilitiesTree != null) {

			double xCoordCenter = coordStart.getX();
			double yCoordCenter = coordStart.getY();
			double radiusExpand = distance * 1.2;
			double radiusRestrict = distance * 0.8;
			ArrayList<ActivityFacility> ignorableFacilities = new ArrayList<ActivityFacility>();
			int k = 0;
			while (facilities.size() == 0) {
				facilities = (ArrayList<ActivityFacility>) facilitiesTree.get(
						xCoordCenter, yCoordCenter, radiusExpand);
				ignorableFacilities = (ArrayList<ActivityFacility>) facilitiesTree.get(
						xCoordCenter, yCoordCenter, radiusRestrict);

				facilities.removeAll(ignorableFacilities);

				radiusExpand *= 1.2;
				radiusRestrict *= 0.8;
				if (k > 20) {
					noFacilitiesFoundCounter++;
					return null;
				}
				k++;
			}
		} else {
			throw new NullPointerException(activityType + "-facilitiesTree = null");
		}
		return facilities;
	}

	private Activity createHomeActivityFromFacilitiesMap(Id personId,
			PopulationFactory populationFactory) {
		ActivityFacility facility;
		facility = (ActivityFacility) this.personsHomeLocations.getAttribute(
				personId.toString(), "home");
		ActivityImpl activity;
		activity = (ActivityImpl) populationFactory.createActivityFromCoord("home",
				facility.getCoord());
		activity.setFacilityId(facility.getId());
		activity.setLinkId(facility.getLinkId());
		activity.setType("home");
		return activity;

	}

	private double createEndTimeForLastAcivity(String activityType,
			double lastTripEndTime, Id personId) {

		double activityDuration = 0;
		int activityType_initialLetter = activityType.charAt(0);
		switch (activityType_initialLetter) {
		case 115:
			activityDuration = 0.2 + random.nextDouble(); // s = 115, shopping: 0.2 -
																										// 1.2h
			break;
		case 101:
			activityDuration = 2 + random.nextDouble() * 4; // e = 101, education:
																											// 2-6h
			break;
		case 108:
			activityDuration = 1 + random.nextDouble() * 3; // l = 108, leisure: 1 -
																											// 4h
			break;
		case 119:
			activityDuration = 3 + random.nextDouble() * 4; // w = 119, work: 3 - 7h
			break;
		default:
			break;
		}
		activityDuration *= 3600;

		double tripEndTime = activityDuration + lastTripEndTime;

		// avoid that person is longer en route than 12 p.m.
		double redundantSeconds = (tripEndTime) - 24 * 3600;
		while ((redundantSeconds > 0)) {
			activityDuration *= 0.85;
			tripEndTime = activityDuration + lastTripEndTime;
			redundantSeconds = (tripEndTime) - 24 * 3600;
		}
		return tripEndTime;
	}

	private void createPlans() {
		/*
		 * For convenience and code readability store population and population
		 * factory in a local variable
		 */
		Population population = this.scenario.getPopulation();
		PopulationFactory populationFactory = population.getFactory();

		/*
		 * Read the trips file
		 */
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					this.tripsFile));
			String line = bufferedReader.readLine(); // skip header

			int index_personId = 0;
			int index_tripID = 1;
			int index_mode = 2;
			int index_activityType = 3;
			int index_distance = 4;
			// int index_activityDuration = 5;
			int index_startTime = 6;
			int index_endTime = 7;
			// int index_sex = 8;
			// int index_age = 9;
			// int index_CarAvailable = 10;
			// int index_license =11;
			// int index_numberOfCompanions = 12;
			// int index_isEmployed = 13;

			ErrorHandler errorHandler = new ErrorHandler();
			FacilityChooser fc = new FacilityChooser();
			Activity previousActivity = null;
			Person previousPerson = null;
			double distanceFromPreviousToCurrentAct = 0;
			String previousMode = new String();
			double previousEndTime = 0;
			double startTime = 0;
			double endTime = 0;
			/**
			 * the personsWithLicenseCounter is used for giving a CS_Card to every 4th
			 * person that is possessing a driver's license
			 */
			int licenseCounter = 0;
			Plan plan = null;

			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");

				Id personId = new IdImpl(parts[index_personId]);

				/*
				 * Errorhandling: skip the line if personId doesn't exist (person was
				 * put on errorList in CreatePopulationWithMID_Data.java)
				 */
				if (!(this.errorPersons.contains(personId))) {
					Person person = population.getPersons().get(personId);

					if (!(Integer.valueOf(parts[index_tripID]) > 99)
							&& !(parts[index_startTime].equals("NULL"))
							&& !(parts[index_endTime].equals("NULL"))) {
						startTime = calc.calculateTimeInSeconds(parts[index_startTime]
								.trim());
						endTime = calc.calculateTimeInSeconds(parts[index_endTime].trim());
					} else {
						/*
						 * remove persons from the population for which there won't be any
						 * plan because of incomplete input-data.
						 */
						population.getPersons().remove(personId);
						PersonsRemovedFromPopulationCounter++;
						continue;
					}
					/*
					 * set subsequently for previous trip the end-time of activity and
					 * store persons desired activity duration in AttributesMap. Also set
					 * persons CS_CardExistence-Attribute.
					 */
					if (previousPerson != null && personId.equals(previousPerson.getId())) {
						previousActivity.setEndTime(startTime);
						// store the desired duration in the persons knowledge
						storePersonsDesiredDuration(startTime, previousEndTime,
								previousPerson, previousActivity.getType());
						licenseCounter = hasCS_Card(previousPerson,
								licenseCounter);
					} else {
						/*
						 * eliminate endTime from previousPersons's plan
						 */
						if (previousPerson != null) {
							Plan tempPlan = new PlanImpl();
							tempPlan = plan;
							plan = eliminateEndTimeForLastActHome(tempPlan,
									populationFactory, previousActivity, previousMode,
									previousEndTime, previousPerson);
						}
						/*
						 * If a new person is read(i.e. !person.equals(previousPerson))
						 * create a plan for that person and add a home activity (first
						 * origin activity) before adding a leg and another activity
						 * (destination activity)
						 */
						plan = populationFactory.createPlan();
						person.addPlan(plan);
						((PersonImpl) person).createDesires("desired activity durations");
						Activity activity = createHomeActivityFromFacilitiesMap(
								person.getId(), populationFactory);
						activity.setEndTime(startTime);
						previousActivity = activity;
						plan.addActivity(activity);
						previousPerson = person;
					}

					/*
					 * Add a leg from previous location to this location with the given
					 * mode
					 */
					String mode = parts[index_mode];
					if (mode.contains("passenger")) {
						mode = "pt";
					}
					previousMode = mode;
					/*
					 * Add activity given its type.
					 */
					Coord coordStart = previousActivity.getCoord();
					String activityType = parts[index_activityType].trim();
					ActivityFacility facility;

					if (activityType.equals("home")) {
						if (previousActivity.getType().equals("home")) {
							continue;
						}
						facility = (ActivityFacility) this.personsHomeLocations
								.getAttribute(person.getId().toString(), "home");

					} else {
						// error-handling if there's no actType given
						if (activityType.equals("other") || activityType.equals("NULL")) {
							activityType = errorHandler.chooseActType(startTime, endTime,
									previousActivity);
						}
						// get distance from previous to current
						// activity-facility-coordinates
						if (parts[index_distance].equals("NULL")) {
							distanceFromPreviousToCurrentAct = errorHandler
									.chooseDistance(mode);
						} else {
							distanceFromPreviousToCurrentAct = calc
									.calculateDistanceInMeter(parts[index_distance].trim());
						}
						facility = fc.chooseFacility(activityType, coordStart,
								distanceFromPreviousToCurrentAct);
						// facility = chooseFacility(activityType, coordStart,
						// distanceFromPreviousToCurrentAct);
					}
					if (facility != null) {

						Activity activity = populationFactory.createActivityFromCoord(
								activityType, facility.getCoord());
						((ActivityImpl) activity).setFacilityId(facility.getId());

						previousEndTime = endTime;
						plan.addLeg(populationFactory.createLeg(mode));
						plan.addActivity(activity);
						previousActivity = activity;
					}
				}
			}
			bufferedReader.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Plan eliminateEndTimeForLastActHome(Plan plan,
			PopulationFactory populationFactory, Activity previousActivity,
			String previousMode, double previousEndTime, Person previousPerson) {

		Activity activity = createHomeActivityFromFacilitiesMap(
				previousPerson.getId(), populationFactory);
		plan.getPlanElements().remove(previousActivity);
		if (!previousActivity.getType().equals("home")) {
			double endTime = createEndTimeForLastAcivity(previousActivity.getType(),
					previousEndTime, previousPerson.getId());
			previousActivity.setEndTime(endTime);
			plan.getPlanElements().add(previousActivity);
			plan.addLeg(populationFactory.createLeg(previousMode));
		}
		plan.addActivity(activity);
		return plan;
	}

	public List<PlanElement> printPlanElements(List<PlanElement> planElements) {
		Iterator<PlanElement> it = planElements.iterator();
		PlanElement pe = null;
		System.out.println(" PlanElements:  ");
		while (it.hasNext()) {
			pe = (PlanElement) it.next();
			if (pe instanceof Activity) {
				System.out.println("type = " + ((Activity) pe).getType()
						+ "  endTime = " + ((Activity) pe).getEndTime());
			} else {
				System.out.println("mode = " + ((Leg) pe).getMode()
						+ "  getDepartureTime = " + ((Leg) pe).getDepartureTime());
			}
		}
		return planElements;
	}

	private void storePersonsDesiredDuration(double startTime,
			double previousEndTime, Person person, String prevActType) {
		double activityDuration = startTime - previousEndTime;
		if (activityDuration > 0) {
			((PersonImpl) person).getDesires().putActivityDuration(prevActType,
					activityDuration);
			personsActDurDesire.putAttribute(person.getId().toString(), prevActType,
					activityDuration);
		}
	}

	/**
	 * @param person
	 *          for this person there will be made a decision about owning a
	 *          CS_Card
	 * @param personsWithLicenseCounter
	 *          helps to give a CS_Card only to every 4th license-owner.
	 * @return actual personsWithLicenseCounter
	 */

	public int hasCS_Card(Person person, int licenseCounter) {
		if (((PersonImpl) person).getLicense().equals("yes")) {
			if ((++licenseCounter) % 4 == 0) {
				personsCS_CardExistence.putAttribute(person.getId().toString(),
						"FF_CARD", true);
			} 
			licenseCounter =  licenseCounter % 4;
		}		
			return licenseCounter;
		
	}

	private boolean IsCarmodeIncludedInAgentsPlans(Person person) {
		boolean usesCar = false;
		List<PlanElement> planElements = person.getSelectedPlan().getPlanElements();
		Iterator iterator = planElements.iterator();
		while (iterator.hasNext()) {
			PlanElement planElement = (PlanElement) iterator.next();
			if (planElement instanceof Leg
					&& ((Leg) planElement).getMode().equals("car")) {
				usesCar = true;
			}
		}
		return usesCar;
	}

	public Scenario getScenario() {
		return scenario;
	}

	private QuadTree<ActivityFacility> createActivitiesTree(String activityType,
			Scenario scenario) {

		QuadTree<ActivityFacility> facQuadTree;
		FacilitiesTreeBuilder ftb = new FacilitiesTreeBuilder();

		facQuadTree = ftb.buildFacQuadTree(activityType, ((ScenarioImpl) scenario)
				.getActivityFacilities().getFacilitiesForActivityType(activityType));

		return facQuadTree;
	}

	public class FacilityChooser {
		/**
		 * @param activityType
		 * @param coordStart
		 *          coordinate of the starting point for this trip
		 * @param distance
		 *          real trip-distance from starting point to destination(read from
		 *          MID-Data)
		 * @return facility with the correct activityType at optimal destination
		 *         point
		 * 
		 *         This method finds a facility which satisfies the following
		 *         condition: absolutValue( distance(startingPoint, facility) -
		 *         real_tripDistance ) = minimal. If activityType is work, then
		 *         additionally to satisfying the above condition the facility with
		 *         maximal capacity is chosen.
		 */
		public ActivityFacility chooseFacility(String activityType,
				Coord coordStart, double distance) {

			ArrayList<ActivityFacility> facilities = new ArrayList<ActivityFacility>();
			facilities = findFacilitiesInCertainArea(activityType, distance,
					coordStart);
			if (facilities == null) {
				return null;
			}
			/*
			 * find nearest facility to a coordinate which has input-distance to
			 * starting point.
			 */
			ActivityFacility facility = null;
			/*
			 * if actType = work then find facility with biggest capacity and at the
			 * same time with the most suitable coordinates
			 */
			if (activityType.startsWith("w")) {
				facility = findFacilityWithMinimalDeviationAndMaximalCapacity(
						facilities, coordStart, distance);
			} else {
				facility = findFacilityWithMinimalDeviation(facilities, coordStart,
						distance);
			}
			return facility;
		}

		/**
		 * @param facilities
		 *          set of facilities.
		 * @param coordStart
		 *          starting point
		 * @param distance
		 *          optimal distance from starting point to facility
		 * @return facility from facility-set so that deviation(simuatedDistance
		 *         from facility to starting-point, optimal distance) is minimal
		 */
		private ActivityFacility findFacilityWithMinimalDeviation(
				ArrayList<ActivityFacility> facilities, Coord coordStart,
				double distance) {

			ActivityFacility facility = null;
			double simDistance = 0;
			double deviation = 0;
			double min = Double.POSITIVE_INFINITY;

			for (ActivityFacility activityFacility : facilities) {

				simDistance = calc.calculateDistance(activityFacility.getCoord(),
						coordStart);
				deviation = Math.abs(simDistance - distance);
				if (deviation <= min) {
					min = deviation;
					facility = activityFacility;
				}
			}
			return facility;
		}

		/**
		 * @param facilities
		 *          set of facilities.
		 * @param coordStart
		 *          starting point
		 * @param distance
		 *          optimal distance from starting point to facility
		 * @return facility from facility-set so that its capacility is maximal. For
		 *         facilities with the same capacity the one with the lower
		 *         deviation(simuatedDistance from facility to starting-point,
		 *         optimal distance) is chosen
		 */
		private ActivityFacility findFacilityWithMinimalDeviationAndMaximalCapacity(
				ArrayList<ActivityFacility> facilities, Coord coordStart,
				double distance) {

			ActivityFacility facility = null;
			double capacity = 0;
			double simDistance = 0;
			double deviation = 0;
			double max = Double.NEGATIVE_INFINITY;
			double min = Double.POSITIVE_INFINITY;

			for (ActivityFacility activityFacility : facilities) {

				capacity = workFacilityCapacities.get(activityFacility.getId());
				simDistance = calc.calculateDistance(activityFacility.getCoord(),
						coordStart);
				deviation = Math.abs(simDistance - distance);
				if (capacity != 0 && capacity > max) {
					max = capacity;
					facility = activityFacility;
				} else if (capacity == max) {
					if (deviation <= min) {
						min = deviation;
						facility = activityFacility;
					}
				}
			}
			if (capacity <= 0) {
				return null;
			} else {
				decreaseCapacityOfActualFacility(facility);
			}
			return facility;
		}

		private void decreaseCapacityOfActualFacility(ActivityFacility facility) {
			int capacity = workFacilityCapacities.get(facility.getId());
			capacity--;
			workFacilityCapacities.put(facility.getId(), capacity);
		}
	}
}
