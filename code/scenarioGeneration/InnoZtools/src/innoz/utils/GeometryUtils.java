package innoz.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.PolylineFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import innoz.utils.io.AbstractCsvReader;

/**
 * 
 * Utilities in terms of geometries.
 * 
 * @author dhosse
 *
 */
public class GeometryUtils {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(GeometryUtils.class);
	private static final String SPACE = " ";
	private static final String TRIPLE_SPACE = "   ";
	private static final String TAB = "\t";
	private static final String END = "END";
	static enum geometryType{shell,hole};
	/////////////////////////////////////////////////////////////////////////////////////////
	
	// No instance!
	private GeometryUtils(){};
	
	/**
	 * 
	 * Creates a {@link com.vividsolutions.jts.geom.Geometry} from a given polygon file.
	 * 
	 * @param file Input polygon (*.poly) file
	 * @return A polygon geometry
	 */
	public static Geometry createGeometryFromPolygonFile(String file){
		
		//create separate lists for shell and hole geometries
		Set<LinkedList<Coordinate>> shellGeometriesSet = new HashSet<>();
		Set<LinkedList<Coordinate>> holeGeometriesSet = new HashSet<>();
		
		BufferedReader reader = IOUtils.getBufferedReader(file);
		
		String line = null;
		LinkedList<Coordinate> coordinateList = new LinkedList<>();
		String type = null;
		
		try {
			
			while((line = reader.readLine()) != null){

				String[] parts = line.split(TAB);
				
				if(parts[0].equals(END) || type == null){
					
					// Check what type of geometry we currently have
					if(type == null){
					
						if(parts[0].startsWith("!")){
							
							type = geometryType.hole.name();
							
						} else{
							
							type = geometryType.shell.name();
							
						}
						
					} else if(parts[0].equals(END)){

						// check if the coordinate list is part of a shell or a hole geometry
						// and add it to the corresponding collection
						if(type.equals(geometryType.shell.name())){

							shellGeometriesSet.add(coordinateList);
							
						} else {
							
							holeGeometriesSet.add(coordinateList);
							
						}
						
						coordinateList = new LinkedList<>();
						type = null;
						
					}
					
				} else{

					// This part is necessary because some polygon files are not
					// well-formatted
					if(parts.length < 2){
						
						String[] subParts = parts[0].split(TRIPLE_SPACE)[1].split(SPACE);
						coordinateList.addLast(new Coordinate(Double.parseDouble(subParts[0]),
								Double.parseDouble(subParts[1])));
						
					} else {
						
						coordinateList.addLast(new Coordinate(Double.parseDouble(parts[1]),
								Double.parseDouble(parts[2])));
						
					}
					
				}
				
			}
			
			//close the file reader
			reader.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		LinearRing[] holes = new LinearRing[holeGeometriesSet.size()];

		List<Coordinate> shellCoordinatesList = new ArrayList<>();
		
		// Create shell coordinate lists
		for(List<Coordinate> coordinatesList : shellGeometriesSet){
			
			shellCoordinatesList.addAll(coordinatesList);
			
		}
		
		int index = 0;
		// Create hole coordinate lists
		for(List<Coordinate> hole : holeGeometriesSet){
			
			Coordinate[] coordinates = new Coordinate[hole.size()];
			holes[index] = new GeometryFactory().createLinearRing(hole.toArray(coordinates));
			index++;
			
		}
		
		Coordinate[] coordinates = new Coordinate[shellCoordinatesList.size()];

		// Create a polygon from the created shell and hole coordinate lists 
		Polygon p1 = new GeometryFactory().createPolygon(new GeometryFactory().createLinearRing(shellCoordinatesList.toArray(coordinates)), holes);
		
		return new GeometryFactory().createMultiPolygon(new Polygon[]{p1});
		
	}
	
	/**
	 * 
	 * Creates an ESRI shapefile of a  MATSim network.
	 * 
	 * @param network A MAtsim network.
	 * @param shapefilePath The output directory for the shape file.
	 * @param crs The coordinate reference system used for the shape file's features.
	 */
	public static void writeNetwork2Shapefile(Network network, String shapefilePath, String crs){
		
		// Create a polyline feature collection and add the most important characteristics
		Collection<SimpleFeature> linkFeatures = new ArrayList<SimpleFeature>();
		PolylineFeatureFactory linkFactory = new PolylineFeatureFactory.Builder().
				setCrs(MGC.getCRS(crs)).
				setName("link").
				addAttribute("ID", String.class).
				addAttribute("fromID", String.class).
				addAttribute("toID", String.class).
				addAttribute("length", Double.class).
				addAttribute("type", String.class).
				addAttribute("capacity", Double.class).
				addAttribute("freespeed", Double.class).
				addAttribute("modes", String.class).
				addAttribute("nLanes", Integer.class).
				addAttribute("origId", String.class).
				create();

		// Go through all links, create features for each of them and add them to the feature 
		// collection.
		for (Link link : network.getLinks().values()) {
			Coordinate fromNodeCoordinate = new Coordinate(link.getFromNode().getCoord().getX(),
					link.getFromNode().getCoord().getY());
			Coordinate toNodeCoordinate = new Coordinate(link.getToNode().getCoord().getX(),
					link.getToNode().getCoord().getY());
			Coordinate linkCoordinate = new Coordinate(link.getCoord().getX(), link.getCoord().getY());
			SimpleFeature ft = linkFactory.createPolyline(new Coordinate [] {fromNodeCoordinate,
					linkCoordinate, toNodeCoordinate},
					new Object [] {link.getId().toString(), link.getFromNode().getId().toString(),
					link.getToNode().getId().toString(), link.getLength(), ((LinkImpl)link).getType(),
					link.getCapacity(), link.getFreespeed(), CollectionUtils.setToString(
					link.getAllowedModes()), link.getNumberOfLanes(), ((LinkImpl)link).getOrigId()},
					null);
			linkFeatures.add(ft);
		}
		
		// If the feature collection contains at least one element, write it to the specified
		// location, else log an error.
		if(linkFeatures.size() > 0){
			
			ShapeFileWriter.writeGeometries(linkFeatures, shapefilePath + "/links.shp");
			
		} else {
			
			log.error("Link feature collection is empty and thus there is no file to write...");
			log.info("Continuing anyways...");
			
		}
		
		// Create a point feature collection
		Collection<SimpleFeature> nodeFeatures = new ArrayList<>();
		PointFeatureFactory pointFactory = new PointFeatureFactory.Builder().
				setCrs(MGC.getCRS(crs)).
				addAttribute("id", String.class).
				create();

		// Go through all nodes, create features for each of them and add them to the feature 
		// collection.
		for(Node node : network.getNodes().values()){
			
			SimpleFeature feature = pointFactory.createPoint(MGC.coord2Coordinate(node.getCoord()),
					new Object[]{node.getId().toString()}, null);
			nodeFeatures.add(feature);
			
		}
		
		// If the feature collection contains at least one element, write it to the specified
		// location, else log an error.
		if(nodeFeatures.size() > 0){
			
			ShapeFileWriter.writeGeometries(nodeFeatures, shapefilePath + "/nodes.shp");
			
		} else {
			
			log.error("Point feature collection is empty and thus there is no file to write...");
			log.info("Continuing anyways...");
			
		}
		
	}
	
	/**
	 * 
	 * Creates an ESRI shapefile of MATSim facilities.
	 * 
	 * @param facilities MATSim facilities
	 * @param shapefile The output directory for the shape file.
	 * @param crs The coordinate reference system used for the shape file's features.
	 */
	public static void writeFacilities2Shapefile(Collection<? extends ActivityFacility> facilities,
			String shapefile, String crs){
		
		// Create a point feature collection and add some characteristics
		Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
		PointFeatureFactory factory = new PointFeatureFactory.Builder().
				setCrs(MGC.getCRS(crs)).
				setName("facilities").
				addAttribute("ID", String.class).
				addAttribute("actType", String.class).
				create();
				
		// Go through all facilities, create features for each of them and add them to the feature 
		// collection.
		for(ActivityFacility facility : facilities){
			
			Set<String> options = new HashSet<>();
			options.addAll(facility.getActivityOptions().keySet());
			SimpleFeature feature = factory.createPoint(MGC.coord2Coordinate(facility.getCoord()),
					new Object[]{facility.getId().toString(), CollectionUtils.setToString(options)},
					null);
			features.add(feature);
			
		}
		
		// If the feature collection contains at least one element, write it to the specified
		// location, else log an error.
		if(features.size() > 0){
			
			ShapeFileWriter.writeGeometries(features, shapefile);
			
		} else {
			
			log.error("Point feature collection is empty and thus there is no file to write...");
			log.info("Continuing anyways...");
			
		}
		
	}
	
	/**
	 * 
	 * Creates an ESRI shapefile of MATSim stop facilities.
	 * 
	 * @param facilities MATSim stop facilities.
	 * @param shapefile The output directory for the shape file.
	 * @param crs The coordinate reference system used for the shape file's features.
	 */
	public static void writeStopFacilities2Shapefile(Collection<? extends TransitStopFacility> facilities, String shapefile, String crs){
		
		// Create a point feature collection and add some characteristics
		Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
		PointFeatureFactory factory = new PointFeatureFactory.Builder().
				setCrs(MGC.getCRS(crs)).
				setName("stops").
				addAttribute("ID", String.class).
				addAttribute("name", String.class).
				addAttribute("linkId", String.class).
				create();
		
		// Go through all stop facilities, create features for each of them and add them to the feature 
		// collection.
		for(TransitStopFacility facility : facilities){
			
			SimpleFeature feature = factory.createPoint(MGC.coord2Coordinate(facility.getCoord()),
					new Object[]{facility.getId().toString(), facility.getName(),
					facility.getLinkId().toString()},
					null);
			features.add(feature);
			
		}
		
		// If the feature collection contains at least one element, write it to the specified
		// location, else log an error.
		if(features.size() > 0){
			
			ShapeFileWriter.writeGeometries(features, shapefile + "/stops.shp");
			
		} else {
			
			log.error("Point feature collection is empty and thus there is no file to write...");
			log.info("Continuing anyways...");
			
		}
		
	}
	
	/**
	 * 
	 * Shoots a random {@link Coord} that lies inside of the given geometry.
	 * 
	 * @param geometry The target geometry.
	 * @param random The random object that mutates the coordinate.
	 * @return A MATSim coord.
	 */
	public static Coord shoot(Geometry geometry, Random random){
		
		Point point = null;
		double x, y;
		
		do{
			
			// Shoot random x and y components and create a point of them
			x = geometry.getEnvelopeInternal().getMinX() + random.nextDouble() *
					(geometry.getEnvelopeInternal().getMaxX() -
							geometry.getEnvelopeInternal().getMinX());
			
	  	    y = geometry.getEnvelopeInternal().getMinY() + random.nextDouble() *
	  	    		(geometry.getEnvelopeInternal().getMaxY() -
	  	    				geometry.getEnvelopeInternal().getMinY());
	  	    
	  	    point = MGC.xy2Point(x, y);
			
	  	    //if the point lies within the geometry, break out of the loop
		} while(!geometry.contains(point));
		
		return MGC.point2Coord(point);
		
	}
	
	/**
	 *
	 * Shoots a random {@link Coord} that lies inside of the given geometry. Also, the last known
	 * geometry of e.g. a travel chain must be given as well as the maximum distance between
	 * this last and the next coordinate. 
	 * 
	 * @param geometry The target geometry.
	 * @param fromCoord The last known coord of a travel chain.
	 * @param distance The distance between {@code fromCoord} and the next coordinate.
	 * @param random The random object that mutates the coordinate.
	 * @return A Matsim coord.
	 */
	public static Coord shoot(Geometry geometry, Coord fromCoord, double distance, Random random){
		
		Coord coord = null;
		
		do{
			
			coord = GeometryUtils.shoot(geometry, random);
			
			//if the distance between the coords is equal or smaller than the given distance,
			//break out of the loop.
		}while(CoordUtils.calcDistance(coord, fromCoord) > distance);
		
		return coord;
		
	}
	
	/**
	 * 
	 * Merges a given administrative boundaries shapefile and a region type csv file into a result
	 * csv file. The classification is based on BBSR.</br>
	 * 
	 * I don't know if we need this method... Keep it for now /dhosse 05/16
	 * 
	 * @param inputShapefile The shapefile to parse
	 * @param inputRegionTypesFile The list of the region types, sorted by their Gemeindekennzahl
	 * @param outputFile The resulting csv
	 */
	public static void mergeGeometriesAndRegionTypesIntoCsv(String inputShapefile, String inputRegionTypesFile,
			String outputFile){

		// Create a map for the region geometries (sorted by their GKZ)
		Map<String, String> gkz2geometryString = new HashMap<String, String>();

		// Go through all features and add their geometries to the map
		Collection<SimpleFeature> features = new ShapeFileReader().readFileAndInitialize(inputShapefile);
		for(SimpleFeature feature : features){
			
			String kennzahl = (String)feature.getAttribute("KENNZAHL");
			if(kennzahl.startsWith("0")){
				kennzahl = kennzahl.substring(1);
			}
			String geometryString = (String)feature.getDefaultGeometry().toString();
			
			int typ = (int) feature.getAttribute("KREISTYP");
			
			if(typ > 0){
				gkz2geometryString.put(kennzahl, geometryString);
			}
			
		}

		// Read the csv file containing the information about the region types
		// and store the information inside a map
		int idxGKZ = 0;
		int idxRtyp = 2;
		int idxRtypD = 4;
		Map<String, Tuple<String, String>> gkz2RtypAndRtypD = new HashMap<>();
		
		AbstractCsvReader reader = new AbstractCsvReader(";",false) {
			
			@Override
			public void handleRow(String[] line) {
				
				String gkz = line[idxGKZ].substring(0, line[idxGKZ].length()-3);
				String rTyp = line[idxRtyp];
				String rTypD = line[idxRtypD];
				
				gkz2RtypAndRtypD.put(gkz, new Tuple<String, String>(rTyp, rTypD));
				
			}
			
		};
		
		reader.read(inputRegionTypesFile);
		
		// In the end, write the output csv file
		BufferedWriter writer = IOUtils.getBufferedWriter(outputFile);
		
		try {
			
			for(String key : gkz2geometryString.keySet()){
				
				Tuple<String, String> tuple = gkz2RtypAndRtypD.get(key);
			
				if(tuple != null){

					writer.write(key + ";" + gkz2geometryString.get(key) + ";" + key.substring(0, 1) + ";" +
							tuple.getFirst() + ";" + tuple.getSecond());
					writer.newLine();
					
				} else {
					
					log.warn("Could not find additional information for gkz " + key);
					log.warn("Skipping this line!");
					
				}
				
			}
			
			writer.flush();
			writer.close();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static void writeActivityLocationsToShapefile(final Population population, String shapefile, String crs){
		
		// Create a point feature collection and add some characteristics
		Collection<SimpleFeature> features = new ArrayList<SimpleFeature>();
		PointFeatureFactory factory = new PointFeatureFactory.Builder().
				setCrs(MGC.getCRS(crs)).
				setName("acts").
				addAttribute("personId", String.class).
				addAttribute("actType", String.class).
				create();
		
		// Go through all stop facilities, create features for each of them and add them to the feature 
		// collection.
		for(Person person : population.getPersons().values()){
			
			for(PlanElement pe : person.getSelectedPlan().getPlanElements()){
				
				if(pe instanceof Activity){
					
					SimpleFeature feature = factory.createPoint(MGC.coord2Coordinate(((Activity)pe).getCoord()),
							new Object[]{person.getId().toString(), ((Activity)pe).getType()},
							null);
					features.add(feature);
					
				}
				
			}
			
		}
		
		// If the feature collection contains at least one element, write it to the specified
		// location, else log an error.
		if(features.size() > 0){
			
			ShapeFileWriter.writeGeometries(features, shapefile + "acts.shp");
			
		} else {
			
			log.error("Point feature collection is empty and thus there is no file to write...");
			log.info("Continuing anyways...");
			
		}
		
	}
	
}
