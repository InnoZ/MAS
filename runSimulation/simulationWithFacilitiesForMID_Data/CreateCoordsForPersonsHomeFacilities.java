package simulationWithFacilitiesForMID_Data;

import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author yasemin
 * 
 *         reads a shapefile, calculates probabilities from the shape-attributes
 *         and stores them in public arrays. Every probability gives a statement
 *         about the probability with that a person in a given age lives in a
 *         special municipality. array == group of age array index == special
 *         municipality
 */
public class CreateCoordsForPersonsHomeFacilities {

	private static final String GAP = "./input/Geodaten/gap_plzStruktWGS84/gap_plzStruktWGS84.shp";

	/**
	 * every row-index of the Matrix stands for a certain age group and every
	 * column-index represents a certain municipality -> every matrix-entry
	 * (i,j) contains something like the probability that a person belonging to
	 * the age class of index i is living in the municipality j. More precisely:
	 * The entry contains the above described probability plus the value from
	 * entry (i,j-1) -> the probability how it is described above can be
	 * calculated by value of entry(i,j) -> value of entry(i, j-1).
	 */
	private double[][] probabilitiesMatrix = new double[4][31];
	private Geometry[] geometries;
	private Random random = new Random(); 


	/**
	 * fills the rows of the probabilitiesMatrix with probabilities and the
	 * entries of geometries-array with Geometries.
	 */
	public void run() {
		/*
		 * read populationsize for every municipality dependent on age of people
		 */
		int[] numberPersCertainAge = new int[31];

		readShapeAndWriteProb(numberPersCertainAge, 1);
		readShapeAndWriteProb(numberPersCertainAge, 2);
		readShapeAndWriteProb(numberPersCertainAge, 3);
		readShapeAndWriteProb(numberPersCertainAge, 4);

		this.geometries = readShapeFileGeometry(GAP);
	}

	/**
	 * @param numberPersCertainAge
	 *            contains in its entries the populationsize of municipalities
	 *            for people in a certain age
	 * @param attributeIndex
	 *            the case-marker which leads to the headString(attribute) of
	 *            read-row from the attributetable of shapefile
	 */
	private void readShapeAndWriteProb(int[] numberPersCertainAge,
			int attributeIndex) {

		String attribute;
		switch (attributeIndex) {

		case 1: // write probabilities for people at the age between 0-20 to
				// live in certain areas
			attribute = "ALT0BU20";
			numberPersCertainAge = readShapeFileAttribute(GAP, attribute);
			probabilitiesMatrix[0] = calculateProbabilitiesForDeterminedAgeGroup(numberPersCertainAge);
			break;

		case 2: // write probs for people at the age between 20-40
			attribute = "ALT20BU40";
			numberPersCertainAge = readShapeFileAttribute(GAP, attribute);
			probabilitiesMatrix[1] = calculateProbabilitiesForDeterminedAgeGroup(numberPersCertainAge);
			break;

		case 3: // calculate probs for people at the age between 40-60
			attribute = "ALT40BU60";
			numberPersCertainAge = readShapeFileAttribute(GAP, attribute);
			probabilitiesMatrix[2] = calculateProbabilitiesForDeterminedAgeGroup(numberPersCertainAge);
			break;
		case 4: // calculate probs for people at the age of >60
			attribute = "ALT60UM";
			numberPersCertainAge = readShapeFileAttribute(GAP, attribute);
			probabilitiesMatrix[3] = calculateProbabilitiesForDeterminedAgeGroup(numberPersCertainAge);
			break;
		default:
			attribute = "Invalid attributeIndex";
			break;
		}
	}

	/**
	 * calculates the populationsize of the county for certain agegroup
	 * 
	 * @return total number of age-population of the whole county
	 */
	private int calculateTotalNumberOfCountyPopulationForCertainAge(
			int[] population) {
		int totalPopOfCounty = 0;

		// iterate over number of population of single municipalities to get
		// total number of county
		for (int i = 0; i < population.length; i++) {
			totalPopOfCounty += population[i];
		}

		return totalPopOfCounty;
	}

	/**
	 * @param numberPersCertainAge
	 *            from its entries the probabilities are calculated.
	 * @return array which contains in every entry the probability for people of
	 *         given age-group to live in a municipality. Every municipality has
	 *         its own array-index. More precisely: The first entry contains the
	 *         probability to live in the municipality 0. The entry number i
	 *         contains the sum of the probabilities to live in municipalities 0
	 *         - i.
	 */
	private double[] calculateProbabilitiesForDeterminedAgeGroup(
			int[] numberPersCertainAge) {
		double[] probability = new double[31];
		int totalPopulation = calculateTotalNumberOfCountyPopulationForCertainAge(numberPersCertainAge);

		/*
		 * calculate first prob and then iterate over numberPersCertainAge and
		 * add the other probs.
		 */
		probability[0] = ((double) numberPersCertainAge[0] * 100)
				/ totalPopulation;

		for (int i = 1; i < numberPersCertainAge.length; i++) {
			probability[i] = probability[i - 1]
					+ ((double) numberPersCertainAge[i] * 100)
					/ totalPopulation;
		}
		return probability;
	}

