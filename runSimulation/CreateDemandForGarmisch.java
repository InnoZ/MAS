package simulation;

import readTrafficParticipants.*;
import readTrafficVorlaeufig.*;
import readTrafficVorlaeufig.TrafficPeopleMode.Sex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Arrays;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
// import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * 
 * Eine Klasse, die eine plans.xml-Datei erzeugt. Auf diese plans.xml-Datei kann
 * in der config.xml verwiesen werden, mithilfe derer wiederum der Controler
 * eine Simualtion durchfuehrt.
 * 
 */
public class CreateDemandForGarmisch {

	private static final String NETWORKFILE = "input/networks/network_bayern.xml";
	private static final String GEMEINDEN = "input/Geodaten/BayernGemeindenWGS84_UTM32N/bayern_gemeinden_UTM32N.shp";
	// private static final String GARMISCH =
	// "input/Geodaten/Garmisch_Gemeindegrenzen/Garmisch_Gemeindegrenzen_UTM32N.shp";
	private static final String PLANSFILEOUTPUT = "input/plans/plans_test.xml";
	private static final String BUILDINGS = "input/Geodaten/buildings_lk_ga/buildings_landkreis_garmisch_partenkirchen_utm32N.shp";
	private static final String MODALSPLIT_TABLE = "input/Verkehrsdaten/ModalSplit_GAP.csv";
	private static final String TRAFFIC_TABLE = "input/Verkehrsdaten/DurchschnittWege.csv";

	private Scenario scenario;
	private Map<String, Geometry> shapeMap;
	private Map<String, Geometry> buildingsMap;
	Geometry[] buildingsAsArray;
	private static double SCALEFACTOR = 0.1;

