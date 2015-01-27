package simulationWithFacilitiesForMID_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.utils.objectattributes.ObjectAttributes;

import com.ctc.wstx.dtd.StarModel;

public class CreateDemandWithMID_Data {
	private Scenario scenario;

	// We need another population, the PUS population
	private Scenario scenarioPUS;

	// [[ 3 ]] here you have to fill in the path of the pus files
	private String tripsFile = "./input/CensusAndTravelsurveys/MID/travelsurvey.csv";
	// private String pusPersonsFile = "./InputZurich/travelsurvey_persons.txt";

	private ObjectAttributes personHomeAndWorkLocations;
	private Random random = new Random(3838494);

	private List<Id> pusWorkers = new Vector<Id>();
	private List<Id> pusNonWorkers = new Vector<Id>();

	private Map<Id, Integer> facilityCapacities = new HashMap<Id, Integer>();

	private QuadTree<ActivityFacility> shopFacilitiesTree;
	private QuadTree<ActivityFacility> leisureFacilitiesTree;
	private QuadTree<ActivityFacility> educationFacilitiesTree;
	private QuadTree<ActivityFacility> workFacilitiesTree;

	private final static Logger log = Logger
			.getLogger(CreateDemandWithMID_Data.class);

	public void run(Scenario scenario, ObjectAttributes objectAttributes,
			Map<Id, Integer> facilityCapacities) {
		this.scenario = scenario;
		this.scenarioPUS = ScenarioUtils.createScenario(ConfigUtils
				.createConfig());
		this.personHomeAndWorkLocations = objectAttributes;
		this.facilityCapacities = facilityCapacities;
		this.init();
		this.createPlans();
	}

	private void init() {
		/*
		 * Build quad trees for assigning facility locations
		 */
		this.shopFacilitiesTree = this.createActivitiesTree("shop",
				this.scenario);
		this.leisureFacilitiesTree = this.createActivitiesTree("leisure",
				this.scenario);
		this.educationFacilitiesTree = this.createActivitiesTree("education",
				this.scenario);
		this.workFacilitiesTree = this.createActivitiesTree("work",
				this.scenario);
	}

	/*
	 * Create a day plan and add it to the person
	 */
	private void CreatePlanForPerson(Person person, Population population) {
		PopulationFactory populationFactory = population.getFactory();

		Plan plan = populationFactory.createPlan();
		person.addPlan(plan);
		((PersonImpl) person).setSelectedPlan(plan);
	}

	/**
	 * @param time
	 *            String which contains a daytime in the form
	 *            "hour:minute:second"
	 * @return the time as a double expressed in seconds
	 */
	private double calculateTimeInSeconds(String time) {
		String timeParts[] = time.split(":");

		int hours = Integer.valueOf(timeParts[0]);
		int minutes = Integer.valueOf(timeParts[1]);
		return hours * 360 + minutes * 60;
	}

	/**
	 * @param activityType 
	 * @return QuadTree that contains all facilities for the specified input-activityType
	 */
	private QuadTree<ActivityFacility> getFacilitesTree(String activityType){
		QuadTree<ActivityFacility> facilitiesTree = null;

		int type = activityType.charAt(0); 
		switch (type) { // e = 101, l =  108, s = 115, w = 119 
		
		case 101 : facilitiesTree = this.educationFacilitiesTree;
					break;
		case 108 : facilitiesTree = this.leisureFacilitiesTree;
					break;
		case 115 : facilitiesTree = this.shopFacilitiesTree;
					break;
		case 119 : facilitiesTree = this.workFacilitiesTree;
					break;
		default: 	break;
		}
		return facilitiesTree;
	}
	
	/**
	 * @param activityType 
	 * @param coordStart coordinate of the starting point for this trip
	 * @param distance tripdistance from starting point to destination 
	 * @return facility for the correct activityType at destination point
	 * @throws NullPointerException
	 * 
	 * This method finds a facility whose coordinates have least a distance to the starting point of the trip that is 
	 * as long as the given input-distance.
	 */
	private ActivityFacility chooseFacility(String activityType, Coord coordStart, double distance) throws NullPointerException{
		
		QuadTree<ActivityFacility> facilitiesTree = getFacilitesTree(activityType);

		if(facilitiesTree != null){
			
		  double xCoordCenter = coordStart.getX();
		  double yCoordCenter = coordStart.getY();
		  ArrayList<ActivityFacility> facilities = new ArrayList<ActivityFacility>();
		  ArrayList<ActivityFacility> previousFacilitiesCollection = new ArrayList<ActivityFacility>();
		  double radius = distance;
		  double realDistance = 0;	
		  
		  while (realDistance < distance){

			 facilities = (ArrayList<ActivityFacility>) facilitiesTree.get(xCoordCenter, yCoordCenter, radius);
		     facilities.removeAll(previousFacilitiesCollection);
		     
			 if(facilities != null && facilities.size() != 0){
			   for (ActivityFacility activityFacility : facilities) {
			     realDistance = calculateDistance(activityFacility.getCoord(), coordStart);
			     if(realDistance >= distance){
				   return activityFacility;
			     }
		       }
			 }
			 /*
			  * Es ist möglich, dass innerhalb des Gebiets mit radius x keine facility liegt, deren Abstand zum Mittelpunkt
			  * mindestens distance groß ist. Deshalb erweitern wir den Radius um 20%.
			  */
			 radius *= 1.2;
		     previousFacilitiesCollection = facilities;
		 }  
		}else{
			throw new NullPointerException("facilitiesTree = null");
		}
		return null;
	}

