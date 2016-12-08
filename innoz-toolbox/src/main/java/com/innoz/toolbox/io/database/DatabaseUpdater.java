package com.innoz.toolbox.io.database;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.groups.PsqlConfigurationGroup.OutputTablesParameterSet;
import com.innoz.toolbox.scenarioGeneration.population.utils.PersonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.misc.Time;
import org.matsim.utils.objectattributes.ObjectAttributes;

/**
 * This class transfers MATSim simulation data into a postgreSQL database.
 * 
 * @author dhosse
 *
 */
public class DatabaseUpdater {

	private static final Logger log = Logger.getLogger(DatabaseUpdater.class);
	
	private Scenario scenario;
	private String vehiclesFile = null;
	
	/**
	 * 
	 * @param configuration The configuration file.
	 * @param scenario The MATSim scenario containing
	 * @param databaseSchemaName The identifier of the database namespace (schema).
	 * @param intoMobilityDatahub Defines if the simulation data should be written into the MobilityDatahub
	 * or into a local database
	 * 
	 */
	public void update(Configuration configuration, Scenario scenario, String vehiclesFile){
		
		this.scenario = scenario;
		this.vehiclesFile = vehiclesFile;
		this.writeIntoDatabase(configuration);
		
	}

	/**
	 * 
	 * Write MATSim simulation output into the mobility database.
	 * 
	 * @param configuration The scenario generation configuration.
	 * @param databaseSchemaName The schema name (namespace) of the database tables to be written.
	 * @param intoMobilityDatahub Defines if the MATSim data should be written directly into the MobilityDatahub or into a local database.
	 */
	public void writeIntoDatabase(Configuration configuration){
		
		// Initialize database, user, password and port for the MobilityDatahub
		String dbName = DatabaseConstants.SIMULATIONS_DB;
		String dbUser = configuration.psql().getPsqlUser();
		String dbPassword = configuration.psql().getPsqlPassword();
		int localPort = configuration.psql().getPsqlPort();
		
		try {
			
			if(!configuration.psql().isWritingIntoMobilityDatahub()){
				
				// Change these parameters if the tables are only written into a local database
				dbName = DatabaseConstants.SIMULATIONS_DB_LOCAL;
				dbUser = DatabaseConstants.DEFAULT_USER;
				dbPassword = DatabaseConstants.DEFAULT_PASSWORD;
				localPort = 5432;
				
				StringBuffer output = new StringBuffer();
				Process p;
				
				try {

					// Check, if the simulations database exists locally
					p = Runtime.getRuntime().exec("psql -lqt");
					p.waitFor();
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = "";
			        while ((line = br.readLine())!= null) {
			            output.append(line + "\n");
			        }
					
				} catch (IOException | InterruptedException e) {
					
					e.printStackTrace();
					
				}
				
				if(!output.toString().contains(dbName)){
					
					// Create a new local database if it doesn't exist already
					p = Runtime.getRuntime().exec("createdb -p 5432 -e " + dbName);
					
				}
				
			}
			
			// Instantiate a postgreSQL driver and establish a connection to the database
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" +
					localPort + "/" + dbName, dbUser, dbPassword);
		
			// If the connection...
			if(connection != null){
				
				log.info("Connection to database established.");
				
				OutputTablesParameterSet ot = (OutputTablesParameterSet)configuration.psql().getOutputTablesParameterSet();
				
				// ...could be established - proceed
				if(scenario.getPopulation().getPersons().size() > 0){

					if(scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES) != null){

						processPersons(connection, ot.getSchemaName());
							
					}
						
					processPlans(connection, ot.getSchemaName(), ot.getScenarioName());
					
				}
			
				if(this.vehiclesFile != null && scenario.getNetwork().getLinks().size() > 0) {
					
					processVehicles(connection, scenario.getNetwork(), this.vehiclesFile, ot.getSchemaName(),
							ot.getScenarioName());
					
				}
				
			}
			
