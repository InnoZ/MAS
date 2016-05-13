package playground.dhosse.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.io.IOUtils;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.utils.SshConnector;

import com.jcraft.jsch.JSchException;

/**
 * This class transfers MATSim simulation data into a postgreSQL database.
 * 
 * @author dhosse
 *
 */
public class DatabaseUpdater {

	private static final Logger log = Logger.getLogger(DatabaseUpdater.class);
	
	private Scenario scenario;
	
	/**
	 * 
	 * @param configuration The configuration file.
	 * @param scenario The MATSim scenario containing
	 * @param databaseSchemaName The identifier of the database namespace (schema).
	 * @param intoMobilityDatahub Defines if the simulation data should be written into the MobilityDatahub
	 * or into a local database
	 * 
	 */
	public void update(Configuration configuration, Scenario scenario, String databaseSchemaName,
			boolean intoMobilityDatahub){
		
		this.scenario = scenario;
		this.writeIntoDatabase(configuration, databaseSchemaName, intoMobilityDatahub);
		
	}

	/**
	 * 
	 * Write MATSim simulation output into the mobility database.
	 * 
	 * @param args
	 */
	public void writeIntoDatabase(Configuration configuration, String databaseSchemaName,
			boolean intoMobilityDatahub){
		
		try {
			
			//create ssh tunnel to the playground (if wanted)
			if(intoMobilityDatahub){
				SshConnector.connect(configuration);
			}
			
			//instantiate a postgresql driver and establish a connection to the mobility database
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" +
					configuration.getLocalPort() + "/simulated_mobility",
					configuration.getDatabaseUsername(), configuration.getPassword());
		
			//if the connection...
			if(connection != null){
				
				log.info("Connection to mobility database established.");
				
				//...could be established - proceed
				//TODO these don't need to be updated for every scenario dump. on/off switch?
				processPersons(connection, databaseSchemaName);
				processPlans(connection, databaseSchemaName);
				
			}
			
			//after everything is finished, close the connection
			connection.close();
		
		} catch (JSchException | IOException | InstantiationException | IllegalAccessException |
				ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
			
		}
		
