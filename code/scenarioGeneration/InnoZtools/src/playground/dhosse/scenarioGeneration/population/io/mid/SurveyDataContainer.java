package playground.dhosse.scenarioGeneration.population.io.mid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import playground.dhosse.scenarioGeneration.utils.Hydrograph;
import playground.dhosse.utils.RecursiveStatsContainer;

/**
 * 
 * Container class to store survey data (i.e. MiD, SrV, MoP) for further demand generation.
 * 
 * @author dhosse
 *
 */
public class SurveyDataContainer {

	private Map<String, SurveyHousehold> households;
	private Map<String, SurveyPerson> persons;
	
	private Map<String, ArrayList<SurveyPerson>> classifiedPersons;
	
	private double sumOfHouseholdWeights;
	private double sumOfPersonWeights;
	
	private Map<String, RecursiveStatsContainer> modeStatsContainer;
	private Map<String, Hydrograph> activityTypeHydrographs;
	
	public SurveyDataContainer(){
		
		this.households = new HashMap<String, SurveyHousehold>();
		this.persons = new HashMap<String, SurveyPerson>();
		this.classifiedPersons = new HashMap<String, ArrayList<SurveyPerson>>();
		this.sumOfHouseholdWeights = 0.0d;
		this.sumOfPersonWeights = 0.0d;
		this.modeStatsContainer = new HashMap<String, RecursiveStatsContainer>();
		this.activityTypeHydrographs = new HashMap<String, Hydrograph>();
		
	}
	
	public Map<String, SurveyHousehold> getHouseholds(){
		
		return this.households;
		
	}
	
	public Map<String, SurveyPerson> getPersons(){
		
		return this.persons;
		
	}
	
	public void incrementSumOfHouseholdWeigtsBy(double d){
		
		this.sumOfHouseholdWeights += d;
		
	}
	
	public double getSumOfHouseholdWeights(){
		
		return this.sumOfHouseholdWeights;
		
	}
	
	public void incrementSumOfPersonWeightsBy(double d){
		
		this.sumOfPersonWeights += d;
		
	}
	
	public double getSumOfPersonWeights(){
		
		return this.sumOfPersonWeights;
		
	}
	
	public Map<String, ArrayList<SurveyPerson>> getPersonsByGroup(){
		
		return this.classifiedPersons;
		
	}
	
	public Map<String, RecursiveStatsContainer> getModeStatsContainer(){
		
		return this.modeStatsContainer;
		
	}
	
	public Map<String, Hydrograph> getActivityTypeHydrographs(){
		
		return this.activityTypeHydrographs;
		
	}
	
}
