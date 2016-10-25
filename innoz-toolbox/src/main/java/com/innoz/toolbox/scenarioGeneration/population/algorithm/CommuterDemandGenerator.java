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

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.CommuterDatabaseParser;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.commuters.CommuterDataElement;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class CommuterDemandGenerator extends DemandGenerationAlgorithm {

	public CommuterDemandGenerator(final Scenario scenario, final Geoinformation geoinformation,
			final CoordinateTransformation transformation, final Distribution distribution) {

		super(scenario, geoinformation, transformation, distribution);
		
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
		
		CommuterDatabaseParser parser = new CommuterDatabaseParser();
		parser.run(configuration);

		Population population = scenario.getPopulation();
		
		Set<String> idSet = CollectionUtils.stringToSet(ids);
		
		int n = 0;
		
		for(CommuterDataElement entry : parser.getCommuterRelations()){
			
			if(idSet.contains(entry.getFromId())){
				
				int d = (int) (entry.getNumberOfCommuters() * configuration.scenario().getScaleFactor());

				for(int i = n; i < n + d; i++){
					
					createOneCommuter(entry, population, i);
					
				}
				
				n += d;
				
			}
			
		}
		
	}

	@SuppressWarnings("deprecation")
	private void createOneCommuter(CommuterDataElement el, Population population, int n){

		String homeId = el.getFromId();
		String workId = el.getToId();

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
		workAct.setMaximumDuration(8 * 3600);
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