package readTrafficParticipants;

import java.util.ArrayList;
import java.util.List;

import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

public class TrafficParticipantsParser implements TabularFileHandler {

	private ArrayList<TrafficParticipants> participantgroups;
	
	private boolean female;
	
	private String trafficMode = "";
	
	public List<TrafficParticipants> getParticipantgroups()
	{
		return participantgroups;
	}

	public void readData(String filename) {
		this.participantgroups = new ArrayList<TrafficParticipants>();
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex(";");
		config.setFileName(filename);
		config.setCommentRegex("#");
		System.out.println("Start reading file: " + filename);
		// TabularFileParser().parse(..) ruft die ueberschriebene startRow-Methode auf
		new TabularFileParser().parse(config, this);
	}

	public void startRow(String[] row) {
		// System.out.println("row[3]:  " + row[3]);
		// row should be 2 entries long
		if (row.length <= 2)
			return;
		if(row[3].equals("maennlich")){
			female = false;
			return;
		} else if(row[3].equals("weiblich")){
			female = true;
			return;
		}
		if(row[3].equals("Hauptverkehrsmittel")){
			trafficMode = row[4];
			return;
		}

		TrafficParticipants tp = new TrafficParticipants();
		tp.setAge(row[3]);
		tp.setWork(row[4]);
		tp.setBusinessRelatedTravel(row[5]);
		tp.setEducation(row[6]);
		tp.setShopping(row[7]);
		tp.setErrand(row[8]);
		tp.setLeisure(row[9]);
		tp.setAttendance(row[10]);
		tp.setSex(female);
		tp.setMode(trafficMode);
		
		this.participantgroups.add(tp);
	}

}