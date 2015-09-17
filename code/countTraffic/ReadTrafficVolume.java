package countTraffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;
/**
 * class for reading in a table containing traffic-data and storing them in a map.
 * @author yasemin
 *
 */
public class ReadTrafficVolume implements TabularFileHandler {

	/**
	 * @param args
	 */
	private final static String TRAFFIC_VOLUME_TABLE = "input/Verkehrsdaten/verkehrsaufkommen.csv";
	private Map<Id, Integer> trafficVolume;
	private ArrayList<Integer> orderOfLinkIds;

	public Map<Id, Integer> getVolume() {
		return trafficVolume;
	}
	
	public ArrayList<Integer> getOrder() {
		return orderOfLinkIds;
	}
	
	public void readData() {
		this.trafficVolume = new HashMap<Id, Integer>();
		this.orderOfLinkIds = new ArrayList<Integer>();
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex(",");
		config.setFileName(TRAFFIC_VOLUME_TABLE);
		config.setCommentRegex("#");
		System.out.println("Start reading file: " + TRAFFIC_VOLUME_TABLE);
		/*
		 *  TabularFileParser().parse(..) ruft die ueberschriebene startRow-Methode auf
		 */
		new TabularFileParser().parse(config, this);
	}

	public static void main(String[] args) {
		ReadTrafficVolume rtv = new ReadTrafficVolume();

		rtv.readData();
		Map<Id,Integer> trafficVolume = rtv.getVolume();

		printMap(trafficVolume);
	}
	
	public static void printMap(Map mp) {
    Iterator it = mp.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        System.out.println("LinkId: " + pairs.getKey() + " = " + "TotalVeh.: " + pairs.getValue());
        it.remove(); // avoids a ConcurrentModificationException
    }
	}
	
	@Override
	public void startRow(String[] row) {
		int index_LinkId = 1;
		int index_totalVehicles = 2;
		String linkIdAsString = row[index_LinkId];
		Id linkId = new IdImpl(linkIdAsString);
		trafficVolume.put(linkId, Integer.valueOf(row[index_totalVehicles]));
		this.orderOfLinkIds.add(Integer.parseInt(row[index_LinkId]));
	}

}
