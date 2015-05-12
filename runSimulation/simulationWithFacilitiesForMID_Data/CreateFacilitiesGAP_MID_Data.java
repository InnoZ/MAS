package simulationWithFacilitiesForMID_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.facilities.ActivityFacility;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.facilities.ActivityFacilityImpl;
import org.matsim.core.facilities.ActivityOptionImpl;
import org.matsim.core.facilities.FacilitiesWriter;
import org.matsim.core.facilities.OpeningTimeImpl;
import org.matsim.core.facilities.OpeningTime.DayType;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import com.vividsolutions.jts.geom.Geometry;

public class CreateFacilitiesGAP_MID_Data {

	private final static Logger log = Logger
			.getLogger(CreateFacilitiesGAP_MID_Data.class);
	private Scenario scenario;
	private String censusFile = "./input/CensusAndTravelsurveys/MID/census_GAP.csv";
	private String businessCensusFile = "./input/CensusAndTravelsurveys/business_census_GAP_Test.csv";
	private String OUTPUTFILE = "./input/facilitiesGAP_mitHome.xml";
	private CoordinateTransformation ct = TransformationFactory
			.getCoordinateTransformation("EPSG:4326", "EPSG:32632");
	/*
	 * stores the capacity of workers for workplaces, i.e. for facilities with
	 * type "work", and corresponding facilityId.
	 */
	private Map<Id, Integer> facilityCapacities = new HashMap<Id, Integer>();
	/*
	 * stores two types of Ids, personId and the corresponding facilityId for the
	 * persons home-facility.
	 */
	private Map<Id, Id> personHomeFacilities = new HashMap<Id, Id>();

	public static void main(String[] args) {
		CreateFacilitiesGAP_MID_Data facilitiesCreator = new CreateFacilitiesGAP_MID_Data();
		// * Create the scenario *
		Config config = ConfigUtils.createConfig();
		facilitiesCreator.scenario = ScenarioUtils.createScenario(config);
		facilitiesCreator.run();
		facilitiesCreator.write();
		log.info("Creation finished #################################");
	}

	void initAndRun(Scenario scenario) {
		/*
		 * Create the scenario
		 */
		// Config config = ConfigUtils.createConfig();
		// this.scenario = ScenarioUtils.createScenario(config);
		this.scenario = scenario;
		run();
	}

	public void run() {
		/*
		 * Read the business census for work, shop, leisure and education
		 * facilities.
		 */
		int startIndex = this.readBusinessCensus();
		
		/*
		 * Read the person census and create home facilities. Store them in personHomeFacilities-Map.
		 */
		this.readCensus(startIndex);

	}

	private int readBusinessCensus() throws NullPointerException{
		int cnt = 0;
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					this.businessCensusFile));
			String line = bufferedReader.readLine(); // skip header

			// facility_Id = 0 at start.
			int index_xCoord = 0;
			int index_yCoord = 1;
			int index_types = 2;
			int index_facilityCapacity = 3;

			// store a read line in parts[]
			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");

				Coord businessCoord = new CoordImpl(
						Double.parseDouble(parts[index_xCoord]),
						Double.parseDouble(parts[index_yCoord]));

				Coord coord = ct.transform(businessCoord);

				ActivityFacilityImpl facility = (ActivityFacilityImpl) ((ScenarioImpl) this.scenario)
						.getActivityFacilities().createAndAddFacility(new IdImpl(cnt),
								coord);

				// store all activityTypes corresponding to the current facility in types[]
				String types[] = parts[index_types].split(",");
				for (int i = 0; i < types.length; i++) {
					this.addActivityOption(facility, types[i].trim());
					// If there is a capacity for working place to read in, do so and store the capacity in facilityCapacities-Map.
					if(types[i].startsWith("w")){
						if (parts.length != index_facilityCapacity + 1) {
							throw new NullPointerException("capacity for facilityId " + facility.getId().toString() + " is missing ");
						} else{
							String capacity = parts[index_facilityCapacity];
							this.facilityCapacities.put(new IdImpl(cnt),
									Integer.valueOf(capacity));
					//		System.out.println("facId: " + facility.getId().toString() + " Corresponding capacity: " + facilityCapacities.get(facility.getId()));
						}
					}
				}
				cnt++;
			}
		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
		return cnt;
	}

	private void readCensus(int startIndex) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					this.censusFile));
			String line = bufferedReader.readLine(); // skip header

			int index_personId = 0;
			int index_age = 2;

			// facility_Id = startIndex + cnt.
			int cnt = 0;

			CreateCoordsForPersonsHomeFacilities CoordsHomeFac = new CreateCoordsForPersonsHomeFacilities();
			CoordsHomeFac.run();

			while ((line = bufferedReader.readLine()) != null) {
				String parts[] = line.split("\t");

				String ageString = parts[index_age];
				if (!ageString.equals("NULL")) {

					Id personId = new IdImpl(parts[index_personId]);
					int age = Integer.valueOf(ageString);
					Geometry municipal = CoordsHomeFac.getMunicipality(age);
					Coord homeCoordinate = CoordsHomeFac
							.CreateRandomCoordinateInMunicipality(municipal);
					Id facilityId = new IdImpl(startIndex + cnt);

					ActivityFacility facility = ((ScenarioImpl) this.scenario)
							.getActivityFacilities().createAndAddFacility(facilityId,
									homeCoordinate);
					addActivityOption(facility, "home");
					this.personHomeFacilities.put(personId, facilityId);
					cnt++;
				}
			}

		} // end try
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<Id, Integer> getCapacities() {
		return this.facilityCapacities;
	}

	public Map<Id, Id> getIDs() {
		return this.personHomeFacilities;
	}

	private void addActivityOption(ActivityFacility facility, String type) {
		((ActivityFacilityImpl) facility).createActivityOption(type);

		/*
		 * Specify the constant opening hours here for all kinds of activity.
		 */
		ActivityOptionImpl actOption = (ActivityOptionImpl) facility
				.getActivityOptions().get(type);
		OpeningTimeImpl opentime;
		if (type.equals("shop")) {
			opentime = new OpeningTimeImpl(DayType.wkday, 8.0 * 3600.0, 18.5 * 3600);
		} else if (type.equals("leisure") || type.equals("education")) {
			opentime = new OpeningTimeImpl(DayType.wk, 8.0 * 3600.0, 18.0 * 3600);
		} else if (type.equals("work")) {
			opentime = new OpeningTimeImpl(DayType.wkday, 8.0 * 3600.0, 19.0 * 3600); // [[
																																								// 1
																																								// ]]
																																								// opentime
																																								// =
																																								// null;
		}
		// home
		else {
			opentime = new OpeningTimeImpl(DayType.wk, 0.0 * 3600.0, 24.0 * 3600);
		}

		actOption.addOpeningTime(opentime);
	}

	public void write() {
		new FacilitiesWriter(((ScenarioImpl) this.scenario).getActivityFacilities())
				.write(OUTPUTFILE); // facilitiesGAP.xml.gz
	}
}
