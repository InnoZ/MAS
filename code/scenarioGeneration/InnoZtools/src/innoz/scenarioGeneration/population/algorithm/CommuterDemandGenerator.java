package innoz.scenarioGeneration.population.algorithm;

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
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;

import innoz.config.Configuration;
import innoz.io.database.CommuterDatabaseParser;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.commuters.CommuterDataElement;
import innoz.scenarioGeneration.utils.ActivityTypes;

public class CommuterDemandGenerator extends DemandGenerationAlgorithm {

	public CommuterDemandGenerator(Geoinformation geoinformation,
			final CoordinateTransformation transformation) {

		super(geoinformation, transformation);
		
	}

	@Override
	public void run(Scenario scenario, Configuration configuration, String ids) {

		createCommuterPopulation(configuration, scenario, ids);
		
	}
	
	/**
	 * 
	 * Creates a commuter population consisting of persons that perform only three activities during a simulated day:</br>
	 * <ol>
	 * <li>home
	 * <li>work
	 * <li>home
	 * </ol>
	 * 
	 * The home and work locations are chosen according to landuse data and a gravitation model.</br>
	 * 
	 * At the moment, this method is a stub and does nothing. dhosse 05/16
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario A Matsim scenario.
	 */
	private void createCommuterPopulation(Configuration configuration, Scenario scenario, String ids){
		
		CommuterDatabaseParser parser = new CommuterDatabaseParser();
		parser.run(configuration);

		Population population = scenario.getPopulation();
		
		Set<String> idSet = CollectionUtils.stringToSet(ids);
		
		int n = 0;
		
		Set<String> toIds = CollectionUtils.stringToSet(configuration.getSurveyAreaIds());
		
		for(CommuterDataElement entry : parser.getCommuterRelations()){
			
			if(idSet.contains(entry.getFromId()) && toIds.contains(entry.getToId())){

				for(int i = n; i < n + (entry.getNumberOfCommuters() * configuration.getScaleFactor()); i++){
					
					createOneCommuter(entry, population, i);
					
				}
				
				n += entry.getNumberOfCommuters() * configuration.getScaleFactor();
				
			}
			
		}
		
	}
	
	private void createOneCommuter(CommuterDataElement el, Population population, int n){

		String homeId = el.getFromId();
		String workId = el.getToId();

		if(!this.geoinformation.getAdminUnits().containsKey(homeId) || !this.geoinformation.getAdminUnits().containsKey(workId)){
			
			return;
			
		}
		
		District homeDistrict = this.geoinformation.getAdminUnits().get(homeId);
		AdministrativeUnit home = this.chooseAdminUnitInsideDistrict(homeDistrict, ActivityTypes.HOME);
		Coord homeLocation = this.chooseActivityCoordInAdminUnit(home, ActivityTypes.HOME);
		
		District workDistrict = this.geoinformation.getAdminUnits().get(workId);
		AdministrativeUnit work = chooseAdminUnitInsideDistrict(workDistrict, ActivityTypes.WORK);
		Coord workLocation = chooseActivityCoordInAdminUnit(work, ActivityTypes.WORK);
		
		Person person = population.getFactory().createPerson(Id.createPersonId(homeId + "-" + workId + "_" + n));
		Plan plan = population.getFactory().createPlan();
		
		Activity homeAct = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeLocation);
		homeAct.setEndTime(7 * 3600);
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
		
	}

}
