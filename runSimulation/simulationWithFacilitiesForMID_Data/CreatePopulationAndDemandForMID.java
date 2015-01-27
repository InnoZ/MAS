package simulationWithFacilitiesForMID_Data;

import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.FacilitiesReaderMatsimV1;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;

public class CreatePopulationAndDemandForMID {
	
	private final static Logger log = Logger.getLogger(CreatePopulationAndDemandForMID.class);
	private Scenario scenario;
	
//	private String facilitiesFile = "./input/facilities.xml.gz";
	private String networkFile = "./input/networks/network_bayern.xml";
	
	// --------------------------------------------------------------------------
	public static void main(String[] args) {
		CreatePopulationAndDemandForMID creator = new CreatePopulationAndDemandForMID();
		creator.run();		
	}
	
	private void run() {
		this.init();
		CreateFacilitiesGAP_MID_Data facilityCreator = new CreateFacilitiesGAP_MID_Data();
		facilityCreator.initAndRun(this.scenario);
		CreatePopulationWithMID_Data populationCreator = new CreatePopulationWithMID_Data();
		populationCreator.run(this.scenario, facilityCreator.getIDs());
		//CreateDemandWithMID_Data demandCreator = new CreateDemandWithMID_Data();
		//demandCreator.run(this.scenario, populationCreator.getPersonHomeLocations(), facilityCreator.getCapacities());
		this.write();
	}
	
	private void init() {
		/*
		 * Create the scenario
		 */
		Config config = ConfigUtils.createConfig();
		this.scenario = ScenarioUtils.createScenario(config);
		/*
		 * Read the network and store it in the scenario
		 */
		new MatsimNetworkReader(this.scenario).readFile(networkFile);
		/*
		 * Read the facilities and store them in the scenario
		 */
	//	new FacilitiesReaderMatsimV1((ScenarioImpl)this.scenario).readFile(this.facilitiesFile);	
	}
	
	private void write() {
		PopulationWriter populationWriter = new PopulationWriter(this.scenario.getPopulation(), this.scenario.getNetwork());
		populationWriter.write("./output/plans.xml.gz");
		log.info("Number of persons: " + this.scenario.getPopulation().getPersons().size());
	}
}
