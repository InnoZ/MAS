package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;

import com.innoz.toolbox.io.database.TracksDatabaseReader;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.tracks.Track;
import com.innoz.toolbox.scenarioGeneration.population.tracks.TrackedPerson;

public class TracksDemandGenerator extends DemandGenerationAlgorithm {

	public TracksDemandGenerator() {}

	@Override
	public void run(String ids) {
		
		TracksDatabaseReader reader = new TracksDatabaseReader();
		reader.parse();
		Map<String,TrackedPerson> persons = reader.getPersons();

		Population population = Controller.scenario().getPopulation();
		
		for(TrackedPerson person : persons.values()){
			
			Person p = population.getFactory().createPerson(Id.createPersonId(person.getId()));
			
			Plan plan = population.getFactory().createPlan();
			
			for(Track track : person.getTracks().values()){
				
				if(plan.getPlanElements().isEmpty()){
					
					Coord c = Geoinformation.getTransformation().transform(track.getStart());
					Activity act = population.getFactory().createActivityFromCoord("sighting", c);
					act.setEndTime(track.getStartTime());
					plan.addActivity(act);
					
				} else {
					
					Activity act = (Activity) plan.getPlanElements().get(plan.getPlanElements().size() - 1);
					act.setEndTime(track.getStartTime());
					
				}
				
				plan.addLeg(population.getFactory().createLeg(track.getMode()));
				
				Coord c = Geoinformation.getTransformation().transform(track.getEnd());
				Activity act = population.getFactory().createActivityFromCoord("sighting", c);
				act.setStartTime(track.getEndTime());
				plan.addActivity(act);
				
			}
			
			p.addPlan(plan);
			p.setSelectedPlan(plan);
			population.addPerson(p);
			
		}
		
		//and so on...

	}

}