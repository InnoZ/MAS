package simulationWithFacilitiesForMID_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

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
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.FacilitiesReaderMatsimV1;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.ObjectAttributes;

import Mathfunctions.Calculator;

import com.ctc.wstx.dtd.StarModel;

public class CreateDemandWithMID_Data {
	Scenario scenario;

	private String tripsFile = "./input/CensusAndTravelsurveys/MID/travelsurvey.csv";

	private ObjectAttributes personHomeAndWorkLocations;
	private Random random = new Random(3838494);
	private Calculator calc = new Calculator();
	private ArrayList<Id> errorPersons;
	private Map<Id, Integer> workFacilityCapacities = new HashMap<Id, Integer>();

	private QuadTree<ActivityFacility> shopFacilitiesTree;
	private QuadTree<ActivityFacility> leisureFacilitiesTree;
	private QuadTree<ActivityFacility> educationFacilitiesTree;
	private QuadTree<ActivityFacility> workFacilitiesTree;

	private final static Logger log = Logger
			.getLogger(CreateDemandWithMID_Data.class);

	public void run(Scenario scenario, ObjectAttributes objectAttributes,
			ArrayList<Id> errorPersons, Map<Id, Integer> workFacilityCapacities) {
		this.scenario = scenario;
		this.personHomeAndWorkLocations = objectAttributes;
		this.errorPersons = errorPersons;
		this.workFacilityCapacities = workFacilityCapacities;
		this.init();
		this.createPlans();
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

	/*
	 * Create a day plan and add it to the person
	 */
	private Plan CreatePlanForPerson(Person person, Population population) {
		PopulationFactory populationFactory = population.getFactory();

		Plan plan = populationFactory.createPlan();
		person.addPlan(plan);
		((PersonImpl) person).setSelectedPlan(plan);
		return plan;
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
	 * @param coordStart
	 *          coordinate of the starting point for this trip
	 * @param distance
	 *          real trip-distance from starting point to destination(read from
	 *          MID-Data)
	 * @return facility with the correct activityType at optimal destination point
	 * 
	 *         This method finds a facility which satisfies the following
	 *         condition: absolutValue( distance(startingPoint, facility) -
	 *         real_tripDistance ) = minimal. If activityType is work, then
	 *         additionally to satisfying the above condition the facility with
	 *         maximal capacity is chosen.
	 */
	private ActivityFacility chooseFacility(String activityType,
			Coord coordStart, double distance) {

		/*
		 * find suitable facilities in the ring with width 0.4*[input-distance]
		 * around coordStart and with a distance of 0.8*[input-distance] from this
		 * center.
		 */
		ArrayList<ActivityFacility> facilities = new ArrayList<ActivityFacility>();

		facilities = findFacilitiesInCertainArea(activityType, distance, coordStart);

		if (facilities == null) {
			return null;
		}

		/*
		 * find nearest facility to a coordinate which has input-distance to
		 * starting point.
		 */
		double simulatedDistance = 0;
		double variance = 0;
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		ActivityFacility facility = null;

		if (activityType.startsWith("w")) {
			int capacity = 0;

			for (ActivityFacility activityFacility : facilities) {
				capacity = workFacilityCapacities.get(activityFacility.getId());
				simulatedDistance = calc.calculateDistance(activityFacility.getCoord(),
						coordStart);
				variance = Math.abs(simulatedDistance - distance);
				if (capacity > max) {
					max = capacity;
					facility = activityFacility;
				} else if (capacity == max) {

					if (variance <= min) {
						min = variance;
						facility = activityFacility;
					}
				}
			}
		} else {
			for (ActivityFacility activityFacility : facilities) {

				simulatedDistance = calc.calculateDistance(activityFacility.getCoord(),
						coordStart);
				variance = Math.abs(simulatedDistance - distance);
				if (variance <= min) {
					min = variance;
					facility = activityFacility;
				}
			}
		}
		return facility;
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
					// System.out.println("no facility found!");
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
		facility = (ActivityFacility) this.personHomeAndWorkLocations.getAttribute(
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
			// int index_xCoordOrigin = 2;
			// int index_yCoordOrigin = 3;
			int index_mode = 2;
			int index_activityType = 3;
			int index_distance = 4;
			int index_activityDuration = 5;
			int index_startTime = 6;
			int index_endTime = 7;
			// int index_sex = 8;
			// int index_age = 9;
			// int index_CarAvailable = 10;
			// int index_license =11;
			int index_numberOfCompanions = 12;
			// int index_isEmployed = 13;

			Activity previousActivity = null;
			Activity prepreviousActivity = null;
			Activity temporary = null;
			Person previousPerson = null;
			String previousMode = new String();
			double previousStartTime = 0.0;
			double previousEndTime = 0.0;
			double startTime = 0.0;
			double duration = 0.0;
			Plan plan = null;

			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");
				Id personId = new IdImpl(parts[index_personId]);
				/*
				 * Errorhandling: skip the line if personId doesn't exist (person was
				 * put on errorList in CreatePopulationWithMID_Data.java) or tripId > 99
				 * (means that the line with this tripId contains many "NULL"s)
				 */
				if (!(this.errorPersons.contains(personId))
						&& (Integer.valueOf(parts[index_tripID]) < 99)
						&& !(parts[index_startTime].equals("NULL"))
						&& !(parts[index_endTime].equals("NULL"))) {

					Person person = population.getPersons().get(personId);
					/*
					 * set subsequently for previous trip the end-time of activity
					 */
					if (previousPerson != null && personId.equals(previousPerson.getId())) {
						startTime = calc.calculateTimeInSeconds(parts[index_startTime]
								.trim());
						/*
						 * error-handling: If currentActivity starts earlier than
						 * previousActivity, swap startTimes
						 */
						/*
						 * if (previousStartTime > startTime) { double temp = startTime;
						 * startTime = previousStartTime; previousStartTime = temp; if
						 * (prepreviousActivity != null) {
						 * if(person.getId().toString().equals("2017483")){
						 * System.out.println("ACTS vorher: temp =  " + temporary.toString()
						 * + " prevAct = " + previousActivity.toString() + " preprev = " +
						 * prepreviousActivity); } temporary = prepreviousActivity;
						 * prepreviousActivity = previousActivity; previousActivity =
						 * temporary; } if(person.getId().toString().equals("2017483")){
						 * System.out.println("ACTS nachher: temp =  " +
						 * temporary.toString() + " prevAct = " +
						 * previousActivity.toString() + " preprev = " +
						 * prepreviousActivity); } }
						 */
						previousActivity.setEndTime(startTime);
						// store the desired duration in the persons knowledge
						storePersonsDesiredDuration(startTime, previousEndTime,
								previousPerson, previousActivity.getType());

					} else {
						/*
						 * eliminate subsequently for previous activity home the end-time
						 * (last activity in plan doesn't have an end-time) and add a leg to
						 * home-act if necessary
						 */
						if (previousPerson != null) {
							List<PlanElement> planE = new LinkedList<PlanElement>();
							planE = sortListOfPlanElementsByEndTimes(plan.getPlanElements(),
									populationFactory);
							plan.getPlanElements().clear();
							insertPlanElementsInPlan(plan, planE);
							Activity activity = createHomeActivityFromFacilitiesMap(
									previousPerson.getId(), populationFactory);
							plan.getPlanElements().remove(previousActivity);
							if (!previousActivity.getType().equals("home")) {
								double endTime = createEndTimeForLastAcivity(
										previousActivity.getType(), previousEndTime,
										previousPerson.getId());
								previousActivity.setEndTime(endTime);
								plan.getPlanElements().add(previousActivity);
								plan.addLeg(populationFactory.createLeg(previousMode));
							}
							plan.addActivity(activity);
							previousStartTime = 0.0;
						}
						/*
						 * If a new person is read(i.e. !person.equals(previousPerson))
						 * create a plan for that person and add a home activity (first
						 * origin activity) before adding a leg and another activity
						 * (destination activity)
						 */
						plan = CreatePlanForPerson(person, population);
						((PersonImpl) person).createDesires("desired activity durations");
						Activity activity = createHomeActivityFromFacilitiesMap(
								person.getId(), populationFactory);
						startTime = calc.calculateTimeInSeconds(parts[index_startTime]
								.trim());
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
					plan.addLeg(populationFactory.createLeg(mode));
					/*
					 * Add activity given its type.
					 */
					Coord coordStart = previousActivity.getCoord();
					String activityType = parts[index_activityType].trim();
					ActivityFacility facility;

					if (activityType.equals("home")) {
						facility = (ActivityFacility) this.personHomeAndWorkLocations
								.getAttribute(person.getId().toString(), "home");

					} else {
						if (activityType.equals("other") || activityType.equals("NULL")) {
							// get trip-duration
							if (parts[index_activityDuration].trim().equals("NULL")) {
								duration = calc.calculateDurationInMinutes(
										parts[index_startTime], parts[index_endTime]);
							} else {
								duration = Double.parseDouble(parts[index_activityDuration]);
							}
							String previousActivityType = previousActivity.getType()
									.toString();

							activityType = chooseRandomActivity(duration,
									previousActivityType);
						}

						double distanceFromPreviousToCurrentAct = 0;

						if (parts[index_distance].equals("NULL")) {
							distanceFromPreviousToCurrentAct = errorHandlingForDistance(mode);
						} else {
							distanceFromPreviousToCurrentAct = calc
									.calculateDistanceInMeter(parts[index_distance].trim());
						}
						facility = chooseFacility(activityType, coordStart,
								distanceFromPreviousToCurrentAct);
						if (facility == null) {
							continue;
						}
					}

					Activity activity = populationFactory.createActivityFromCoord(
							activityType, facility.getCoord());
					((ActivityImpl) activity).setFacilityId(facility.getId());

					/*
					 * if(person.getId().toString().equals("2017483")){
					 * System.out.println("currentAct: " + activity.getType() + "  prev: "
					 * + calc.makeTimePrintable(previousStartTime) + " start: " +
					 * calc.makeTimePrintable(startTime) +
					 * " previousStartTime > startTime? " + (previousStartTime >
					 * startTime)); }
					 */
					previousStartTime = startTime;
					previousEndTime = calc.calculateTimeInSeconds(parts[index_endTime]
							.trim());
					plan.addActivity(activity);
					prepreviousActivity = previousActivity;
					previousActivity = activity;

				}
			}
			bufferedReader.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void insertPlanElementsInPlan(Plan plan, List<PlanElement> planE) {
		System.out.println("PLANELEMENTS:  " + planE.toString());
		for (PlanElement planElement : planE) {
			if (planElement instanceof Activity) {
				plan.addActivity((Activity) planElement);
			} else {
				plan.addLeg((Leg) planElement);
			}
		}
	}

	private List<PlanElement> sortListOfPlanElementsByEndTimes(
			List<PlanElement> planElements, PopulationFactory populationFactory) {
		Plan plan = populationFactory.createPlan();
		List<Activity> activities = new ArrayList<Activity>();
		ListIterator<PlanElement> listIterator = planElements
				.listIterator(planElements.size());
		/*
		 * partition of planElements in activities and legs
		 */
		while (listIterator.hasNext()) {
			if (listIterator.next() instanceof Activity) {
				activities.add((Activity) listIterator.next());
				planElements.remove(listIterator.next());
			}
		}

		for (int i = 0; i < activities.size() - 1; i++) {
			double endTime1 = (activities.get(i)).getEndTime();
			double endTime2 = (activities.get(i + 1)).getEndTime();
			if (endTime1 > endTime2) {
				ActivityImpl temp = new ActivityImpl(activities.get(i + 1));
				activities.add(i, temp);
				activities.remove(i + 2);
			}
		}

		for (int i = 0; i < planElements.size() - 1; i += 2) {
			System.out.println("sizeof(acts): " + activities.size() + " sizeof(legs) " + planElements.size());
			ActivityImpl temp = new ActivityImpl(activities.get(0));
			planElements.add(i, temp);
			activities.remove(0);
		}
		return planElements;
	}

	private void storePersonsDesiredDuration(double startTime,
			double previousEndTime, Person previousPerson, String prevActType) {
		double activityDuration = startTime - previousEndTime;
		if (activityDuration > 0) {
			((PersonImpl) previousPerson).getDesires().putActivityDuration(
					prevActType, activityDuration);
		}
	}

	/**
	 * @param mode
	 *          trafficmode used on current trip
	 * @return random distance in meters which is lying for walk between 0km and
	 *         2km, for bike between 0km and 10km and for car/pt between 0km and
	 *         30km.
	 */
	private Double errorHandlingForDistance(String mode) {
		double randomDist = 0;
		if (mode.startsWith("w")) {
			randomDist = random.nextDouble() * 2;
		} else if (mode.startsWith("b")) {
			randomDist = random.nextDouble() * 10;
		} else {
			randomDist = random.nextDouble() * 30;
		}
		// convert from km to meters
		return randomDist * 1000;
	}

	/**
	 * @param duration
	 *          duration of activity at destination
	 * @param previousActivityType
	 *          type of the activity that was performed previously
	 * @return activityType of random activity at destination
	 */
	private String chooseRandomActivity(double duration,
			String previousActivityType) {
		String activityType = null;
		/*
		 * if activityDuration < 60 minutes choose between shop and leisure, else
		 * choose between these two and additionally work and education
		 */
		if (duration < 60) {
			boolean shop = random.nextBoolean();
			if (shop) {
				activityType = "shop";
			} else {
				activityType = "leisure";
			}
		} else {
			do {
				int chooseActivity = random.nextInt(3);
				switch (chooseActivity) {
				case 0:
					activityType = "work";
					break;
				case 1:
					activityType = "shop";
					break;
				case 2:
					activityType = "education";
					break;
				case 3:
					activityType = "leisure";
					break;
				default:
					break;
				}
			} while (activityType.equals(previousActivityType));
		}
		return activityType;
	}

	public Scenario getScenario() {
		return scenario;
	}

	private QuadTree<ActivityFacility> createActivitiesTree(String activityType,
			Scenario scenario) {
		QuadTree<ActivityFacility> facQuadTree;

		if (activityType.equals("all")) {
			facQuadTree = this.builFacQuadTree(activityType,
					((ScenarioImpl) scenario).getActivityFacilities().getFacilities());
		} else {
			facQuadTree = this.builFacQuadTree(activityType,
					((ScenarioImpl) scenario).getActivityFacilities()
							.getFacilitiesForActivityType(activityType));
		}
		return facQuadTree;
	}

	private QuadTree<ActivityFacility> builFacQuadTree(String type,
			Map<Id, ? extends ActivityFacility> facilities_of_type) {
		log.info(" building " + type + " facility quad tree");
		double minx = Double.POSITIVE_INFINITY;
		double miny = Double.POSITIVE_INFINITY;
		double maxx = Double.NEGATIVE_INFINITY;
		double maxy = Double.NEGATIVE_INFINITY;

		for (final ActivityFacility f : facilities_of_type.values()) {
			if (f.getCoord().getX() < minx) {
				minx = f.getCoord().getX();
			}
			if (f.getCoord().getY() < miny) {
				miny = f.getCoord().getY();
			}
			if (f.getCoord().getX() > maxx) {
				maxx = f.getCoord().getX();
			}
			if (f.getCoord().getY() > maxy) {
				maxy = f.getCoord().getY();
			}
		}
		minx -= 1.0;
		miny -= 1.0;
		maxx += 1.0;
		maxy += 1.0;
		System.out.println("        xrange(" + minx + "," + maxx + "); yrange("
				+ miny + "," + maxy + ")");
		QuadTree<ActivityFacility> quadtree = new QuadTree<ActivityFacility>(minx,
				miny, maxx, maxy);
		for (final ActivityFacility f : facilities_of_type.values()) {
			quadtree.put(f.getCoord().getX(), f.getCoord().getY(), f);
		}
		log.info("Quadtree size: " + quadtree.size());
		return quadtree;
	}
}