	/**
	 * @param filename
	 *            of shapefile.
	 * @param attribute
	 *            String-name of attribute which is to read.
	 * @return array which contains in every entry the populationsize of a
	 *         certain municipality
	 */
	private int[] readShapeFileAttribute(String filename, String attribute) {

		int[] attributes = new int[31];
		int i = 0;
		for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {
			/*
			 * read attribute and store them in array
			 */
			int numberOfPersons;

			numberOfPersons = Integer.valueOf(ft.getAttribute(attribute)
					.toString());
			attributes[i] = numberOfPersons;
			i++;
		}
		return attributes;
	}

	public double[][] getProbMatrix() {
		return this.probabilitiesMatrix;
	}

	/**
	 * @param filename
	 *            of shapefile
	 * @return array which contains Geometries. Every entry represents the
	 *         Geometry of a certain municipality.
	 */
	private Geometry[] readShapeFileGeometry(String filename) {

		Geometry[] geometries = new Geometry[31];
		int i = 0;
		for (SimpleFeature ft : ShapeFileReader.getAllFeatures(filename)) {
			/*
			 * prepare for reading Geometry
			 */
			GeometryFactory geometryFactory = new GeometryFactory();
			WKTReader wktReader = new WKTReader(geometryFactory);
			Geometry geometry;
			/*
			 * read Geometry store it in arrays
			 */
			try {
				geometry = wktReader.read((ft.getAttribute("the_geom"))
						.toString());
				geometries[i] = geometry;
				i++;

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return geometries;
	}

	public Geometry[] getGeometries() {
		return this.geometries;
	}

	private static double round(double toBeRounded, int decimalPlaces) {
		int decimal = (int) Math.pow(10, decimalPlaces);
		return ((int) (toBeRounded * decimal)) / (double) decimal;
	}

	/**
	 * @param age of a person, whose home-municipality is to be found.
	 * @return Geometry of the municipality from the shape-file.
	 */
	Geometry getMunicipality(int age) {

		int rowIndex = (int) ((double) age / 20.1);
		if(age > 60){
			rowIndex = 3;
		}
		double chooseMunicipaliy = random.nextDouble() * 100;

		int i = 0;
		if (this.probabilitiesMatrix[rowIndex][i] < chooseMunicipaliy) {
			while (this.probabilitiesMatrix[rowIndex][i] < chooseMunicipaliy) {
				i++;
			}
		}
		else if(chooseMunicipaliy < this.probabilitiesMatrix[rowIndex][i] && i == 0){
			return this.geometries[0];
		}
		return this.geometries[i - 1];
	}
	
	Coord CreateRandomCoordinateInMunicipality(Geometry municipality){
				
		//TODO  noch einbauen: Suche GebäudeKoordinaten in der Nähe von geom.getCentroid und 
		// gleiche mit facilities ab: liegt in jener Koordinate keine work-etc.-facility, 
		// so kann dort eine "home"-facility erstellt werden.
		
		Point center = municipality.getCentroid();

		int coordRandomizer = random.nextInt(100);

		Coord homeCoordinate = new CoordImpl(center.getX()+coordRandomizer, center.getY()-coordRandomizer);
		return homeCoordinate;
	}

	public static void main(String[] args) {
		Random random = new Random(); 

		CreateCoordsForPersonsHomeFacilities cp = new CreateCoordsForPersonsHomeFacilities();
		cp.run();
		String geo0 = cp.getMunicipality(15).getCentroid().toString();
		System.out.println(" alter 15 : " + geo0);
		String geo1 = cp.getMunicipality(25).getCentroid().toString();
		System.out.println(" alter 20 : " + geo1);
		String geo2 = cp.getMunicipality(45).getCentroid().toString();
		System.out.println(" alter 35 : " + geo2);
		Geometry geo3a = cp.getMunicipality(61);
			String geo3 = geo3a.getCentroid().toString();
		System.out.println(" alter 61 : " + geo3);
		Coord coord = cp.CreateRandomCoordinateInMunicipality(geo3a);
		System.out.println("coords : " + coord.toString());
	}

}
