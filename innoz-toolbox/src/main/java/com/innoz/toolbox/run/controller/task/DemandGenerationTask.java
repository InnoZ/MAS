package com.innoz.toolbox.run.controller.task;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.matrices.Matrix;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.ConfigurationGroup;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet.PopulationSource;
import com.innoz.toolbox.io.database.CommuterDatabaseParser;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.ZensusGrid;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.CommuterDemandGenerator;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.DemandGenerationAlgorithm;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.SurveyBasedDemandGenerator;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.TracksDemandGenerator;
import com.innoz.toolbox.utils.GlobalNames;

public class DemandGenerationTask implements ControllerTask {

	private static final Logger log = Logger.getLogger(DemandGenerationTask.class);
	
	Scenario scenario;
	
	Configuration configuration;
	ZensusGrid grid;
	Distribution distribution;
	CoordinateTransformation transformation;
	Matrix od;
	
	private DemandGenerationTask(final Builder builder) {
		
		this.scenario = builder.scenario;
		this.configuration = builder.configuration;
		this.transformation = builder.transformation;
		
	}
	
	@Override
	public void run() {

		CommuterDatabaseParser parser = new CommuterDatabaseParser();
		parser.run(configuration);
		od = parser.getOD();
		
		distribution = new Distribution(scenario.getNetwork(), transformation);
		
		Map<String, ConfigurationGroup> areaSets = configuration.scenario().getAreaSets();
		
		for(String key : areaSets.keySet()){
			
			AreaSet set = (AreaSet)areaSets.get(key);
			
			PopulationSource populationSource = set.getPopulationSource();
			
			if(populationSource != null){

				String ids = set.getIds();
				
				log.info("Selected type of population: " + populationSource.name());
				
				String className = null;
				
				// Choose the demand generation method according to what type of population was defined in the configuration
				switch(populationSource){
									
					case COMMUTER:	className = CommuterDemandGenerator.class.getName();
									break;
									
					case SURVEY:	className = SurveyBasedDemandGenerator.class.getName();
									break;
									
					case TRACKS:	className = TracksDemandGenerator.class.getName();
									break;
									
					default: 		break;
					
				}
				
				if(className != null){
					
					try {
					
						((DemandGenerationAlgorithm)Class.forName(className).getConstructor(
								Scenario.class, CoordinateTransformation.class, Matrix.class, Distribution.class)
								.newInstance(this.scenario, this.transformation, this.od, this.distribution))
								.run(this.configuration, ids);
					
					} catch (InstantiationException | IllegalAccessException
					        | IllegalArgumentException
					        | InvocationTargetException | NoSuchMethodException
					        | SecurityException | ClassNotFoundException e) {
						
						e.printStackTrace();
						
					}
					
				}
				
			}
			
		}
		
		new PopulationWriter(Controller.scenario().getPopulation()).write(Controller.configuration().misc().getOutputDirectory() + "plans.xml.gz");
		
	}
	
	public static class Builder {
		
		Scenario scenario;
		
		Configuration configuration;
		ZensusGrid grid;
		CoordinateTransformation transformation;
		Distribution distribution;
		
		
		public Builder(Configuration configuration, Scenario scenario) {
			
			this.scenario = scenario;
			this.configuration = configuration;
			
		}
		
		public Builder zensusGrid(ZensusGrid grid) {
			
			this.grid = grid;
			return this;
			
		}
		
		public DemandGenerationTask build() {
			
			try {
				
				CoordinateReferenceSystem from = CRS.decode(GlobalNames.WGS84, true);
				CoordinateReferenceSystem to = CRS.decode(configuration.misc().getCoordinateSystem(), true);
				transformation = TransformationFactory.getCoordinateTransformation(
						from.toString(), to.toString());
				
				return new DemandGenerationTask(this);
				
			} catch (FactoryException e) {

				e.printStackTrace();
				
			}
			
			return null;
			
		}
		
	}
	
}