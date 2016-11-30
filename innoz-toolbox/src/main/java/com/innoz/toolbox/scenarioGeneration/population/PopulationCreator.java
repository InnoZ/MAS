package com.innoz.toolbox.scenarioGeneration.population;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.Configuration.PopulationSource;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.CommuterDemandGenerator;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.DemandGenerationAlgorithm;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.DummyDemandGenerator;
import com.innoz.toolbox.scenarioGeneration.population.algorithm.SurveyBasedDemandGenerator;
import com.innoz.toolbox.utils.GlobalNames;

/**
 * 
 * This class generates an initial demand (MATSim population) for a given scenario. </br>
 * 
 * dhosse, 05/16:
 * At the moment, only demand generation from MiD survey data is supported. Possible additional /
 * alternative data sources would be:
 * <ul>
 * <li> other surveys (e.g. SrV, MoP)
 * <li> innoz tracks
 * </ul>
 * 
 * @author dhosse
 *
 */
public class PopulationCreator {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private final Geoinformation geoinformation;
	private static final Logger log = Logger.getLogger(PopulationCreator.class);
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private Distribution distribution;
	private CoordinateTransformation transformation;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param geoinformation The geoinformation container.
	 */
	public PopulationCreator(final Geoinformation geoinformation){
		
		this.geoinformation = geoinformation;
		
	};
	
	/**
	 * 
	 * This is the "main method" of the demand generation process.
	 * According to what type of population was defined in the given configuration (one of: {@code dummy}, {@code commuter},
	 * {@code complete}), an initial demand is created and added to the MATSim scenario.
	 * 
	 * @param configuration The scenario generation configuration file.
	 * @param scenario The MATsim scenario eventually containing all of the information about network, demand etc.
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 */
	public void run(Configuration configuration, Scenario scenario) {

		
		try {
			
			if(!configuration.getPopulationSource().equals(PopulationSource.none) ||
					!configuration.getVicinityPopulationSource().equals(PopulationSource.none)){
				
				// Create the coordinate transformation for all of the geometries
				// This could also be done by just passing the auth id strings, but doing it this way suppresses
				// warnings.
				CoordinateReferenceSystem from = CRS.decode(GlobalNames.WGS84, true);
				CoordinateReferenceSystem to = CRS.decode(configuration.getCrs(), true);
				transformation = TransformationFactory.getCoordinateTransformation(
						from.toString(), to.toString());
				
				this.distribution = new Distribution(scenario.getNetwork(), this.geoinformation, this.transformation);
			
				
				log.info("Creating population for MATSim scenario...");
			
				runI(configuration, scenario, configuration.getPopulationSource(), configuration.getSurveyAreaIds());
				runI(configuration, scenario, configuration.getVicinityPopulationSource(), configuration.getVicinityIds());
			
			}
		
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
				IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
				SecurityException | FactoryException e) {
			
			e.printStackTrace();
			
		}
		
		log.info("...done.");
		
	}
	
	private void runI(Configuration configuration, Scenario scenario, PopulationSource populationType, String ids)
			throws NoSuchAuthorityCodeException, FactoryException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
			ClassNotFoundException{
		
		if(ids != null && !populationType.equals(PopulationSource.none)){
			
			log.info("Selected type of population: " + populationType.name());

			String className = null;
			
			// Choose the demand generation method according to what type of population was defined in the configuration
			switch(populationType){
			
				case dummy: 	className = DummyDemandGenerator.class.getName();
								break;
								
				case commuter:	className = CommuterDemandGenerator.class.getName();
								break;
								
				case survey:	className = SurveyBasedDemandGenerator.class.getName();
								break;
								
				default: 		break;
			
			}
			
			if(className != null){
				
				((DemandGenerationAlgorithm)Class.forName(className).getConstructor(
						Scenario.class, Geoinformation.class, CoordinateTransformation.class, Distribution.class).newInstance(
						scenario, this.geoinformation, this.transformation, this.distribution)).run(configuration, ids);
				
			}
			
		}
		
	}
	
}