			// After everything is finished, close the connection
			connection.close();
		
		} catch (IOException | InstantiationException | IllegalAccessException |
				ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
			
		}
		
	}

	/**
	 * 
	 * Creates a database table containing all persons in the MATSim population.
	 * 
	 * @param connection The database connection.
	 * @param databaseSchemaName The schema name (namespace) of the database tables to be written.
	 * @throws SQLException
	 */
	private void processPersons(Connection connection, String databaseSchemaName) throws SQLException {
		
		log.info("Creating persons table and inserting the values found in the given scenario...");
		
		Statement statement = connection.createStatement();
			
		// Create the schema only if it doesn't exist already
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS \"" + databaseSchemaName + "\";");
		// Drop the old table
		statement.executeUpdate("DROP TABLE IF EXISTS \"" + databaseSchemaName + "\".persons;");
		// Create a new database table
		statement.executeUpdate("CREATE TABLE \"" + databaseSchemaName + "\".persons(id character varying,"
				+ "sex character varying,age integer,car_available boolean DEFAULT FALSE, has_driving_license"
				+ " boolean DEFAULT FALSE, is_emplyed boolean DEFAULT FALSE);");

		long counter = 0;
		int nextMsg = 1;
		
		// Write new columns for all persons in the MATSim population
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			String id = person.getId().toString();
			String sex = (String) ((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES)).getAttribute(id, PersonUtils.ATT_SEX);
			Integer age = (Integer) ((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES)).getAttribute(id, PersonUtils.ATT_AGE);
			String carAvail = (String ) ((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES)).getAttribute(id, PersonUtils.ATT_CAR_AVAIL);
			String hasLicense = (String) ((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES)).getAttribute(id, PersonUtils.ATT_LICENSE);
			Boolean isEmployed = (Boolean) ((ObjectAttributes) scenario.getScenarioElement(PersonUtils.PERSON_ATTRIBUTES)).getAttribute(id, PersonUtils.ATT_EMPLOYED);
			
			if(sex == null) sex = "";
			else if(sex.equals("0")) sex = "m";
			else if(sex.equals("1")) sex = "f";
			
			if(age == null) age = -1;
			
			if(carAvail == null) carAvail = "false";
			else if(carAvail.equals("always") || carAvail.equals("sometimes")) carAvail = "true";
			else if(carAvail.equals("never")) carAvail = "false";
			
			if(hasLicense == null) hasLicense = "false";
			else if(hasLicense.equals("yes")) hasLicense = "true";
			else if(hasLicense.equals("no")) hasLicense = "false";
			
			if(isEmployed == null) isEmployed = false;
			
			statement.executeUpdate("INSERT INTO \"" + databaseSchemaName + "\".persons VALUES('"
					+ id + "','" + sex + "'," + age + ",'" + Boolean.parseBoolean(carAvail) + "'," + Boolean.parseBoolean(hasLicense)
					+ "," + isEmployed + ");");
			
			counter++;
			if(counter % nextMsg == 0){
				log.info("person # " + counter);
				nextMsg *= 2;
			}
			
		}
		
		// Close the statement after all persons have been processed
		statement.close();
		
		log.info("Done.");
		
	}
	
	/**
	 * 
	 * Creates a database table containing all trips of all persons' selected plans.
	 * 
	 * @param connection The database connection.
	 * @param databaseSchemaName
	 * @throws SQLException
	 */
	private void processPlans(Connection connection, String databaseSchemaName, String tableSuffix)
			throws SQLException{
		
		log.info("Inserting trips from persons' selected plans into database...");
		
		Statement statement = connection.createStatement();
		// Create the schema only if it doesn't exist already
		statement.executeUpdate("CREATE SCHEMA IF NOT EXISTS \"" + databaseSchemaName + "\";");
		// Drop the old table
		statement.executeUpdate("DROP TABLE IF EXISTS \"" + databaseSchemaName + "\".trips_" + tableSuffix + ";");
		// Create a new database table
		statement.executeUpdate("CREATE TABLE \"" + databaseSchemaName + "\".trips_" + tableSuffix + "(person_id character varying,"
				+ "trip_index integer, travel_time numeric, distance numeric, departure_time numeric,"
				+ " arrival_time numeric, from_act_type character varying, from_x numeric, from_y numeric,"
				+ " to_act_type character varying, to_x numeric, to_y numeric, main_mode character varying,"
				+ " access_time numeric, access_distance numeric, egress_time numeric, egress_distance numeric);");

		long counter = 0;
		int nextMsg = 1;
		
		for(Person person : scenario.getPopulation().getPersons().values()){
			
			int tripCounter = 1;
			Plan plan = person.getSelectedPlan();
			
			// Process all plan elements and write the legs into the database
			for(int i = 0; i < plan.getPlanElements().size(); i++){
				
				PlanElement pe = plan.getPlanElements().get(i);
				
				if(pe instanceof Leg){
					
					Leg leg = (Leg)pe;
					Activity fromAct = (Activity)plan.getPlanElements().get(i - 1);
					Activity toAct = (Activity)plan.getPlanElements().get(i + 1);

//					String geometry = ""; TODO
					String personId = person.getId().toString();
					String tripIndex = Integer.toString(tripCounter);
					String travelTime = leg.getTravelTime() == Time.UNDEFINED_TIME ? "'NaN'" : Double.toString(leg.getTravelTime());
					String distance = leg.getRoute() != null ? Double.toString(leg.getRoute().getDistance()): "0";
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
					
					// If the mode is "transit walk" check if it's an access or egress leg
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
							
							travelTime = travelTime.equals("undefined") ? "'NaN'" : Double.toString(mainModeLeg.getTravelTime());
							
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
					if(distance.equals("NaN")){
						distance = "'NaN'";
					}

					statement.executeUpdate("INSERT INTO \"" + databaseSchemaName + "\".trips_" + tableSuffix + " VALUES('"
							+ personId + "'," + tripIndex + "," + travelTime + "," + distance + "," +
							startTime + "," + endTime + ",'" + fromActType + "'," + fromX + "," + fromY +
							",'" + toActType + "'," + toX + "," + toY + ",'" + mainMode + "'," + accessTime +
							"," + accessDistance + "," + egressTime + "," + egressDistance + ");");
					
					tripCounter++;
					
				}
				
			}
			
			counter++;
			if(counter % nextMsg == 0){
				log.info("person # " + counter);
				nextMsg *= 2;
			}
			
		}
		
		// Close the statement after all trips have been processed
		statement.close();
		
		log.info("Done.");
		
	}
	
	/**
	 * 
	 * Identifies the activity type of the MATSim activity. This is useful since sometimes, activity types are classified and named by their
	 * typical duration, e.g. home10.0 for a 10 hrs home activity.
	 * 
	 * @param activity The MATSim activity
	 * @return
	 */
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
	
	/**
	 * 
	 * Writes car sharing stations into a database.
	 * 
	 * @param connection
	 * @param network
	 * @param vehicleLocationsFile
	 * @throws IOException
	 * @throws SQLException
	 */
	private static void processVehicles(Connection connection, Network network, String vehicleLocationsFile,
				String databaseSchemaName, String tableSuffix) throws IOException, SQLException{

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
			station.linkId = NetworkUtils.getNearestLink(network, new Coord(station.x, station.y))
					.getId().toString();
			stations.add(station);
			
		}
		
		reader.close();
		
		Statement statement = connection.createStatement();
		statement.executeUpdate("DROP TABLE IF EXISTS \"" + databaseSchemaName + "\".carsharing_stations_" + tableSuffix + ";");
		statement.executeUpdate("CREATE TABLE \"" + databaseSchemaName + "\",carsharing_stations_" + tableSuffix + "(station_id character varying,"
				+ "x numeric, y numeric, link_id character varying, name character varying, n_vehicles integer);");
		
		for(CsStation station : stations){
			
			statement.executeUpdate("INSERT INTO \"" + databaseSchemaName + "\".carsharing_stations_" + tableSuffix + " VALUES('"
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