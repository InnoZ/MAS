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
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

public class CreatePopulationAndDemandForMID {

	private final static Logger log = Logger
			.getLogger(CreatePopulationAndDemandForMID.class);
	private Scenario scenario;

	// private String facilitiesFile = "./input/facilities.xml.gz";
	private String networkFile = "./input/networks/network_bayern.xml";

	// --------------------------------------------------------------------------
	public static void main(String[] args) {
		CreatePopulationAndDemandForMID creator = new CreatePopulationAndDemandForMID();
		creator.run();
	}

	/*
	 * Build a scenario, read facilities and store them in the scenario. Also
	 * store workplace-capacities in a Map. Run CreatePopulationWithMID_Data with
	 * facilities and then run CreateDemandWithMID_Data with PersonHomeLocations
	 * and the capacitiesMap. At last write ObjectAttributes-files for
	 * personsActivityDurationsDesires and personsCS_CardExistence.
	 */
	private void run() {
		this.init();
		CreateFacilitiesGAP_MID_Data facilityCreator = new CreateFacilitiesGAP_MID_Data();
		facilityCreator.initAndRun(this.scenario);
		CreatePopulationWithMID_Data populationCreator = new CreatePopulationWithMID_Data();
		populationCreator.run(this.scenario, facilityCreator.getIDs());
		CreateDemandWithMID_Data demandCreator = new CreateDemandWithMID_Data();
		demandCreator.run(this.scenario,
				populationCreator.getPersonHomeLocations(),
				populationCreator.getErrorPersons(), facilityCreator.getCapacities());

		ObjectAttributes personsDesires = new ObjectAttributes();
		personsDesires = demandCreator.getPersonsActDurDesire();
		System.out.println(demandCreator.getPersonsActDurDesire().toString());
		new ObjectAttributesXmlWriter(personsDesires)
				.writeFile("./input/Attributes/agentAttributesDesires.xml");
		ObjectAttributes personsCS_CardExistence = new ObjectAttributes();
		personsCS_CardExistence = demandCreator.getPersonsCS_CardExistence();
		System.out.println(demandCreator.getPersonsCS_CardExistence().toString());
		new ObjectAttributesXmlWriter(personsCS_CardExistence)
				.writeFile("./input/Attributes/personsCS_CardExistence.xml");

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
		// new
		// FacilitiesReaderMatsimV1((ScenarioImpl)this.scenario).readFile(this.facilitiesFile);
	}

	private void write() {
		PopulationWriter populationWriter = new PopulationWriter(
				this.scenario.getPopulation(), this.scenario.getNetwork());
		populationWriter.write("./output/plansMitTestFacilities.xml");
		log.info("Number of persons: "
				+ this.scenario.getPopulation().getPersons().size());
	}
}
