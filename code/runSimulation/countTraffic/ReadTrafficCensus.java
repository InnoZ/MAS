package countTraffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import readTrafficVorlaeufig.TrafficPeopleMode;
import readTrafficVorlaeufig.TrafficPeopleParser;

/**
 * class for reading in a table containing traffic-data and storing them in a map.
 * @author yasemin
 *
 */
public class ReadTrafficCensus implements TabularFileHandler {

	/**
	 * @param args
	 */
	private final static String CENSUS_TABLE = "input/Verkehrsdaten/verkehrsaufkommen.csv";
	private Map<Id, Integer> censusData;

	public Map<Id, Integer> getCensus() {
		return censusData;
	}

	public void readData() {
		this.censusData = new HashMap<Id, Integer>();
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex(",");
		config.setFileName(CENSUS_TABLE);
		config.setCommentRegex("#");
		System.out.println("Start reading file: " + CENSUS_TABLE);
		// TabularFileParser().parse(..) ruft die ueberschriebene startRow-Methode
		// auf
		new TabularFileParser().parse(config, this);
	}

	public static void main(String[] args) {
		ReadTrafficCensus rtc = new ReadTrafficCensus();

		rtc.readData();

		System.out.println("CENSUS Entryset:  "
				+ rtc.getCensus().entrySet().toString() + "\n" + "size: "
				+ rtc.getCensus().size());

	}

	@Override
	public void startRow(String[] row) {
		Id linkId = new IdImpl(row[1]);
		censusData.put(linkId, Integer.valueOf(row[2]));
	}

}