		finally{

			//this needs to be done because otherwise the ssh tunnel would stay open...
			log.info("Closing SSH tunnel and exiting...");
			
			System.exit(0);
			
		}
		
	}

	private void processPersons(Connection connection, String databaseSchemaName) throws SQLException{
		
		log.info("Creating persons table and inserting the values found in the given scenario...");
		
		Statement statement = connection.createStatement();
		
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + databaseSchemaName + ";");
		statement.executeUpdate("DROP TABLE IF EXISTS \"" + databaseSchemaName + "\".persons;");
		statement.executeUpdate("CREATE TABLE \"" + databaseSchemaName + "\".persons(id character varying,"
				+ "sex character varying,age integer,car_available character varying, has_driving_license"
				+ " boolean DEFAULT FALSE);");

		for(Person person : scenario.getPopulation().getPersons().values()){
			
			String id = person.getId().toString();
			String sex = (String) scenario.getPopulation().getPersonAttributes().getAttribute(id, "SEX");
			String age = (String) scenario.getPopulation().getPersonAttributes().getAttribute(id, "AGE");
			String carAvail = (String) scenario.getPopulation().getPersonAttributes().getAttribute(id, "CAR_AVAIL");
			String hasLicense = (String) scenario.getPopulation().getPersonAttributes().getAttribute(id, "LICENSE");
			
			if(sex == null) sex = "";
			else if(sex.equals("0")) sex = "m";
			else if(sex.equals("1")) sex = "f";
			
			if(age == null) age = "-1";
			
			if(carAvail == null) carAvail = "never";
			else if(carAvail.equals("true")) carAvail = "always";
			else if(carAvail.equals("false")) carAvail = "never";
			
			if(hasLicense == null) hasLicense = "false";
			
			statement.executeUpdate("INSERT INTO \"" + databaseSchemaName + "\".persons VALUES('"
					+ id + "','" + sex + "'," + Integer.parseInt(age) + ",'" + carAvail + "',"
					+ Boolean.parseBoolean(hasLicense) + ");");
			
		}
		
		statement.close();
		
		log.info("Done.");
		
	}
	
	private void processPlans(Connection connection, String databaseSchemaName)
			throws SQLException{
		
		log.info("Inserting trips from persons' selected plans into mobility database...");
		
		Statement statement = connection.createStatement();
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + databaseSchemaName + ";");
		statement.executeUpdate("DROP TABLE IF EXISTS \"" + databaseSchemaName + "\".trips;");
		statement.executeUpdate("CREATE TABLE \"" + databaseSchemaName + "\".trips(person_id character varying,"
				+ "trip_index integer, travel_time numeric, distance numeric, departure_time numeric,"
				+ " arrival_time numeric, from_act_type character varying, from_x numeric, from_y numeric,"
				+ " to_act_type character varying, to_x numeric, to_y numeric, main_mode character varying,"
				+ " access_time numeric, access_distance numeric, egress_time numeric, egress_distance numeric);");
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			int tripCounter = 1;
			Plan plan = person.getSelectedPlan();
			
			for(int i = 0; i < plan.getPlanElements().size(); i++){
				
				PlanElement pe = plan.getPlanElements().get(i);
				
				if(pe instanceof Leg){
					
					Leg leg = (Leg)pe;
					Activity fromAct = (Activity)plan.getPlanElements().get(i - 1);
					Activity toAct = (Activity)plan.getPlanElements().get(i + 1);

//					String geometry = ""; TODO
					String personId = person.getId().toString();
					String tripIndex = Integer.toString(tripCounter);
					String travelTime = Double.toString(leg.getTravelTime());
					String distance = Double.toString(leg.getRoute().getDistance());
					String startTime = Double.toString(fromAct.getEndTime());
					String endTime = Double.toString(fromAct.getEndTime() + leg.getTravelTime());
					String fromActType = getActType(fromAct);
					String fromX = Double.toString(fromAct.getCoord().getX());
					String fromY = Double.toString(fromAct.getCoord().getY());
					String toActType = getActType(toAct);
					String toX = Double.toString(toAct.getCoord().getX());
					String toY = Double.toString(toAct.getCoord().getY());
					String mainMode = leg.getMode();
					String accessTime = "0";
					String accessDistance = "0";
					String egressTime = "0";
					String egressDistance = "0";
					
					if(leg.getMode().equals("transit_walk")){
						
						if(toAct.getType().equals("pt interaction")){
							
							Leg accessLeg = leg;
							accessTime = Double.toString(accessLeg.getTravelTime());
							accessDistance = Double.toString(accessLeg.getRoute().getDistance());
							
							Leg mainModeLeg = (Leg)plan.getPlanElements().get(i + 2);
							mainMode = "pt";
							startTime = Double.toString(fromAct.getEndTime() + accessLeg.getTravelTime());
							endTime = Double.toString(fromAct.getEndTime() + accessLeg.getTravelTime() 
									+ mainModeLeg.getTravelTime());
							distance = Double.toString(mainModeLeg.getRoute().getDistance());
							
							travelTime = Double.toString(mainModeLeg.getTravelTime());
							
							Leg egressLeg = (Leg)plan.getPlanElements().get(i + 4);
							egressTime = Double.toString(egressLeg.getTravelTime());
							egressDistance = Double.toString(egressLeg.getRoute().getDistance());
							
							toAct = (Activity)plan.getPlanElements().get(i + 5);
							toActType = getActType(toAct);
							
							toX = Double.toString(toAct.getCoord().getX());
							toY = Double.toString(toAct.getCoord().getY());
							
							i += 5;
							
						} else {
							
							mainMode = "walk";
							
						}
						
					}
					
					if(startTime.contains("Infinity")){
						startTime = "'NaN'";
					}
					if(endTime.contains("Infinity")){
						endTime = "'NaN'";
					}
					if(travelTime.contains("Infinity")){
						travelTime = "'NaN'";
					}
					if(accessTime.contains("Infinity")){
						accessTime = "'NaN'";
					}
					if(accessDistance.contains("NaN")){
						accessDistance = "'NaN'";
					}
					if(egressTime.contains("Infinity")){
						egressTime = "'NaN'";
					}
					if(egressDistance.contains("NaN")){
						egressDistance = "'NaN'";
					}
					
					statement.executeUpdate("INSERT INTO \"" + databaseSchemaName + "\".trips VALUES('"
							+ personId + "'," + tripIndex + "," + travelTime + "," + distance + "," +
							startTime + "," + endTime + ",'" + fromActType + "'," + fromX + "," + fromY +
							",'" + toActType + "'," + toX + "," + toY + ",'" + mainMode + "'," + accessTime +
							"," + accessDistance + "," + egressTime + "," + egressDistance + ");");
					
				}
				
			}
			
		}
		
		statement.close();
		
		log.info("Done.");
		
	}
	
	private static String getActType(Activity activity){
		
		if(activity.getType().contains("home")){
			return "home";
		} else if(activity.getType().contains("work")){
			return "work";
		} else if(activity.getType().contains("educ")){
			return "education";
		} else if(activity.getType().contains("shop")){
			return "shopping";
		} else{
			return "other";
		}
		
	}
	
	private static void processVehicles(Connection connection, Network network, String vehicleLocationsFile) throws IOException, SQLException{

		log.info("Inserting carsharing vehicle locations into mobility database...");
		
		BufferedReader reader = IOUtils.getBufferedReader(vehicleLocationsFile);
		
		String line = reader.readLine();
		
		List<CsStation> stations = new ArrayList<CsStation>();
		
		while((line = reader.readLine()) != null){
			
			String[] l = line.split("\t");
			CsStation station = new CsStation();
			station.id = l[0];
			station.name = l[1];
			station.x = Double.parseDouble(l[2]);
			station.y = Double.parseDouble(l[3]);
			station.nVehicles = Integer.parseInt(l[6]);
			station.linkId = NetworkUtils.getNearestLink(network, new CoordImpl(station.x, station.y))
					.getId().toString();
			stations.add(station);
			
		}
		
		reader.close();
		
		Statement statement = connection.createStatement();
		statement.executeUpdate("DROP TABLE IF EXISTS \"garmisch-partenkirchen\".carsharing_stations_extended;");
		statement.executeUpdate("CREATE TABLE \"garmisch-partenkirchen\".carsharing_stations_extended(station_id character varying,"
				+ "x numeric, y numeric, link_id character varying, name character varying, n_vehicles integer);");
		
		for(CsStation station : stations){
			
			statement.executeUpdate("INSERT INTO \"garmisch-partenkirchen\".carsharing_stations_extended VALUES('"
					+ station.id + "'," + station.x + "," + station.y + ",'" + station.linkId + "','" + station.name + "'," +
					station.nVehicles + ");");
			
		}
		
		statement.close();
		
		log.info("Done.");
		
	}
	
	static class CsStation{

		private String id;
		private String name;
		private double x;
		private double y;
		private int nVehicles;
		private String linkId;
		
	}
	
}