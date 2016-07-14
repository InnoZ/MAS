package innoz.io.database;

import innoz.config.Configuration;
import innoz.io.SurveyConstants;
import innoz.io.database.SurveyDatabaseParser.Subtour.subtourType;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlan;
import innoz.scenarioGeneration.population.surveys.SurveyPlanActivity;
import innoz.scenarioGeneration.population.surveys.SurveyPlanElement;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;
import innoz.scenarioGeneration.population.surveys.SurveyVehicle;
import innoz.scenarioGeneration.population.utils.HashGenerator;
import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.scenarioGeneration.utils.Hydrograph;
import innoz.utils.matsim.RecursiveStatsContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.misc.Time;

public class SurveyDatabaseParser {

	private static final Logger log = Logger.getLogger(SurveyDatabaseParser.class);
	
	private SurveyConstants constants;
	
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation){
		
		this.constants = new SurveyConstants(configuration.getDatasource());
		
		try {
			
			log.info("Parsing surveys database to create a synthetic population");
			
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL + configuration.getLocalPort() +
					"/" + DatabaseConstants.SURVEYS_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating survey households...");
					
					parseHouseholdsDatabase(connection, geoinformation, container);
					
				}
				
				log.info("Creating survey persons...");
				
				parsePersonsDatabase(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				log.info("Creating survey ways...");
				
				parseWaysDatabase(connection, configuration.isOnlyUsingWorkingDays(), container);
				
				if(configuration.isUsingVehicles() && configuration.getDatasource().equals("mid")){
				
					log.info("Creating survey cars...");
					
					parseVehiclesDatabase(connection, container);
					
				}
				
				log.info("Conversion statistics:");
				log.info("#Households in survey: " + container.getHouseholds().size());
				log.info("#Persons in survey   : " + container.getPersons().size());
				if(configuration.isUsingVehicles()){
					log.info("#Vehicles in survey  : " + container.getVehicles().size());
				}
				
				connection.close();
				
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private void parseHouseholdsDatabase(Connection connection, Geoinformation geoinformation,
			SurveyDataContainer container) throws RuntimeException, SQLException{
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(100);
	
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.households_raw" : "srv2013.households";
		
		String q = "select * from " + table;
		
		if(this.constants.getNamespace().equals("mid")){
			
			q +=  " where ";
			
			int cntOut = 0;
			
			for(Entry<Integer, Set<Integer>> entry : geoinformation.getRegionTypes().entrySet()){

				cntOut++;
				
				q += this.constants.regionType() + " = " + entry.getKey();

				if(cntOut < geoinformation.getRegionTypes().size()){

					q += " or ";
					
				}
				
			}
			
		} else {
			
			q += " where st_code=44;"; //Osnabrück 
			
		}
		
		q += ";";
		
		ResultSet set = statement.executeQuery(q);
		
		while(set.next()){
			
			String hhId = set.getString(this.constants.householdId());
			SurveyHousehold hh = new SurveyHousehold(hhId);
			
			hh.setWeight(set.getDouble(this.constants.householdWeight()));
			
			double income = set.getDouble(this.constants.householdIncomePerMonth());
			hh.setIncome(handleHouseholdIncome(income));
			
			container.getHouseholds().put(hhId, hh);
			container.incrementSumOfHouseholdWeigtsBy(hh.getWeight());
			
//			int bland = set.getInt(this.constants.bundesland());
			int rtyp = this.constants.getNamespace().equals("mid") ? set.getInt(this.constants.regionType()) : 3;
			
			if(container.getHouseholdsForRegionType(rtyp) == null){
				container.getStateId2Households().put(rtyp, new HashSet<String>());
			}
			
			container.getStateId2Households().get(rtyp).add(hhId);

		}
		
		set.close();
		statement.close();
		
		if(container.getHouseholds().isEmpty()){
			
			log.warn("The query \"" + q + "\" yielded no results...");
			log.warn("This eventually results in no population.");
			log.warn("Continuing anyway");
			
		} else {
			
			log.info("Created " + container.getHouseholds().size() + " households from surveys database.");
			
		}
		
	}
	
	/**
	 * 
	 * Parses the mid persons database
	 * 
	 * @param args
	 * 0: url</br>
	 * 1: username</br>
	 * 2: password</br>
	 * @throws SQLException 
	 * 
	 */
	private void parsePersonsDatabase(Connection connection, boolean isUsingHouseholds, boolean onlyWorkingDays,
			SurveyDataContainer container) throws SQLException{
		
		Statement statement = connection.createStatement();

		ResultSet set = null;
		String q = null;
		
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.persons_raw" : "srv2013.persons";
		
		if(isUsingHouseholds){
			
			q = "select * from " + table;
			if(onlyWorkingDays){
				q += " where " + this.constants.dayOfTheWeek() + " < 6";
			}
			set = statement.executeQuery(q);
			
		}
		
		while(set.next()){
			
			String hhId = set.getString(this.constants.householdId());
			String personId = set.getString(this.constants.personId());
			double personWeight = set.getDouble(this.constants.personWeight());
			String carAvail = set.getString(this.constants.personCarAvailability());
			String license = set.getString(this.constants.personDrivingLicense());
			String sex = set.getString(this.constants.personSex());
			String age = set.getString(this.constants.personAge());
			String employed = set.getString(this.constants.personEmployment());
			
			String carshare = "2";
			if(this.constants.getNamespace().equalsIgnoreCase("srv")){
				carshare = set.getString(this.constants.personIsCarsharingUser());
			}
			
			int personGroup = this.constants.getNamespace().equalsIgnoreCase("mid") ? set.getInt(this.constants.personGroup()) : 0; //TODO
			int phase = this.constants.getNamespace().equalsIgnoreCase("mid") ? set.getInt(this.constants.personLifephase()) : 0; //TODO
			
			SurveyPerson person = new SurveyPerson(hhId + personId, sex, age, carAvail, license, employed, this.constants, carshare);
			person.setWeight(personWeight);
			person.setPersonGroup(personGroup);
			person.setLifePhase(phase);
			
			if(isUsingHouseholds){
				
				if(!container.getHouseholds().containsKey(hhId)){
					
					continue;
					
				} else {
					
					container.getHouseholds().get(hhId).getMemberIds().add(person.getId());
					
				}
				
			}
			
			if(!container.getPersons().containsKey(person.getId())){
			
				container.getPersons().put(person.getId(), person);
				
			}
			
			//generate person hash in order to classify the current person
			String hash = HashGenerator.generateAgeGroupHash(person);
			
			if(!container.getPersonsByGroup().containsKey(hash)){
				
				container.getPersonsByGroup().put(hash, new ArrayList<SurveyPerson>());
				
			}
			
			container.getPersonsByGroup().get(hash).add(person);
			
			container.incrementSumOfPersonWeightsBy(personWeight);
			
		}
		
		set.close();
		statement.close();
		
		if(container.getPersons().isEmpty()){

			log.warn("The selected query \"" + q + "\" yielded no results...");
			log.warn("This eventually results in no population.");
			log.warn("Continuing anyway");
			
		} else {
			
			log.info("Created " + container.getPersons().size() + " persons from surveys database.");
			
		}
		
	}
	
	private void parseWaysDatabase(Connection connection, boolean onlyWorkingDays, SurveyDataContainer container) throws SQLException {
		
		Statement statement = connection.createStatement();
		
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.ways_raw" : "srv2013.ways";

		String query = "select * from " + table;
		
		if(onlyWorkingDays){
			query+=" where " + this.constants.dayOfTheWeek() + " < 6";
		}
		
		query+=" and " + this.constants.wayTravelDistance() + " <> 'NaN' and " + this.constants.wayTravelTime() + " <> 'NaN' and "
				+ this.constants.wayDeparture() + " <> 'NaN' and " + this.constants.wayArrival() + " <>'NaN' order by "
				+ this.constants.householdId() + "," + this.constants.personId() + "," + this.constants.wayId() + ";";
		
		ResultSet set = statement.executeQuery(query);
		
		int lastWayIdx = 100;
		int counter = 0;
		String lastPersonId = "";
		
		while(set.next()){
			
			String hhId = set.getString(this.constants.householdId());
			String personId = set.getString(this.constants.personId());
			SurveyPerson person = container.getPersons().get(hhId + personId);
		
			if(person != null){
				
				SurveyPlan plan = null;
				
				int currentWayIdx = set.getInt(this.constants.wayId());
				
				//if the index of the current way is lower than the previous index
				//or the currently processed way is a rbw
				//it's probably a new plan...
				if(currentWayIdx < lastWayIdx || currentWayIdx >= 100 ||
						!lastPersonId.equals(person.getId())){
					
					plan = new SurveyPlan();
					person.getPlans().add(plan);
					
				} else {
					
					plan = person.getPlans().get(person.getPlans().size() - 1);
					
				}
				
				//the act type index at the destination
				double purpose = set.getDouble(this.constants.wayPurpose());
				double detailedPurpose = this.constants.getNamespace().equalsIgnoreCase("mid") ? set.getDouble(this.constants.wayDetailedPurpose()) : 0d;
				
				//the main mode of the leg and the mode combination
				String mainMode = handleMainMode(set.getString(this.constants.wayMode()));
//				Set<String> modes = CollectionUtils.stringToSet(set.getString(MiDConstants
//						.MODE_COMBINATION));
				
				double startHour = set.getDouble(this.constants.wayDepartureHour());
				double startTime = this.constants.getNamespace().equals("mid") ? set.getDouble(this.constants.wayDeparture())
						: set.getDouble(this.constants.wayDeparture()) * 60;
				double endTime = this.constants.getNamespace().equals("mid") ? set.getDouble(this.constants.wayArrival())
						: set.getDouble(this.constants.wayArrival()) * 60;
				
				int startDate = this.constants.getNamespace().equalsIgnoreCase("mid") ? set.getInt(this.constants.startDate()) : 0;
				int endDate = this.constants.getNamespace().equalsIgnoreCase("mid") ? set.getInt(this.constants.endDate()) : 0;
				double travelDistance = 1000 * set.getDouble(this.constants.wayTravelDistance());
				
				if(!container.getModeStatsContainer().containsKey(mainMode)){
					container.getModeStatsContainer().put(mainMode, new RecursiveStatsContainer());
				}
				
				container.getModeStatsContainer().get(mainMode).handleNewEntry(travelDistance);
				
				//if the way ends on the next day, add 24 hrs to the departure / arrival time
				if(startDate != 0){
					startTime += Time.MIDNIGHT;
				}
				if(endDate != 0){
					endTime += Time.MIDNIGHT;
				}
				
				double weight = set.getDouble(this.constants.wayWeight());

				//create a new way and set the main mode, mode combination
				//and departure / arrival time
				SurveyPlanTrip way = new SurveyPlanTrip(currentWayIdx);
				way.setMainMode(mainMode);
//				way.setModes(modes);
				way.setStartTime(startTime);
				way.setEndTime(endTime);
				way.setWeight(weight);
				way.setTravelDistance(travelDistance);
				
				plan.incrementWeight(weight);
				
				if(plan.getPlanElements().size() < 1){
					
					//add the source activity
					double firstActType = set.getDouble(this.constants.waySource());
					SurveyPlanActivity firstAct = new SurveyPlanActivity(handleActTypeAtStart(firstActType));
					if(firstAct.getActType().equals(ActivityTypes.HOME)){
						plan.setHomeIndex(0);
					}
					firstAct.setId(0);
					firstAct.setStartTime(0);
					firstAct.setEndTime(way.getStartTime());
					firstAct.setPriority(this.setActPriority(firstAct.getActType()));
					plan.getPlanElements().add(firstAct);
					
				}
				
				int dp = Double.isNaN(detailedPurpose) ? 0 : (int)detailedPurpose;
				int p = Double.isNaN(purpose) ? 0 : (int)purpose;
				String actType = handleActType(p, dp);

				int id = plan.getPlanElements().size() + 1;
				
				if(actType.equals(ActivityTypes.HOME)){
					if(!plan.homeIndexIsSet()){
						plan.setHomeIndex(id);
					} else{
						id = plan.getHomeIndex();
					}
				}

				//if it's a return leg, set the act type according to the act type
				// before the last activity
				if(actType.equals("return")){
				
					if(plan.getPlanElements().size() > 2){
						
						actType = ((SurveyPlanActivity)plan.getPlanElements().get(plan.getPlanElements()
								.size() - 3)).getActType();
						id -= 2;
						
					} else{
						
						actType = ActivityTypes.HOME;
						plan.setHomeIndex(id);
						
					}
					
				}
				
				double endPoint = set.getDouble(this.constants.waySink());
				
				//if it's a round-based trip, the act types at origin and destination equal
				if(endPoint == 5){
					
//					addRoundBasedWayAndActivity(plan, way, currentWayIdx, actType, id);
					//ignore it for now since round-trips cause some problems...
					
				} else {
					
					if(endPoint == 1){
						
						actType = ActivityTypes.HOME;
						plan.setHomeIndex(id);
						
					}
					
					if(!container.getActivityTypeHydrographs().containsKey(actType)){
						container.getActivityTypeHydrographs().put(actType, new Hydrograph(actType));
					}
					if(!Double.isNaN(startHour)){
						container.getActivityTypeHydrographs().get(actType).handleEntry(startHour, 1);
					}
					
					addWayAndActivity(plan, way, actType, id, container);
					if(endPoint == 5){
						id += 2;
					}
					
				}
				
				counter++;
					
				lastWayIdx = currentWayIdx;
				lastPersonId = person.getId();
			
			}
			
		}
		
		set.close();
		statement.close();

		log.info("Created " + counter + " ways from surveys database.");
		
		postprocessData(container);
		
	}
	
	private void parseVehiclesDatabase(Connection connection, SurveyDataContainer container) throws SQLException{
		
		Statement statement = connection.createStatement();
		
		String query = "select * from mid2008.cars_raw";
		
		ResultSet results = statement.executeQuery(query);
		
		while(results.next()){
			
			String hhid = results.getString(this.constants.householdId());
			String vid = results.getString(this.constants.vehicleId());
			int fuelType = results.getInt(this.constants.vehicleFuelType());
			int kbaClass = results.getInt(this.constants.vehicleSegmentKBA());
			
			SurveyHousehold household = container.getHouseholds().get(hhid);
			
			if(household != null){
				
				if(fuelType <= 5 || kbaClass <= 12){
					
					SurveyVehicle vehicle = new SurveyVehicle(vid);
					vehicle.setFuelType(fuelType);
					vehicle.setKbaClass(kbaClass);
					household.getVehicleIds().add(vid);
					container.getVehicles().put(vid, vehicle);
					
				}
				
			}
			
		}
		
		results.close();
		statement.close();
		
	}
	
	private void addWayAndActivity(SurveyPlan plan, SurveyPlanTrip way, String actType, int id, SurveyDataContainer container) throws SQLException{
		
		SurveyPlanActivity activity = new SurveyPlanActivity(actType);
		
		//set end time of last activity in plan to
		//the departure time of the current way
		SurveyPlanActivity previousAct = ((SurveyPlanActivity)plan.getPlanElements().get(plan.getPlanElements().size()-1));
		previousAct.setEndTime(way.getStartTime());
		
		if(!container.getActivityTypeHydrographs().containsKey(previousAct.getActType())){
			container.getActivityTypeHydrographs().put(previousAct.getActType(), new Hydrograph(previousAct.getActType()));
		}
		container.getActivityTypeHydrographs().get(previousAct.getActType()
				).handleDurationEntry(previousAct.getEndTime() - previousAct.getStartTime());
		
		//set start time of current activity to	
		//the arrival time of the current way
		activity.setStartTime(way.getEndTime());
		activity.setPriority(this.setActPriority(actType));
		activity.setId(id);
		
		//add current way and activity
		plan.getPlanElements().add(way);
		plan.getPlanElements().add(activity);
		
	}
	
	private void postprocessData(SurveyDataContainer container){

		if(!container.getHouseholds().isEmpty()){
			
			Set<String> emptyHouseholdIds = new HashSet<>();
			Set<String> personsToRemove = new HashSet<>();
			
			for(SurveyHousehold household : container.getHouseholds().values()){

				for(Iterator<String> it = household.getMemberIds().iterator(); it.hasNext();){
					
					String pid = it.next();
			
					boolean b = this.postprocessPerson(container.getPersons().get(pid));
					
					if(!b){
						it.remove();
					}
					
				}
				
				if(household.getMemberIds().size()<1)
					emptyHouseholdIds.add(household.getId());
				
			}
			
			for(String s : personsToRemove){
				container.incrementSumOfPersonWeightsBy(-container.getPersons().get(s).getWeight());
				container.getPersons().remove(s);
			}
			
			for(String s : emptyHouseholdIds){
				container.incrementSumOfHouseholdWeigtsBy(-container.getHouseholds().get(s).getWeight());
				container.getHouseholds().remove(s);
			}
			
		} else {
			
			for(SurveyPerson person : container.getPersons().values()){
				
				this.postprocessPerson(person);
				
			}
			
		}
		
	}
	
	private boolean postprocessPerson(SurveyPerson person){
		
		if(person != null){

			boolean licenseAndCarAvailabilitySet = false;
			
			if(person.getPlans().size() < 1) return false;
			
			for(SurveyPlan plan : person.getPlans()){
				
				if(plan.getPlanElements().size() < 1) continue;
				
				person.incrementPlansWeight(plan.getWeigt());

				SurveyPlanActivity firstAct = (SurveyPlanActivity) plan.getPlanElements().get(0);
				SurveyPlanActivity lastAct = (SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
				
				plan.setFirstActEqualsLastAct(firstAct.getActType().equals(lastAct.getActType()));
				
				if(firstAct.getActType().equals(ActivityTypes.HOME) && lastAct.getActType().equals(ActivityTypes.HOME) && plan.getPlanElements().size() == 3){
					
					//remove the last element twice to leave only one home act in the plan
					plan.getPlanElements().removeLast();
					plan.getPlanElements().removeLast();
					
				}
				
				SurveyPlanActivity mainAct = null;
				
				for(SurveyPlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof SurveyPlanActivity){
						
						SurveyPlanActivity activity = (SurveyPlanActivity)pe;
						
						if(mainAct != null){
							
							mainAct = evaluateActTypes(activity, mainAct);
							
						} else {
							
							mainAct = activity;
							
						}
						
					} else {
						
						SurveyPlanTrip way = (SurveyPlanTrip)pe;
						
						if(way.getTravelDistance() > plan.getLongestLeg()){
							plan.setLongestLeg(way.getTravelDistance());
						}
						
						if(way.getMainMode().equals(TransportMode.car) && !person.hasCarAvailable()){
							if(!licenseAndCarAvailabilitySet){
								log.warn("Person " + person.getId() + " reported that no car was available, but it is driving anyway!");
								log.info("Setting car availability and license ownership to ’true’.");
								person.setCarAvailable(true);
								person.setHasLicense(true);
								licenseAndCarAvailabilitySet = true;
								
							}

						}
						
					}
					
				}

				plan.setMainActType(mainAct.getActType());
				plan.setMainActId(mainAct.getId());
				plan.setMainActIndex(plan.getPlanElements().indexOf(mainAct));
				
				for(SurveyPlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof SurveyPlanActivity){
						
						SurveyPlanActivity act = (SurveyPlanActivity)pe;
						
						if(act.getActType().equals(plan.getMainActType()) && act.getId() != plan.getMainActId()){
							
							if(plan.getPlanElements().indexOf(act) > plan.getMainActIndex() + 2){
								
								act.setId(plan.getMainActId());
								
							}
							
						}
					
					}
				
				}
				
				List<Subtour> subtours = createSubtours(plan);
				
				Set<Integer> breakpoints = new HashSet<>();
				
				for(Subtour subtour : subtours){
					
					breakpoints.add(subtour.startIndex);
					breakpoints.add(subtour.endIndex);
					
				}
				
				for(SurveyPlanElement pe : plan.getPlanElements()){
					if(pe instanceof SurveyPlanActivity){
						SurveyPlanActivity act = (SurveyPlanActivity)pe;
						if(act.getId() == plan.getMainActId()){
							breakpoints.add(plan.getPlanElements().indexOf(pe));
						}
					}
				}
				
				List<Integer> breakpointsList = new ArrayList<>();
				breakpointsList.addAll(breakpoints);
				Collections.sort(breakpointsList);
				
				for(int i = 0; i < breakpointsList.size() - 1; i++){
					
					SurveyPlanActivity act1 = (SurveyPlanActivity) plan.getPlanElements().get(breakpointsList.get(i));
					SurveyPlanActivity act2 = (SurveyPlanActivity) plan.getPlanElements().get(breakpointsList.get(i + 1));

					Subtour subtour = new Subtour(breakpointsList.get(i), breakpointsList.get(i+1));
					
					if(act1.getActType().equals(act2.getActType())){
						
						subtour.type = subtourType.inter;
						plan.getSubtours().add(subtour);
						
					} else if(act2.getActType().equals(plan.getMainActType())){
						
						subtour.type = subtourType.forth;
						plan.getSubtours().add(subtour);

					} else if(act1.getActType().equals(plan.getMainActType())){

						subtour.type = subtourType.back;
						plan.getSubtours().add(subtour);

					}
					
				}
				
			}
			
		}
		
		return true;
		
	}
	
	private SurveyPlanActivity evaluateActTypes(SurveyPlanActivity activity, SurveyPlanActivity currentMainAct){
		
		if(activity.getPriority() < currentMainAct.getPriority()){
			
			return activity;
			
		}
		
		return currentMainAct;
		
	}
	
	private List<Subtour> createSubtours(SurveyPlan plan){
		
		List<Subtour> subtours = new ArrayList<>();
		
		if(plan.getPlanElements().size() > 1){
			
			List<Integer> originIds = new ArrayList<>();
			
			Integer destinationId = null;
			
			for(SurveyPlanElement pe : plan.getPlanElements()){
				
				if(pe instanceof SurveyPlanTrip){
					
					SurveyPlanActivity from = (SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)-1);
					
					SurveyPlanActivity to = (SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)+1);
					
					originIds.add(from.getId());
					originIds.add(null);
					
					destinationId = to.getId();
					
					if(originIds.contains(destinationId)){
						
						subtours.add(new Subtour(originIds.lastIndexOf(destinationId), originIds.size()));
						
						for(int i = originIds.lastIndexOf(destinationId); i < originIds.size(); i++){
							originIds.set(i, null);
						}
						
					} else if(plan.getPlanElements().indexOf(pe) >= plan.getPlanElements().size() - 2){
						
						subtours.add(new Subtour(0, originIds.size()));
						
					}
					
				}
				
			}
			
		}
		
		return subtours;
		
	}
	
	public static class Subtour{
		
		private subtourType type;
		private int startIndex;
		private int endIndex;
		
		enum subtourType{back,forth,inter};
		
		Subtour(int from, int to){this.startIndex=from;this.endIndex=to;}
		@Override
		public String toString(){
			return ("[" + startIndex + "," + endIndex + "], type: " + this.type.name());
		}
		public int getStartIndex(){
			return this.startIndex;
		}
		public int getEndIndex(){
			return this.endIndex;
		}
		public subtourType getType(){
			return type;
		}
		
	}
	
	private String handleActType(int idx, int idxD){
		
		if(this.constants.getNamespace().equalsIgnoreCase("mid")){
			
			return handleMiDActType(idx, idxD);
			
		} else {
			
			return handleSrVActType(idx);
			
		}

	}
	
	private String handleMiDActType(int idx, int idxD){
	
		switch(idx){
		
			case 1: return ActivityTypes.WORK;
			case 3: return ActivityTypes.EDUCATION;
			case 4: return handleActTypeDetailed(idxD); //shopping
			case 7: return handleActTypeDetailed(idxD); //leisure
			case 8: return ActivityTypes.HOME;
			case 9: return "return";
			case 32: return ActivityTypes.KINDERGARTEN;
			default: return handleActTypeDetailed(idxD); //other
	
		}
		
	}
	
	private String handleActTypeDetailed(int idx){
		
		switch(idx){

			case 501: return ActivityTypes.SUPPLY;
			case 504: return ActivityTypes.SERVICE;
			case 502:
			case 503:
			case 505: return ActivityTypes.SHOPPING;
			case 601: return ActivityTypes.HEALTH;
			case 602: 
			case 603: return ActivityTypes.ERRAND;
			case 702: return ActivityTypes.CULTURE;
			case 703: return ActivityTypes.EVENT;
			case 704: return ActivityTypes.SPORTS;
			case 705: return ActivityTypes.FURTHER;
			case 706: return ActivityTypes.EATING;
			case 701:
			case 707:
			case 710:
			case 712:
			case 713:
			case 714:
			case 715:
			case 716:
			case 717:
			case 718:
			case 719:
			case 720:
			case 799: return ActivityTypes.LEISURE;
			default: return ActivityTypes.OTHER;
		
		}
		
	}
	
	private String handleSrVActType(int idx){
		
		switch(idx){
		
			case 1: return ActivityTypes.WORK;
			case 2: return ActivityTypes.WORK;
			case 3: return ActivityTypes.KINDERGARTEN;
			case 4:
			case 5:
			case 6:
			case 7: return ActivityTypes.EDUCATION;
			case 8: return ActivityTypes.SUPPLY;
			case 9: return ActivityTypes.SHOPPING;
			case 10: return ActivityTypes.ERRAND;
			case 12: return ActivityTypes.CULTURE;
			case 15:
			case 16:
			case 17: return ActivityTypes.LEISURE;
			case 18: return ActivityTypes.HOME;
			default: return ActivityTypes.OTHER;
		
		}
		
	}
	
	private String handleActTypeAtStart(double idx){
		
		switch((int)idx){
		
//			case 1: return ActivityTypes.HOME;
			case 2: return ActivityTypes.WORK;
			case 3:
			case 4: return ActivityTypes.OTHER;
			default: return ActivityTypes.HOME;
			
		}
		
	}
	
	private String handleMainMode(String modeIdx){
		
		if(this.constants.getNamespace().equalsIgnoreCase("mid")){
			
			return handleMainModeMiD(modeIdx);
			
		} else{
			
			return handleMainModeSrV(modeIdx);
			
		}
		
	}
	
	private String handleMainModeMiD(String modeIdx){

		switch(modeIdx){
		
			case "1": return TransportMode.walk;
			case "2": return TransportMode.bike;
	//		case "3": return Modes.SCOOTER;
	//		case "4": return Modes.MOTORCYCLE;
			case "3": return TransportMode.ride;
			case "4": return TransportMode.car;
			default: return TransportMode.pt;
	//		default: return TransportMode.other;
	
		}
		
	}
	
	private String handleMainModeSrV(String modeIdx){
		
		switch(modeIdx){
		
			case "1": return TransportMode.walk;
			case "2": return TransportMode.bike;
			case "3": //return Modes.SCOOTER;
			case "4": return TransportMode.car;
			case "5": //return "freefloating";
			case "6": return TransportMode.car;
			case "7":
			case "8":
			case "9": return TransportMode.ride;
			case "10":
			case "11":
			case "12":
			case "13":
			case "14":
			case "15": return TransportMode.pt;
			case "16": //return Modes.TAXI;
			default: return TransportMode.other;
		
		}
		
	}
	
	private double handleHouseholdIncome(double incomeIdx){
		
		switch((int)incomeIdx){
		
		case 1: return 250;
		case 2: return 750;
		case 3: return 1200;
		case 4: return 1750;
		case 5: return 2300;
		case 6: return 2800;
		case 7: return 3300;
		case 8: return 3800;
		case 9: return 4300;
		case 10: return 4800;
		case 11: return 5300;
		case 12: return 5800;
		case 13: return 6300;
		case 14: return 6800;
		case 15: return 7300;
		default: return 0;
		
		}
		
	}
	
	private int setActPriority(String type){
		
		if(type.equals(ActivityTypes.WORK) || type.equals(ActivityTypes.EDUCATION) || type.equals(ActivityTypes.KINDERGARTEN) || type.equals(ActivityTypes.PRIMARY_SCHOOL)
				|| type.equals(ActivityTypes.PROFESSIONAL_SCHOOL) || type.equals(ActivityTypes.SECONDARY_SCHOOL) || type.equals(ActivityTypes.UNIVERSITY)){
			return 1;
		} else if(type.equals(ActivityTypes.LEISURE) || type.equals(ActivityTypes.EATING) || type.equals(ActivityTypes.CULTURE) || type.equals(ActivityTypes.SPORTS) || type.equals(ActivityTypes.FURTHER)
				|| type.equals(ActivityTypes.EVENT)){
			return 2;
		} else if(type.equals(ActivityTypes.SHOPPING) || type.equals(ActivityTypes.SUPPLY) || type.equals(ActivityTypes.SERVICE)){
			return 3;
		} else  if(type.equals(ActivityTypes.OTHER) || type.equals(ActivityTypes.HEALTH) || type.equals(ActivityTypes.ERRAND)){
			return 4;
		} else{
			return 5;
		}
		
	}
	
}
