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
 * A class for counting traffic on specified links in matsim-simulaion and
 * compare it to real traffic-count.
 * 
 * @author yasemin
 * 
 */
public class CountTraffic implements LinkEnterEventHandler,
		AgentDepartureEventHandler {

	private static final String NETWORKFILE = "input/networks/network_bayern.xml";
	private static final String EVENTSFILE = "pendlerOutputMultiModal/ITERS/it.20/20.events.xml.gz";
	private static final String OUTPUTFILE = "output/CountFuerPendlerSim.csv";
	private static final String KALIBRIERUNGSTUFE = "1";
	/**
	 * Map that stores the number of counted vehicles and walking agents in the
	 * simulation for specified (relevant) links.
	 */
	private Map<Id, Counts> simCounts = new HashMap<Id, Counts>();
	/**
	 * Map that stores the number of counted vehicles from traffic count for
	 * specified (relevant) links.
	 */
	private Map<Id, Integer> trafficVolume;
	/**
	 * Map that stores a person's LegMode if the person owns an
	 * AgentDepartureEvent.
	 */
	private Map<Id, String> modes = new HashMap<Id, String>();
	private Calculator calc = new Calculator();
	private int totalCountsInSim = 0;

	public void run(Map<Id, Integer> calibrationLinks) {
		/*
		 * create the scenario, read the networkfile and trafficCount and choose
		 * calibrationLinks.
		 */
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile(NETWORKFILE);
		this.trafficVolume = new HashMap<Id, Integer>();
		this.trafficVolume.putAll(calibrationLinks);
		/*
		 * read events-file and fill sim-Counts-Map with entries
		 */
		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(this);
		new MatsimEventsReader(manager).readFile(EVENTSFILE);
		writeNumberOfMovingAgentsForSpecifiedLinks(OUTPUTFILE);
	}

	public static void main(String[] args) {
		CreateCalibrationLinks cc = new CreateCalibrationLinks();
		cc.run();

		// SeparateCalibrationFromValidationLinks cv = new
		// SeparateCalibrationFromValidationLinks();
		// cv.run();

		CountTraffic ct = new CountTraffic();
		ct.run(cc.getCalibrationLinks());
	}

	private void writeAnalysis(ArrayList<Double> differences, Writer writer) {

		double arithmeticMean = calc.arithmeticMean(differences);
		double variance = calc.variance(differences, arithmeticMean);
		try {
			writer.write("Differenz im Mittel: " + arithmeticMean + "\t"
					+ "Varianz: " + variance + "\t" + "Standardabweichung: "
					+ calc.standardDeviaion(variance) + "\n" + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param outfileName
	 *          outputfile in which the table of counts is written.
	 * 
	 *          Writes number of counted agents moving in the sim for links given
	 *          in the calibrationLinks-map to the outputtable. For comparison
	 *          additionally the CountData is written to the outputtable.
	 */
	private void writeNumberOfMovingAgentsForSpecifiedLinks(String outfileName) {
		/**
		 * create lists to store values of preciseness relating traffic-counts and
		 * simulation-counts for further analysis.
		 */
		ArrayList<Double> diffInPercent = new ArrayList<Double>();
		ArrayList<Double> absDifferences = new ArrayList<Double>();

		try {
			Writer writer = IOUtils.getBufferedWriter(outfileName);
			writer.write("KALIBRIERUNGSSTUFE " + this.KALIBRIERUNGSTUFE + "\n\n" );
			// write header
			writer.write("LinkId" + "\t" + "sim-cars" + "\t" + "sim-bikes" + "\t"
					+ "sim-pt" + "\t" + "sim-walk" + "\t" + "total" + "\t"
					+ "traffic-counts" + "\t" + "total diff |traffic-count|-|sim-count|"
					+ "\t" + "diff anteilig an traffic-count in %" + "\n");

			Counts currentValue = null;
			Id currentLinkId = null;
			/*
			 * iterate over simulation-counts and write them an their difference to
			 * traffic-counts to outputfile
			 */
			for (Entry<Id, Counts> countEntry : this.simCounts.entrySet()) {

				currentValue = countEntry.getValue();
				currentLinkId = countEntry.getKey();
				int volumeTrafficCountForCurrentLink = this.trafficVolume
						.get(currentLinkId);
				int volumeSimForCurrentLink = currentValue.getTotal();

				int absDifference = volumeTrafficCountForCurrentLink
						- volumeSimForCurrentLink;
				absDifferences.add((double) Math.abs(absDifference));

				double relatDifference = calc.relativeDifference(
						volumeTrafficCountForCurrentLink, volumeSimForCurrentLink);
				diffInPercent.add(relatDifference);

				writer.write(currentLinkId + "\t" + currentValue.getCars() + "\t"
						+ currentValue.getBikes() + "\t" + currentValue.getPT() + "\t"
						+ currentValue.getWalk() + "\t" + currentValue.getTotal() + "\t"
						+ volumeTrafficCountForCurrentLink + "\t" + absDifference + "\t"
						+ relatDifference + "\n");
			}
			/**
			 * write overall result to outputtable
			 */
			writer.write("\n");
			writer.write("trips gesamt in Simulation: "
					+ calc.scaleReverse(this.totalCountsInSim) + "\t"
					+ "Counts relevante Links in Sim: "
					+ calc.sumOverCountsMapValues(simCounts) + "\t"
					+ "Counts aus Verkehrszählung: "
					+ calc.sumOverIntegerMapValues(trafficVolume) + "\n");
			writer.write("\n");
			/*
			 * write analysis for difference in % and total difference
			 */
			writer
					.write("Analyse für Differenz anteilig an Verkehrszählung in %: \n");
			writeAnalysis(diffInPercent, writer);
			writer.write("Analyse für absolute Differenz in Anzahl Agenten/Autos: \n");
			writeAnalysis(absDifferences, writer);

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();

		}
		System.out.println("done!");
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
	 *          once created such an event. Here for every trip the specified mode
	 *          is attached to a personId by storing in a map. Addidtionally we
	 *          count the number if all trips in the simulation in
	 *          totalCountsInSim.
	 */
	@Override
	public void handleEvent(AgentDepartureEvent event) {
		Id personId = event.getPersonId();
		String mode = event.getLegMode();
		this.modes.put(personId, mode);
		this.totalCountsInSim++;
	}
}