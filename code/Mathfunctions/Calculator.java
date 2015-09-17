package Mathfunctions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

import countTraffic.Counts;

public class Calculator {

	private double scalingfactor = 10;
	private Random random = new Random();

	/**
	 * scale from person to agent
	 * 
	 * @param d
	 *          number of persons
	 * @return number of agents
	 */
	public double scale(double d) {
		return d / this.scalingfactor;
	}

	/**
	 * scale from agents to persons
	 * 
	 * @param d
	 *          number of agents
	 * @return number of persons
	 */
	public int scaleReverse(double d) {
		return (int) (d * this.scalingfactor);

	}

	/**
	 * @param a
	 * @param absoluteDifference
	 * @return n2 as % of n1
	 */
	private double percentage(double a, double absoluteDifference) {
		return Math.abs(roundTo2DecimalPlaces((100.0 / a) * absoluteDifference));
	}

	/**
	 * @param a
	 * @param b
	 * @return (a-b) as percentage of a
	 */
	public double relativeDifference(double a, double b) {
		double absoluteDifference = a - b;
		return percentage(a, absoluteDifference);
	}

	private double roundTo2DecimalPlaces(double inputNumber) {
		return Math.round(inputNumber * 100) / 100.0;
	}

	/**
	 * @param inputvalues
	 * @return arithmetic mean of all given inputvalues
	 */
	public double arithmeticMean(ArrayList<Double> inputvalues) {
		double sum = 0;
		for (Double input : inputvalues) {
			sum += input;
		}
		double mean = sum / (inputvalues.size());
		return roundTo2DecimalPlaces(mean);
	}

	/**
	 * @param inputvalues
	 * @return arithmetic mean of all given inputvalues
	 */
	public double arithmeticMeanInteger(ArrayList<Integer> inputvalues) {
		int sum = 0;
		for (Integer input : inputvalues) {
			sum += input;
		}
		double mean = (double) sum / (double) (inputvalues.size());
		return roundTo2DecimalPlaces(mean);
	}

	/**
	 * @param inputvalues
	 * @param arithmeticMean
	 * @return variance of given inputvalues
	 */
	public double variance(ArrayList<Double> inputvalues, double arithmeticMean) {
		double quadSum = 0;
		for (Double input : inputvalues) {
			quadSum += Math.pow((input - arithmeticMean), 2);
		}
		double variance = quadSum / (inputvalues.size());
		return roundTo2DecimalPlaces(variance);
	}

	/**
	 * @param variance
	 * @return standard deviation of given variance
	 */
	public double standardDeviaion(double variance) {
		double deviation = Math.sqrt(variance);
		return roundTo2DecimalPlaces(deviation);
	}

	/**
	 * @param map
	 *          with Integer-values
	 * @return sum over all map-values
	 */
	public int sumOverIntegerMapValues(Map<Id, Integer> map) {
		int currentValue;
		int sum = 0;
		for (Entry<Id, Integer> countEntry : map.entrySet()) {
			currentValue = countEntry.getValue();
			sum += currentValue;
		}
		return sum;
	}

	/**
	 * @param map
	 *          with Counts-values
	 * @return sum over all values which are stored in the Counts-elements of the
	 *         map
	 */
	public int sumOverCountsMapValues(Map<Id, Counts> volume) {
		Counts currentValue;
		int sum = 0;
		for (Entry<Id, Counts> countEntry : volume.entrySet()) {
			currentValue = countEntry.getValue();
			sum += currentValue.getTotal();
		}
		return sum;
	}

	/**
	 * @param time
	 *          String which contains a daytime in the form "hour:minute:second"
	 * @return the time as a double expressed in seconds
	 */
	public double calculateTimeInSeconds(String time) {
		String timeParts[] = time.split(":");

		int hours = Integer.valueOf(timeParts[0]);
		int minutes = Integer.valueOf(timeParts[1]);
		return hours * 3600 + minutes * 60;
	}

	/**
	 * @param start_time
	 *           Strings which contains a daytime in the form "hour:minute:second"
	 * @param end_time
	 *           Strings which contains a daytime in the form "hour:minute:second"
	 * @return the duration between start- and endtime in minutes
	 */
	public double calculateDurationInMinutes(String start_time, String end_time) {
		double start = calculateTimeInSeconds(start_time);
		double end = calculateTimeInSeconds(end_time);
		return (end - start) / 60;
	}

	/**
	 * @param distance
	 *          String which contains a distance in the form "x,yz..." km
	 * @return the distance as an int expressed in meters
	 */
	public int calculateDistanceInMeter(String distance) {

		String distanceParts[] = distance.split(",");
		int km = Integer.valueOf(distanceParts[0]) * 1000;
		if (distanceParts.length > 1) {
			int hunderterStelle = Integer.valueOf(distanceParts[1].charAt(0) + "") * 100;
			km += hunderterStelle;
			if (distanceParts[1].length() >= 2) {
				int zehnerStelle = Integer.valueOf(distanceParts[1].charAt(1) + "") * 10;
				km += zehnerStelle;
			}
		}
		return km;
	}

	/**
	 * @param tripStart
	 * @param tripEnd
	 * @return distance length of the trip in meters
	 */
	public double calculateDistance(Coord tripStart, Coord tripEnd) {

		double x = Math.abs(tripEnd.getX() - tripStart.getX());
		double y = Math.abs(tripEnd.getY() - tripStart.getY());

		double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double beeLineToRouteDistance = 1.3;
		return distance * beeLineToRouteDistance;
	}

	private double randomizeTimes() {
		final double sigma = 1.0;
		return random.nextGaussian() * sigma * 3600.0;
	}
}
