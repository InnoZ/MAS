package com.innoz.toolbox.io.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;

import javax.inject.Inject;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.Config;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.population.PersonUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.postgis.PGgeometry;
import org.postgis.Point;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.io.database.DatabaseConstants.DatabaseTable;
import com.innoz.toolbox.utils.PsqlUtils;

/**
 * 
 * Class for data exchange between the MATSim data format and postgreSQL databases.<br>
 * 
 * @author dhosse
 *
 */
public class MatsimPsqlAdapter {
	
	@Inject static Configuration configuration;

	// only one connection at a time
	private static Connection connection;
	
	// private!
	private MatsimPsqlAdapter() {};
	
	/**
	 * 
	 * This method is equivalent to MATSim's {@link org.matsim.core.scenario.ScenarioUtils#loadScenario(Config)} method.
	 * Takes data tables contained in the given schema name from the 'simulation' database to generate MATSim scenario data
	 * (network, population etc.)
	 * 
	 * @param scenario The MATSim scenario to read the data in.
	 * @param tablespace The schema name containing the MATSim data tables.
	 */
	public static void createScenarioFromPsql(final Scenario scenario, final Configuration configuration, final String tablespace) {
		
		try {
		
			connection = PsqlAdapter.createConnection(DatabaseConstants.SIMULATIONS_DB);
			
			createNetworkFromTable(scenario.getNetwork(), tablespace);
			createPopulationFromTable(scenario.getPopulation(), tablespace);
			
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static void writeScenarioToPsql(final Scenario scenario, final String tablespace) {
		
		try {
		
			connection = PsqlAdapter.createConnection(DatabaseConstants.SIMULATIONS_DB);
			
			network2Table(scenario.getNetwork(), tablespace);
			plans2Table(scenario.getPopulation(), tablespace);
			
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * @param network The MATSim network object.
	 * @param tablespace The schema name containing the MATSim nodes and links table.
	 */
	private static void createNetworkFromTable(final Network network, final String tablespace) {
		
		try {
		
			Statement statement = connection.createStatement();

			ResultSet nodesSet = statement.executeQuery("SELECT * FROM " + tablespace + ".nodes;");
			
			while(nodesSet.next()) {
				
				Id<Node> id = Id.createNodeId(nodesSet.getString("id"));
				Coord coord = new Coord(nodesSet.getDouble("x_coord"), nodesSet.getDouble("y_coord"));
				
				Node nn = network.getFactory().createNode(id, coord);
				network.addNode(nn);
				
			}
			
			nodesSet.close();
			
			ResultSet linksSet = statement.executeQuery("SELECT * FROM " + tablespace + ".links;");
			
			while(linksSet.next()) {
				
				Id<Link> id = Id.createLinkId(linksSet.getString("id"));
				Node fromNode = network.getNodes().get(Id.createNodeId(linksSet.getString("from_node_id")));
				Node toNode = network.getNodes().get(Id.createNodeId(linksSet.getString("to_node_id")));
				
				Link ll = network.getFactory().createLink(id, fromNode, toNode);
				
				ll.setLength(linksSet.getDouble("length"));
				ll.setFreespeed(linksSet.getDouble("freespeed"));
				ll.setCapacity(linksSet.getDouble("capacity"));
				ll.setNumberOfLanes(linksSet.getInt("permlanes"));
				ll.setAllowedModes(CollectionUtils.stringToSet(linksSet.getString("modes")));
				((LinkImpl)ll).setOrigId(linksSet.getString("origid"));
				((LinkImpl)ll).setType(linksSet.getString("type"));
				
				network.addLink(ll);
				
			}
			
			linksSet.close();
			
			statement.close();
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * @param network
	 * @param tablespace
	 */
	public static void network2Table(final Network network, final String tablespace) {
		
		try {
		
			Statement statement = connection.createStatement();
			
			statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + tablespace + ";");
			
			statement.executeUpdate("DROP TABLE IF EXISTS " + tablespace + ".nodes;");
			statement.executeUpdate("DROP TABLE IF EXISTS " + tablespace + ".links;");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablespace + ".nodes("
					+ "id varchar,"
					+ "x_coord double precision,"
					+ "y_coord double precision"
					+ ");");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablespace + ".links("
					+ "id varchar,"
					+ "from_node_id varchar,"
					+ "to_node_id varchar,"
					+ "length double precision,"
					+ "freespeed double precision,"
					+ "capacity double precision,"
					+ "permlanes double precision,"
					+ "oneway integer,"
					+ "modes varchar,"
					+ "origid varchar,"
					+ "type varchar"
					+ ");");
			
			writeNodesTable(network.getNodes().values(), tablespace);
			writeLinksTable(network.getLinks().values(), tablespace);
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * @param nodes
	 * @param tablespace
	 * @throws SQLException
	 */
	private static void writeNodesTable(final Collection<? extends Node> nodes, String tablespace) throws SQLException {
		
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + tablespace + ".nodes (id, x_coord, y_coord) VALUES(?, ?, ?);");
		
		for(Node node : nodes) {
			
			stmt.setString(1, node.getId().toString());
			stmt.setDouble(2, node.getCoord().getX());
			stmt.setDouble(3, node.getCoord().getY());
			stmt.addBatch();
			
		}
		
		stmt.executeBatch();
		stmt.close();
		
	}
	
	/**
	 * 
	 * @param links
	 * @param tablespace
	 * @throws SQLException
	 */
	private static void writeLinksTable(final Collection<? extends Link> links, String tablespace) throws SQLException {

		PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + tablespace + ".links (id, from_node_id, to_node_id, length,"
				+ "freespeed, capacity, permlanes, oneway, modes, origid, type) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		
		for(Link link : links) {
		
			stmt.setString(1, link.getId().toString());
			stmt.setString(2, link.getFromNode().getId().toString());
			stmt.setString(3, link.getToNode().getId().toString());
			stmt.setDouble(4, link.getLength());
			stmt.setDouble(5, link.getFreespeed());
			stmt.setDouble(6, link.getCapacity());
			stmt.setDouble(7, link.getNumberOfLanes());
			stmt.setInt(8, 1);
			stmt.setString(9, CollectionUtils.setToString(link.getAllowedModes()));
			stmt.setString(10, ((LinkImpl)link).getOrigId());
			stmt.setString(11, ((LinkImpl)link).getType());
			stmt.addBatch();
			
		}
		
		stmt.executeBatch();
		stmt.close();
		
	}
	
	/**
	 * 
	 * @param population
	 * @param tablespace
	 */
	public static void plans2Table(final Population population, final String tablespace) {
		
		try {
			
			Statement statement = connection.createStatement();
			
			statement.executeUpdate("DROP TABLE IF EXISTS " + tablespace + ".persons;");
			statement.executeUpdate("DROP TABLE IF EXISTS " + tablespace + ".plans;");
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablespace + ".persons("
					+ "id varchar,"
					+ "age double precision,"
					+ "sex char,"
					+ "license varchar,"
					+ "car_avail varchar,"
					+ "employed boolean"
					+ ");");
			
			((org.postgresql.PGConnection)connection).addDataType("geometry", Class.forName("org.postgis.PGgeometry"));
			
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tablespace + ".plans("
					+ "person_id varchar,"
					+ "element_index integer,"
					+ "selected boolean,"
					+ "act_type varchar,"
					+ "act_coord geometry,"
					+ "act_start double precision,"
					+ "act_end double precision,"
					+ "act_duration double precision,"
					+ "leg_mode varchar"
					+ ");");
			
			statement.close();
			
			writePersonsTable(population, tablespace);
			writePlansTable(population, tablespace);
			
		} catch (ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * 
	 * @param population
	 * @param tablespace
	 * @throws SQLException
	 */
	private static void writePersonsTable(final Population population, String tablespace) throws SQLException {
		
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + tablespace +
				".persons (id, age, sex, license, car_avail, employed) VALUES (?, ?, ?, ?, ?, ?)");		
		
		ObjectAttributes personAttributes = population.getPersonAttributes();
		
		for(Person person : population.getPersons().values()) {
			
			String personId = person.getId().toString();
			Double age = (Double) personAttributes.getAttribute(personId, "age");
			String sex = (String) personAttributes.getAttribute(personId, "sex");
			Boolean license = (Boolean) personAttributes.getAttribute(personId, "hasLicense");
			Boolean carAvail = (Boolean) personAttributes.getAttribute(personId, "carAvail");
			Boolean employed = (Boolean) personAttributes.getAttribute(personId, "employed");
			
			stmt.setString(1, personId);
			stmt.setDouble(2, age != null ? age : -1);
			stmt.setString(3, sex != null ? sex : "");
			stmt.setBoolean(4, license != null ? license : false);
			stmt.setBoolean(5, carAvail != null ? carAvail : false);
			stmt.setBoolean(6, employed != null ? employed : false);
			stmt.addBatch();
			
		}
		
		stmt.executeBatch();
		
		stmt.close();
		
	}
	
	/**
	 * 
	 * @param population
	 * @param tablespace
	 * @throws SQLException
	 */
	private static void writePlansTable(final Population population, String tablespace) throws SQLException {
		
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO " + tablespace + ".plans (person_id, element_index, selected, act_type,"
				+ "act_coord, act_start, act_end, act_duration, leg_mode) VALUES(?, ?, ?, ?, st_geomfromtext(?), ?, ?, ?, ?);");
		
		for(Person person : population.getPersons().values()) {
			
			stmt.setString(1, person.getId().toString());
			
			for(Plan plan : person.getPlans()) {
				
				for(PlanElement pe : plan.getPlanElements()) {
					
					stmt.setInt(2, plan.getPlanElements().indexOf(pe));
					stmt.setBoolean(3, (boolean) PersonUtils.isSelected(plan));
					
					if(pe instanceof Activity) {
						
						Activity act = (Activity) pe;
						
						stmt.setString(4, act.getType());
						stmt.setString(5, createWKT(act.getCoord()));
						stmt.setDouble(6, act.getStartTime());
						stmt.setDouble(7, act.getEndTime());
						stmt.setDouble(8, act.getMaximumDuration());
						stmt.setNull(9, Types.VARCHAR);
						
					} else {
						
						Leg leg = (Leg) pe;
						
						stmt.setNull(4, Types.VARCHAR);
						stmt.setNull(5, Types.OTHER);
						stmt.setNull(6, Types.DOUBLE);
						stmt.setNull(7, Types.DOUBLE);
						stmt.setNull(8, Types.DOUBLE);
						stmt.setString(9, leg.getMode());
						
					}
					
					stmt.addBatch();
					
				}
				
			}
			
		}
		
		stmt.executeBatch();
		stmt.close();
		
	}
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	private static String createWKT(Coord coord) {
		
		return "POINT(" + Double.toString(coord.getX()) + " " + Double.toString(coord.getY()) + ")";
		
	}
	
	/**
	 * 
	 * @param population
	 * @param tablespace
	 */
	private static void createPopulationFromTable(final Population population, final String tablespace) {
		
		try {
			
			DatabaseTable table = DatabaseConstants.getDatabaseTable(DatabaseConstants.PLANS_TABLE);
			
			Statement statement = connection.createStatement();
			
			ResultSet results = statement.executeQuery("SELECT * from " + tablespace + "." + table.getTableName() + ";");
			
			PopulationFactory factory = population.getFactory();
			ObjectAttributes personAttributes = population.getPersonAttributes();
			
			while(results.next()) {
				
				// Create a person from the id
				Id<Person> personId = Id.createPersonId(results.getString("id"));
				Person current = factory.createPerson(personId);
				population.addPerson(current);
				
				// Create the person's attributes
				/*
				 * double, string, boolean, boolean, boolean
				 */
				Double age = results.getDouble("age");
				String sex = results.getString("sex");
				String license = results.getString("license");
				String carAvail = results.getString("car_avail");
				Boolean employed = results.getBoolean("employed");
				
				personAttributes.putAttribute(personId.toString(), "age", age != null ? age : -1);
				personAttributes.putAttribute(personId.toString(), "sex", sex != null ? sex : "n");
				personAttributes.putAttribute(personId.toString(), "license", license != null ? Boolean.parseBoolean(license) : false);
				personAttributes.putAttribute(personId.toString(), "car_avail", carAvail != null ? Boolean.parseBoolean(carAvail) : false);
				personAttributes.putAttribute(personId.toString(), "employed", employed != null ? employed : false);
				
			}
			
			results.close();
			
			String sql = new PsqlUtils.PsqlStringBuilder(PsqlUtils.processes.SELECT.name(), tablespace, "plans")
				.orderClause("person_id, element_index").build();
			
			results = statement.executeQuery(sql);
			
			Plan plan = null;
			Id<Person> lastPersonId = null;
			Id<Person> currentPersonId = null;
			
			while(results.next()) {
				
				Person current = population.getPersons().get(Id.createPersonId(results.getString("person_id")));
				currentPersonId = current.getId();

				if(current != null) {
					
					if(currentPersonId != lastPersonId) {
					
						plan = factory.createPlan();
						lastPersonId = currentPersonId;
						
						current.addPlan(plan);
						
						Boolean isSelected = results.getBoolean("selected");
						
						if(isSelected) {
							
							current.setSelectedPlan(plan);
							
						}
						
					}
					
					String actType = results.getString("act_type");
					
					if(actType != null) {
						
						// Create an activity
						double actStartTime = results.getDouble("act_start");
						double actEndTime = results.getDouble("act_end");
						double maxDuration = results.getDouble("act_duration");
						
						PGgeometry geometry = (PGgeometry) results.getObject("act_coord");
						
						Point point = (Point) geometry.getGeometry();
						
						Activity act = factory.createActivityFromCoord(actType, new Coord(point.getX(), point.getY()));

						if(Double.isFinite(actStartTime)) act.setStartTime(actStartTime);
						if(Double.isFinite(actEndTime)) act.setEndTime(actEndTime);
						if(Double.isFinite(maxDuration)) act.setMaximumDuration(maxDuration);
						
						plan.addActivity(act);
						
					} else {
						
						// Create a leg
						plan.addLeg(factory.createLeg(results.getString("leg_mode")));
						
					}
					
				}
				
			}

		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
}