package garmisch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.MatsimNetworkReader;
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
 * eine Simualtion durchführt.
 * 
 * TODO: Klasse testen -> Simualtions-DurchschnittsLaufzeit?
 */
public class CreateDemandForGarmisch {

	private static final String NETWORKFILE = "inputGarmisch/network_oberbayern.xml";
	private static final String LANDKREIS = "Bayern/Gemeinden/gmd_ex.shp";

	private static final String PLANSFILEOUTPUT = "outputGarmisch/plans_garmischAlle.xml";

	private static final String BUILDINGS = "shp/actsGarmisch.shp";
//private static final String BUILDINGS = "Bayern/Bayern_buildings/buildings.shp";
	private Scenario scenario;
	private Map<String, Geometry> shapeMap;
	private Map<String, Geometry> buildingsMap;
	private static double SCALEFACTOR = 0.1;

	CreateDemandForGarmisch() {
		this.scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new MatsimNetworkReader(scenario).readFile(NETWORKFILE);
	}

	private void run() {
		
		// Pendler: Garmisch -> Garmisch
		ReadPendler rp = new ReadPendler();

		// Pendler: Garmisch -> Bayern
		ReadAuspendler ra = new ReadAuspendler();
		
		// Pendler: Bayern -> Garmisch
		ReadEinpendler re = new ReadEinpendler();

		Map<String, Integer> relationsGarmisch = new TreeMap<String, Integer>();
		Map<String, Integer> relationsEinpendler = new TreeMap<String, Integer>();
		Map<String, Integer> relationsAuspendler = new TreeMap<String, Integer>();

		// run() gibt eine map zurück mit Key: Relation homeGemeindeschlüssel - workGemeindeschlüssel,
		// Value: zugehörige Anzahl an Pendlern.
		relationsGarmisch = rp.run();
		relationsAuspendler = ra.run();			
		relationsEinpendler = re.run();


		findKeysAndWritePlans(relationsGarmisch);
		findKeysAndWritePlans(relationsAuspendler);
		findKeysAndWritePlans(relationsEinpendler);

		PopulationWriter pw = new PopulationWriter(scenario.getPopulation(),
				scenario.getNetwork());
		pw.write(PLANSFILEOUTPUT);

	}

	private void findKeysAndWritePlans(Map<String, Integer> relationsMap) {

		// Iteriere über Liste der Pendlerzahlen und rufe für jede Pendlergruppe
		// writePlansFor auf
		Iterator<Entry<String, Integer>> entries = relationsMap.entrySet()
				.iterator();

		while (entries.hasNext()) {
			Entry<String, Integer> thisEntry = (Entry<String, Integer>) entries
					.next();
			String key = (String) thisEntry.getKey();

			// finde Trennzeichen
			int bindestrich = 0;
			for (int i = 0; i < key.length(); i++) {
				if (key.charAt(i) == 45) {
					bindestrich = i;
				}
			}

			// trenne Gemeindeschlüssel nach "von", "nach"
			String from = key.substring(0, bindestrich - 1);
			String to = key.substring(bindestrich + 2);

			Integer value = (Integer) thisEntry.getValue();
			// System.out.println(thisEntry.getKey() + "\t" + thisEntry.getValue());

			writePlansFor(from, to, value);

		}
	}