	private double calculateDistance(Coord start, Coord destination) {

		double x = start.getX() - start.getY();
		double y = destination.getX() - destination.getY();

		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		return distance;
	}

	/*
	 * [[ 4 ]]
	 */
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
			int index_numerOfCompanions = 12;
			// int index_isEmployed = 13;

			Activity previousActivity = null;

			Id previousPerson = null;
			boolean worker = false;
			double time = 0.0;

			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");

				Id personId = new IdImpl(parts[index_personId]);
				Person person = population.getPersons().get(personId);
				CreatePlanForPerson(person, population);
				Plan plan = person.getSelectedPlan();
				/*
				 * If a new person is read add a home activity (first origin
				 * activity) Otherwise add a leg and an activity (destination
				 * activity)
				 */
				if (!personId.equals(previousPerson)) {

					((PersonImpl) person)
							.createDesires("desired activity durations");
					ActivityFacility facility;
					facility = (ActivityFacility) this.personHomeAndWorkLocations
							.getAttribute(person.getId().toString(), "home");
					ActivityImpl activity;
					activity = (ActivityImpl) populationFactory
							.createActivityFromCoord("home",
									facility.getCoord());
					/*
					 * add facility-properties to activity
					 */
					activity.setFacilityId(facility.getId());
					activity.setLinkId(facility.getLinkId());

					activity.setType("h");
					time = calculateTimeInSeconds(parts[index_startTime].trim());
					activity.setEndTime(time);
					previousActivity = activity;
					plan.addActivity(activity);
				} else {
					/*
					 * Add a leg from previous location to this location with
					 * the given mode
					 */
					String mode = parts[index_mode].trim();
					plan.addLeg(populationFactory.createLeg(mode));

					/*
					 * Add activity given its type.
					 */
					Coord coordStart = previousActivity.getCoord();
					String activityType = parts[index_activityType].trim();
					double distanceFromPreviousToCurrentAct = Double
							.parseDouble(parts[index_distance].trim());

					ActivityFacility facility = chooseFacility(activityType,
							coordStart, distanceFromPreviousToCurrentAct);

					Activity activity = populationFactory
							.createActivityFromCoord(activityType,
									facility.getCoord());

					Double duration = Double
							.parseDouble(parts[index_activityDuration]);
					// store the desired duration in the persons knowledge
					((PersonImpl) person).getDesires().putActivityDuration(
							activityType, duration);
					plan.addActivity(activity);
				}
			}

			bufferedReader.close();
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ActivityFacility getRandomLocation(Activity activity,
			Coord coordPreviousActivity, double radius) {
		double xCoordCenter = coordPreviousActivity.getX();
		double yCoordCenter = coordPreviousActivity.getY();
		ArrayList<ActivityFacility> facilities = new ArrayList<ActivityFacility>();

		if (activity.getType().startsWith("s")) {
			while (facilities.size() == 0) {
				facilities = (ArrayList<ActivityFacility>) this.shopFacilitiesTree
						.get(xCoordCenter, yCoordCenter, radius);
				radius *= 2.0;
			}
		} else if (activity.getType().startsWith("l")) {
			while (facilities.size() == 0) {
				facilities = (ArrayList<ActivityFacility>) this.leisureFacilitiesTree
						.get(xCoordCenter, yCoordCenter, radius);
				radius *= 2.0;
			}
		} else if (activity.getType().startsWith("e")) {
			while (facilities.size() == 0) {
				facilities = (ArrayList<ActivityFacility>) this.educationFacilitiesTree
						.get(xCoordCenter, yCoordCenter, radius);
				radius *= 2.0;
			}
		} else { // work
			while (facilities.size() == 0) {
				facilities = (ArrayList<ActivityFacility>) this.workFacilitiesTree
						.get(xCoordCenter, yCoordCenter, radius);
				radius *= 2.0;
			}
		}
		int randomIndex = (int) (random.nextFloat() * (facilities.size()));
		return facilities.get(randomIndex);
	}

	private double randomizeTimes() {
		final double sigma = 1.0;
		return random.nextGaussian() * sigma * 3600.0;
	}

	public Scenario getScenario() {
		return scenario;
	}

	public QuadTree<ActivityFacility> createActivitiesTree(String activityType,
			Scenario scenario) {
		QuadTree<ActivityFacility> facQuadTree;

		if (activityType.equals("all")) {
			facQuadTree = this.builFacQuadTree(activityType,
					((ScenarioImpl) scenario).getActivityFacilities()
							.getFacilities());
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
		QuadTree<ActivityFacility> quadtree = new QuadTree<ActivityFacility>(
				minx, miny, maxx, maxy);
		for (final ActivityFacility f : facilities_of_type.values()) {
			quadtree.put(f.getCoord().getX(), f.getCoord().getY(), f);
		}
		log.info("Quadtree size: " + quadtree.size());
		return quadtree;
	}
}
