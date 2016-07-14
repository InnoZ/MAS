package innoz.scenarioGeneration.population.algorithm;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.AdministrativeUnit;
import innoz.scenarioGeneration.geoinformation.District;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.utils.GeometryUtils;

import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.utils.geometry.CoordinateTransformation;

public class DummyDemandGenerator extends DemandGenerationAlgorithm {

	public DummyDemandGenerator(final Scenario scenario, Geoinformation geoinformation,
			final CoordinateTransformation transformation) {

		super(scenario, geoinformation, transformation);
		
	}

	@Override
	public void run(final Configuration configuration, String ids) {

		this.createDummyPopulation(ids);
		
	}
	
	/**
	 * 
	 * Creates a "dummy" population, meaning:
	 * <ul>
	 * <li> No input data of any kind (e.g. surveys, tracks) is used to generate travel chains
	 * <li> No person attributes are created (e.g. age, employment, car ownership, ...)
	 * <li> The transport mode of all the persons is chosen randomly
	 * <li> The activity locations are chosen randomly within the survey area
	 * </ul>
	 * 
	 * The resulting initial demand contains of persons whose plans only consist of three activities (home-work-home)
	 * and connecting legs.
	 * 
	 * @param scenario The MATsim scenario eventually containing all of the information about network, demand etc.
	 */
	private void createDummyPopulation(String ids){
		
		log.info("Creating a dummy population without any preferences...");
		
		// From each administrative unit to each administrative unit, create a certain amount of commuters
		for(String s : ids.split(",")){

			District d = this.geoinformation.getAdminUnits().get(s);
			
			for(Entry<String,AdministrativeUnit> fromEntry : d.getAdminUnits().entrySet()){
				
				for(Entry<String,AdministrativeUnit> toEntry : d.getAdminUnits().entrySet()){

					//TODO maybe make the max number configurable...
					for(int i = 0; i < 1000; i++){

						// Create a new person and an empty plan
						Person person = scenario.getPopulation().getFactory().createPerson(Id.createPersonId(
								fromEntry.getKey() + "_" + toEntry.getKey() + "-" + i));
						Plan plan = scenario.getPopulation().getFactory().createPlan();
						
						// Shoot the activity coords (home activity located inside of the FROM admin unit,
						// work activity inside of the TO admin unit)
						Coord homeCoord = transformation.transform(GeometryUtils.shoot(fromEntry.getValue()
								.getGeometry(),random));
						Coord workCoord = transformation.transform(GeometryUtils.shoot(toEntry.getValue()
								.getGeometry(),random));
						
						// Create activities and legs and add them to the plan
						Activity home = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
								homeCoord);
						home.setEndTime(7 * 3600);
						plan.addActivity(home);
						
						Leg leg = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
						plan.addLeg(leg);
						
						Activity work = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.WORK,
								workCoord);
						work.setEndTime(18 * 3600);
						plan.addActivity(work);
						
						Leg leg2 = scenario.getPopulation().getFactory().createLeg(TransportMode.car);
						plan.addLeg(leg2);
						
						Activity home2 = scenario.getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
								homeCoord);
						plan.addActivity(home2);
						
						// Add the plan to the current person and make it the selected one
						person.addPlan(plan);
						person.setSelectedPlan(plan);

						// Add the current person to the population
						scenario.getPopulation().addPerson(person);
						
					}
					
				}
				
			}
			
		}
		
	}

}
