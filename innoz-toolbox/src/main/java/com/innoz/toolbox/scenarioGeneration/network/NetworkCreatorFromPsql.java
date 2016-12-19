package com.innoz.toolbox.scenarioGeneration.network;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.io.database.DatabaseReader;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;

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
	private static AtomicInteger linkCounter = new AtomicInteger(0);
	
	private Map<String, OsmNodeEntry> nodes = new HashMap<>();
	private Map<String, WayEntry> ways = new HashMap<>();
	
	private boolean scaleMaxSpeed = false;
	private boolean cleanNetwork = false;
	private boolean simplifyNetwork = false;

	private GeometryFactory gf;
	
	private Set<String> unknownTags = new HashSet<>();
	
	private Map<String, HighwayDefaults> highwayDefaults = new HashMap<String, HighwayDefaults>();

	private Geometry bufferedArea;
	
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
		CoordinateReferenceSystem to = CRS.decode(configuration.misc().getCoordinateSystem(), true);
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
		this.simplifyNetwork = b;
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
		dbReader.readOsmRoads(this.configuration, ways, nodes);
		this.bufferedArea = dbReader.getBufferedArea();
		processEntries();
		
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
			final double freespeedFactor, final double laneCapacity_vehPerHour, final boolean oneway, String modes){
		
		this.highwayDefaults.put(highwayType, new HighwayDefaults(hierarchyLevel, freespeed, freespeedFactor, lanesPerDirection,
				laneCapacity_vehPerHour, oneway, modes));
		
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
			final double freespeedFactor, final double laneCapacity_vehPerHour, String modes){
		
		this.setHighwayDefaults(hierarchyLevel, highwayType, lanesPerDirection, freespeed, freespeedFactor, laneCapacity_vehPerHour, false, modes);
		
	}
	
	/**
	 * Default setter for the highway defaults. If no defaults were specified, this
	 * method is called and sets the defaults according to the level of detail specified.
	 */
	private void setHighwayDefaultsAccordingToLevelOfDetail(){

		this.setHighwayDefaults(1, MOTORWAY, 2.0, 100/3.6, 1.2, 2000.0, true, "car");
		this.setHighwayDefaults(1, MOTORWAY_LINK, 1,  60.0/3.6, 1.2, 1500, true, "car");
		
		if(this.levelOfDetail > 1){
			
			this.setHighwayDefaults(2, TRUNK, 1,  80.0/3.6, 0.5, 1000, "car");
			this.setHighwayDefaults(2, TRUNK_LINK, 1,  60.0/3.6, 0.5, 1500, "car");
			
			if(this.levelOfDetail > 2){
				
				this.setHighwayDefaults(3, PRIMARY, 1,  50.0/3.6, 0.5, 1000, "car");
				this.setHighwayDefaults(3, PRIMARY_LINK, 1,  50.0/3.6, 0.5, 1000, "car");
				
				if(this.levelOfDetail > 3){
					
					this.setHighwayDefaults(4, SECONDARY, 1,  50.0/3.6, 0.5, 1000, "car");
					
					if(this.levelOfDetail > 4){
						
						if(this.configuration.scenario().getScaleFactor() <= 0.1) return;
						
						this.setHighwayDefaults(5, TERTIARY, 1,  30.0/3.6, 0.8,  600, "car");
						
						if(this.levelOfDetail > 5){
							
							this.setHighwayDefaults(6, MINOR, 1,  30.0/3.6, 0.8,  600, "car");
							this.setHighwayDefaults(6, UNCLASSIFIED, 1,  30.0/3.6, 0.8,  600, "car");
							this.setHighwayDefaults(6, RESIDENTIAL, 1,  30.0/3.6, 0.6,  600, "car");
							this.setHighwayDefaults(6, LIVING_STREET, 1,  15.0/3.6, 1.0,  600, "car");
							
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
	private void processEntries(){
		
		this.gf = new GeometryFactory();
		
		for(WayEntry entry : this.ways.values()){
			
			String highway = entry.getHighwayTag();
			
			if(highway != null && this.highwayDefaults.containsKey(highway)){

				nodes.get(entry.getNodes().get(0)).incrementWays();
				nodes.get(entry.getNodes().get(entry.getNodes().size()-1)).incrementWays();
				
				for(String id : entry.getNodes()){

					OsmNodeEntry node = nodes.get(id);
					node.incrementWays();
						
					boolean inSurveyArea = this.bufferedArea.contains(MGC.coord2Point(node.getCoord()));
					int hierarchyLevel = inSurveyArea ? this.levelOfDetail : this.levelOfDetail - 2;
				
					if(this.highwayDefaults.get(entry.getHighwayTag()).hierarchyLevel <= hierarchyLevel){
						
						node.setUsed(true);
						
					}
					
				}
				
			}
			
		}
		
		if(this.simplifyNetwork){

			for(OsmNodeEntry entry : this.nodes.values()){
				
				if(entry.getWays() == 1){
					
					entry.setUsed(false);
					
				}
				
			}
			
		}
		
		for(WayEntry entry : this.ways.values()){
			
			String highway = entry.getHighwayTag();
			
			if(highway != null && this.highwayDefaults.containsKey(highway)){
				
				int prevRealNodeIndex = 0;
				OsmNodeEntry prevRealNode = this.nodes.get(entry.getNodes().get(prevRealNodeIndex));
				
				for (int i = 1; i < entry.getNodes().size(); i++) {
					
					OsmNodeEntry node = this.nodes.get(entry.getNodes().get(i));
					
					if(node.isUsed()){
						
						if(prevRealNode == node){
							
							double increment = Math.sqrt(i - prevRealNodeIndex);
							double nextNodeToKeep = prevRealNodeIndex + increment;
						
							for (double j = nextNodeToKeep; j < i; j += increment) {
								
								int index = (int) Math.floor(j);
								OsmNodeEntry intermediaryNode = this.nodes.get(entry.getNodes().get(index));
								intermediaryNode.setUsed(true);
								
							}
							
						}
						
						prevRealNodeIndex = i;
						prevRealNode = node;
						
					}
					
				}
				
			}
			
		}
		
		for(OsmNodeEntry node : this.nodes.values()){
			
			if(node.isUsed()){
				
				Node nn = this.network.getFactory().createNode(Id.createNodeId(node.getId()), this.transformation.transform(node.getCoord()));
				this.network.addNode(nn);
				
			}
			
		}
		
		for(WayEntry way : this.ways.values()){
			
			String highway = way.getHighwayTag();
			
			if(highway != null){
				
				OsmNodeEntry fromNode = this.nodes.get(way.getNodes().get(0));
				double length = 0.0d;
				OsmNodeEntry lastToNode = fromNode;
				if(fromNode.isUsed()){
					
					for (int i = 1, n = way.getNodes().size(); i < n; i++) {
						
						OsmNodeEntry toNode = this.nodes.get(way.getNodes().get(i));
						
						if(toNode != lastToNode){
							
							length += CoordUtils.calcEuclideanDistance(this.transformation.transform(lastToNode.getCoord()),
									this.transformation.transform(toNode.getCoord()));
							
							if(toNode.isUsed()){
								
								this.createLink(way, length, fromNode, toNode);
								
								fromNode = toNode;
								length = 0.0;
								
							}
							
							lastToNode = toNode;
							
						}
						
					}
					
				}
				
			}
			
		}
		
		log.info("Conversion statistics:");
		log.info("OSM nodes:    " + nodes.size());
		log.info("OSM ways:     " + ways.size());
		log.info("MATSim nodes: " + network.getNodes().size());
		log.info("MATSim links: " + network.getLinks().size());

	}
		
	public void createLink(WayEntry entry, double length, OsmNodeEntry fromNode,
			OsmNodeEntry toNode){
		
		HighwayDefaults defaults = this.highwayDefaults.get(entry.getHighwayTag());
		
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
			Set<String> modes = CollectionUtils.stringToSet(defaults.modes);

			// Handle the freespeed tag
			String freespeedTag = entry.getMaxspeedTag();
			
			if(freespeedTag != null){
				
				try{
					
					freespeed = Double.parseDouble(freespeedTag) / 3.6;
					
				} catch(NumberFormatException e){

					freespeed = resolveUnknownFreespeedTag(freespeedTag);
					
					if(!unknownTags.contains(freespeedTag) && freespeed == 0){
						
						unknownTags.add(freespeedTag);
						log.warn("Could not parse freespeed tag: " + freespeedTag + ". Ignoring it.");
						
					}
					
				}
				
			}
			
			if("roundabout".equals(entry.getJunctionTag())){
				
				oneway = true;
				
			}
			
			// Handle the oneway tag
			if(entry.getOnewayTag() != null){

				if(entry.getOnewayTag().equals("yes") || entry.getOnewayTag().equals("true") ||
						entry.getOnewayTag().equals("1")){
					
					oneway = true;
					
				} else if(entry.getOnewayTag().equals("no")){
					
					oneway = false;
					
				} else if(entry.getOnewayTag().equals("-1")){
					
					onewayReverse = true;
					oneway = false;
					
				}
				
			}
			
			if(entry.getHighwayTag().equalsIgnoreCase(TRUNK) ||
					entry.getHighwayTag().equalsIgnoreCase(PRIMARY) ||
					entry.getHighwayTag().equalsIgnoreCase(SECONDARY)){
	            
				if((oneway || onewayReverse) && lanesPerDirection == 1.0){
	            
					lanesPerDirection = 2.0;
					
	            }
				
			}
			
			// Handle the lanes tag.
			String lanesTag = entry.getLanesTag();
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
			double capacity = lanesPerDirection * laneCapacity * this.configuration.scenario().getScaleFactor();
			
			if(this.scaleMaxSpeed){
				
				freespeed *= freespeedFactor;
				
			}

			
			String origId = entry.getOsmId();

			synchronized(linkCounter){
				
				Id<Node> from = Id.createNodeId(fromNode.getId());
				Id<Node> to = Id.createNodeId(toNode.getId());
				
				if(this.network.getNodes().get(from) != null && this.network.getNodes().get(to) != null){

				// Create a link in one direction
					if(!onewayReverse){
						
						Link link = network.getFactory().createLink(Id.createLinkId(linkCounter.get()), this.network.getNodes().get(from), this.network.getNodes().get(to));
						link.setCapacity(capacity);
						link.setFreespeed(freespeed);
						link.setLength(length);
						link.setNumberOfLanes(lanesPerDirection);
						link.setAllowedModes(modes);
						
						if(link instanceof LinkImpl){
							
							((LinkImpl)link).setOrigId(origId);
							((LinkImpl)link).setType(entry.getHighwayTag());
							
						}
						
						network.addLink(link);
						linkCounter.incrementAndGet();
						
					}
					
					// If it's not a oneway link, create another link in the opposite direction
					if(!oneway){
						
						Link link = network.getFactory().createLink(Id.createLinkId(linkCounter.get()), this.network.getNodes().get(to), this.network.getNodes().get(from));
						link.setCapacity(capacity);
						link.setFreespeed(freespeed);
						link.setLength(length);
						link.setNumberOfLanes(lanesPerDirection);
						link.setAllowedModes(modes);
						
						if(link instanceof LinkImpl){
							
							((LinkImpl)link).setOrigId(origId);
							((LinkImpl)link).setType(entry.getHighwayTag());
							
						}
						
						network.addLink(link);
						linkCounter.incrementAndGet();
						
					}
					
				}
					
			}

		}
			
	}
	
	private double resolveUnknownFreespeedTag(String s){
		
		double kmh = 0;
		
		if("DE:urban".equals(s)){
			
			kmh = 50;
			
		} else if("DE:rural".equals(s)){
			
			kmh = 100;
			
		} else if("DE:motorway".equals(s) || "none".equals(s)){
			
			kmh = 130;
			
		} else if("walk".equals(s) || "DE:living_street".equals(s)){
			
			kmh = 5;
			
		} else if("5 mph".equals(s)){
			
			kmh = 5 * 1.609;
			
		} else if(s.contains(";")){
			
			kmh = Double.parseDouble(s.split(";")[0]);
			
		}
		
		return kmh / 3.6;
		
	}
		
	public Geometry getBufferedArea(){
		return this.bufferedArea;
	}
	
	public GeometryFactory getGeomFactory(){
		return this.gf;
	}
	
	public Geoinformation getGeoinformation(){
		return this.geoinformation;
	}
	
	public CoordinateTransformation getTransformation(){
		return this.transformation;
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
		String modes;
		
		HighwayDefaults(int hierarchyLevel, double freespeed, double freespeedFactor, double lanesPerDirection, double laneCapacity, 
				boolean oneway, String modes){
			this.hierarchyLevel = hierarchyLevel;
			this.freespeed = freespeed;
			this.freespeedFactor = freespeedFactor;
			this.lanesPerDirection = lanesPerDirection;
			this.laneCapacity = laneCapacity;
			this.oneway = oneway;
			this.modes = modes;
		}
		
	}
	
}