package com.innoz.toolbox.scenarioGeneration.population;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.matrices.Matrix;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.PopulationSource;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Distribution;
import com.innoz.toolbox.scenarioGeneration.geoinformation.ZensusGrid;

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
	private static final Logger log = Logger.getLogger(PopulationCreator.class);
	public static ZensusGrid grid;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private static Distribution distribution;
	private static CoordinateTransformation transformation;
	private static Matrix od;
	/////////////////////////////////////////////////////////////////////////////////////////
	
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
	public static void run(Configuration configuration, Scenario scenario) {

//		try {
//			
//			if(configuration.scenario().getActivityLocationsType().equals(ActivityLocationsType.GRID)) {
//				
//				grid = new ZensusGrid(configuration);
//				
//			}
//			
//			Map<String, ConfigurationGroup> areaSets = configuration.scenario().getAreaSets();
//			
//			CommuterDatabaseParser parser = new CommuterDatabaseParser();
//			parser.run(configuration);
//			od = parser.getOD();
//			
//			for(String key : areaSets.keySet()){
//				
//				AreaSet set = (AreaSet)areaSets.get(key);
//					
//					if(set.getPopulationSource() != null){
//					
//					// Create the coordinate transformation for all of the geometries
//					// This could also be done by just passing the auth id strings, but doing it this way suppresses
//					// warnings.
//					CoordinateReferenceSystem from = CRS.decode(GlobalNames.WGS84, true);
//					CoordinateReferenceSystem to = CRS.decode(configuration.misc().getCoordinateSystem(), true);
//					transformation = TransformationFactory.getCoordinateTransformation(
//							from.toString(), to.toString());
//					
//					distribution = new Distribution(scenario.getNetwork(), transformation);
//				
//					
//					log.info("Creating population for MATSim scenario...");
//				
//					runI(configuration, scenario, set.getPopulationSource(), set.getIds());
//					
//				}
//				
//			}
//		
//		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
//				IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
//				SecurityException | FactoryException e) {
//			
//			e.printStackTrace();
//			
//		}
//		
//		log.info("...done.");
		
	}
	
	private static void runI(Configuration configuration, Scenario scenario, PopulationSource populationType, String ids)
			throws NoSuchAuthorityCodeException, FactoryException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
			ClassNotFoundException{
		
//		log.info("Selected type of population: " + populationType.name());
//		
//		String className = null;
//		
//		// Choose the demand generation method according to what type of population was defined in the configuration
//		switch(populationType){
//							
//			case COMMUTER:	className = CommuterDemandGenerator.class.getName();
//							break;
//							
//			case SURVEY:	className = SurveyBasedDemandGenerator.class.getName();
//							break;
//							
//			case TRACKS:	className = TracksDemandGenerator.class.getName();
//							break;
//							
//			default: 		break;
//			
//		}
//		
//		if(className != null){
//			
//			((DemandGenerationAlgorithm)Class.forName(className).getConstructor(
//					Scenario.class, Geoinformation.class, CoordinateTransformation.class, Matrix.class, Distribution.class)
//					.newInstance(scenario, transformation, od, distribution))
//					.run(configuration, ids);
//			
//		}
			
	}
	
}