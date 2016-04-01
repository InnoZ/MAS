package playground.dhosse.scenarios.generic.network;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import playground.dhosse.scenarios.generic.Configuration;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
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

	private static final String LINK_ATTRIBUTES = "linkAttributes";
	
	private static final String TAG_ACCESS = "access";
	private static final String TAG_GEOMETRY = "way";
	private static final String TAG_HIGHWAY = "highway";
	private static final String TAG_ID = "osmId";
	private static final String TAG_JUNCTION = "junction";
	private static final String TAG_LANES = "lanes";
	private static final String TAG_MAXSPEED = "maxspeed";
	private static final String TAG_ONEWAY = "oneway";

	private static int nodeCounter = 0;
	private static int linkCounter = 0;
	
	//TODO
	static enum modification{};

	private NetworkCreatorFromPsql(){};
	
	public static void create(Configuration configuration, Scenario scenario){
		
		WKTReader wktReader = new WKTReader();
		Set<WayEntry> wayEntries = new HashSet<>();
		
		//this is for custom attributes
		scenario.addScenarioElement(LINK_ATTRIBUTES, new ObjectAttributes());
		
		try {
			
			log.info("Connection to mobility database...");
		
			Class.forName("org.postgresql.Driver").newInstance();
			
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/geodata",
					configuration.getDatabaseUsername(), configuration.getPassword());
		
			if(connection != null){
				
				log.info("Connection establised.");
				
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("select * from ..."); //TODO query, needs to return geometry as wkt
				
				while(result.next()){
					
					WayEntry entry = new WayEntry();
					entry.osmId = result.getString(TAG_ID);
					entry.accessTag = result.getString(TAG_ACCESS);
					entry.highwayTag = result.getString(TAG_HIGHWAY);
					entry.junctionTag = result.getString(TAG_JUNCTION);
					entry.lanesTag = result.getString(TAG_LANES);
					entry.maxspeedTag = result.getString(TAG_MAXSPEED);
					entry.onewayTag = result.getString(TAG_ONEWAY);
					entry.geometry = wktReader.read(result.getString(TAG_GEOMETRY));
					wayEntries.add(entry);
					
				}
				
				processWayEntries(scenario, wayEntries);
				
				result.close();
				statement.close();
				
			}
			
			connection.close();
			
			new NetworkWriter(scenario.getNetwork()).write(configuration.getWorkingDirectory() + "network.xml.gz");
			new ObjectAttributesXmlWriter((ObjectAttributes) scenario.getScenarioElement(LINK_ATTRIBUTES))
				.writeFile(configuration.getWorkingDirectory() + "linkAttributes.xml.gz");
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException | ParseException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * For later changes (e.g. additional modes, additional links)
	 * 
	 * @param network
	 */
	public static void modify(Network network){
		
	}
	
	private static void processWayEntries(Scenario scenario, Set<WayEntry> wayEntries){
		
		for(WayEntry entry : wayEntries){

			//if access is restricted, we skip the way
			if("no".equals(entry.accessTag)) continue;
			
			Coordinate[] coordinates = entry.geometry.getCoordinates();
			
			//calc length of the way
			if(coordinates.length > 1){
				
				Coordinate from = coordinates[0];
				Coordinate to = coordinates[coordinates.length - 1];
				double length = 0.;
				Coordinate lastTo = from;
				
				for(int i = 0; i < coordinates.length; i++){
					
					Coordinate next = coordinates[i];
					
					if(!next.equals(to)){
						
						length += CoordUtils.calcEuclideanDistance(MGC.coordinate2Coord(lastTo), MGC.coordinate2Coord(next));
						
					}
					
					lastTo = next;
					
				}
				
				createLink(scenario.getNetwork(), entry, length);
				
			}
			
		}
		
	}
	
	private static void createLink(Network network, WayEntry entry, double length){
	
		Node fromNode = network.getFactory().createNode(Id.createNodeId(nodeCounter),
				MGC.coordinate2Coord(entry.geometry.getCoordinates()[0]));
		network.addNode(fromNode);
		nodeCounter++;
		Node toNode = network.getFactory().createNode(Id.createNodeId(nodeCounter),
				MGC.coordinate2Coord(entry.geometry.getCoordinates()[entry.geometry.getCoordinates().length - 1]));
		network.addNode(toNode);
		nodeCounter++;
		
		//TODO link attributes and custom attributes
		Link link = network.getFactory().createLink(Id.createLinkId(linkCounter), fromNode, toNode);
		network.addLink(link);
		linkCounter++;
	}
	
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
