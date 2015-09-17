package simulationWithFacilitiesForMID_Data;

import java.util.Random;

import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationFactory;

import Mathfunctions.Calculator;

/**
 * @author yasemin 
 * 				 handles errors by choosing "random" actType according to
 *         tripduration or choosing tripdistance according to mode
 *
 */
public class ErrorHandler {
	private Calculator calc = new Calculator();
	private Random random = new Random();

	/**
	 * @param startTime
	 *          startTime of currently performed activity
	 * @param endTime
	 *          endTime of currently performed activity
	 * @param previousActivity
	 *          activity that was performed previously
	 * @return type of activity that is currently performed calculates the
	 *         duration of current activity and chooses a type according to the
	 *         duration.
	 */
	public String chooseActType(double startTime, double endTime,
			Activity previousActivity) {

		String activityType = new String();
		double duration = calc.calculateDurationInMinutes(startTime, endTime);
		String previousActivityType = previousActivity.getType().toString();
		activityType = chooseRandomActivity(duration, previousActivityType);
		return activityType;
	}

	/**
	 * @param duration
	 *          duration of activity at destination
	 * @param previousActivityType
	 *          type of the activity that was performed previously
	 * @return activityType of random activity at destination, but not the same as
	 *         previousActivityType
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

	/**
	 * @param mode
	 *          trafficmode used on current trip
	 * @return random distance in meters which is lying for walk between 0km and
	 *         2km, for bike between 0km and 10km and for car/pt between 0km and
	 *         30km.
	 */
	public Double chooseDistance(String mode) {
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
}