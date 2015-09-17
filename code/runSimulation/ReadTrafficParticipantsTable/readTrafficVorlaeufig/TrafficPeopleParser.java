package readTrafficVorlaeufig;

import java.util.ArrayList;
import java.util.List;

import org.matsim.core.utils.io.tabularFileParser.TabularFileHandler;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParser;
import org.matsim.core.utils.io.tabularFileParser.TabularFileParserConfig;

import readTrafficParticipants.TrafficParticipantsParser;

public class TrafficPeopleParser implements TabularFileHandler {

	private static final String TRAFFIC_TABLE = "input/Verkehrsdaten/DurchschnittWege.csv";
	private ArrayList<TrafficPeopleMode> participantgroups;
	private boolean female;
	private String trafficMode = "";

	public List<TrafficPeopleMode> getParticipantgroups() {
		return participantgroups;
	}

	public void readData(String filename) {
		this.participantgroups = new ArrayList<TrafficPeopleMode>();
		TabularFileParserConfig config = new TabularFileParserConfig();
		config.setDelimiterRegex(";");
		config.setFileName(filename);
		config.setCommentRegex("#");
		System.out.println("Start reading file: " + filename);
		// TabularFileParser().parse(..) ruft die ueberschriebene startRow-Methode
		// auf
		new TabularFileParser().parse(config, this);
	}

	public void startRow(String[] row) {
		// System.out.println("row[0]:  " + row[0]);
		// row should be 2 entries long
		if (row.length <= 2)
			return;
		if (row[0].equals("maennlich")) {
			female = false;
			return;
		} else if (row[0].equals("weiblich")) {
			female = true;
			return;
		}

		TrafficPeopleMode tpm = new TrafficPeopleMode();
		tpm.setAge(row[0]);
		tpm.setPedestrian(row[1]);
		tpm.setBike(row[2]);
		tpm.setCar(row[4]);
		tpm.setPt(row[5]);
		tpm.setSex(female);

		this.participantgroups.add(tpm);
	}

	public static void main(String[] args) {
		TrafficPeopleParser tpp = new TrafficPeopleParser();

		tpp.readData(TRAFFIC_TABLE);

		System.out.println("Liste: " + "Alter = "
				+ (tpp.getParticipantgroups()).get(14).getAge() + "  Geschlecht =  "
				+ (tpp.getParticipantgroups()).get(14).getSex() + "  Fahrrad =  "
				+ (tpp.getParticipantgroups()).get(14).getBike() + "  Fußgänger =  "
				+ (tpp.getParticipantgroups()).get(14).getPedestrian() + "  Auto =  "
				+ (tpp.getParticipantgroups()).get(14).getCar() + "  Öffis =  "
				+ (tpp.getParticipantgroups()).get(14).getPt() + "size  "
				+ tpp.getParticipantgroups().size());

	}

}
