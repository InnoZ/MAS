package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.VehicleType;
import com.innoz.toolbox.scenarioGeneration.population.utils.HashGenerator;
import com.innoz.toolbox.scenarioGeneration.utils.Hydrograph;
import com.innoz.toolbox.utils.matsim.RecursiveStatsContainer;

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
	
	private Map<Integer, Set<String>> regionType2Households;
	private Map<Integer, Double> stateAndRegionType2HouseholdWeights;
	
	private Map<String, ArrayList<SurveyPerson>> classifiedPersons;
	
	private double sumOfHouseholdWeights;
	private double sumOfPersonWeights;
	
	private Map<String, RecursiveStatsContainer> modeStatsContainer;
	private Map<String, Hydrograph> activityTypeHydrographs;
	
	public SurveyDataContainer(final Configuration configuration){
		
		if(configuration.surveyPopulation().isUsingHouseholds()){
			this.households = new HashMap<String, SurveyHousehold>();
		}
		
		if(configuration.surveyPopulation().getVehicleType().equals(VehicleType.SURVEY)){
			this.vehicles = new HashMap<String, SurveyVehicle>();
		}
		
		this.persons = new HashMap<String, SurveyPerson>();
		this.classifiedPersons = new HashMap<String, ArrayList<SurveyPerson>>();
		this.sumOfHouseholdWeights = 0.0d;
		this.sumOfPersonWeights = 0.0d;
		this.modeStatsContainer = new HashMap<String, RecursiveStatsContainer>();
		this.activityTypeHydrographs = new HashMap<String, Hydrograph>();
		
		this.regionType2Households = new HashMap<Integer, Set<String>>();
		this.stateAndRegionType2HouseholdWeights = new HashMap<Integer, Double>();
		
	}
	
	public void addHousehold(SurveyHousehold household, int rtyp){
		
		if(household.getWeight() != null){
			
			this.households.put(household.getId(), household);
			this.sumOfHouseholdWeights += household.getWeight();
			
			if(!this.regionType2Households.containsKey(rtyp)){
				this.regionType2Households.put(rtyp, new HashSet<String>());
			}
			
			this.regionType2Households.get(rtyp).add(household.getId());
			
		}
		
	}
	
	public void addPerson(SurveyPerson person){
		
		this.persons.put(person.getId(), person);
		this.sumOfPersonWeights += person.getWeight();
		
		String hash = HashGenerator.generateAgeGroupHash(person);
		
		if(!this.classifiedPersons.containsKey(hash)){
			
			this.classifiedPersons.put(hash, new ArrayList<>());
			
		}
		
		this.classifiedPersons.get(hash).add(person);
		
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
	
	boolean b = false;
	
	public double getSumOfPersonWeights(){
		
		if(!b){
			b = true;
			this.sumOfPersonWeights = 0.0;
			for(SurveyPerson p : this.persons.values()){
				this.sumOfPersonWeights += p.getWeight();
			}
		}
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
	
	public Map<Integer,Set<String>> getStateId2Households(){
		
		return this.regionType2Households;
		
	}
	
	public Set<String> getHouseholdsForRegionType(int rtyp){
		
		return this.regionType2Households.get(rtyp);
		
	}
	
	public double getWeightForHouseholdsInRegionType(int rtyp){

		if(this.stateAndRegionType2HouseholdWeights.get(rtyp) == null){
			
			double w = 0;
			
			for(String hhId : this.regionType2Households.get(rtyp)){

				if(this.households.get(hhId)!=null){
					
					w += this.households.get(hhId).getWeight();
					
				}
				
			}
			
			this.stateAndRegionType2HouseholdWeights.put(rtyp, w);
			
		}
		
		return this.stateAndRegionType2HouseholdWeights.get(rtyp);
		
	}
	
	public void removePerson(String id){
		SurveyPerson p = this.persons.get(id);
		if(p != null){
			this.sumOfPersonWeights -= p.getWeight();
		}
		
		this.persons.remove(id);
	}
	
	public void removeHousehold(String id){
		SurveyHousehold hh = this.households.get(id);
		if(hh != null){
			this.sumOfHouseholdWeights -= hh.getWeight();
		}
		
		this.households.remove(id);
	}
	
	public void handleNewModeEntry(String mode, double distance){
	
		if(!this.modeStatsContainer.containsKey(mode)){
			
			this.modeStatsContainer.put(mode, new RecursiveStatsContainer());
			
		}
		
		this.modeStatsContainer.get(mode).handleNewEntry(distance);
		
	}
	
}