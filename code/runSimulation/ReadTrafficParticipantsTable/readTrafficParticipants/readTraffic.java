package readTrafficParticipants;

public class readTraffic {

	private static final String MODALSPLIT_TABLE = "input/Verkehrsdaten/ModalSplit_GAP.csv";

	public static void main(String[] args){
		TrafficParticipantsParser tpp = new TrafficParticipantsParser();
		
		tpp.readData(MODALSPLIT_TABLE);
		
		System.out.println("Liste: " + "Alter = " + (tpp.getParticipantgroups()).get(14).getAge() 
				+ "  Geschlecht=  " + (tpp.getParticipantgroups()).get(14).getSex()
				+ "  Begleitung=  " + (tpp.getParticipantgroups()).get(14).getAttendance()
				+ "  dienstlich=  " + (tpp.getParticipantgroups()).get(14).getBusinessRelatedTravel()
				+ "  Ausbildung=  " + (tpp.getParticipantgroups()).get(14).getEducation()
				+ "  Erledigung=  " + (tpp.getParticipantgroups()).get(14).getErrand() );
		System.out.println( "  Freizeit=  " + (tpp.getParticipantgroups()).get(14).getLeisure()  
				+ "  Fahrzeug=  " + (tpp.getParticipantgroups()).get(14).getMode()  
				+ "  Einkaufen=  " + (tpp.getParticipantgroups()).get(14).getShopping()  
				+ "  Arbeit=  " + (tpp.getParticipantgroups()).get(14).getWork() );

	}
}
