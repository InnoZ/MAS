package simulationWithFacilitiesForMID_Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.FacilitiesReaderMatsimV1;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import Mathfunctions.Calculator;

public class InitializeTest {
	
	private Scenario scenario;
	private Scenario facilitiesScenario;
	private String facilitiesFile = "./input/facilitiesGAP_mitHome.xml";
	private String networkFile = "./input/networks/network_bayern.xml";
	private Map<Id,Integer> capacities = new HashMap<Id, Integer>();

	private void initForTesting() {
		/*
		 * Create the scenario
		 */
		Config config = ConfigUtils.createConfig();
		this.scenario = ScenarioUtils.createScenario(config);		
		this.facilitiesScenario = ScenarioUtils.createScenario(config);
		/*
		 * Read the network and store it in the scenario
		 */
		new MatsimNetworkReader(this.scenario).readFile(networkFile);
		/*
		 * Read the facilities from facilities File and store them in the scenario
		 */
		new FacilitiesReaderMatsimV1((ScenarioImpl)this.scenario).readFile(facilitiesFile);
		/*
		 * Read the facilities in facilities Object and store them in the facilitiesScenario
		 */
		CreateFacilitiesGAP_MID_Data facilityCreator = new CreateFacilitiesGAP_MID_Data();
		facilityCreator.initAndRun(this.facilitiesScenario);
		this.capacities = facilityCreator.getCapacities();
	}
	
	private void printFacilities(){
		
		Map<Id,ActivityFacility> testFromFile = ((ScenarioImpl)this.scenario).getActivityFacilities().getFacilities();
		System.out.println("LIES FACILITIES AUS DER DATEI");
		for (Id id : testFromFile.keySet()) {
			System.out.println("fac.Id: " + id + " Koordinaten: " + testFromFile.get(id).getCoord().toString());
		}
		System.out.println("LIES FACILITIES IM OBJEKT UND SPEICHERE DIREKT IN SC. OHNE DATEI");
		Map<Id,ActivityFacility> testFromObject = ((ScenarioImpl)this.scenario).getActivityFacilities().getFacilities();
		for (Id id : testFromObject.keySet()) {
			System.out.println("fac.Id: " + id + " Koordinaten: " + testFromObject.get(id).getCoord().toString());
		}
	}
	public static void main(String[] args) {
		InitializeTest test = new InitializeTest();
		test.initForTesting();
		//test.printFacilities();
		
		CreateDemandWithMID_Data cd = new CreateDemandWithMID_Data();
		cd.scenario = test.scenario;
		Calculator calc = new Calculator();

		CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation("EPSG:4326", "EPSG:32632");

		Coord testcoord = new CoordImpl(11.1033, 47.4990);
		Coord coord = ct.transform(testcoord);
	//	System.out.println("testCoord: " + coord.toString());
		ActivityFacility facility = null;
		double dist = 1.43*1000;
		double km = calc.calculateDistanceInMeter("28");
		double km1 = calc.calculateDistanceInMeter("28,5");
		double km2 = calc.calculateDistanceInMeter("28,53");
		double km3 = calc.calculateDistanceInMeter("28,53,4");
	//	System.out.println("km: " + km + " km1: " + km1 + " km2: " + km2 + " km3: " + km3);
		//facility = cd.chooseFacility("work", coord, dist);
		double time = calc.calculateTimeInSeconds("07:02:00");
	//	System.out.println("TIME: " + time);
		double duration = calc.calculateDurationInMinutes("07:02:00", "08:05:00");
		System.out.println("duration: " + duration);		
	}

}
