package playground.dhosse.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.List;

import playground.dhosse.scenarioGeneration.population.utils.HashGenerator;

public class SurveyPerson {

	private String id;
	private String sex;
	private int age;
	private boolean carAvailable;
	private boolean hasLicense;
	private boolean isEmployed;
	private double weight;
	private int personGroup;
	private int lifePhase;
	private double weightOfAllPlans = 0.;
	
	private List<SurveyPlan> plans;
	
	public SurveyPerson(String id, String sex, String age, String carAvailable, String hasLicense, String isEmployed){
		
		this.id = id;
		this.sex = sex.equals(MiDConstants.SEX_MALE) ? "male" : "female";
		this.age = !age.equals(MiDConstants.NAN) ? Integer.parseInt(age) : Integer.MIN_VALUE;
		
		if(carAvailable.equals("1") || carAvailable.equals("2")){
			
			this.carAvailable = true;
			
		} else{
			
			this.carAvailable = false;
			
		}
		
		this.hasLicense = hasLicense.equals("1") ? true : false;
		this.isEmployed = isEmployed.equals("1") ? true : false;
		
		this.plans = new ArrayList<>();
		
	}
	
	public String getId() {
		return id;
	}

	public String getSex() {
		return sex;
	}

	public int getAge() {
		return age;
	}

	public boolean getCarAvailable() {
		return carAvailable;
	}
	
	public void setCarAvailable(boolean b){
		
		this.carAvailable = b;
		
	}

	public boolean hasLicense() {
		return hasLicense;
	}
	
	public void setHasLicense(boolean b){
		this.hasLicense = b;
	}

	public boolean isEmployed() {
		return isEmployed;
	}
	
	public String generateHash(){
		
		return HashGenerator.generateMiDPersonHash(this);
		
	}

	public List<SurveyPlan> getPlans() {
		return plans;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getPersonGroup() {
		return personGroup;
	}

	public void setPersonGroup(int personGroup) {
		this.personGroup = personGroup;
	}

	public int getLifePhase() {
		return lifePhase;
	}

	public void setLifePhase(int lifePhase) {
		this.lifePhase = lifePhase;
	}
	
	public double getWeightOfAllPlans(){
		return this.weightOfAllPlans;
	}
	
	public void incrementPlansWeight(double v){
		this.weightOfAllPlans += v;
	}
	
}