	/**
	 * @param fromKreis
	 *          : Kreis, aus dem die Pendler kommen.
	 * @param toKreis
	 *          : Kreis, in den die Pendler fahren.
	 * @param commuters
	 *          : Anzahl von Pendlern, die aus fromKreis kommen und nach toKreis
	 *          fahren.
	 * 
	 *          Diese Methode schreibt die Tagespläne aller Pendler, die aus
	 *          fromKreis kommen und nach toKreis fahren. Dazu werden zunächst die
	 *          beiden shapefiles "Landkreise" und "Gebäude" im Bezug auf das
	 *          Gebiet Cottbus eingelesen. Danach werden die Pendler in zwei
	 *          Personengruppen aufgeteilt: Die erste besteht aus
	 *          Autofahrern(53%), die zweite aus Personen, die den öffetnlichen
	 *          Verkehr nutzen(47%). Zum Schluss werden zufällige Koordinaten für
	 *          Wohn- und Arbeitsorte der Personen erstellt, die so liegen, das
	 *          sich dort auch tatsächlich Gebäude befinden.
	 */
	private void writePlansFor(String fromKreis, String toKreis, int commuters) {
		System.out.println("Write Plans for: " + fromKreis + " - " + toKreis);

		if (shapeMap == null)
			this.shapeMap = readShapeFile(LANDKREIS, "SCH");
		if (buildingsMap == null)
			this.buildingsMap = readShapeFile(BUILDINGS, "LINK_ID");
	//	this.buildingsMap = readShapeFile(BUILDINGS, "osm_id");
	//	System.out.println(this.buildingsMap.keySet());

		double comm = commuters * SCALEFACTOR;
		double carcomm = 0.53 * comm;
		float roundCarcomm = Math.round(carcomm);
		int roundedCarcomm = Math.round(roundCarcomm);

		// print out keySets of maps
		// System.out.println(this.shapeMap.keySet());
		// System.out.println(this.buildingsMap.keySet());

		Geometry[] buildingsAsArray = new Geometry[buildingsMap.size()];
		buildingsMap.values().toArray(buildingsAsArray);

		for (int i = 0; i <= comm; i++) {
			String mode = "car";
			if (i > carcomm)
				mode = "pt";

			Coord homec = drawRandomPointFromGeometry(this.shapeMap.get(fromKreis));
			if(toKreis.length() == 5 && !toKreis.equals("09180")){
				toKreis = toKreis + "000";
				System.out.println(this.shapeMap.get(toKreis));
			}
			Coord workc = drawRandomPointFromGeometry(this.shapeMap.get(toKreis));

			/*
			 * Coord homec =
			 * drawRandomPointInTwoGeometries(this.shapeMap.get(fromKreis),
			 * buildingsAsArray); Coord workc =
			 * drawRandomPointInTwoGeometries(this.shapeMap.get(toKreis),
			 * buildingsAsArray);
			 */

			createOnePerson(i, homec, workc, mode, fromKreis + "_" + toKreis);
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
	 *          Diese Methode erzeugt einen MatSimAgenten aus den input-Daten und
	 *          fügt diesen dem "scenario" hinzu. Dazu wird zunächst eine Reihe
	 *          von Aktivitäten erzeugt(home -> work -> home). Diese werden
	 *          anschließend in einem Plan gespeichert. Der Plan wird einer Person
	 *          hinzugefügt und diese wiederum der population des scenarios.
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
	 * @param g
	 *          Gemeinde, aus der wir einen zufälligen Punkt auswählen.
	 * @return Koordinaten des zufällig gewählen Punktes.
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
	 *          : Landkreis, in dem wir ein Gebäude suchen, das zufällig
	 *          ausgewählt wurde.
	 * @param buildings
	 *          : Array, das die Koordinaten aller Gebäuden enthält.
	 * @return ein Gebäude, welches zufällig ausgewählt wurde und im
	 *         input-Landkreis liegt.
	 */
	private Coord drawRandomPointInTwoGeometries(Geometry landkreis,
			Geometry[] buildings) {
		Random rnd = MatsimRandom.getLocalInstance();
		Coord result;

		int size = buildings.length;

		while (true) {
			int random = rnd.nextInt(size);
			Geometry building = buildings[random];

			if (landkreis.contains(building)) {
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
	 *          : Name des gewünschten Attributs aus der .shp-Datei
	 * @return eine Map, die geometrische Daten aus der .shp-Datei als geometry
	 *         speichert und dazugehörige .shp-Attribute als Schlüssel.
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
