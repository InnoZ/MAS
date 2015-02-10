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

	/**
	 * walking-/biking-/pt-people: number of persons with this LegMode in the
	 * whole simulation -> problem: agents that walk, use bike or pt are
	 * teleported in the simulation which means: their tripduration is calculated
	 * by multiplying the beeline of the covered distance with a certain constant.
	 * So they don't enter on links i.e. don't have any LinkEnterEvents which
	 * could be counted.
	 */
	private int scalingfactor = 10;
	private static final String NETWORKFILE = "input/networks/network_bayern.xml";
	private static final String EVENTSFILE = "pendlerOutputMultiModal/ITERS/it.20/20.events.xml.gz";
	private static final String OUTPUTFILE = "output/flowTest.csv";
	/**
	 * Map that stores the number of counted cars for specified (relevant) links.
	 */
	private Map<Id, Counts> counts = new HashMap<Id, Counts>();
	/**
	 * Map that stores a person's LegMode if the person owns an
	 * AgentDepartureEvent.
	 */
	private Map<Id, String> modes = new HashMap<Id, String>();
	private Calculator calc = new Calculator();
	private Counts totalCounts = new Counts();
	private Map<Id, Integer> calibrationLinks;
	Object[] linkIds;

	public Map<Id, Counts> getCounts() {
		return this.counts;
	}

	public void run() {
		/*
		 * create the scenario and read the networkfile
		 */
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(NETWORKFILE);
		/*
		 * read trafficCount, choose calibrationLinks and fill linkId-array with
		 * them.
		 */
		SeparateCalibrationFromValidationLinks cv = new SeparateCalibrationFromValidationLinks();
		cv.run();
		this.calibrationLinks = new HashMap<Id, Integer>();
		this.calibrationLinks.putAll(cv.getCalibrationLinks());
		linkIds = this.calibrationLinks.keySet().toArray();
	}

	public static void main(String[] args) {

		CountTraffic ct = new CountTraffic();
		ct.run();
		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(ct);
		new MatsimEventsReader(manager).readFile(EVENTSFILE);
		ct.writeNumberOfMovingAgentsForSpecifiedLinks(OUTPUTFILE);
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
			for (Entry<Id, Counts> countEntry : this.counts.entrySet()) {
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
	 *          Writes number of counted agents moving in the sim for links given in the linkIds-array
	 *          in the outputtable. Additionally for comparison CountData is
	 *          written in the outputtable.
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
			writer.write("LinkId" + "\t" + "cars" + "\t" + "bikes" + "\t" + "pt"
					+ "\t" + "walk" + "\t" + "traffic-counts" + "\t"
					+ "difference between traffic-count and sim-count" + "\t"
					+ "difference in % of census" + "\n");
			for (Entry<Id, Counts> countEntry : this.counts.entrySet()) {
		
				for (int i = 0; i < linkIds.length; i++) {
					/**
					 * calculate statistical values for the outputtable.
					 */
					int volumeForCurrentLink = this.calibrationLinks
							.get(linkIds[i]);
					int difference = volumeForCurrentLink
							- countEntry.getValue().getCars() * scalingfactor;
					differences.add(difference);

					double percentage = calculatePercentage(volumeForCurrentLink,
							difference);

					if (countEntry.getKey().equals(linkIds[i])) {
						writer.write(countEntry.getKey() + "\t"
								+ countEntry.getValue().getCars() * scalingfactor + "\t"
								+ countEntry.getValue().getBikes() * scalingfactor + "\t"
								+ countEntry.getValue().getPT() * scalingfactor + "\t"
								+ countEntry.getValue().getWalk() * scalingfactor + "\t"
								+ volumeForCurrentLink + "\t" + difference + "\t" + percentage
								+ "\n");

					}
				}
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

	private double calculatePercentage(int n1, int n2) {
		return Math.round((((100.0 / n1) * n2) * 100)) / 100.0;
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
	 *          is a LinkEnterEvent. In our simulation this is an event which can
	 *          only performed by a person using a car because the other
	 *          transport-modes are teleported. Therefore the differentiation
	 *          between car, bike, pt and walk is senseless for the moment but
	 *          could be needed later.
	 */
	@Override
	public void handleEvent(LinkEnterEvent event) {
		Id linkId = event.getLinkId();
		// if(linkIds contain linkId){ do... damit nur die events gespeichert
		// werden, die an relevanten links stattfinden
		Id personId = event.getPersonId();

		Counts countAll = new Counts();
		if (counts.containsKey(linkId)) {
			countAll = counts.get(linkId);
		}
		String mode = modes.get(personId);

		int mode_initialLetter = mode.charAt(0);
		switch (mode_initialLetter) {
		case 98:
			countAll.raiseBikesByOne(); // b = 98 Unicode
			break;
		case 99:
			countAll.raiseCarsByOne(); // c = 99
			break;
		case 112:
			countAll.raisePTByOne(); // p = 112
			break;
		case 119:
			countAll.raiseWalkByOne(); // w = 119
			break;
		default:
			break;
		}
		this.counts.put(linkId, countAll);
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