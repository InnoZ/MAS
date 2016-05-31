package innoz.scenarioGeneration.network;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

import innoz.config.Configuration;
import innoz.io.database.DatabaseReader;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.utils.matsim.NetworkSimplifier;

/**
 * 
 * This class provides functionalities to create and modify a {@link org.matsim.api.core.v01.network.Network}
 * by using OpenStreetMap data stored in a postgreSQL database.
 * 
 * @author dhosse
 *
 */
public class NetworkCreatorFromPsql {
	
	private static final Logger log = Logger.getLogger(NetworkCreatorFromPsql.class);

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private final Network network;
	private final CoordinateTransformation transformation;
	private final Configuration configuration;
	private final Geoinformation geoinformation;
	
	public static final String MOTORWAY = "motorway";
	public static final String MOTORWAY_LINK = "motorway_link";
	public static final String TRUNK = "trunk";
	public static final String TRUNK_LINK = "trunk_link";
	public static final String PRIMARY = "primary";
	public static final String PRIMARY_LINK = "primary_link";
	public static final String SECONDARY = "secondary";
	public static final String TERTIARY = "tertiary";
	public static final String MINOR = "minor";
	public static final String UNCLASSIFIED = "unclassified";
	public static final String RESIDENTIAL = "residential";
	public static final String LIVING_STREET = "living_street";
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////	
	private static int nodeCounter = 0;
	private static int linkCounter = 0;
	
	private boolean scaleMaxSpeed = false;
	private boolean cleanNetwork = false;
	private boolean simplifyNetworK = false;
	
	private Set<String> unknownTags = new HashSet<>();
	
	private Map<String, HighwayDefaults> highwayDefaults = new HashMap<String, HighwayDefaults>();

	private Map<Coord,Node> coords2Nodes = new HashMap<Coord, Node>();
	
	//TODO what can you modify?
	static enum modification{};
	
	/*TODO make network generation depend on
	 * 1)sample size
	 * 2)survey area or surrounding
	 */
	private int levelOfDetail = 6;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Constructor.
	 * 
	 * @param network An empty MATSim network.
	 * @param configuration The scenario generation configuration.
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 */
	public NetworkCreatorFromPsql(final Network network, final Geoinformation geoinformation, Configuration configuration)
			throws NoSuchAuthorityCodeException, FactoryException{
		
		this.network = network;
		this.geoinformation = geoinformation;
		
		CoordinateReferenceSystem from = CRS.decode("EPSG:4326", true);
		CoordinateReferenceSystem to = CRS.decode(configuration.getCrs(), true);
		this.transformation = TransformationFactory.getCoordinateTransformation(
				from.toString(), to.toString());
		this.configuration = configuration;
		
	};
	
	/**
	 * 
	 * Setter for the network simplification variable.
	 * 
	 * @param b true / false
	 */
	public void setSimplifyNetwork(boolean b){
		this.simplifyNetworK = b;
	}
	
	/**
	 * 
	 * Setter for the network cleaning variable.
	 * 
	 * @param b true / false
	 */
	public void setCleanNetwork(boolean b){
		this.cleanNetwork = b;
	}

	/**
	 * 
	 * Setter for the scale maximum speed variable.
	 * 
	 * @param b true / false
	 */
	public void setScaleMaxSpeed(boolean b){
		this.scaleMaxSpeed = b;
	}
	
	/**
	 * 
	 * The main method of the {@code NetworkCreatorFromPsql}.
	 * Via database connection OpenStreetMap road data is retrieved and converted into a MATSim network.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void create(DatabaseReader dbReader) throws InstantiationException, IllegalAccessException, ClassNotFoundException,
		SQLException, ParseException{
		
		// If highway defaults have been specified, use them. Else fall back to default values.
		if(this.highwayDefaults.size() < 1){

			this.setHighwayDefaultsAccordingToLevelOfDetail();
			
		}
		
		// Convert the way entries into a MATSim network
		processWayEntries(dbReader.readOsmRoads(this.configuration));
		
		// Simplify the network if needed
		if(this.simplifyNetworK) {
			
			new NetworkSimplifier().run(network);
			
		}
		
		// Clean the network to avoid dead ends during simulation (clustered network)
		if(this.cleanNetwork){
		
			new NetworkCleaner().run(network);
			
		}
			
	}

	/**
	 * 
	 * The main method of the {@code NetworkCreatorFromPsql}.
	 * Via database connection OpenStreetMap road data is retrieved and converted into a MATSim network.
	 * 
	 * In addition, you can set the level of detail for the network (default value is "6"). The lower hierarchies contain the higher
	 * hierarchies' highway types)
	 * <ul>
	 * <li>1: motorways
	 * <li>2: trunk roads
	 * <li>3: primary roads
	 * <li>4: secondary roads
	 * <li>5: tertiary roads
	 * <li>6: residential roads (minor, unclassified, residential, living street)
	 * </ul>
	 * 
	 * @param dbReader
	 * @param levelOfDetail
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 */
	public void create(DatabaseReader dbReader, int levelOfDetail) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, SQLException, ParseException{
		
		this.levelOfDetail = levelOfDetail;
		this.create(dbReader);
		
	}
	
