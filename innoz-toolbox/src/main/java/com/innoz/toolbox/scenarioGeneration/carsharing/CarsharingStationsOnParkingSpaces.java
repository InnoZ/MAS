package com.innoz.toolbox.scenarioGeneration.carsharing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.contrib.carsharing.vehicles.StationBasedVehicle;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.postgis.PGgeometry;
import org.postgis.Point;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.FFEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.OnewayEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.TwoWayEntry;
import com.innoz.toolbox.scenarioGeneration.carsharing.CreateCarsharingVehicles.VehicleEntry;
import com.innoz.toolbox.utils.GlobalNames;

/**
 * 
 * Class that builds a carsharing system in the defined survey area. On each parking space, carsharing stations / vehicles are
 * generated using OpenStreetMap landuse data.
 * 
 * @author dhosse
 *
 */
public class CarsharingStationsOnParkingSpaces {
	
	/**
	 * Example usage in 3connect context.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String args[]) throws IOException {
//		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//		new PopulationReader(scenario).readFile("/home/dhosse/scenarios/3connect/scenarios2025/plans_2025.xml.gz");
//		MembershipContainer container = new MembershipContainer();
//		scenario.getPopulation().getPersons().values().stream().forEach(person -> {
//			Map<String, Set<String>> membershipsPerCompany = new HashMap<>();
//			if(person.getAttributes().getAttribute("hasLicense") != null && 
//					person.getAttributes().getAttribute("hasLicense").equals("yes")) {
//				membershipsPerCompany.put("stadtteilauto", new HashSet<>(Arrays.asList(new String[] {"twoway","oneway","freefloating"})));
//			} else {
//				membershipsPerCompany.put("stadtteilauto", new HashSet<>());
//			}
//			Map<String, Set<String>> membershipsPerCSType = new HashMap<>();
//			PersonMembership personMembership = new PersonMembership(membershipsPerCompany, membershipsPerCSType);
//			container.addPerson(person.getId().toString(), personMembership);
//		});
//		new CSMembersXmlWriter(container).writeFile("/home/dhosse/scenarios/3connect/carsharingMembers.xml");
		CarsharingStationsOnParkingSpaces.generate("stadtteilauto", "/home/dhosse/scenarios/3connect/carsharingVehiclesParking.xml");
		
	}
	
	/**
	 * 
	 * Generates carsharing stations on public parking spaces inside the survey area and writes these stations / vehicles into a
	 * MATSim charsharing vehicles file.
	 * 
	 * @param companyName A string representing the name of the carsharing company one wants to generate vehicles for.
	 * @param pathToOutputFile The path to the output carsharing vehicles xml file.
	 * @throws IOException 
	 */
	public static void generate(String companyName, String pathToOutputFile) throws IOException {
		
		// The map that stores the new carsharing vehicles. The vehicle positions are mapped to the company name string.
		Map<String, Map<Coord, VehicleEntry>> vehicles = new HashMap<>();
		vehicles.put(companyName, new HashMap<>());
		
		// Internal vehicle counter
		int vehicleCounter = 0;
		
		try {
			
			// Set up the psql port for port forwarding
			Controller.configuration().psql().setPsqlPort(9999);
			
			// Create a new connection to the geodata database
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
			
			Statement statement = connection.createStatement();
			
			String q = "SELECT st_astext(geom) as wkt FROM gadm.districts where cca_2='03404';";
			ResultSet set = statement.executeQuery(q);
			String geom = null;
			while(set.next()) {
				geom = set.getString("wkt");
			}
			set.close();
			
			// Build an sql query that receives all parking amenities from the OpenStreetMap table which lie within
			// the geometry of the survey area
//			String geom = Geoinformation.getInstance().getSurveyAreaBoundingBox().toText();
			String query = "SELECT st_centroid(way) as centroid FROM osm.osm_germany_polygon WHERE amenity='parking' AND st_contains("
					+ "st_geomfromtext('" + geom + "',4326), way);";
			
			ResultSet results = statement.executeQuery(query);

			CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(GlobalNames.WGS84, GlobalNames.UTM32N);
			
			while(results.next()) {
				
				// The geometry should only contain one point since we queried for the centroids of the geometries
				PGgeometry geometry = (PGgeometry)results.getObject("centroid");
				Point point = geometry.getGeometry().getPoint(0);
				
				// Convert the point objects into coords
				// We need two coordinates here because the map entries are each mapped to their location and would overwrite each
				// other
				Coord coord = ct.transform(new Coord(point.x, point.y));
				Coord ffcoord = new Coord(coord.getX()+1, coord.getY()+1);
				Coord owCoord = new Coord(coord.getX()-1, coord.getY()-1);
				
				// The actual generation of the carsharing vehicles (twoway and freefloating)
				// This part is specifically written for 3connect and could / should be changed in other project contexts
				TwoWayEntry twEntry = new TwoWayEntry("twoway_" + vehicles.get(companyName).size());
				twEntry.c = coord;
				twEntry.vehicles.add(new StationBasedVehicle("car", twEntry.id + "_" + vehicleCounter++, twEntry.id, "twoway",
						companyName));
				twEntry.vehicles.add(new StationBasedVehicle("car", twEntry.id + "_" + vehicleCounter++, twEntry.id, "twoway",
						companyName));
				vehicles.get(companyName).put(coord, twEntry);
				
				OnewayEntry owEntry = new OnewayEntry("oneway_" + vehicles.get(companyName).size());
				owEntry.c = owCoord;
				owEntry.vehicles.add(new StationBasedVehicle("car", owEntry.id + "_" + vehicleCounter++, owEntry.id, "oneway", companyName));
				owEntry.freeparking = 2;
				vehicles.get(companyName).put(owCoord, owEntry);
				
				vehicles.get(companyName).put(ffcoord, new FFEntry("freefloating_" + vehicleCounter++, coord, "car"));
				
			}
			
			results.close();
			statement.close();
			connection.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			
			e.printStackTrace();
			
		}
		
		// When everything's finished, write the vehicles to an xml file
		CarsharingVehiclesWriter writer = new CarsharingVehiclesWriter();
		writer.write(pathToOutputFile, vehicles);
		writer.createSurveyAreaFromShapefile("/home/dhosse/01_Projects/3connect/service_area/serviceArea_stufe2.shp", "/home/dhosse/scenarios/3connect/serviceArea_extended.xml");
		
	}
	
}