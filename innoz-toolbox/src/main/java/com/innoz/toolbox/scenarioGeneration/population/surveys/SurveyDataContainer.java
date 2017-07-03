package com.innoz.toolbox.scenarioGeneration.population.surveys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.SurveyPopulationConfigurationGroup.SurveyVehicleType;
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
	
	private Map<String, ArrayList<SurveyPerson>> classifiedPersons;
	
	private Map<String, RecursiveStatsContainer> modeSpeedStatsContainer;
	private Map<String, Hydrograph> activityTypeHydrographs;
	
	public SurveyDataContainer(final Configuration configuration){
		
		if(configuration.surveyPopulation().isUsingHouseholds()){
			this.households = new HashMap<String, SurveyHousehold>();
		}
		
		if(configuration.surveyPopulation().getVehicleType().equals(SurveyVehicleType.SURVEY)){
			this.vehicles = new HashMap<String, SurveyVehicle>();
		}
		
		this.persons = new HashMap<String, SurveyPerson>();
		this.classifiedPersons = new HashMap<String, ArrayList<SurveyPerson>>();
		this.modeSpeedStatsContainer = new HashMap<String, RecursiveStatsContainer>();
		this.activityTypeHydrographs = new HashMap<String, Hydrograph>();
		
	}
	
	public void addHousehold(SurveyHousehold household){
		
		this.households.put(household.getId(), household);
			
	}
	
	public void addPerson(SurveyPerson person){
		
		this.persons.put(person.getId(), person);
		
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
	
	public Map<String, ArrayList<SurveyPerson>> getPersonsByGroup(){
		
		return this.classifiedPersons;
		
	}
	
	public Map<String, RecursiveStatsContainer> getModeStatsContainer(){
		
		return this.modeSpeedStatsContainer;
		
	}
	
	public Map<String, Hydrograph> getActivityTypeHydrographs(){
		
		return this.activityTypeHydrographs;
		
	}
	
	public Set<String> getHouseholdsForRegionType(int rtyp){
		
		return this.households.values().stream().filter(hh -> hh.getRegionType() == rtyp).map(SurveyHousehold::getId)
				.collect(Collectors.toSet());
		
	}
	
	public void removePerson(String id){

		this.persons.remove(id);
		
	}
	
	public void removeHousehold(String id){
		
		this.households.remove(id);
		
	}
	
	public void handleNewModeEntry(String mode, double speed){
	
		if(!this.modeSpeedStatsContainer.containsKey(mode)){
			
			this.modeSpeedStatsContainer.put(mode, new RecursiveStatsContainer());
			
		}
		
		this.modeSpeedStatsContainer.get(mode).handleNewEntry(speed);
		
	}
	
}