	/**
	 * 
	 * Setter for highway defaults.
	 * 
	 * @param highwayType The road type.
	 * @param lanesPerDirection Number of lanes per direction.
	 * @param freespeed Allowed free speed on this road type.
	 * @param freespeedFactor Factor for scaling of free speed.
	 * @param laneCapacity_vehPerHour The capacity of one lane on this road type per hour.
	 * @param oneway Road type is only accessible in one direction or not.
	 */
	public void setHighwayDefaults(final int hierarchyLevel, final String highwayType, final double lanesPerDirection, final double freespeed,
			final double freespeedFactor, final double laneCapacity_vehPerHour, final boolean oneway){
		
		this.highwayDefaults.put(highwayType, new HighwayDefaults(hierarchyLevel, freespeed, freespeedFactor, lanesPerDirection,
				laneCapacity_vehPerHour, oneway));
		
	}
	
	/**
	 * 
	 * Setter for highway defaults. The one way attribute is set to false in this method.
	 * 
	 * @param highwayType The road type.
	 * @param lanesPerDirection Number of lanes per direction.
	 * @param freespeed Allowed free speed on this road type.
	 * @param freespeedFactor Factor for scaling of free speed.
	 * @param laneCapacity_vehPerHour The capacity of one lane on this road type per hour.
	 */
	public void setHighwayDefaults(final int hierarchyLevel, final String highwayType, final double lanesPerDirection, final double freespeed,
			final double freespeedFactor, final double laneCapacity_vehPerHour){
		
		this.setHighwayDefaults(hierarchyLevel, highwayType, lanesPerDirection, freespeed, freespeedFactor, laneCapacity_vehPerHour, false);
		
	}
	
