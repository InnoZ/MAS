package com.innoz.toolbox.matsim.sharedMobility.carsharing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.carsharing.config.CarsharingConfigGroup;
import org.matsim.contrib.carsharing.config.FreeFloatingConfigGroup;
import org.matsim.contrib.carsharing.config.FreefloatingAreasReader;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.contrib.carsharing.manager.supply.CompanyContainer;
import org.matsim.contrib.carsharing.manager.supply.VehiclesContainer;
import org.matsim.contrib.carsharing.readers.CarsharingXmlReaderNew;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.filter.NetworkFilterManager;
import org.matsim.core.network.filter.NetworkLinkFilter;

public class MyCarsharingSupplyContainer implements CarsharingSupplyInterface {
	
	private Map<String, CompanyContainer> companies = new HashMap<String, CompanyContainer>();
	private Map<String, CompanyAgent> companyAgents = new HashMap<String, CompanyAgent>();
	private Map<String, CSVehicle> allVehicles = new HashMap<String, CSVehicle>();
	private Map<CSVehicle, Link> allVehicleLocations = new HashMap<CSVehicle, Link>();
	private Set<String> companyNames;
	private Scenario scenario;
	
	@Inject
	public MyCarsharingSupplyContainer(Scenario scenario) {
		this.scenario = scenario;
	}	
	
	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#getAllVehicleLocations()
	 */
	@Override
	public Map<CSVehicle, Link> getAllVehicleLocations() {
		return allVehicleLocations;
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#getAllVehicles()
	 */
	@Override
	public Map<String, CSVehicle> getAllVehicles() {
		return allVehicles;
	}
	
	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#getCompany(java.lang.String)
	 */
	@Override
	public CompanyContainer getCompany(String companyId) {
		
		return this.companies.get(companyId);
	}
	
	
	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#getVehicleWithId(java.lang.String)
	 */
	@Override
	public CSVehicle getVehicleWithId (String vehicleId) {
		
		return this.allVehicles.get(vehicleId);
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#findClosestAvailableVehicle(org.matsim.api.core.v01.network.Link, java.lang.String, java.lang.String, java.lang.String, double)
	 */
	@Override
	public CSVehicle findClosestAvailableVehicle(Link startLink, String carsharingType, String typeOfVehicle,
			String companyId, double searchDistance) {
		
		CompanyContainer companyContainer = this.companies.get(companyId);
		VehiclesContainer vehiclesContainer = companyContainer.getVehicleContainer(carsharingType);
		CSVehicle vehicle = vehiclesContainer.findClosestAvailableVehicle(startLink, typeOfVehicle, searchDistance);

		return vehicle;
		
	}		

	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#findClosestAvailableParkingSpace(org.matsim.api.core.v01.network.Link, java.lang.String, java.lang.String, double)
	 */
	@Override
	public Link findClosestAvailableParkingSpace(Link destinationLink, String carsharingType,
			String companyId, double searchDistance) {
		CompanyContainer companyContainer = this.companies.get(companyId);
		VehiclesContainer vehiclesContainer = companyContainer.getVehicleContainer(carsharingType);
		return vehiclesContainer.findClosestAvailableParkingLocation(destinationLink, searchDistance);	
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#populateSupply()
	 */
	@Override
	public void populateSupply() {
		
		Network network = filterNetwork();

		final CarsharingConfigGroup configGroup = (CarsharingConfigGroup)
				scenario.getConfig().getModules().get( CarsharingConfigGroup.GROUP_NAME );

		final FreeFloatingConfigGroup ffConfigGroup = (FreeFloatingConfigGroup)
				this.scenario.getConfig().getModules().get(FreeFloatingConfigGroup.GROUP_NAME);

		CarsharingXmlReaderNew reader = new CarsharingXmlReaderNew(network);

		String areasFile = ffConfigGroup.getAreas();
		if (areasFile != null) {
			FreefloatingAreasReader ffAreasReader = new FreefloatingAreasReader();
			ffAreasReader.parse(ConfigGroup.getInputFileURL(scenario.getConfig().getContext(), areasFile));
			reader.setFreefloatingAreas(ffAreasReader.getFreefloatingAreas());
		}

		reader.readFile(configGroup.getvehiclelocations());
		this.companies = reader.getCompanies();
		this.allVehicleLocations = reader.getAllVehicleLocations();
		this.allVehicles = reader.getAllVehicles();
		this.companyNames = reader.getCompanyNames();
		
		for(String companyName : this.companyNames) {
			CompanyAgent agent = new CompanyAgentImpl(this.companies.get(companyName), "");
			this.companyAgents.put(companyName, agent);
		}
		
	}

	/* (non-Javadoc)
	 * @see org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface#getCompanyNames()
	 */
	@Override
	public Set<String> getCompanyNames() {
		return companyNames;
	}
	
	public Map<String, CompanyAgent> getCompanyAgents() {
		return this.companyAgents;
	}
	
	private Network filterNetwork() {
		
		NetworkFilterManager mng = new NetworkFilterManager(scenario.getNetwork());
		mng.addLinkFilter(new NetworkLinkFilter() {
			
			@Override
			public boolean judgeLink(Link l) {
				
				String type = NetworkUtils.getType(l);
				
				if(type != null){
	
					boolean motorway = l.getFreespeed() > 50/3.6;
					
					if(l.getAllowedModes().contains("pt") || motorway) return false;
					
					return true;
					
				}
				
				return false;
				
			}
			
		});
		
		return mng.applyFilters();
		
	}
	
}