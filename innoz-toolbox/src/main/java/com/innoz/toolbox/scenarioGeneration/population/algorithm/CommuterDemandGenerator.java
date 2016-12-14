package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.matrices.Entry;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.commuters.CommuterDataElement;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class CommuterDemandGenerator extends DemandGenerationAlgorithm {

	public CommuterDemandGenerator(final Scenario scenario, final Geoinformation geoinformation,
			final CoordinateTransformation transformation, final Matrix od, final Distribution distribution) {

		super(scenario, geoinformation, transformation, od, distribution);
		
	}

	@Override
	public void run(Configuration configuration, String ids) {

		createCommuterPopulation(configuration, ids);
		
	}
	
	/**
	 * 
	 * Creates a commuter population consisting of persons that perform only three activities during a simulated day:</br>
	 * <ol>
	 * <li>home
	 * <li>work
	 * <li>home
	 * </ol>
	 * <br>
	 * The home and work locations are chosen according to landuse data and a gravitation model.</br>
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param ids The admin unit ids of the commuters' home locations
	 */
	private void createCommuterPopulation(Configuration configuration, String ids){
		
		Population population = scenario.getPopulation();
		
		Set<String> idSet = CollectionUtils.stringToSet(ids);
		
		int n = 0;
		
		for(String id : idSet){
			
			for(Entry e : this.od.getFromLocations().get(id)){
				
				int d = (int) (e.getValue() * configuration.scenario().getScaleFactor());
				
				for(int i = n; i < n + d; i++){
					
					createOneCommuter(e.getFromLocation(), e.getToLocation(), population, i);
					
				}
				
				n += d;
				
			}
			
		}
		
	}

	void createOneCommuter(CommuterDataElement el, Population population, int n){

		createOneCommuter(el.getFromId(), el.getToId(), population, n);
		
	}
	
	@SuppressWarnings("deprecation")
	void createOneCommuter(String homeId, String workId, Population population, int n){
		
		if(this.geoinformation.getAdminUnit(homeId) == null || this.geoinformation.getAdminUnit(workId) == null){
			
			log.warn("Could not find geometry for home or work cell! Thus, no agent"
					+ "was created.");
			log.info("Check administrative unit ids for missing or wrong entries...");
			return;
			
		}
		
		AdministrativeUnit homeDistrict = this.geoinformation.getAdminUnit(homeId).getData();
		AdministrativeUnit home = this.chooseAdminUnit(homeDistrict, ActivityTypes.HOME);
		Coord homeLocation = this.chooseActivityCoordInAdminUnit(home, ActivityTypes.HOME);
		
		AdministrativeUnit workDistrict = this.geoinformation.getAdminUnit(workId).getData();
		AdministrativeUnit work = chooseAdminUnit(workDistrict, ActivityTypes.WORK);
		Coord workLocation = chooseActivityCoordInAdminUnit(work, ActivityTypes.WORK);
		
		Person person = population.getFactory().createPerson(Id.createPersonId(homeId + "-" + workId + "_" + n));
		Plan plan = population.getFactory().createPlan();
		
		Activity homeAct = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeLocation);
		int start = 6 * 3600 + random.nextInt(10801);
		homeAct.setEndTime(start);
		plan.addActivity(homeAct);
		
		Leg firstLeg = population.getFactory().createLeg(TransportMode.car);
		plan.addLeg(firstLeg);
		
		Activity workAct = population.getFactory().createActivityFromCoord(ActivityTypes.WORK, workLocation);
		workAct.setMaximumDuration(9 * 3600);
		plan.addActivity(workAct);
		
		Leg secondLeg = population.getFactory().createLeg(TransportMode.car);
		plan.addLeg(secondLeg);
		
		Activity secondHomeAct = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeLocation);
		secondHomeAct.setEndTime(24 * 3600);
		plan.addActivity(secondHomeAct);
		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		population.addPerson(person);
		PersonUtils.setCarAvail(person, "always");
		scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(),
				com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils.ATT_CAR_AVAIL, "always");
		
	}

}