	private void setHighwayDefaultsAccordingToLevelOfDetail(){

		this.setHighwayDefaults(1, MOTORWAY, 2.0, 100/3.6, 1.2, 2000.0, true);
		this.setHighwayDefaults(1, MOTORWAY_LINK, 1,  60.0/3.6, 1.2, 1500, true);
		
		if(this.levelOfDetail > 1){
			
			this.setHighwayDefaults(2, TRUNK, 1,  80.0/3.6, 0.5, 1000);
			this.setHighwayDefaults(2, TRUNK_LINK, 1,  60.0/3.6, 0.5, 1500);
			
			if(this.levelOfDetail > 2){
				
				this.setHighwayDefaults(3, PRIMARY, 1,  50.0/3.6, 0.5, 1000);
				this.setHighwayDefaults(3, PRIMARY_LINK, 1,  50.0/3.6, 0.5, 1000);
				
				if(this.levelOfDetail > 3){
					
					this.setHighwayDefaults(4, SECONDARY, 1,  50.0/3.6, 0.5, 1000);
					
					if(this.levelOfDetail > 4){
						
						this.setHighwayDefaults(5, TERTIARY, 1,  30.0/3.6, 0.8,  600);
						
						if(this.levelOfDetail > 5){
							
							this.setHighwayDefaults(6, MINOR, 1,  30.0/3.6, 0.8,  600);
							this.setHighwayDefaults(6, UNCLASSIFIED, 1,  30.0/3.6, 0.8,  600);
							this.setHighwayDefaults(6, RESIDENTIAL, 1,  30.0/3.6, 0.6,  600);
							this.setHighwayDefaults(6, LIVING_STREET, 1,  15.0/3.6, 1.0,  600);
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Method to perform changes on an existing network (e.g. additional network modes, additional links).
	 * 
	 * @param network
	 */
	public void modify(Network network){
		
	}
	
	/**
	 * 
	 * Transforms OSM ways into MATSim nodes and links and adds them to the network.
	 * 
	 * @param wayEntries The collection of entries for each OSM way.
	 */
	private void processWayEntries(Set<WayEntry> wayEntries){
		
		// Create a new geometry factory to create points for MATSim nodes
		// This makes it easier to perform methods like contains
		GeometryFactory gf = new GeometryFactory();
		
		// Iterate over all OSM ways
		for(WayEntry entry : wayEntries){

			// If access is restricted, we skip the way
			if("no".equals(entry.accessTag)) continue;
			
			Coordinate[] coordinates = entry.geometry.getCoordinates();
			
			if(coordinates.length > 1){
				
				// Set the from coordinate initially and the current way length to zero
				Coordinate from = coordinates[0];
				double length = 0.;
				Coordinate lastTo = from;
				
				// Go through all coordinates contained in the way
				for(int i = 1; i < coordinates.length; i++){
					
					// Get the next coordinate in the sequence and calculate the length between it and the last coordinate
					Coordinate next = coordinates[i];
					
					length = CoordUtils.calcDistance(this.transformation.transform(MGC.coordinate2Coord(lastTo)),
							this.transformation.transform(MGC.coordinate2Coord(next)));

//					for(AdministrativeUnit au : this.geoinformation.getSurveyArea().values()){

					boolean inSurveyArea = true;
					// If the coordinates are contained in the survey area, add a new link to the network
					if(!this.geoinformation.getSurveyAreaBoundingBox().contains(gf.createPoint(lastTo)) &&
							!this.geoinformation.getSurveyAreaBoundingBox().contains(gf.createPoint(next))){
						
						inSurveyArea = false;
						
					}
						
					createLink(entry, length, lastTo, next, inSurveyArea);
							//TODO make a difference between inner and outer au's (w/ respect to highway hierarchy)
							
//							break;
//						}
						
//					}
					//Update last visited coordinate in the sequence
					lastTo = next;
					
				}
				
			}
			
		}
		
		log.info("Conversion statistics:");
		log.info("OSM ways:     " + wayEntries.size());
		log.info("MATSim nodes: " + network.getNodes().size());
		log.info("MATSim links: " + network.getLinks().size());
		
	}
	
	/**
	 * 
	 * The actual conversion from OSM into MATSim data.
	 * 
	 * @param entry The OSM way entry.
	 * @param length The euclidean length of the link.
	 * @param from The coordinate at the beginning of the link.
	 * @param to The coordinate at the end of the link.
	 */
	private void createLink(WayEntry entry, double length, Coordinate from, Coordinate to, boolean inSurveyArea){
		
		HighwayDefaults defaults = this.highwayDefaults.get(entry.highwayTag);
		
		// If there are defaults for the highway type of the current way, we proceed
		// Else the way is simply skipped
		if(defaults != null){
			
			if(!inSurveyArea){
				if(defaults.hierarchyLevel > this.levelOfDetail - 2){
					return;
				}
			}
			
			//set all values to default
			double freespeed = defaults.freespeed;
			double freespeedFactor = defaults.freespeedFactor;
			double lanesPerDirection = defaults.lanesPerDirection;
			double laneCapacity = defaults.laneCapacity;
			boolean oneway = defaults.oneway;
			boolean onewayReverse = false;

			// Handle the freespeed tag
			String freespeedTag = entry.maxspeedTag;
			
			if(freespeedTag != null){
				
				try{
					
					freespeed = Double.parseDouble(freespeedTag) / 3.6;
					
				} catch(NumberFormatException e){
					
					if(!unknownTags.contains(freespeedTag)){
						
						unknownTags.add(freespeedTag);
						log.warn("Could not parse freespeed tag: " + freespeedTag + ". Ignoring it.");
						
					}
					
				}
				
			}
			
			if("roundabout".equals(entry.junctionTag)){
				
				oneway = true;
				
			}
			
			// Handle the oneway tag
			if(entry.onewayTag != null){

				if(entry.onewayTag.equals("yes") || entry.onewayTag.equals("true") || entry.onewayTag.equals("1")){
					
					oneway = true;
					
				} else if(entry.onewayTag.equals("no")){
					
					oneway = false;
					
				} else if(entry.onewayTag.equals("-1")){
					
					onewayReverse = true;
					oneway = false;
					
				}
				
			}
			
			if(entry.highwayTag.equalsIgnoreCase("trunk") || entry.highwayTag.equalsIgnoreCase("primary") ||
					entry.highwayTag.equalsIgnoreCase("secondary")){
	            
				if((oneway || onewayReverse) && lanesPerDirection == 1.0){
	            
					lanesPerDirection = 2.0;
					
	            }
				
			}
			
			// Handle the lanes tag.
			String lanesTag = entry.lanesTag;
			if(lanesTag != null){
				
				try {
					
					double totalNofLanes = Double.parseDouble(lanesTag);
					
					if (totalNofLanes > 0) {
						lanesPerDirection = totalNofLanes;

			            if (!oneway && !onewayReverse) {
			            	
			                lanesPerDirection /= 2.;
			                
			            }
					}
				
				} catch (Exception e) {
					
					if(!unknownTags.contains(lanesTag)){
						
						unknownTags.add(freespeedTag);
						log.warn("Could not parse lanes tag: " + freespeedTag + ". Ignoring it.");
						
					}
				
				}
					
			}
			
			// Set the link's capacity and the resulting freespeed (if it's meant to be scaled)
			double capacity = lanesPerDirection * laneCapacity * this.configuration.getScaleFactor();
			
			if(this.scaleMaxSpeed){
				
				freespeed *= freespeedFactor;
				
			}

			// If a node already exists at the from location, use it as from node, else create a new one
			Coord fromCoord = this.transformation.transform(MGC.coordinate2Coord(from));
			Node fromNode = null;
			if(!coords2Nodes.containsKey(fromCoord)){
				fromNode = createNode(fromCoord);
				coords2Nodes.put(fromCoord, fromNode);
			} else {
				fromNode = coords2Nodes.get(fromCoord);
			}
			
			// If a node already exists at the to location, use it as to node, else create a new one			
			Coord toCoord = this.transformation.transform(MGC.coordinate2Coord(to));
			Node toNode = null;
			if(!coords2Nodes.containsKey(toCoord)){
				toNode = createNode(toCoord);
				coords2Nodes.put(toCoord, toNode);
			} else {
				toNode = coords2Nodes.get(toCoord);
			}
			
			String origId = entry.osmId;
			
			// Create a link in one direction
			if(!onewayReverse){
				
				Link link = network.getFactory().createLink(Id.createLinkId(linkCounter), fromNode, toNode);
				link.setCapacity(capacity);
				link.setFreespeed(freespeed);
				link.setLength(length);
				link.setNumberOfLanes(lanesPerDirection);
				
				if(link instanceof LinkImpl){
					
					((LinkImpl)link).setOrigId(origId);
					((LinkImpl)link).setType(entry.highwayTag);
					
				}
				
				network.addLink(link);
				linkCounter++;
				
			}
			
			// If it's not a oneway link, create another link in the opposite direction
			if(!oneway){
				
				Link link = network.getFactory().createLink(Id.createLinkId(linkCounter), toNode, fromNode);
				link.setCapacity(capacity);
				link.setFreespeed(freespeed);
				link.setLength(length);
				link.setNumberOfLanes(lanesPerDirection);
				
				if(link instanceof LinkImpl){
					
					((LinkImpl)link).setOrigId(origId);
					((LinkImpl)link).setType(entry.highwayTag);
					
				}
				
				network.addLink(link);
				linkCounter++;
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Creates a new MATSim node.
	 * 
	 * @param coord The location of the node.
	 * @return A MATSim node.
	 */
	private Node createNode(Coord coord){
		
		Node node = null;
		
		node = network.getFactory().createNode(Id.createNodeId(nodeCounter), coord);
		network.addNode(node);
		nodeCounter++;
		
		return node;
		
	}

	/**
	 * 
	 * Stores default values for link characteristics.
	 * 
	 * @author dhosse
	 *
	 */
	static class HighwayDefaults{
		
		int hierarchyLevel;
		double freespeed;
		double freespeedFactor;
		double lanesPerDirection;
		double laneCapacity;
		boolean oneway;
		
		HighwayDefaults(int hierarchyLevel, double freespeed, double freespeedFactor, double lanesPerDirection, double laneCapacity, 
				boolean oneway){
			this.hierarchyLevel = hierarchyLevel;
			this.freespeed = freespeed;
			this.freespeedFactor = freespeedFactor;
			this.lanesPerDirection = lanesPerDirection;
			this.laneCapacity = laneCapacity;
			this.oneway = oneway;
		}
		
	}
	
}
