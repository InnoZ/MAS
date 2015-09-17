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
import org.matsim.core.api.experimental.events.handler.AgentDepartureEventHandler;
import org.matsim.core.api.experimental.events.handler.LinkEnterEventHandler;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

/**
 * A class for counting cars on links in matsim-simulaion
 * 
 * @author yasemin
 * 
 */
public class CountTraffic implements LinkEnterEventHandler,
		AgentDepartureEventHandler {

	/**
	 * counts: Map that stores the number of counted cars for every link.
	 * 
	 * modes: Map that stores a person's LegMode if the person owns an
	 * AgentDepartureEvent.
	 * 
	 * walking-/biking-/pt-people: number of persons with this LegMode in the
	 * whole simulation -> problem: agents that walk, use bike or pt are
	 * teleported in the simulation which means: their tripduration is calculated
	 * by multiplying the beeline of the covered distance with a certain constant.
	 * So they don't enter on links i.e. don't have any LinkEnterEvents which
	 * could be counted.
	 * 
	 * Id[]: Array that stores the relevant linkIds. Relevant means the links
	 * which are interesting for the user.
	 */
	Map<Id, Counts> counts = new HashMap<Id, Counts>();
	Map<Id, String> modes = new HashMap<Id, String>();
	static int walkingpeople = 0;
	static int bikingpeople = 0;
	static int ptpeople = 0;
	Id[] linkIds = new Id[18];

	public static void main(String[] args) {
		Scenario sc = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(sc).readFile("input/networks/network_bayern.xml");
		CountTraffic ct = new CountTraffic();
		EventsManager manager = EventsUtils.createEventsManager();
		manager.addHandler(ct);
		new MatsimEventsReader(manager)
				.readFile("outputTransportMode/ITERS/it.20/20.events.xml.gz");
		// ct.writeFlowForAllLinks("output/flow.csv");
		ct.writeNumberOfCarsForSpecialLinks("output/flow.csv");
	}

	/**
	 * Here the linkIds of links which are important are stored.
	 */
	private void createArrayOfLinkIds() {
		String[] links = { "36020", "36019", "45978", "45977", "44872", "44873",
				"30679", "30680", "27701", "12768", "17278", "20842", "27698", "27697",
				"15245", "15244", "20480", "20481" };

		for (int i = 0; i < links.length; i++) {
			IdImpl id = new IdImpl(links[i]);
			linkIds[i] = id;
		}
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
						+ countEntry.getValue().getCars() * 10 + "\t"
						+ countEntry.getValue().getBikes() * 10 + "\t"
						+ countEntry.getValue().getPT() * 10 + "\t"
						+ countEntry.getValue().getWalk() * 10 + "\n");
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
	 *          Writes number of counted cars for links given in the linkIds-array
	 *          in the outputtable. Additionally for comparison censusData is
	 *          written in the outputtable.
	 */
	private void writeNumberOfCarsForSpecialLinks(String outfileName) {
		createArrayOfLinkIds();
		/**
		 * read census. These data is later used for comparing census with
		 * matsim-counts of the simulation.
		 */
		Map<Id, Integer> census = new HashMap<Id, Integer>();
		ReadTrafficCensus rtc = new ReadTrafficCensus();
		rtc.readData();
		census = rtc.getCensus();
		/**
		 * create list to store values of difference between traffic-survey and
		 * simulation counts. Out of these values we will calculate the
		 * standard_deviation.
		 */
		List<Integer> differences = new ArrayList<Integer>();

		try {
			Writer writer = IOUtils.getBufferedWriter(outfileName);
			writer.write("LinkId" + "\t" + "cars" + "\t" + "bikes" + "\t" + "pt"
					+ "\t" + "walk" + "\t" + "traffic-survey" + "\t"
					+ "difference: survey-sim" + "\t" + "difference in % of census"
					+ "\n");
			for (Entry<Id, Counts> countEntry : this.counts.entrySet()) {
				for (int i = 0; i < linkIds.length; i++) {
					/**
					 * calculate statistical values for the outputtable.
					 */
					int censusForCurrentLink = census.get(linkIds[i]);
					int difference = censusForCurrentLink
							- countEntry.getValue().getCars() * 10;
					differences.add(difference);
					double percentage = Math
							.round((((100.0 / censusForCurrentLink) * difference) * 100)) / 100.0;

					if (countEntry.getKey().equals(linkIds[i])) {
						writer.write(countEntry.getKey() + "\t"
								+ countEntry.getValue().getCars() * 10 + "\t"
								+ countEntry.getValue().getBikes() * 10 + "\t"
								+ countEntry.getValue().getPT() * 10 + "\t"
								+ countEntry.getValue().getWalk() * 10 + "\t"
								+ censusForCurrentLink + "\t" + difference + "\t" + percentage
								+ "\n");

					}
				}
			}
			/**
			 * write overall result to outputtable
			 */
			writer.write("\n");
			writer.write("walk/bike/pt gesamt" + "\t" + "" + "\t" + bikingpeople
					+ "\t" + ptpeople + "\t" + walkingpeople + "\t" + "" + "\t"
					+ "Standardabweichung: " + CalculateStandardDeviation(differences)
					+ "\n");
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
	 *          is a LinkEnterEvent. In our simulation this is an event which can
	 *          only performed by a person using a car because the other
	 *          transport-modes are teleported. Therefore the differentiation
	 *          between car, bike, pt and walk is senseless for the moment but
	 *          could be needed later.
	 */
	@Override
	public void handleEvent(LinkEnterEvent event) {
		Id linkId = event.getLinkId();
		Id personId = event.getPersonId();

		Counts countAll = new Counts();

		String mode = modes.get(personId);

		if (mode.equals("car")) {
			if (counts.containsKey(linkId)) {
				countAll = counts.get(linkId);
			}
			countAll.countCars++;
		} else if (mode.equals("bike")) {
			if (counts.containsKey(linkId)) {
				countAll = counts.get(linkId);
			}
			countAll.countBikes++;
		} else if (mode.equals("pt")) {
			if (counts.containsKey(linkId)) {
				countAll = counts.get(linkId);
			}
			countAll.countPT++;
		} else if (mode.equals("walk")) {
			if (counts.containsKey(linkId)) {
				countAll = counts.get(linkId);
			}
			countAll.countWalk++;
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
		if (mode.equals("bike")) {
			bikingpeople++;
		} else if (mode.equals("walk")) {
			walkingpeople++;
		} else if (mode.equals("pt")) {
			ptpeople++;
		}
	}

}