package readTrafficParticipants;

/**
 * Ein Objekt zum Speichern von Daten aus einer ModalSplit-Tabelle. Diese Daten
 * sagen aus, wieviele Personen gleichen Alters & Geschlechts einen bestimmten
 * Wegezweck haben. Zu jedem möglichen Wegezewck wird also eine Zahl gespeichert
 * und darüberhinaus das Alter und Geschlecht dieser Personengruppe.
 * 
 * @author yasemin
 * 
 */
public class TrafficParticipants implements Comparable<TrafficParticipants> {

	private int age;
	private int work;
	private String mode;
	private int businessRelatedTravel;
	private int education;
	private int shopping;
	// Erledigung
	private int errand;
	private int leisure;
	// Begleitung
	private int attendance;
	private Sex sex;

	public enum Sex {
		m, f
	}

	/**
	 * @return int age of the Traffic Participant
	 */
	public int getAge() {
		return age;
	}

	/**
	 * @param int age of the Traffic Participant
	 */
	public void setAge(int age) {
		this.age = age;
	}

	/**
	 * @param String
	 *          age of the Traffic Participant
	 */
	public void setAge(String age) {
		this.age = Integer.parseInt(age);
	}

	/**
	 * @return String sex of the Traffic Participant
	 */
	public String getSex() {
		return sex.toString();
	}

	/**
	 * @param Sex
	 *          sex of the Traffic Participant
	 */
	public void setSex(Sex sex) {
		this.sex = sex;
	}

	/**
	 * @param sex
	 *          boolean, false for male and true for female.
	 */
	public void setSex(boolean sex) {
		if (sex == false)
			this.sex = Sex.m;
		else
			this.sex = Sex.f;
	}

	/**
	 * @return int number of working poeple.
	 */
	public int getWork() {
		return work;
	}

	public void setWork(int work) {
		this.work = work;
	}

	public void setWork(String work) {
		this.work = Integer.parseInt(work);
	}

	/**
	 * @return String mode of participantgroup
	 */
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	/**
	 * @return number of businessRelatedTravelling people
	 */
	public int getBusinessRelatedTravel() {
		return businessRelatedTravel;
	}

	public void setBusinessRelatedTravel(int businessRelatedTravel) {
		this.businessRelatedTravel = businessRelatedTravel;
	}

	public void setBusinessRelatedTravel(String businessRelatedTravel) {
		this.businessRelatedTravel = Integer.parseInt(businessRelatedTravel);
	}

	/**
	 * @return number of people belonging to a certain group of traffic
	 *         participants, going to school.
	 */
	public int getEducation() {
		return education;
	}

	public void setEducation(int education) {
		this.education = education;
	}

	public void setEducation(String education) {
		this.education = Integer.parseInt(education);
	}

	/**
	 * @return number of people belonging to a certain group of traffic
	 *         participants, going shopping.
	 */
	public int getShopping() {
		return shopping;
	}

	public void setShopping(int shopping) {
		this.shopping = shopping;
	}

	public void setShopping(String shopping) {
		this.shopping = Integer.parseInt(shopping);
	}

	/**
	 * @return number of people belonging to a certain group of traffic
	 *         participants, running an errand.
	 */
	public int getErrand() {
		return errand;
	}

	public void setErrand(int errand) {
		this.errand = errand;
	}

	public void setErrand(String errand) {
		this.errand = Integer.parseInt(errand);
	}

	/**
	 * @return number of people belonging to a certain group of traffic
	 *         participants, going to do leisure.
	 */
	public int getLeisure() {
		return leisure;
	}

	public void setLeisure(int leisure) {
		this.leisure = leisure;
	}

	public void setLeisure(String leisure) {
		this.leisure = Integer.parseInt(leisure);
	}

	/**
	 * @return number of people belonging to a certain group of traffic
	 *         participants, attending other people with their activities.
	 */
	public int getAttendance() {
		return attendance;
	}

	public void setAttendance(int attendance) {
		this.attendance = attendance;
	}

	public void setAttendance(String attendance) {
		this.attendance = Integer.parseInt(attendance);
	}

	@Override
	public int compareTo(TrafficParticipants tp) {
		if (this.age > tp.age) {
			return 1;
		} else if (this.age < tp.age) {
			return -1;
		} else {
			return 0;
		}
	}

}