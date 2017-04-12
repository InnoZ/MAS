package com.innoz.toolbox.scenarioGeneration.population.algorithm;

import java.util.Map;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.TracksDatabaseReader;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.population.tracks.Track;
import com.innoz.toolbox.scenarioGeneration.population.tracks.TrackedPerson;
import com.innoz.toolbox.utils.GlobalNames;

public class TracksDemandGenerator extends DemandGenerationAlgorithm {

	public TracksDemandGenerator(Scenario scenario, CoordinateTransformation transformation, final Matrix od, Distribution distribution) {
		
		super(scenario, transformation, od, distribution);
		
	}

	@Override
	public void run(Configuration configuration, String ids) {
		
		TracksDatabaseReader reader = new TracksDatabaseReader(configuration);
		reader.parse();
		Map<String,TrackedPerson> persons = reader.getPersons();

		CoordinateTransformation transform = TransformationFactory.getCoordinateTransformation(GlobalNames.WGS84,
				configuration.misc().getCoordinateSystem());
		
		Population population = scenario.getPopulation();
		
		for(TrackedPerson person : persons.values()){
			
			Person p = population.getFactory().createPerson(Id.createPersonId(person.getId()));
			
			Plan plan = population.getFactory().createPlan();
			
			for(Track track : person.getTracks().values()){
				
				if(plan.getPlanElements().isEmpty()){
					
					Coord c = transform.transform(track.getStart());
					Activity act = population.getFactory().createActivityFromCoord("sighting", c);
					act.setEndTime(track.getStartTime());
					plan.addActivity(act);
					
				} else {
					
					Activity act = (Activity) plan.getPlanElements().get(plan.getPlanElements().size() - 1);
					act.setEndTime(track.getStartTime());
					
				}
				
				plan.addLeg(population.getFactory().createLeg(track.getMode()));
				
				Coord c = transform.transform(track.getEnd());
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