package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils;
import com.innoz.toolbox.scenarioGeneration.utils.Weighted;

/**
 * 
 * The representation of a person that participated in a traffic survey like MiD or MOP.</br>
 * All kinds of information that were reported (e.g. sex, age, car availability, trips etc.) are stored
 * inside this class to make them accessible for demand generation.
 * 
 * @author dhosse
 *
 */
public class SurveyPerson extends SurveyObject implements Weighted, Comparable<Double> {

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private String sex;
	
	private int age;
	private int personGroup;
	private int lifePhase;
	
	private Integer regionType;

	private double weight;
	private double weightOfAllPlans = 0.;
	
	private boolean carAvailable;
	private boolean hasLicense;
	private boolean isEmployed;
	private boolean bikeAvailable;
	private boolean carsharingUser;
	
	private List<SurveyPlan> plans;
	
	private Map<Integer, Logbook> day2logbook;
	
	private boolean isMobile;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	SurveyPerson() {
		
		this.day2logbook = new HashMap<Integer, Logbook>();
		this.plans = new ArrayList<>();

	};
	
	public void setId(String id){
		
		this.id = id;
		
	}
	
	/**
	 * 
	 * Getter for the person's identifier.
	 * 
	 * @return The person's identifier.
	 */
	public String getId() {
		return id;
	}

	public void setSex(String sex) {

		this.sex = sex;
		
	}
	
	/**
	 * 
	 * Getter for the person's sex.
	 * 
	 * @return The person's sex.
	 */
	public String getSex() {
		
		return sex;
		
	}

	public void setAge(int age) {
		
		this.age = age;
	
	}
	
	/**
	 * 
	 * Getter for the person's age.
	 * 
	 * @return The person's age.
	 */
	public int getAge() {
		return age;
	}

	/**
	 * 
	 * Getter for the person's car availability.
	 * 
	 * @return {@code true} if the person has access to a private car, {@code false} otherwise.
	 */
	public boolean hasCarAvailable() {
		return carAvailable;
	}
	
	/**
	 * 
	 * Setter for the {@code carAvailable} variable.
	 * 
	 * @param b Boolean value that represents the availability of a private car for this person.
	 */
	public void setCarAvailable(boolean b){
		
		this.carAvailable = b;
		
	}

	/**
	 * 
	 * Getter for the person's driving license.
	 * 
	 * @return {@code true} if the person has a driving license, {@code false} otherwise.
	 */
	public boolean hasLicense() {
		return hasLicense;
	}
	
	/**
	 * 
	 * Setter for the {@code hasLicense} variable.
	 * 
	 * @param b Boolean value that represents the possession of a driving license for this person.
	 */
	public void setHasLicense(boolean b) {
		
		this.hasLicense = b;
		
	}
	
	/**
	 * 
	 * Getter for the person's employment status.
	 * 
	 * @return {@code true} if the person is employed, {@code false} otherwise.
	 */
	public boolean isEmployed() {
		
		return isEmployed;
		
	}
	
	public void setEmployed(boolean b) {
		
		this.isEmployed = b;
		
	}
	
	public void setBikeAvailable(boolean b) {
		
		this.bikeAvailable = b;
		
	}
	
	public boolean hasBikeAvailable() {
		
		return this.bikeAvailable;
		
	}
	
	public boolean isCarsharingUser() {
		
		return this.carsharingUser;
		
	}

	/**
	 * 
	 * Getter for the person's plans.
	 * 
	 * @return A collection of all the plans the survey person reported.
	 */
	public List<SurveyPlan> getPlans() {
		
		return plans;
		
	}

	/**
	 * 
	 * Getter for the person's weight factor.
	 * 
	 * @return The person's weight factor.
	 */
	@Override
	public double getWeight() {
		
		return weight;
		
	}

	/**
	 * 
	 * Setter for the person's weight factor.
	 * 
	 * @param weight The weight factor.
	 */
	@Override
	public void setWeight(double weight) {
		
		this.weight = weight;
		
	}

	/**
	 * 
	 * Getter for the person group this person is in (according to MiD classification).
	 * 
	 * @return The person group.
	 */
	public int getPersonGroup() {
		
		return personGroup;
		
	}

	/**
	 * 
	 * Setter for the person group this person is in (according to MiD classification).
	 * 
	 * @param personGroup Integer representation of the person group.
	 */
	public void setPersonGroup(int personGroup) {
	
		this.personGroup = personGroup;
		
	}

	/**
	 * 
	 * Getter for the person's life phase (according to MiD classification).
	 * 
	 * @return The person's life phase.
	 */
	public int getLifePhase() {
	
		return lifePhase;
		
	}

	/**
	 * 
	 * Setter for the person's life phase (according to MiD classification).
	 * 
	 * @param lifePhase Integer representation of the life phase.
	 */
	public void setLifePhase(int lifePhase) {
		
		this.lifePhase = lifePhase;
		
	}
	
	/**
	 * 
	 * Getter for the sum of the weight factors of all the person's plans.
	 * 
	 * @return The sum of the person's plans' weight factors.
	 */
	public double getWeightOfAllPlans() {
	
		if(this.weightOfAllPlans == 0) {
			
			this.weightOfAllPlans = this.plans.stream().collect(Collectors.summarizingDouble(SurveyPlan::getWeight)).getSum();
			
		}
		
		return this.weightOfAllPlans;
		
	}
	
	public Map<Integer,Logbook> getLogbook() {
		
		return this.day2logbook;
		
	}
	
	public boolean isMobile() {
		
		return this.isMobile;
		
	}
	
	public void setMobile(boolean mobile) {
		
		this.isMobile = mobile;
		
	}

	public Integer getRegionType() {
		
		return regionType;
		
	}

	public void setRegionType(Integer regionType) {
	
		this.regionType = regionType;
		
	}

	@Override
	public String toString() {
		
		return "[id='" + this.id + "']";
		
	}

	@Override
	public int compareTo(Double w) {

		return Double.compare(this.weight, w);
		
	}
	
	public int getAgeGroup() {
		
		return PersonUtils.getAgeGroup(this.age);
		
	}
	
}