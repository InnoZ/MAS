package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.innoz.toolbox.io.SurveyConstants;
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
public class SurveyPerson implements SurveyObject, Weighted {

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private String id;
	private String sex;
	
	private int age;
	private int personGroup;
	private int lifePhase;

	private double weight;
	private double weightOfAllPlans = 0.;
	
	private boolean carAvailable;
	private boolean hasLicense;
	private boolean isEmployed;
	private boolean carsharingUser;
	
	private List<SurveyPlan> plans;
	
	private Map<Integer, Logbook> day2logbook;
	
	private boolean isMobile;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public SurveyPerson(){
		this.day2logbook = new HashMap<Integer, Logbook>();
		this.plans = new ArrayList<>();
	};
	
	/**
	 * 
	 * Creates a new survey person object and assigns basic socio-demographic data to it.
	 * 
	 * @param id A unique identifier for this person. Normally the combination of the person's household id and its
	 * number in the household.
	 * @param sex The sex of the person (male / female).
	 * @param age The person's age in years.
	 * @param carAvailable Defines if the person has access to a private car (prerequisite for 'car' and 'ride' modes).
	 * @param hasLicense Defines if the person is allowed to drive a car (prerequisite for 'car' mode).
	 * @param isEmployed Defines if the person has a job or not.
	 */
	SurveyPerson(String id, String sex, String age, String carAvailable, String hasLicense, String isEmployed, SurveyConstants constants){
		
		this(id, sex, age, carAvailable, hasLicense, isEmployed, "2");
		
	}
	
	public SurveyPerson(String id, String sex, String age, String carAvailable, String hasLicense, String isEmployed, String isCarsharingUser){
		
		this.id = id;
		this.sex = sex.equals(SurveyConstants.getSexMale()) ? "m" : "f";
		this.age = !age.equals("NaN") ? Integer.parseInt(age) : Integer.MIN_VALUE;
		
		if(carAvailable.equals("1") || carAvailable.equals("2")){
			
			this.carAvailable = true;
			
		} else{
			
			this.carAvailable = false;
			
		}
		
		this.hasLicense = hasLicense.equals("1") ? true : false;
		this.isEmployed = isEmployed.equals("1") ? true : false;
		this.carsharingUser = isCarsharingUser.equals("1") ? true : false;
		
		this.plans = new ArrayList<>();
		
	}
	
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

	public void setSex(String sex){
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

	public void setAge(int age){
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
	public void setHasLicense(boolean b){
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
	
	public void setEmployed(boolean b){
		
		this.isEmployed = b;
		
	}
	
	public boolean isCarsharingUser(){
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
	public double getWeightOfAllPlans(){
		return this.weightOfAllPlans;
	}
	
	/**
	 * 
	 * Increments the total weight of all plans of this person. Both positive and negative values can be handled.
	 * Negative values imply that a plan has been removed from the plan collection.
	 * 
	 * @param v The additional weight.
	 */
	public void incrementPlansWeight(double v){
		this.weightOfAllPlans += v;
	}
	
	public Map<Integer,Logbook> getLogbook(){
		return this.day2logbook;
	}
	
	public boolean isMobile(){
		return this.isMobile;
	}
	
	public void setMobile(boolean mobile){
		this.isMobile = mobile;
	}
	
	public int getAgeGroup() {
		
		return PersonUtils.getAgeGroup(this.age);
		
	}
	
}
