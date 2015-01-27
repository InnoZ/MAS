package simulationWithFacilitiesForMID_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.ObjectAttributes;

import com.vividsolutions.jts.geom.Geometry;

public class CreatePopulationWithMID_Data {
private Scenario scenario;
	
	// [[ 0 ]] here you have to fill in the path of the census file
	private String censusFile = "./input/CensusAndTravelsurveys/MID/census_GAP.csv";
	private Map<Id, Id> personHomeFacilities;
	private ObjectAttributes personHomeLocations = new ObjectAttributes();
	private final static Logger log = Logger.getLogger(CreatePopulationWithMID_Data.class);

	// --------------------------------------------------------------------------
	
	public void run(Scenario scenario, Map<Id, Id> personHomeFacilities) {
		this.scenario = scenario;
		this.init(personHomeFacilities);
		this.populationCreation();
	}
	
	private void init(Map<Id,Id> personHomeFacilities) {		
		/*
		 * Build quad tree for assigning home locations. Read sozioDemographics and Geometries.
		 */
		this.personHomeFacilities = personHomeFacilities;
		}
	
	private void populationCreation() {
		/*
		 * For convenience and code readability store population and population factory in a local variable 
		 */
		Population population = this.scenario.getPopulation();   
		PopulationFactory populationFactory = population.getFactory();

		/*
		 * Read the file
		 * Create the persons and add the socio-demographics
		 */
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.censusFile));
			String line = bufferedReader.readLine(); //skip header
			
			int index_personId = 0;
			int index_sex = 1;
			int index_age = 2;
			int index_CarAvailabe = 3;
			int index_License = 4;
			int index_isEmployed = 5;
			String personId = new String();
			String sex = new String();
			String age = new String();
			String caravailability = new String();
			String license = new String();
			String isEmployed = new String();
			boolean employed;

			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");
			
				/*
				 * test, if any of the read entries is NULL. In this case skip the line.
				 */
				personId = parts[index_personId].trim();
				sex = parts[index_sex].trim();
				age = parts[index_age].trim();
				caravailability = parts[index_CarAvailabe].trim();
				license = parts[index_License].trim();
				isEmployed = parts[index_isEmployed].trim();
				
				if(! (personId.equals("NULL") || sex.equals("NULL") || age.equals("NULL") || caravailability.equals("NULL") || 
						license.equals("NULL") || isEmployed.equals("NULL")) ){ 
					
					/*
					 * Create a person, set its sociodemographic parameters and add it to the population
					 */
					Person person = populationFactory.createPerson(this.scenario.createId(personId));
					// set sex
					((PersonImpl)person).setSex("" + sex.charAt(0)); 
					// set age
					((PersonImpl)person).setAge(Integer.parseInt(age));
					// set driver's license
					if(license.equals("true")){
					((PersonImpl)person).setLicence("yes");
					} else{
						((PersonImpl)person).setLicence("no");
					}
					// set car availability
					((PersonImpl)person).setCarAvail(caravailability);
					// set employment
					employed = (parts[index_isEmployed].trim()).equals("true");
					((PersonImpl)person).setEmployed(employed);
	
					population.addPerson(person);
					/* 
					 * Get the home facility for the current person and buffer it in the ObjectAttributes-map.
					 */
					Id person_Id = new IdImpl(Integer.valueOf(personId));
					Id homeFacility_Id = this.personHomeFacilities.get(person_Id);
					ActivityFacility homeFacility = ((ScenarioImpl) scenario).getActivityFacilities().getFacilities().get(homeFacility_Id);	
	
					personHomeLocations.putAttribute(person.getId().toString(), "home", homeFacility);	
				}
			}
			
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Scenario getScenario() {
		return scenario;
	}

	public ObjectAttributes getPersonHomeLocations() {
		return personHomeLocations;
	}
}
