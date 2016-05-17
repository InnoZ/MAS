package playground.dhosse.scenarios.generic.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.utils.AdministrativeUnit;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.utils.NetworkSimplifier;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

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
	private static final String TAG_ACCESS = "access";
	private static final String TAG_GEOMETRY = "st_astext";
	private static final String TAG_HIGHWAY = "highway";
	private static final String TAG_ID = "osm_id";
	private static final String TAG_JUNCTION = "junction";
//	private static final String TAG_LANES = "lanes";
//	private static final String TAG_MAXSPEED = "maxspeed";
	private static final String TAG_ONEWAY = "oneway";
	private static final String MOTORWAY = "motorway";
	private static final String MOTORWAY_LINK = "motorway_link";
	private static final String TRUNK = "trunk";
	private static final String TRUNK_LINK = "trunk_link";
	private static final String PRIMARY = "primary";
	private static final String PRIMARY_LINK = "primary_link";
	private static final String SECONDARY = "secondary";
	private static final String TERTIARY = "tertiary";
	private static final String MINOR = "minor";
	private static final String UNCLASSIFIED = "unclassified";
	private static final String RESIDENTIAL = "residential";
	private static final String LIVING_STREET = "living_street";
	
	private final Network network;
	private final CoordinateTransformation transform;
	private final Configuration configuration;
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
	static enum networkDetail{};
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param network An empty MATSim network.
	 * @param configuration The scenario generation configuration.
	 */
	public NetworkCreatorFromPsql(final Network network, Configuration configuration){
		
		this.network = network;
		this.transform = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, configuration.getCrs());
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
	public void create() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, ParseException{
		
		// If highway defaults have been specified, use them. Else fall back to default values.
		if(this.highwayDefaults.size() < 1){
			
			this.setHighwayDefaults(MOTORWAY, 2.0, 100/3.6, 1.2, 2000.0, true);
			this.setHighwayDefaults(MOTORWAY_LINK, 1,  60.0/3.6, 1.2, 1500, true);
			this.setHighwayDefaults(TRUNK, 1,  80.0/3.6, 0.5, 1000);
			this.setHighwayDefaults(TRUNK_LINK, 1,  60.0/3.6, 0.5, 1500);
			this.setHighwayDefaults(PRIMARY, 1,  50.0/3.6, 0.5, 1000);
			this.setHighwayDefaults(PRIMARY_LINK, 1,  50.0/3.6, 0.5, 1000);
			this.setHighwayDefaults(SECONDARY, 1,  50.0/3.6, 0.5, 1000);
			this.setHighwayDefaults(TERTIARY, 1,  30.0/3.6, 0.8,  600);
			this.setHighwayDefaults(MINOR, 1,  30.0/3.6, 0.8,  600);
			this.setHighwayDefaults(UNCLASSIFIED, 1,  30.0/3.6, 0.8,  600);
			this.setHighwayDefaults(RESIDENTIAL, 1,  30.0/3.6, 0.6,  600);
			this.setHighwayDefaults(LIVING_STREET, 1,  15.0/3.6, 1.0,  600);
			
		}
		
		// Initialize a WKTReader to convert geometry strings into geometries.
		WKTReader wktReader = new WKTReader();
		Set<WayEntry> wayEntries = new HashSet<>();
		
		Class.forName("org.postgresql.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() + "/geodata",
				configuration.getDatabaseUsername(), configuration.getPassword());
	
		if(connection != null){

			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery("select osm_id, access, highway, junction, oneway,"
					+ " st_astext(way) from osm.osm_line where highway is not null and"
					+ " st_within(way,st_geomfromtext('" + Geoinformation.getCompleteGeometry().toString() + "',4326));");
			
			while(result.next()){
				
				// Create a new way entry for each result
				WayEntry entry = new WayEntry();
				entry.osmId = result.getString(TAG_ID);
				entry.accessTag = result.getString(TAG_ACCESS);
				entry.highwayTag = result.getString(TAG_HIGHWAY);
				entry.junctionTag = result.getString(TAG_JUNCTION);
				//TODO add lanes and maxspeed as attributes into db table
//				entry.lanesTag = result.getString(TAG_LANES);
//				entry.maxspeedTag = result.getString(TAG_MAXSPEED);
				entry.onewayTag = result.getString(TAG_ONEWAY);
				entry.geometry = wktReader.read(result.getString(TAG_GEOMETRY));
				wayEntries.add(entry);
				
			}
			
			// After all road data is retrieved, close the statement and the connection.
			result.close();
			statement.close();
			
		}
		
		connection.close();

		// Convert the way entries into a MATSim network
		processWayEntries(wayEntries);
		
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
	 * Setter for highway defaults.
	 * 
	 * @param highwayType The road type.
	 * @param lanesPerDirection Number of lanes per direction.
	 * @param freespeed Allowed free speed on this road type.
	 * @param freespeedFactor Factor for scaling of free speed.
	 * @param laneCapacity_vehPerHour The capacity of one lane on this road type per hour.
	 * @param oneway Road type is only accessible in one direction or not.
	 */
	public void setHighwayDefaults(final String highwayType, final double lanesPerDirection, final double freespeed,
			final double freespeedFactor, final double laneCapacity_vehPerHour, final boolean oneway){
		
		this.highwayDefaults.put(highwayType, new HighwayDefaults(freespeed, freespeedFactor, lanesPerDirection, laneCapacity_vehPerHour, oneway));
		
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
	public void setHighwayDefaults(final String highwayType, final double lanesPerDirection, final double freespeed,
			final double freespeedFactor, final double laneCapacity_vehPerHour){
		
		this.setHighwayDefaults(highwayType, lanesPerDirection, freespeed, freespeedFactor, laneCapacity_vehPerHour, false);
		
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
					
					length = CoordUtils.calcDistance(this.transform.transform(MGC.coordinate2Coord(lastTo)),
							this.transform.transform(MGC.coordinate2Coord(next)));

					for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){

						// If the coordinates are contained in the survey area, add a new link to the network
						if(au.getGeometry().contains(gf.createPoint(lastTo)) ||
								au.getGeometry().contains(gf.createPoint(next))){
							//TODO make a difference between inner and outer au's (w/ respect to highway hierarchy)
							createLink(entry, length, lastTo, next);
							break;
						}
						
					}
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
	private void createLink(WayEntry entry, double length, Coordinate from, Coordinate to){
		
		HighwayDefaults defaults = this.highwayDefaults.get(entry.highwayTag);
		
		// If there are defaults for the highway type of the current way, we proceed
		// Else the way is simply skipped
		if(defaults != null){
			
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
			double capacity = lanesPerDirection * laneCapacity;
			
			if(this.scaleMaxSpeed){
				
				freespeed *= freespeedFactor;
				
			}

			// If a node already exists at the from location, use it as from node, else create a new one
			Coord fromCoord = this.transform.transform(MGC.coordinate2Coord(from));
			Node fromNode = null;
			if(!coords2Nodes.containsKey(fromCoord)){
				fromNode = createNode(fromCoord);
				coords2Nodes.put(fromCoord, fromNode);
			} else {
				fromNode = coords2Nodes.get(fromCoord);
			}
			
			// If a node already exists at the to location, use it as to node, else create a new one			
			Coord toCoord = this.transform.transform(MGC.coordinate2Coord(to));
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
		
		double freespeed;
		double freespeedFactor;
		double lanesPerDirection;
		double laneCapacity;
		boolean oneway;
		
		HighwayDefaults(double freespeed, double freespeedFactor, double lanesPerDirection, double laneCapacity, boolean oneway){
			this.freespeed = freespeed;
			this.freespeedFactor = freespeedFactor;
			this.lanesPerDirection = lanesPerDirection;
			this.laneCapacity = laneCapacity;
			this.oneway = oneway;
		}
		
	}
	
	/**
	 * 
	 * Wrapper class that stores the OSM data of a way.
	 * 
	 * @author dhosse
	 *
	 */
	static class WayEntry{
		
		String osmId;
		
		String accessTag;
		String highwayTag;
		String junctionTag;
		String lanesTag;
		String maxspeedTag;
		String onewayTag;
		
		Geometry geometry;
		
	}
	
}
