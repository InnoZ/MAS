package innoz.scenarioGeneration.population.surveys;

import innoz.config.Configuration;
import innoz.scenarioGeneration.utils.Hydrograph;
import innoz.utils.matsim.RecursiveStatsContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.core.utils.collections.Tuple;

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
	private Map<String, SurveyVehicle> vehicles;
	
	private Map<Tuple<Integer, Integer>, Set<String>> state2Households;
	
	private Map<String, ArrayList<SurveyPerson>> classifiedPersons;
	
	private double sumOfHouseholdWeights;
	private double sumOfPersonWeights;
	
	private Map<String, RecursiveStatsContainer> modeStatsContainer;
	private Map<String, Hydrograph> activityTypeHydrographs;
	
	public SurveyDataContainer(final Configuration configuration){
		
		if(configuration.isUsingHouseholds()){
			this.households = new HashMap<String, SurveyHousehold>();
		}
		
		if(configuration.isUsingVehicles()){
			this.vehicles = new HashMap<String, SurveyVehicle>();
		}
		
		this.persons = new HashMap<String, SurveyPerson>();
		this.classifiedPersons = new HashMap<String, ArrayList<SurveyPerson>>();
		this.sumOfHouseholdWeights = 0.0d;
		this.sumOfPersonWeights = 0.0d;
		this.modeStatsContainer = new HashMap<String, RecursiveStatsContainer>();
		this.activityTypeHydrographs = new HashMap<String, Hydrograph>();
		
		this.state2Households = new HashMap<Tuple<Integer, Integer>, Set<String>>();
		
	}
	
	public Map<String, SurveyHousehold> getHouseholds(){
		
		return this.households;
		
	}
	
	public Map<String, SurveyPerson> getPersons(){
		
		return this.persons;
		
	}
	
	public Map<String, SurveyVehicle> getVehicles(){
		
		return this.vehicles;
		
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
	
	public Map<Tuple<Integer,Integer>,Set<String>> getStateId2Households(){
		return this.state2Households;
	}
	
	public Set<String> getHouseholdsForState(int stateId, int rtyp){
		
		return this.state2Households.get(new Tuple<Integer, Integer>(stateId, rtyp));
		
	}
	
	public double getWeightForHouseholdsInState(int stateId, int rtyp){
		
		double w = 0;
		
		for(String hhId : this.state2Households.get(new Tuple<Integer, Integer>(stateId, rtyp))){

			if(this.households.get(hhId)!=null){
				w += this.households.get(hhId).getWeight();
			}
			
		}
		
		return w;
		
	}
	
}
