package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;

import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.utils.GeometryUtils;
import com.innoz.toolbox.utils.data.Tree.Node;

public class DummyDemandGenerator extends DemandGenerationAlgorithm {

	public DummyDemandGenerator() {}

	@Override
	public void run(String ids) {

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

			Node<AdministrativeUnit> d = Geoinformation.getInstance().getAdminUnit(s);
			
			for(Node<AdministrativeUnit> fromEntry : d.getChildren()){
				
				for(Node<AdministrativeUnit> toEntry : d.getChildren()){

					//TODO maybe make the max number configurable...
					for(int i = 0; i < 1000; i++){

						// Create a new person and an empty plan
						Person person = Controller.scenario().getPopulation().getFactory().createPerson(Id.createPersonId(
								fromEntry.getData().getId() + "_" + toEntry.getData().getId() + "-" + i));
						Plan plan = Controller.scenario().getPopulation().getFactory().createPlan();
						
						// Shoot the activity coords (home activity located inside of the FROM admin unit,
						// work activity inside of the TO admin unit)
						Coord homeCoord = Geoinformation.getTransformation().transform(GeometryUtils.shoot(fromEntry.getData()
								.getGeometry(),random));
						Coord workCoord = Geoinformation.getTransformation().transform(GeometryUtils.shoot(toEntry.getData()
								.getGeometry(),random));
						
						// Create activities and legs and add them to the plan
						Activity home = Controller.scenario().getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
								homeCoord);
						home.setEndTime(7 * 3600);
						plan.addActivity(home);
						
						Leg leg = Controller.scenario().getPopulation().getFactory().createLeg(TransportMode.car);
						plan.addLeg(leg);
						
						Activity work = Controller.scenario().getPopulation().getFactory().createActivityFromCoord(ActivityTypes.WORK,
								workCoord);
						work.setEndTime(18 * 3600);
						plan.addActivity(work);
						
						Leg leg2 = Controller.scenario().getPopulation().getFactory().createLeg(TransportMode.car);
						plan.addLeg(leg2);
						
						Activity home2 = Controller.scenario().getPopulation().getFactory().createActivityFromCoord(ActivityTypes.HOME,
								homeCoord);
						plan.addActivity(home2);
						
						// Add the plan to the current person and make it the selected one
						person.addPlan(plan);
						person.setSelectedPlan(plan);

						// Add the current person to the population
						Controller.scenario().getPopulation().addPerson(person);
						
					}
					
				}
				
			}
			
		}
		
	}

}
