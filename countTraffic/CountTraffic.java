package countTraffic;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.AgentDepartureEvent;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.LinkEnterEvent;
import org.matsim.core.api.experimental.events.LinkLeaveEvent;
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import Mathfunctions.Calculator;

/**
 * A class for counting traffic on specified links in matsim-simulaion
 * 
 * @author yasemin
 * 
 */
public class CountTraffic implements LinkEnterEventHandler,
		AgentDepartureEventHandler {

	private int scalingfactor = 10;
	private static final String NETWORKFILE = "input/networks/network_bayern.xml";
	private static final String EVENTSFILE = "pendlerOutputMultiModal/ITERS/it.20/20.events.xml.gz";
	private static final String OUTPUTFILE = "output/flowTest.csv";
	/**
	 * Map that stores the number of counted cars in the simulation for specified
	 * (relevant) links.
	 */
	private Map<Id, Counts> simCounts = new HashMap<Id, Counts>();
	private Map<Id, Integer> trafficVolume;

	/**
	 * Map that stores a person's LegMode if the person owns an
	 * AgentDepartureEvent.
	 */
	private Map<Id, String> modes = new HashMap<Id, String>();
	private Calculator calc = new Calculator();
	private Counts totalCounts = new Counts();
	Object[] linkIds;

	public Map<Id, Counts> getCounts() {
		return this.simCounts;
	}

	public void run(Map<Id, Integer> calibrationLinks) {
		/*
		 * create the scenario and read the networkfile
		 */
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(NETWORKFILE);
		/*
		 * read trafficCount, choose calibrationLinks and fill linkId-array with
		 * them.
		 */
		this.trafficVolume = new HashMap<Id, Integer>();
		this.trafficVolume.putAll(calibrationLinks);
		linkIds = this.trafficVolume.keySet().toArray();

		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(this);
		new MatsimEventsReader(manager).readFile(EVENTSFILE);
		writeNumberOfMovingAgentsForSpecifiedLinks(OUTPUTFILE);
	}

	public static void main(String[] args) {
		SeparateCalibrationFromValidationLinks cv = new SeparateCalibrationFromValidationLinks();
		cv.run();

		CountTraffic ct = new CountTraffic();
		ct.run(cv.getCalibrationLinks());
	}

	/**
	 * @param outfileName
	 *          outputfile in which the table of counts is written.
	 * 
	 *          Writes counted cars for all links in the outputfile.
	 */
	private void writeFlowForAllLinks(String outfileName) {
		try {
			Writer writer = IOUtils.getBufferedWriter(outfileName);
			writer.write("LinkId" + "\t" + "cars" + "\t" + "bikes" + "\t" + "pt"
					+ "\t" + "walk" + "\n");
			for (Entry<Id, Counts> countEntry : this.simCounts.entrySet()) {
				writer.write(countEntry.getKey() + "\t"
						+ calc.scaleReverse(countEntry.getValue().getCars()) + "\t"
						+ countEntry.getValue().getBikes() * scalingfactor + "\t"
						+ countEntry.getValue().getPT() * scalingfactor + "\t"
						+ countEntry.getValue().getWalk() * scalingfactor + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	/**
	 * @param outfileName
	 *          outputfile in which the table of counts is written.
	 * 
	 *          Writes number of counted agents moving in the sim for links given
	 *          in the linkIds-array in the outputtable. Additionally for
	 *          comparison CountData is written in the outputtable.
	 */
	private void writeNumberOfMovingAgentsForSpecifiedLinks(String outfileName) {
		/**
		 * create list to store values of differences between traffic-counts and
		 * simulation counts. Out of these values we will calculate the
		 * standard_deviation.
		 */
		List<Integer> differences = new ArrayList<Integer>();

		try {
			Writer writer = IOUtils.getBufferedWriter(outfileName);
			// write header
			writer.write("LinkId" + "\t" + "sim-cars" + "\t" + "sim-bikes" + "\t"
					+ "sim-pt" + "\t" + "sim-walk" + "\t" + "total" + "\t"
					+ "traffic-counts" + "\t" + "diff traffic-count and sim-count" + "\t"
					+ "diff as % of traffic-count" + "\n");

			Counts currentValue = null;
			Id currentLinkId = null;
			for (Entry<Id, Counts> countEntry : this.simCounts.entrySet()) {
				
				currentValue = countEntry.getValue();
				currentLinkId = countEntry.getKey();
				int volumeForCurrentLink = this.trafficVolume.get(currentLinkId);
				int totalNumberOfVehicles = calc.scaleReverse(currentValue.totalNumber());

				int difference = volumeForCurrentLink
						- calc.scaleReverse(currentValue.getCars());
				differences.add(difference);

				double differenceCounts = calc.relativeDifference(
						volumeForCurrentLink, totalNumberOfVehicles);

				writer.write(countEntry.getKey() + "\t"
						+ calc.scaleReverse(currentValue.getCars()) + "\t"
						+ calc.scaleReverse(currentValue.getBikes()) + "\t"
						+ calc.scaleReverse(currentValue.getPT()) + "\t"
						+ calc.scaleReverse(currentValue.getWalk()) + "\t"
						+ calc.scaleReverse(currentValue.totalNumber()) + "\t"
						+ volumeForCurrentLink + "\t" 
						+ (volumeForCurrentLink - totalNumberOfVehicles) + "\t" 
						+ differenceCounts + "\n");
			}
			/**
			 * write overall result to outputtable
			 */
			writer.write("\n");
			writer.write("walk gesamt: " + this.totalCounts.getWalk() + "\t"
					+ "bike gesamt: " + this.totalCounts.getBikes() + "\t"
					+ "pt gesamt: " + this.totalCounts.getPT() + "\t" + "car gesamt: "
					+ this.totalCounts.getCars() + "\t" + "Standardabweichung: "
					+ CalculateStandardDeviation(differences) + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	/**
	 * @param differences
	 *          List of values for which the standard-deviation shell be
	 *          calculated.
	 * @return standard-deviation of values which are stored in the input-list.
	 */
	private double CalculateStandardDeviation(List<Integer> differences) {
		double deviation = 0;
		double average = 0;
		double result = 0;

		for (int i = 0; i < differences.size(); i++) {
			average += differences.get(i);
		}
		average /= differences.size();

		for (int i = 0; i < differences.size(); i++) {
			deviation += (differences.get(i) - average)
					* (differences.get(i) - average);
		}
		result = Math.round(Math.sqrt((deviation / average)) * 100) / 100.0;
		System.out.println("Abweichung: " + result);
		return result;
	}

	@Override
	public void reset(int iteration) {
		// TODO Auto-generated method stub
	}

	/**
	 * @param event
	 *          is a LinkEnterEvent. According to the mode of the person which is
	 *          performing this event, the Counts-Object for the specified
	 *          events-links-id will be updated if the link-id is included in the
	 *          traffic-counts-map (trafficVolume).
	 */
	@Override
	public void handleEvent(LinkEnterEvent event) {
		Id linkId = event.getLinkId();
		/*
		 * speichere nur events, die an relevanten links stattfinden!
		 */
		if (trafficVolume.containsKey(linkId)) {
			Id personId = event.getPersonId();

			Counts countAll = new Counts();
			if (simCounts.containsKey(linkId)) {
				countAll = simCounts.get(linkId);
			}
			String mode = modes.get(personId);
			setActualCountAccordingToMode(mode, countAll);
			this.simCounts.put(linkId, countAll);
		}
	}

	/**
	 * @param mode
	 *          the actual mode of current person
	 * @param toBeSet
	 *          actual Counts Object for a specified link
	 * @return toBeSet including the last counted mode
	 */
	private Counts setActualCountAccordingToMode(String mode, Counts toBeSet) {

		int mode_initialLetter = mode.charAt(0);
		switch (mode_initialLetter) {
		case 98:
			toBeSet.raiseBikesByOne(); // b = 98 Unicode
			break;
		case 99:
			toBeSet.raiseCarsByOne(); // c = 99
			break;
		case 112:
			toBeSet.raisePTByOne(); // p = 112
			break;
		case 119:
			toBeSet.raiseWalkByOne(); // w = 119
			break;
		default:
			break;
		}

		return toBeSet;
	}

	/**
	 * @param event
	 *          is an AgentDepartureEvent. For every departure of an agent there's
	 *          once created such an event. Here we count whole trips in the
	 *          simulation which are performed by bike, walk or pt.
	 */
	@Override
	public void handleEvent(AgentDepartureEvent event) {
		Id personId = event.getPersonId();
		String mode = event.getLegMode();

		this.modes.put(personId, mode);
		int mode_initialLetter = mode.charAt(0);
		switch (mode_initialLetter) {
		case 98:
			this.totalCounts.raiseBikesByOne(); // b = 98 Unicode
			break;
		case 99:
			this.totalCounts.raiseCarsByOne(); // c = 99
			break;
		case 112:
			this.totalCounts.raisePTByOne(); // p = 112
			break;
		case 119:
			this.totalCounts.raiseWalkByOne(); // w = 119
			break;
		default:
			break;
		}
	}

}