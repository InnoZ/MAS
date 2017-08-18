package com.innoz.toolbox.run.controller.task;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.matsim.core.population.io.PopulationWriter;

import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.PopulationSource;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.CommuterDemandGenerator;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.DemandGenerationAlgorithm;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.SurveyBasedDemandGenerator;
import com.innoz.toolbox.utils.data.Tree.Node;

public class DemandGenerationTask implements ControllerTask {

	private static final Logger log = Logger.getLogger(DemandGenerationTask.class);
	
	private DemandGenerationTask(final Builder builder) {}
	
	@Override
	public void run() {

		PopulationSource populationSource = Controller.configuration().scenario().getPopulationSource();
		
		if(populationSource != null){

			List<String> ids = Geoinformation.getInstance().getAdminUnits().stream().map(Node::getData).map(AdministrativeUnit::getId).filter(id -> id.length() == 5).collect(Collectors.toList());
//			String ids = Controller.configuration().scenario().getSurveyAreaId();
			
			for(String id : ids) {
				
				log.info("Creating demand for admin unit " + id);
				
				String className = null;
				
				if(id.equals(Controller.configuration().scenario().getSurveyAreaId())) {
					className = SurveyBasedDemandGenerator.class.getName();
				} else {
					className = CommuterDemandGenerator.class.getName();
				}
				
				if(className != null){
					
					try {
					
						((DemandGenerationAlgorithm)Class.forName(className).getConstructor().newInstance()).run(id);
					
					} catch (InstantiationException | IllegalAccessException
					        | IllegalArgumentException
					        | InvocationTargetException | NoSuchMethodException
					        | SecurityException | ClassNotFoundException e) {
						
						e.printStackTrace();
						
					}
					
				}
				
			}
			
//			log.info("Selected type of population: " + populationSource.name());
//			
//			String className = null;
//			
//			// Choose the demand generation method according to what type of population was defined in the configuration
//			switch(populationSource){
//								
//				case COMMUTER:	className = CommuterDemandGenerator.class.getName();
//								break;
//								
//				case SURVEY:	className = SurveyBasedDemandGenerator.class.getName();
//								break;
//								
//				case TRACKS:	className = TracksDemandGenerator.class.getName();
//								break;
//								
//				default: 		break;
//				
//			}
//			
//			if(className != null){
//				
//				try {
//				
//					((DemandGenerationAlgorithm)Class.forName(className).getConstructor().newInstance()).run(ids);
//				
//				} catch (InstantiationException | IllegalAccessException
//				        | IllegalArgumentException
//				        | InvocationTargetException | NoSuchMethodException
//				        | SecurityException | ClassNotFoundException e) {
//					
//					e.printStackTrace();
//					
//				}
//				
//			}
			
		}
			
		new PopulationWriter(Controller.scenario().getPopulation()).write(Controller.configuration().misc().getOutputDirectory() + "plans.xml.gz");
		
	}
	
	public static class Builder {
		
		public Builder() {}
		
		public DemandGenerationTask build() {
			
			return new DemandGenerationTask(this);
			
		}
		
	}
	
}