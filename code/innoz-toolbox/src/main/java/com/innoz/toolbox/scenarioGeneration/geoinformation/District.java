package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.util.HashMap;
import java.util.Map;

public class District {

	private final String id;
	private Map<String, AdministrativeUnit> adminUnits;
	
	private int nHouseholds;
	private int nInhabitants;
	
	public District(String id){
		this.id = id;
		this.adminUnits = new HashMap<String, AdministrativeUnit>();
	}
	
	public String getId(){
		return this.id;
	}
	
	public Map<String,AdministrativeUnit> getAdminUnits(){
		return this.adminUnits;
	}

	public int getnHouseholds() {
		return nHouseholds;
	}

	public void setnHouseholds(int nHouseholds) {
		this.nHouseholds = nHouseholds;
	}

	public int getnInhabitants() {
		return nInhabitants;
	}

	public void setnInhabitants(int nInhabitants) {
		this.nInhabitants = nInhabitants;
	}
	
}