	CreateDemandForGarmisch() {
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario).readFile(NETWORKFILE);
	}

	Map<String, Integer> relationsGarmisch = new TreeMap<String, Integer>();
	Map<String, Integer> relationsEinpendler = new TreeMap<String, Integer>();
	Map<String, Integer> relationsAuspendler = new TreeMap<String, Integer>();
	Map<String, Integer> relationsUebrigeEinpendler = new TreeMap<String, Integer>();
	Map<String, Integer> relationsUebrigeAuspendler = new TreeMap<String, Integer>();

	/**
	 * Mit run werden die maps erzeugt, in denen alle Pendlerdaten gespeichert
	 * werden, die zur Verfuegung stehen. Beim Einlesen der Daten wird zwischen
	 * "Pendlern" und "Uebrigen Pendlern" unterschieden, weil die
	 * Gemeindeschluessel in den Listen der csv-Dateien teilweise unvollständig
	 * dargestellt sind und manuell "repariert" werden muessen. Nach dem Einlesen
	 * mithilfe der ReadPendler-Klassen, werden die "WritePlans"-Methoden
	 * aufgerufen, um Plaene fuer einzelne Agenten (PendlerAgenten) zu erstellen.
	 */
	private void run() {

		// Pendler: Garmisch -> Garmisch
		ReadPendler rp = new ReadPendler();

		// Pendler: Garmisch -> Bayern
		ReadAuspendler ra = new ReadAuspendler();

		// Pendler: Bayern -> Garmisch
		ReadEinpendler re = new ReadEinpendler();

		relationsGarmisch = rp.run();
		relationsAuspendler = ra.run("given");
		relationsUebrigeAuspendler = ra.run("other");
		relationsEinpendler = re.run("given");
		relationsUebrigeEinpendler = re.run("other");

		findKeysAndWritePlans(relationsGarmisch, "garmisch");
		findKeysAndWritePlans(relationsAuspendler, "");
		findKeysAndWritePlans(relationsEinpendler, "");
		findKeysAndWritePlansForUebrigePendler(relationsUebrigeAuspendler, true);
		findKeysAndWritePlansForUebrigePendler(relationsUebrigeEinpendler, false);
		// createModalSplitPopulation();
		createModalSplitPopulationNew();
		// createPopulationFromModalSplitVorlaeufig();

		PopulationWriter pw = new PopulationWriter(scenario.getPopulation(),
				scenario.getNetwork());
		pw.write(PLANSFILEOUTPUT);

	}

	/**
	 * Filtert aus den keys der input-map die Gemeindeschluessel der
	 * Pendlergruppen heraus und gibt diese, zusammen mit dem jeweils zuheörigen
	 * map-value, an writePlansFor() weiter. Dort werden Agenten-Pläne erstellt.
	 * 
	 * @param relationsMap
	 *          map mit key: Gemeindeschluessel von Start & Ziel der
	 *          Pendlergruppen, value: Anzahl Pendler mit diesem Start/Ziel.
	 */
	private void findKeysAndWritePlans(Map<String, Integer> relationsMap,
			String garmischOderNicht) {

		// Iteriere ueber map mit Pendlerzahlen und rufe fuer jede Pendlergruppe
		// writePlansFor auf
		Iterator<Entry<String, Integer>> entries = relationsMap.entrySet()
				.iterator();

		while (entries.hasNext()) {
			Entry<String, Integer> thisEntry = (Entry<String, Integer>) entries
					.next();
			String key = (String) thisEntry.getKey();

			// trenne Gemeindeschluessel nach "von", "nach"

			String from = liesStart(key);
			String to = liesZiel(key);
			Integer value = (Integer) thisEntry.getValue();

			writePlansFor(from, to, value, garmischOderNicht);

		}
	}

	/**
	 * @param fromGemeinde
	 *          : Gemeinde, aus der die Pendler kommen.
	 * @param toGemeinde
	 *          : Gemeinde, in die die Pendler fahren.
	 * @param commuters
	 *          : Anzahl von Pendlern, die aus fromGemeinde kommen und nach
	 *          toGemeinde fahren.
	 * 
	 *          Diese Methode schreibt die Tagesplaene aller Pendler außerhalb
	 *          Garmisch-Partenkirchens, die aus fromGemeinde kommen und nach
	 *          toGemeinde fahren. Dazu wird zunaechst das shapefile "Gemeinden"
	 *          im Bezug auf das Gebiet Bayern. eingelesen. Danach werden die
	 *          Pendler in zwei Personengruppen aufgeteilt: Die erste besteht aus
	 *          Autofahrern(53%), die zweite aus Personen, die den oeffentlichen
	 *          Verkehr nutzen(47%). Zum Schluss werden zufaellige Koordinaten
	 *          fuer Wohn- und Arbeitsorte der Personen erstellt.
	 */
	private void writePlansFor(String fromGemeinde, String toGemeinde,
			int commuters, String garmischOderNicht) {

		if (garmischOderNicht.equals("garmisch")) {
			writePlansForGarmisch(fromGemeinde, toGemeinde, commuters);
		} else {

			if (shapeMap == null)
				// "SCH" ist der Gemeindeschlüssel zu der jeweiligen Geometry
				this.shapeMap = readShapeFile(GEMEINDEN, "SCH");

			double comm = commuters * SCALEFACTOR;
			double carcomm = 0.53 * comm;
			float roundedCarcomm = Math.round(carcomm);

			for (int i = 0; i <= comm; i++) {
				String mode = "car";
				if (i > roundedCarcomm)
					mode = "pt";

				Coord homec = drawRandomPointFromGeometry(this.shapeMap
						.get(fromGemeinde));

				Coord workc = drawRandomPointFromGeometry(this.shapeMap.get(toGemeinde));

				createOnePerson(i, homec, workc, mode, fromGemeinde + "_" + toGemeinde);
			}
		}
	}

	/**
	 * @param fromGemeinde
	 *          : Gemeinde, aus der die Pendler kommen.
	 * @param toGemeinde
	 *          : Gemeinde, in die die Pendler fahren.
	 * @param commuters
	 *          : Anzahl von Pendlern, die aus fromGemeinde kommen und nach
	 *          toGemeinde fahren.
	 * 
	 *          Diese Methode schreibt die Tagesplaene aller Pendler innerhalb
	 *          Garmisch-Partenkirchens, die aus fromGemeinde kommen und nach
	 *          toGemeinde fahren. Dazu werden zunaechst die beiden shapefiles
	 *          "Gemeinden" und "Gebaeude" im Bezug auf das Gebiet Oberbayern
	 *          eingelesen. Danach werden die Pendler in zwei Personengruppen
	 *          aufgeteilt: Die erste besteht aus Autofahrern(53%), die zweite aus
	 *          Personen, die den oeffentlichen Verkehr nutzen(47%). Zum Schluss
	 *          werden zufaellige Koordinaten fuer Wohn- und Arbeitsorte der
	 *          Personen erstellt, in denen sich auch tatsaechlich Gebaeude
	 *          befinden.
	 */
	private void writePlansForGarmisch(String fromGemeinde, String toGemeinde,
			int commuters) {

		if (shapeMap == null)
			// "SCH" ist der Gemeindeschlüssel zu der jeweiligen Geometry
			this.shapeMap = readShapeFile(GEMEINDEN, "SCH");
		if (buildingsMap == null)
			this.buildingsMap = readShapeFile(BUILDINGS, "osm_id");
		if (buildingsAsArray == null) {
			buildingsAsArray = new Geometry[buildingsMap.size()];
			buildingsMap.values().toArray(buildingsAsArray);
		}

		double comm = commuters * SCALEFACTOR;
		double carcomm = 0.53 * comm;
		float roundedCarcomm = Math.round(carcomm);

		for (int i = 0; i <= comm; i++) {
			String mode = "car";
			if (i > roundedCarcomm)
				mode = "pt";

			Coord homec = drawRandomPointInBuilding(this.shapeMap.get(fromGemeinde),
					buildingsAsArray);

			Coord workc = drawRandomPointInBuilding(this.shapeMap.get(toGemeinde),
					buildingsAsArray);

			createOnePerson(i, homec, workc, mode, fromGemeinde + "_" + toGemeinde);
		}
	}

	/**
	 * @param i
	 *          : ID der Person, die als MatSimAgent erzeugt werden soll.
	 * @param coord
	 *          : Koordinaten des Wohnortes der Person.
	 * @param coordWork
	 *          : Koordinaten des Arbeitsplatzes der Person.
	 * @param mode
	 *          : Fahrzeugart, die die Person benutzt.
	 * @param toFromPrefix
	 *          : Kombinierte Kennzeichnung des Abfahrts- bzw. Ankunftsortes der
	 *          Person als Pendler.
	 * 
	 *          Diese Methode erzeugt einen MatSimAgenten aus den
	 *          Pendler-input-Daten und fuegt diesen dem "scenario" hinzu. Dazu
	 *          wird zunaechst eine Reihe von Aktivitaeten erzeugt(home -> work ->
	 *          home). Diese werden anschließend in einem Plan gespeichert. Der
	 *          Plan wird einer Person hinzugefuegt und diese wiederum der
	 *          population des scenarios.
	 */
	private void createOnePerson(int i, Coord coord, Coord coordWork,
			String mode, String toFromPrefix) {

		Person person = scenario.getPopulation().getFactory()
				.createPerson(new IdImpl(toFromPrefix + "_" + i));

		Plan plan = scenario.getPopulation().getFactory().createPlan();
		Activity home = scenario.getPopulation().getFactory()
				.createActivityFromCoord("home", coord);
		// create random time to leave home
		double start = Math.random() * 3 + 6; // starting time between 6 and 9
		double end = Math.random() * 3 + 7; // end time between start+7 and start+10

		end = end + start;
		home.setEndTime(start * 60 * 60);
		plan.addActivity(home);
		Leg hinweg = scenario.getPopulation().getFactory().createLeg(mode);
		plan.addLeg(hinweg);
		Activity work = scenario.getPopulation().getFactory()
				.createActivityFromCoord("work", coordWork);
		// work.setStartTime(start * 60 * 60);
		work.setEndTime(end * 60 * 60);
		plan.addActivity(work);
		Leg rueckweg = scenario.getPopulation().getFactory().createLeg(mode);
		plan.addLeg(rueckweg);
		Activity home2 = scenario.getPopulation().getFactory()
				.createActivityFromCoord("home", coord);
		plan.addActivity(home2);
		person.addPlan(plan);
		scenario.getPopulation().addPerson(person);
	}

	/**
	 * Erstellt AgentenPläne auf Basis einer ModalSplitTabelle
	 */
	@SuppressWarnings("unchecked")
	private void createModalSplitPopulationNew() {

		TrafficParticipantsParser tpp = new TrafficParticipantsParser();

		tpp.readData(MODALSPLIT_TABLE);

		/*
		 * Erstelle Liste, die alle Verkehrsteilnehmergruppen als
		 * TrafficParticipants speichert.
		 */
		List<TrafficParticipants> allParticipants = tpp.getParticipantgroups();
		TrafficParticipants participantgroup = new TrafficParticipants();

		/*
		 * Teile die Liste in zwei ArrayLists nach dem Geschlecht der
		 * Verkehrsteilnehmer auf und sortiere sie anschließend nach deren Alter.
		 */
		List<TrafficParticipants> male = new ArrayList<TrafficParticipants>();
		List<TrafficParticipants> female = new ArrayList<TrafficParticipants>();

		Comparator<TrafficParticipants> compareAge = new CompareAge();
		for (int i = 0; i < allParticipants.size(); i++) {
			participantgroup = allParticipants.get(i);

			if (participantgroup.getSex().equals("m")) {
				male.add(participantgroup);
			} else {
				female.add(participantgroup);
			}
		}

		Collections.sort(male, compareAge);
		for (int i = 0; i < male.size(); i++) {
		}
		Collections.sort(female, compareAge);
		for (int i = 0; i < female.size(); i++) {
		}

		/*
		 * Iteriere über die Liste männlicher Verkehrsteilnehmer und betrachte die
		 * einzelnen Altersgruppen separat. Damit ist Geschlecht und Alter eines
		 * MatSimagenten schon festgelegt. Des Weiteren werden bei der Erstellung
		 * einer Person 2 - 4 Aktivitäten erzeugt.
		 */
		String[] activityTypes = { "work", "businessRelatedTravel", "education",
				"shopping", "errand", "leisure", "attendance" };

		Random rnd = MatsimRandom.getLocalInstance();

		int age = 0;
		String sex = "";

		for (int i = 0; i < male.size(); i = i + 4) {

			int activitiesPerDay = 2 + rnd.nextInt(2);
			int randomTrafficMode = rnd.nextInt(4);
			int randomactivity = rnd.nextInt(6);

			TrafficParticipants maleParticipants = male.get(i + randomTrafficMode);
			String mode = maleParticipants.getMode();
			age = maleParticipants.getAge();
			sex = "m";

			while (!(activitiesPerDay == 0)) {
				/*
				 * Lege eine Map für jede Altersgruppe von Verkehrsteilnehmern an, in
				 * der die Anzahl von Personen zu den versch. Wegezwecken gespeichert
				 * werden.
				 */
				Map<String, Integer> activities = new HashMap<String, Integer>();

				activities.put("attendance", participantgroup.getAttendance());
				activities.put("businessRelatedTravel",
						participantgroup.getBusinessRelatedTravel());
				activities.put("education", participantgroup.getEducation());
				activities.put("errand", participantgroup.getErrand());
				activities.put("leisure", participantgroup.getLeisure());
				activities.put("shopping", participantgroup.getShopping());
				activities.put("work", participantgroup.getWork());

				System.out.println("acts:   " + (activities.entrySet()));

				// performingPeople = number of persons performing a certain activity
				// int performingPeople = 0;
				String act = "";
				int numberOfZeroPerformancers = 0;

				/*
				 * Diese while-schleife läuft nur so lange, wie es noch values in der
				 * activities-map gibt, die nicht Null sind, d.h. so lange noch
				 * performingPeople übrig sind. Bei jedem Fund eines nicht-Null-values
				 * wird dieser um einen Wert gesenkt. Sind alle Values gleich Null, so
				 * wurden allen performers schon genügend activities zugewiesen.
				 * PROBLEM: die schleife stimmt noch nicht. bisher wird nur abgefragt,
				 * ob noch activities vergeben werden können, aber nciht, welche. Dann
				 * fehlt noch, dass die Personen, plans usw. konkret erstellt werden.
				 */
				while (numberOfZeroPerformancers < 7) {
					act = activityTypes[randomactivity];
					int performingPeople = activities.get(act);

					Integer zero = new Integer(0);

					for (Integer n : activities.values()) {
						if (zero.equals(n)) {
							numberOfZeroPerformancers++;
						}
					}
				}

				activitiesPerDay--;
			}
		}
	}

	private void createModalSplitPopulation() {

		TrafficParticipantsParser tpp = new TrafficParticipantsParser();

		tpp.readData(MODALSPLIT_TABLE);
		/*
		 * Erstelle Liste, in der alle Verkehrsteilnehmergruppen als
		 * TrafficParticipants gespeichert werden. Dabei besteht eine
		 * Verkehrsteilnehmergruppe aus Personen zwischen x und x+10 Jahren. Die
		 * Mitglieder einer Gruppe haben alle dasselbe Geschlecht und denselben
		 * Verkehrsmodus(walk,bike,car oder pt). Das Objekt TrafficParticipants
		 * speichert je einen Integer als Personenanzahl pro Aktivität.
		 */
		List<TrafficParticipants> allParticipants = tpp.getParticipantgroups();
		TrafficParticipants participantgroup = new TrafficParticipants();

		Random rnd = MatsimRandom.getLocalInstance();

		int age = 0;
		int personIndex = 0;
		int randomAge = 0;

		/*
		 * Iteriere über Liste aller Verkehrsteilnehmer, die aus der
		 * Modalsplittabelle ausgelesen wurden
		 */
		for (int i = 0; i < allParticipants.size(); i++) {
			participantgroup = allParticipants.get(i);

			age = participantgroup.getAge();
			String sex = participantgroup.getSex();

			/*
			 * Lege eine Map für jede Altersgruppe von Verkehrsteilnehmern an, in der
			 * die Anzahl von Personen zu den versch. Wegezwecken gespeichert werden.
			 */
			Map<String, Integer> activities = new HashMap<String, Integer>();

			activities.put("attendance", participantgroup.getAttendance());
			activities.put("businessRelatedTravel",
					participantgroup.getBusinessRelatedTravel());
			activities.put("education", participantgroup.getEducation());
			activities.put("errand", participantgroup.getErrand());
			activities.put("leisure", participantgroup.getLeisure());
			activities.put("shopping", participantgroup.getShopping());
			activities.put("work", participantgroup.getWork());

			System.out.println("acts:   " + (activities.entrySet()));
			Iterator<Entry<String, Integer>> entries = activities.entrySet()
					.iterator();

			/*
			 * Iteriere ueber die activities map
			 */
			while (entries.hasNext()) {
				Entry<String, Integer> thisEntry = (Entry<String, Integer>) entries
						.next();
				String act = (String) thisEntry.getKey();
				Integer value = (Integer) thisEntry.getValue();

				/*
				 * Iteriere ueber die Personen mit gleichem Alter und selbem Wegezweck.
				 * IdImpl bekommt einen String als Argument.
				 */
				for (int j = 0; j < value - 1; j++) {
					Person person = scenario.getPopulation().getFactory()
							.createPerson(new IdImpl(personIndex));
					personIndex++;

					// Kinder, die jünger als Schulkinder sind, werden nicht
					// berücksichtigt.
					if (age == 0) {
						randomAge = 6 + rnd.nextInt(3);
					}
					randomAge = age + rnd.nextInt(9);
					((PersonImpl) person).setSex(sex);
					((PersonImpl) person).setAge(Integer.valueOf(randomAge));

					Coord homeCoord = drawRandomCoordinteInGarmischCounty();
					Coord arrivalCoord = drawRandomCoordinteInGarmischCounty();

					// activity: home
					Plan plan = scenario.getPopulation().getFactory().createPlan();
					Activity home = scenario.getPopulation().getFactory()
							.createActivityFromCoord("home", homeCoord);

					double go = Math.random() * 10 + 8; // starting time between 10 and 13
					double stop = Math.random() * 10 + 12; // end time between 12 and 17

					home.setEndTime(go * 60 * 60);
					plan.addActivity(home);

					String mode = participantgroup.getMode();

					Leg hinweg = scenario.getPopulation().getFactory().createLeg(mode);
					plan.addLeg(hinweg);

					/*
					 * erstelle Activity (act = key des aktuellen Eintrages der
					 * activities-map)
					 */
					if (act.equals("education")) {
						Activity education = scenario.getPopulation().getFactory()
								.createActivityFromCoord("education", arrivalCoord);
						education.setEndTime(stop * 60 * 60);
						plan.addActivity(education);
					} else if (act.equals("leisure") || act.equals("attendance")) {
						Activity leisure = scenario.getPopulation().getFactory()
								.createActivityFromCoord("leisure", arrivalCoord);
						leisure.setEndTime(stop * 60 * 60);
						plan.addActivity(leisure);
					} else if (act.equals("work") || act.equals("businessRelatedTravel")) {
						Activity work = scenario.getPopulation().getFactory()
								.createActivityFromCoord("work", arrivalCoord);
						work.setEndTime(stop * 60 * 60);
						plan.addActivity(work);
					} else if (act.equals("errand") || act.equals("shopping")) {
						Activity shopping = scenario.getPopulation().getFactory()
								.createActivityFromCoord("shopping", arrivalCoord);
						shopping.setEndTime(stop * 60 * 60);
						plan.addActivity(shopping);
					}
					Leg rueckweg = scenario.getPopulation().getFactory().createLeg(mode);
					plan.addLeg(rueckweg);
					Activity home2 = scenario.getPopulation().getFactory()
							.createActivityFromCoord("home", homeCoord);
					plan.addActivity(home2);
					person.addPlan(plan);
					scenario.getPopulation().addPerson(person);

				}
			}
		}

	}

	/**
	 * erzeugt MatSim-Agenten aus den Daten einer Tabelle,die Informationen über
	 * das Verkehrsverhalten von Menschen verschiedener Altersgruppen im
	 * ländlichen Raum enthält. Hierbi werden Personen in Gruppen zusammengefasst,
	 * die das gleiche Alterund das selbe Geschlecht, sowie den gleichen
	 * Verkehrsmodus benutzen. Ueber Aktivitäten wird keine Aussage gemacht.
	 * Allerdings ist die durchschnittliche Anzahl an Wegen pro Person für die
	 * verschiedenen Personengruppen vorhanden.
	 */
	private void createPopulationFromModalSplitVorlaeufig() {
		/*
		 * Read the census file Create the persons and add the socio-demographics
		 */
		TrafficPeopleParser tpp = new TrafficPeopleParser();

		tpp.readData(TRAFFIC_TABLE);

		/*
		 * get list of traffic-participants-data
		 */
		List<TrafficPeopleMode> allParticipants = tpp.getParticipantgroups();

		// manuelle Fehlerausbesserung: weibliches Geschlecht wird vom Parser nicht
		// erkannt
		// for (int i = 9; i <= 17; i++) {
		// allParticipants.get(i).setSex(Sex.f);
		// }

		TrafficPeopleMode participantgroup = new TrafficPeopleMode();
		int personIndex = 0;
		int age = 0;
		Random rnd = MatsimRandom.getLocalInstance();
		int randomAge = 0;
		int randomMapentry = 0;
		/*
		 * Create persons and add them to the population
		 */
		for (int i = 0; i < allParticipants.size(); i++) {
			participantgroup = allParticipants.get(i);

			age = participantgroup.getAge();
			String sex = participantgroup.getSex();

			/*
			 * Erzeugung der Agenten, die zu Fuß gehen
			 */
			for (int j = 0; j < participantgroup.getPedestrian() * SCALEFACTOR; j++) {

				Person person = scenario.getPopulation().getFactory()
						.createPerson(new IdImpl(personIndex));
				personIndex++;
				randomAge = rnd.nextInt(age + 9);
				((PersonImpl) person).setSex(sex);
				((PersonImpl) person).setAge(Integer.valueOf(randomAge));

				Coord homeCoord = drawRandomCoordinteInGarmischCounty();
				Coord arrivalCoord = drawRandomCoordinteInGarmischCounty();

				Plan plan = scenario.getPopulation().getFactory().createPlan();
				Activity home = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);

				double go = Math.random() * 3 + 10; // starting time between 10 and 13
				double stop = Math.random() * 5 + 12; // end time between 12 and 17

				home.setEndTime(go * 60 * 60);
				plan.addActivity(home);
				Leg hinweg = scenario.getPopulation().getFactory().createLeg("walk");
				plan.addLeg(hinweg);
				Activity education = scenario.getPopulation().getFactory()
						.createActivityFromCoord("education", arrivalCoord);
				education.setEndTime(stop * 60 * 60);
				plan.addActivity(education);
				Leg rueckweg = scenario.getPopulation().getFactory().createLeg("walk");
				plan.addLeg(rueckweg);
				Activity home2 = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);
				plan.addActivity(home2);
				person.addPlan(plan);
				scenario.getPopulation().addPerson(person);
			}

			/*
			 * Erzeugung der Agenten, mit dem Rad fahren
			 */
			for (int j = 0; j < participantgroup.getBike() * SCALEFACTOR; j++) {

				Person person = scenario.getPopulation().getFactory()
						.createPerson(new IdImpl(personIndex));
				personIndex++;
				randomAge = rnd.nextInt(age + 9);
				((PersonImpl) person).setSex(sex);
				((PersonImpl) person).setAge(Integer.valueOf(randomAge));

				Coord homeCoord = drawRandomCoordinteInGarmischCounty();
				Coord arrivalCoord = drawRandomCoordinteInGarmischCounty();

				Plan plan = scenario.getPopulation().getFactory().createPlan();
				Activity home = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);

				double go = Math.random() * 3 + 9; // starting time between 9 and 12
				double stop = Math.random() * 5 + 14; // end time between 14 and 19

				home.setEndTime(go * 60 * 60);
				plan.addActivity(home);
				Leg hinweg = scenario.getPopulation().getFactory().createLeg("bike");
				plan.addLeg(hinweg);
				Activity shopping = scenario.getPopulation().getFactory()
						.createActivityFromCoord("shopping", arrivalCoord);
				shopping.setEndTime(stop * 60 * 60);
				plan.addActivity(shopping);
				Leg rueckweg = scenario.getPopulation().getFactory().createLeg("bike");
				plan.addLeg(rueckweg);
				Activity home2 = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);
				plan.addActivity(home2);
				person.addPlan(plan);
				scenario.getPopulation().addPerson(person);
			}

			/*
			 * Erzeugung der Agenten, mit dem Auto fahren
			 */
			for (int j = 0; j < participantgroup.getCar() * SCALEFACTOR; j++) {

				Person person = scenario.getPopulation().getFactory()
						.createPerson(new IdImpl(personIndex));
				personIndex++;
				randomAge = rnd.nextInt(age + 9);
				((PersonImpl) person).setSex(sex);
				((PersonImpl) person).setAge(Integer.valueOf(randomAge));

				Coord homeCoord = drawRandomCoordinteInGarmischCounty();
				Coord arrivalCoord = drawRandomCoordinteInGarmischCounty();

				Plan plan = scenario.getPopulation().getFactory().createPlan();
				Activity home = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);

				double go = Math.random() * 2 + 13; // starting time between 13 and 15
				double stop = Math.random() * 3 + 15; // end time between 15 and 18

				home.setEndTime(go * 60 * 60);
				plan.addActivity(home);
				Leg hinweg = scenario.getPopulation().getFactory().createLeg("car");
				plan.addLeg(hinweg);
				Activity work = scenario.getPopulation().getFactory()
						.createActivityFromCoord("work", arrivalCoord);
				work.setEndTime(stop * 60 * 60);
				plan.addActivity(work);
				Leg rueckweg = scenario.getPopulation().getFactory().createLeg("car");
				plan.addLeg(rueckweg);
				Activity home2 = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);
				plan.addActivity(home2);
				person.addPlan(plan);
				scenario.getPopulation().addPerson(person);
			}

			/*
			 * Erzeugung der Agenten, die den oeffentlichen Verkehr nutzen
			 */
			for (int j = 0; j < participantgroup.getPt() * SCALEFACTOR; j++) {

				Person person = scenario.getPopulation().getFactory()
						.createPerson(new IdImpl(personIndex));
				personIndex++;
				randomAge = rnd.nextInt(age + 9);
				((PersonImpl) person).setSex(sex);
				((PersonImpl) person).setAge(Integer.valueOf(randomAge));

				Coord homeCoord = drawRandomCoordinteInGarmischCounty();
				Coord arrivalCoord = drawRandomCoordinteInGarmischCounty();

				Plan plan = scenario.getPopulation().getFactory().createPlan();
				Activity home = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);

				double go = Math.random() * 3 + 8; // starting time between 8 and 11
				double stop = Math.random() * 4 + 16; // end time between 16 and 20

				home.setEndTime(go * 60 * 60);
				plan.addActivity(home);
				Leg hinweg = scenario.getPopulation().getFactory().createLeg("pt");
				plan.addLeg(hinweg);
				Activity leisure = scenario.getPopulation().getFactory()
						.createActivityFromCoord("leisure", arrivalCoord);
				leisure.setEndTime(stop * 60 * 60);
				plan.addActivity(leisure);
				Leg rueckweg = scenario.getPopulation().getFactory().createLeg("pt");
				plan.addLeg(rueckweg);
				Activity home2 = scenario.getPopulation().getFactory()
						.createActivityFromCoord("home", homeCoord);
				plan.addActivity(home2);
				person.addPlan(plan);
				scenario.getPopulation().addPerson(person);
			}

		}
	}

	/**
	 * Filtert aus den keys der inputMap die Kreisschluessel der Start-/Zielorte
	 * von Pendlergruppen heraus und erweitert diese zu passenden
	 * Random-Gemeindeschluesseln aus dem gegebenen Kreis. Die Gemeindeschluessel
	 * werden dann mit zugehörigem Start- oder Zielort-Gemeindeschluessel und
	 * zugehörigem inputMap-value an writePlansFor() weitergegeben. Dort werden
	 * Pläne erstellt.
	 * 
	 * @param relationsMap
	 *          map mit key: Gemeindeschluessel von Start & Kreisschluessel von
	 *          Ziel der Pendler (oder umgekehrt, je nachdem, ob Ein- oder
	 *          Auspendler- input-map), value: Anzahl Pendler mit diesem
	 *          Start/Ziel.
	 * @param auspendler
	 *          boolean, gibt an, ob die input-map Daten zu Ein- oder Auspendlern
	 *          enthaelt.
	 */
	private void findKeysAndWritePlansForUebrigePendler(
			Map<String, Integer> relationsMap, boolean auspendler) {

		String[] pendlerGemeindeschluessel;
		if (auspendler) {
			pendlerGemeindeschluessel = new String[relationsAuspendler.size()];
			pendlerGemeindeschluessel = pendlerAsArray(relationsAuspendler,
					auspendler);
		} else {
			pendlerGemeindeschluessel = new String[relationsEinpendler.size()];
			pendlerGemeindeschluessel = pendlerAsArray(relationsEinpendler,
					auspendler);
		}

		if (shapeMap == null)
			this.shapeMap = readShapeFile(GEMEINDEN, "SCH");

		int[] gemeindeschluessel = new int[shapeMap.size()];
		// gibt ein sortiertes int-Array aller Gemeindeschluessel zurueck - hier nur
		// 5-stellig, weil UebrigePendler
		gemeindeschluessel = erzeugeGemeindeschluessel();

		// erzeuge Listen für bestimmte Gemeindeschluessel
		ArrayList<String> bayern0916 = new ArrayList<String>();
		ArrayList<String> bayern0917 = new ArrayList<String>();
		ArrayList<String> bayern0918 = new ArrayList<String>();
		ArrayList<String> bayern0919 = new ArrayList<String>();
		ArrayList<String> schwaben09777 = new ArrayList<String>();

		/*
		 * Wir iterieren ueber das gemeindeschluessel-Array, und suchen alle
		 * Schluessel, die mit 091.. und mit 09777 beginnen heraus. Diese werden in
		 * die oben erzeugten Arraylists verteilt, sodass spaeter randomisierte
		 * Werte aus diesen Listen verwendet werden koennen.
		 */
		for (int i = 0; i < gemeindeschluessel.length; i++) {

			int gemeindeschl = gemeindeschluessel[i];
			// beim konvertieren nach int ging die anfangs-0 verloren, diese wird hier
			// wieder vorne angehaengt
			String gemeindeschlToString = "0" + Integer.toString(gemeindeschl);

			// Gemeindeschluessel an der Position 3
			String gemeindeschluesselPos3 = String.valueOf(gemeindeschlToString
					.charAt(2));
			// erste 5 Stellen des Gemeindeschluessels
			String gemeindeschluesselPos0_5 = gemeindeschlToString.substring(0, 5);

			if (gemeindeschluesselPos3.equals("1")
					&& !gemeindeschluesselPos0_5.equals("09180")) {
				int ziffer = Integer.valueOf(String.valueOf(gemeindeschlToString
						.charAt(3)));

				switch (ziffer) {
				case 6:
					bayern0916.add(gemeindeschlToString);
					break;
				case 7:
					bayern0917.add(gemeindeschlToString);
					break;
				case 8:
					bayern0918.add(gemeindeschlToString);
					break;
				case 9:
					bayern0919.add(gemeindeschlToString);
					break;
				}
			} else if (gemeindeschlToString.substring(2, 5).equals("777")) {
				schwaben09777.add(gemeindeschlToString);
			} else {
				continue;
			}
		}

		/*
		 * Wir iterieren ueber die relationsMap, deren keys Informationen ueber die
		 * Gemeindeschluessel von Abfahrts-/Ankunftsort der Pendlers enthalten. Aus
		 * jedem key der map wird die Kreiszahl des Schluessel ("to" oder "from", je
		 * nachdem ob Aus- oder Einpendler) herausgelesen.
		 */
		Random rnd = MatsimRandom.getLocalInstance();
		String kreiszahl;
		String from;
		String to;

		Iterator<Entry<String, Integer>> entries = relationsMap.entrySet()
				.iterator();

		while (entries.hasNext()) {
			Entry<String, Integer> thisEntry = (Entry<String, Integer>) entries
					.next();
			Integer value = (Integer) thisEntry.getValue();

			String relation = (String) thisEntry.getKey();
			kreiszahl = liesKreiszahl(relation, auspendler);

			/*
			 * Wir veraendern die Kreiszahl (5-stellig) und machen einen 8-stelligen
			 * Gemeindeschluessel daraus. Dazu benutzen wir die oben angelegten
			 * arrayLists. Wir holen aus der jeweiligen Liste einen randomSchluessel
			 * heraus und pruefen, ob die Kreiszahl in diesem enthalten ist. Wenn ja,
			 * haengen wir die letzten 3 ziffern des randomWertes hinten an die
			 * Kreiszahl an. Allerdings unter der Bedingung, dass dieser Schluessel in
			 * Kombination mit dem zugehörigen Start/Ziel vorher noch nicht verwendet
			 * wurde, da sonst Agenten mit gleicher ID auftreten können (wirft
			 * Exception).
			 */

			String schluessel = new String();

			while (true) {
				int random = 0;
				// Kreiszahl an der Position 3
				String kreiszahlPos3 = String.valueOf(kreiszahl.charAt(2));
				// Kreiszahl an der Position 4
				String kreiszahlPos4 = String.valueOf(kreiszahl.charAt(3));

				if (kreiszahlPos3.equals("1")) {

					int ziffer = Integer.valueOf(kreiszahlPos4);
					switch (ziffer) {
					case 6:
						random = rnd.nextInt(bayern0916.size());
						schluessel = bayern0916.get(random);
						break;
					case 7:
						random = rnd.nextInt(bayern0917.size());
						schluessel = bayern0917.get(random);
						break;
					case 8:
						random = rnd.nextInt(bayern0918.size());
						schluessel = bayern0918.get(random);
						break;
					case 9:
						random = rnd.nextInt(bayern0919.size());
						schluessel = bayern0919.get(random);
						break;
					}
				} else if (kreiszahlPos3.equals("7")) {

					if (kreiszahlPos4.equals("6")) {
						schluessel = kreiszahl + "000";
					}
					if (kreiszahlPos4.equals("7")) {
						random = rnd.nextInt(schwaben09777.size());
						schluessel = schwaben09777.get(random);
					}
				}
				if (schluessel.substring(0, 5).equals(kreiszahl)
						&& !Arrays.asList(pendlerGemeindeschluessel).contains(schluessel)) {
					kreiszahl = schluessel;
					break;
				} else {
					continue;
				}
			}
			if (auspendler) {
				to = kreiszahl;
				from = liesStart(relation);
			} else {
				from = kreiszahl;
				to = liesZiel(relation);
			}
			writePlansFor(from, to, value, "");
		}
	}

	/**
	 * die Maps Ein-/Auspendler enthalten als Keys Eintraege der Form
	 * "Start - Ziel" (relations), wobei Start und Ziel 8-stellige
	 * Gemeindeschluessel sind (Strings).
	 */

	/**
	 * @param relation
	 *          Kombination zweier Schluessel aus Pendler-map
	 * @param auspendler
	 *          true, wenn die inputmap, aus der relation stammt, Auspendlerdaten
	 *          enthaelt. false, wenn sie Einpendlerdaten enthaelt.
	 * @return die Kreiszahl des Landkreises, in dem die Gemeinde liegt, in die
	 *         der Agent aus Garmisch pendelt, bzw. aus der er nach Garmisch
	 *         pendelt.
	 */
	private String liesKreiszahl(String relation, boolean auspendler) {
		String kreiszahl;

		if (auspendler) {
			kreiszahl = relation.substring(relation.length() - 5, relation.length());
		} else {
			kreiszahl = relation.substring(0, 5);
		}
		return kreiszahl;
	}

	/**
	 * @param relation
	 *          Kombination zweier Schluessel aus Einpendler-map
	 * @return den Startpunkt der Pendler des zugehörigen map-Eintrags als
	 *         8-stelligen Gemeindeschluessel
	 */
	private String liesStart(String relation) {
		return relation.substring(0, 8);
	}

	/**
	 * @param relation
	 *          Kombination zweier Schluessel aus Auspendler-map
	 * @return den Zielort der Pendler des zugehörigen map-Eintrags als
	 *         8-stelligen Gemeindeschluessel
	 */
	private String liesZiel(String relation) {
		return relation.substring(relation.length() - 8, relation.length());
	}

	/**
	 * @param relationsMap
	 *          map, aus deren keys Start- oder Zielorte (8-stellige Strings)
	 *          herausgefiltert und in einem Array gespeichert werden, je nachdem,
	 *          ob es sich beim input um Ein- oder Auspendler handelt.
	 * @param auspendler
	 *          boolean, gibt an, ob die input-map Daten zu Ein- oder Auspendlern
	 *          enthaelt.
	 * @return Array, welches die gefilterten Informationen aus der map enthaelt.
	 */
	private String[] pendlerAsArray(Map<String, Integer> relationsMap,
			boolean auspendler) {

		String[] pendlerGemeindeschluessel = new String[relationsMap.size()];

		Iterator<Entry<String, Integer>> entries = relationsMap.entrySet()
				.iterator();
		int i = 0;
		while (entries.hasNext()) {
			Entry<String, Integer> thisEntry = (Entry<String, Integer>) entries
					.next();
			String key;
			if (auspendler) {
				key = liesZiel(thisEntry.getKey());
			} else {
				key = liesStart(thisEntry.getKey());
			}
			pendlerGemeindeschluessel[i] = key;
			i++;
		}
		return pendlerGemeindeschluessel;
	}

	/**
	 * Diese Methode speichert die Schluessel der shapeMap, d.h. alle existenten
	 * Gemeindeschuessel Bayerns, in einem Array ab. Dieses wird anschließend
	 * sortiert. Wichtig: Da die Gemeindeschluessel als Integer gespeichert
	 * werden, faellt die 0 am Anfang jedes Schluessels weg.
	 * 
	 * @return Array, welches alle Gemeindeschluessel als ints enthaelt.
	 */
	private int[] erzeugeGemeindeschluessel() {

		String[] shapeAsArray = new String[shapeMap.size()];
		int[] shapeAsIntArray = new int[shapeMap.size()];

		shapeMap.keySet().toArray(shapeAsArray);

		for (int i = 0; i < shapeAsArray.length; i++) {
			shapeAsIntArray[i] = Integer.parseInt(shapeAsArray[i]);
		}
		Arrays.sort(shapeAsIntArray);

		return shapeAsIntArray;
	}

	/**
	 * @return random coordinate in the area of Garmisch-Partenkirchn county.
	 */
	private Coord drawRandomCoordinteInGarmischCounty() {
		if (buildingsMap == null)
			this.buildingsMap = readShapeFile(BUILDINGS, "osm_id");
		if (buildingsAsArray == null) {
			buildingsAsArray = new Geometry[buildingsMap.size()];
			buildingsMap.values().toArray(buildingsAsArray);
		}

		Random rnd = MatsimRandom.getLocalInstance();
		int randomMapentry = rnd.nextInt(buildingsAsArray.length - 1);
		Geometry randomGeom = buildingsAsArray[randomMapentry];
		Coord coord = drawRandomPointFromGeometry(randomGeom);

		return coord;
	}

	/**
	 * @param g
	 *          Geometry einer bestimmten Gemeinde.
	 * @return Koordinaten eines zufaellig gewaehlten Punktes, der in der
	 *         input-Geometry g enthalten ist.
	 */
	private Coord drawRandomPointFromGeometry(Geometry g) {
		Random rnd = MatsimRandom.getLocalInstance();
		Point p;
		double x, y;
		do {
			x = g.getEnvelopeInternal().getMinX()
					+ rnd.nextDouble()
					* (g.getEnvelopeInternal().getMaxX() - g.getEnvelopeInternal()
							.getMinX());

			y = g.getEnvelopeInternal().getMinY()
					+ rnd.nextDouble()
					* (g.getEnvelopeInternal().getMaxY() - g.getEnvelopeInternal()
							.getMinY());

			p = MGC.xy2Point(x, y);
		} while (!g.contains(p));
		Coord coord = new CoordImpl(p.getX(), p.getY());
		return coord;
	}

	/**
	 * @param landkreis
	 *          : Landkreis, in dem wir ein Gebaeude suchen, das zufaellig
	 *          ausgewaehlt wurde.
	 * @param buildings
	 *          : Array, das die Koordinaten aller Gebaeuden enthaelt.
	 * @return ein Gebaeude, welches zufaellig ausgewaehlt wurde und im
	 *         input-Landkreis liegt.
	 */
	private Coord drawRandomPointInBuilding(Geometry gemeinde,
			Geometry[] buildings) {
		Random rnd = MatsimRandom.getLocalInstance();
		Coord result;

		int size = buildings.length;

		while (true) {
			int random = rnd.nextInt(size);
			Geometry building = buildings[random];

			if (gemeinde.contains(building)) {
				Point center = building.getCentroid();
				result = new CoordImpl(center.getX(), center.getY());
				break;
			} else
				continue;
		}
		return result;
	}

	public static void main(String[] args) {

		CreateDemandForGarmisch cd = new CreateDemandForGarmisch();
		cd.run();
	}

	/**
	 * @param filename
	 *          : Dateiname der .shp-Datei
	 * @param attrString
	 *          : Name des gewuenschten Attributs aus der .shp-Datei
	 * @return eine Map, die geometrische Daten aus der .shp-Datei als geometry
	 *         speichert und dazugehoerige .shp-Attribute als Schluessel.
	 */
	public Map<String, Geometry> readShapeFile(String filename, String attrString) {
		// attrString: Fuer Brandenburg: Nr
		// fuer OSM: osm_id
		Map<String, Geometry> shapeMap = new HashMap<String, Geometry>();
		for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {

			GeometryFactory geometryFactory = new GeometryFactory();
			WKTReader wktReader = new WKTReader(geometryFactory);
			Geometry geometry;

			try {
				geometry = wktReader.read((ft.getAttribute("the_geom")).toString());
				shapeMap.put(ft.getAttribute(attrString).toString(), geometry);

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return shapeMap;
	}

}
