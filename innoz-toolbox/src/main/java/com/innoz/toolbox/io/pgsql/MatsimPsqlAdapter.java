package com.innoz.toolbox.io.pgsql;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
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
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.pt.PtConstants;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.postgis.PGgeometry;
import org.postgis.Point;

import com.innoz.toolbox.analysis.AggregatedAnalysis;
import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.io.database.DatabaseConstants.DatabaseTable;
import com.innoz.toolbox.io.database.DatabaseConstants.RailsEnvironments;
import com.innoz.toolbox.utils.PsqlUtils;

/**
 * 
 * Class for data exchange between the MATSim data format and postgreSQL databases.<br>
 * 
 * @author dhosse
 *
 */
public class MatsimPsqlAdapter {
	
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
	public static void createScenarioFromPsql(final Scenario scenario, final Configuration configuration, final String scenarioName) {
		
		try {
		
			connection = PsqlAdapter.createConnection(DatabaseConstants.SIMULATIONS_DB);
			
			createNetworkFromTable(scenario.getNetwork(), scenarioName);
			createPopulationFromTable(scenario.getPopulation(), scenarioName);
			
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static void writeScenarioToPsql(final Scenario scenario, final String scenarioName, final String railsEnvironment) {
		
		try {
		
			connection = PsqlAdapter.createConnection(RailsEnvironments.valueOf(railsEnvironment).getDatabaseName());
			
//			network2Table(scenario.getNetwork(), tablespace);
			plans2Table(scenario.getPopulation(), scenarioName);
			writeScenarioMetaData(scenario, scenarioName);
			
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
				NetworkUtils.setOrigId(ll, linksSet.getString("origid"));
				NetworkUtils.setType(ll, linksSet.getString("type"));
				
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
			stmt.setString(10, NetworkUtils.getOrigId(link));
			stmt.setString(11, NetworkUtils.getType(link));
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
			
			writePlansTable(population, tablespace);
			
		} catch (SQLException e) {

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
	private static void writePlansTable(final Population population, String scenario) throws SQLException {
		
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO plans (agent_id, started_at, ended_at,"
				+ "from_activity_type, to_activity_type, location_start, location_end, mode, scenario_id)"
				+ " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);");
		
		filterTransitWalkLegs(population);
		
		for(Person person : population.getPersons().values()) {
			
			stmt.setString(1, person.getId().toString());
			
			Plan plan = person.getSelectedPlan();
			
			for(PlanElement pe : plan.getPlanElements()) {
				
				if(pe instanceof Leg) {
					
					Activity from = (Activity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)-1);
					Activity to = (Activity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)+1);
					
					Leg leg = (Leg) pe;
					
					String mode = interpretLegMode(leg.getMode());
					String fromActType = from.getType().contains(".") ? interpretActivityTypeString(from.getType()) : from.getType();
					String toActType = to.getType().contains(".") ? interpretActivityTypeString(to.getType()) : to.getType();
					
					double startTime = from.getEndTime() != org.matsim.core.utils.misc.Time.UNDEFINED_TIME ? from.getEndTime() :
						leg.getDepartureTime();
					double endTime = to.getStartTime() != org.matsim.core.utils.misc.Time.UNDEFINED_TIME ? to.getStartTime() :
						leg.getDepartureTime() + leg.getTravelTime();
					
					if(!diurnalCurves.containsKey(mode)) {
						List<Integer> list = new ArrayList<>();
						for(int i = 0; i < 24; i++) {
							list.add(0);
						}
						diurnalCurves.put(mode, list);
					}
					
					List<Integer> list = diurnalCurves.get(mode);
					int startHour = (int) startTime / 3600;
					int endHour = (int) endTime / 3600;
					if(startHour < 0 || endHour < 0 || startHour > 23 || endHour > 23)
						continue;
					for(int i = startHour; i <= endHour; i++) {
						int val = list.get(i);
						list.set(i, val+1);
					}
					diurnalCurves.put(mode, list);
					
					stmt.setTime(2, new Time(TimeUnit.SECONDS.toMillis((long)startTime)));
					stmt.setTime(3, new Time(TimeUnit.SECONDS.toMillis((long)endTime)));
					stmt.setString(4, fromActType);
					stmt.setString(5, toActType);
					stmt.setObject(6, new PGgeometry(createWKT(from.getCoord())));
					stmt.setObject(7, new PGgeometry(createWKT(to.getCoord())));
					stmt.setString(8, mode);
					stmt.setString(9, scenario);
					
					stmt.addBatch();
					
				}
				
			}
				
		}
		try {
			
			stmt.executeBatch();
			
		} catch(BatchUpdateException e) {
			System.out.println(e.getNextException().toString());
		}
		
		stmt.close();
		
	}
	
	private static Map<String, List<Integer>> diurnalCurves = new HashMap<>();
	
	private static String interpretActivityTypeString(String type) {
		
		if(type.startsWith("home"))
			return "home";
		else if(type.startsWith("work"))
			return "work";
		else if(type.startsWith("leis"))
			return "leisure";
		else if(type.startsWith("educ"))
			return "education";
		else if(type.startsWith("shop"))
			return "shop";
		else
			return "other";
		
	}
	
	private static void filterTransitWalkLegs(final Population population) {
		
		for(Person person : population.getPersons().values()) {
			
			Plan selectedPlan = person.getSelectedPlan();
			List<PlanElement> planElements = selectedPlan.getPlanElements();
			
			for (int i = 0, n = planElements.size(); i < n; i++) {
				PlanElement pe = planElements.get(i);
				if (pe instanceof Activity) {
					Activity act = (Activity) pe;
					if (PtConstants.TRANSIT_ACTIVITY_TYPE.equals(act.getType())) {
						PlanElement previousPe = planElements.get(i-1);
						if (previousPe instanceof Leg) {
							Leg previousLeg = (Leg) previousPe;
							previousLeg.setMode(TransportMode.pt);
							previousLeg.setRoute(null);
						} else {
							throw new RuntimeException("A transit activity should follow a leg! Aborting...");
						}
						final int index = i;
						PopulationUtils.removeActivity(((Plan) selectedPlan), index); // also removes the following leg
						n -= 2;
						i--;
					}
				}
			}
			
		}
		for (Person person : population.getPersons().values()){
			Plan selectedPlan = person.getSelectedPlan();
			List<PlanElement> planElements = selectedPlan.getPlanElements();
			for (int i = 0, n = planElements.size(); i < n; i++) {
				PlanElement pe = planElements.get(i);
				if (pe instanceof Leg) {
					String legMode = ((Leg) pe).getMode();
					if(legMode.equals(TransportMode.transit_walk)){
						((Leg) pe).setMode(TransportMode.walk);
					}
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @param coord
	 * @return
	 */
	private static String createWKT(Coord coord) {
		
		return "POINT(" + Double.toString(coord.getX()) + " " + Double.toString(coord.getY()) + ")";
		
	}
	
	private static String interpretLegMode(String mode) {
		
		if(mode.contains("oneway") || mode.contains("twoway") || mode.contains("freefloat")) {
			return "carsharing";
		} else {
			return mode;
		}
		
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
	
	static void writeScenarioMetaData(final Scenario scenario, String scenarioId) {
		
		try {
		
			AggregatedAnalysis.generate(scenario);
			
			Map<String, String> modeCounts = AggregatedAnalysis.getModeCounts();
			Map<String, String> modeDistances = AggregatedAnalysis.getModeDistanceStats();
			Map<String, String> modeEmissions = AggregatedAnalysis.getModeEmissionStats();
			
			PreparedStatement statement = connection.prepareStatement("INSERT INTO scenarios (district_id, year, population,"
					+ " population_diff_2017,person_km, trips, diurnal_curve, carbon_emissions, seed, created_at, updated_at) "
					+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
		
			String[][] diurnalCurves = new String[modeDistances.size()*24][3];
			int i = 0;
			for(Entry<String, List<Integer>> entry : MatsimPsqlAdapter.diurnalCurves.entrySet()) {
				
				int j = 0;
				
				for(Integer integer : entry.getValue()) {
					
					diurnalCurves[i][0] = entry.getKey();
					diurnalCurves[i][1] = Integer.toString(j);
					diurnalCurves[i][2] = Integer.toString(integer);
					
					j++;
					i++;
					
				}
				
			}
			
			String[] scenarioData = scenarioId.split("_");
			
			statement.setString(1, scenarioData[0]);
			statement.setInt(2, Integer.parseInt(scenarioData[1]));
			statement.setInt(3, scenario.getPopulation().getPersons().size());
			statement.setInt(4, 0);
			statement.setArray(5, connection.createArrayOf("varchar", createArrayFromMap(modeDistances)));
			statement.setArray(6, connection.createArrayOf("varchar", createArrayFromMap(modeCounts)));
			statement.setArray(7, connection.createArrayOf("varchar", diurnalCurves));
			statement.setArray(8, connection.createArrayOf("varchar", createArrayFromMap(modeEmissions)));
			statement.setBoolean(9, false);
			statement.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
			statement.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
			
			statement.addBatch();
			
			try {
				
				statement.executeBatch();
				
			} catch(BatchUpdateException e) {
				
				System.out.println(e.getNextException().toString());
			
			}
			
			statement.close();
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private static String[][] createArrayFromMap(Map<String, String> map) {

		String[][] array = new String[map.size()][2]; 
		
		int i = 0;
		for(Entry<String, String> entry : map.entrySet()) {
			
			array[i][0] = entry.getKey();
			array[i][1] = entry.getValue();
			i++;
			
		}
		
		return array;
		
	}
	
}