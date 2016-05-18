package playground.dhosse.scenarioGeneration.population.io.mid;

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
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.misc.Time;

import playground.dhosse.scenarioGeneration.Configuration;
import playground.dhosse.scenarioGeneration.population.HashGenerator;
import playground.dhosse.scenarioGeneration.population.io.mid.MiDParser.Subtour.subtourType;
import playground.dhosse.scenarioGeneration.utils.ActivityTypes;
import playground.dhosse.scenarioGeneration.utils.Hydrograph;
import playground.dhosse.utils.RecursiveStatsContainer;

/**
 * 
 * "Parser" for postgreSQL database tables containing MiD survey data.
 * The table data is stored in wrapper classes (households, persons, ways) for further work.
 * 
 * @author dhosse
 *
 */
public class MiDParser {

	private static final Logger log = Logger.getLogger(MiDParser.class);
	
	public void run(Configuration configuration, SurveyDataContainer container){
		
		try {
			
			log.info("Parsing MiD database to create a synthetic population");
			
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() +
					"/surveyed_mobility", configuration.getDatabaseUsername(), configuration.getPassword());
		
			if(connection != null){
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating MiD households...");
					
					parseHouseholdsDatabase(connection, configuration.getSqlQuery(), container);
					
				}
				
				log.info("Creating MiD persons...");
				
				parsePersonsDatabase(connection, configuration.getSqlQuery(), configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				log.info("Creating MiD ways...");
				
				parseWaysDatabase(connection, configuration.isOnlyUsingWorkingDays(), container);
				
				connection.close();
				
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private void parseHouseholdsDatabase(Connection connection, String query, SurveyDataContainer container) throws RuntimeException, SQLException{
		
		Statement statement = connection.createStatement();
	
		ResultSet set = statement.executeQuery(query);
		
		while(set.next()){
			
			String hhId = set.getString(MiDConstants.HOUSEHOLD_ID);
			SurveyHousehold hh = new SurveyHousehold(hhId);
			
			hh.setWeight(set.getDouble(MiDConstants.HOUSEHOLD_WEIGHT));
			
			double income = set.getDouble(MiDConstants.HOUSEHOLD_INCOME);
			hh.setIncome(handleHouseholdIncome(income));
			
			hh.setNCars(set.getDouble(MiDConstants.HOUSEHOLD_NCARS));
			
			container.getHouseholds().put(hhId, hh);
			container.incrementSumOfHouseholdWeigtsBy(hh.getWeight());

		}
		
		set.close();
		statement.close();
		
		if(container.getHouseholds().isEmpty()){
			
			log.warn("The selected query \"" + query + "\" yielded no results...");
			log.warn("This eventually results in no population.");
			log.warn("Continuing anyway");
			
		} else {
			
			log.info("Created " + container.getHouseholds().size() + " households from MiD database.");
			
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
	private void parsePersonsDatabase(Connection connection, String query, boolean isUsingHouseholds,
			boolean onlyWorkingDays, SurveyDataContainer container) throws SQLException{
		
		Statement statement = connection.createStatement();

		ResultSet set = null;
		
		if(isUsingHouseholds){
			
			String q = "select * from mid2008.persons_raw";
			if(onlyWorkingDays){
				q += " where stichtag < 6";
			}
			set = statement.executeQuery(q);
			
		} else {
			
			set = statement.executeQuery(query);
			
		}
		
		while(set.next()){
			
			String hhId = set.getString(MiDConstants.HOUSEHOLD_ID);
			String personId = set.getString(MiDConstants.PERSON_ID);
			double personWeight = set.getDouble(MiDConstants.PERSON_WEIGHT);
			String carAvail = set.getString(MiDConstants.PERSON_CAR_AVAIL);
			String license = set.getString(MiDConstants.PERSON_LICENSE);
			String sex = set.getString(MiDConstants.PERSON_SEX);
			String age = set.getString(MiDConstants.PERSON_AGE);
			String employed = set.getString(MiDConstants.PERSON_EMPLOYED);
			
			int personGroup = set.getInt(MiDConstants.PERSON_GROUP_12);
			int phase = set.getInt(MiDConstants.PERSON_LIFE_PHASE);
			
			SurveyPerson person = new SurveyPerson(hhId + personId, sex, age, carAvail, license, employed);
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
			
			container.incrementSumOfPersonWeightsBy(set.getDouble(MiDConstants.PERSON_WEIGHT));
			
		}
		
		set.close();
		statement.close();
		
		if(container.getPersons().isEmpty()){

			log.warn("The selected query \"" + query + "\" yielded no results...");
			log.warn("This eventually results in no population.");
			log.warn("Continuing anyway");
			
		} else {
			
			log.info("Created " + container.getPersons().size() + " persons from MiD database.");
			
		}
		
	}
	
	private void parseWaysDatabase(Connection connection, boolean onlyWorkingDays, SurveyDataContainer container) throws SQLException {
		
		Statement statement = connection.createStatement();

		String query = "select * from mid2008.ways_raw";
		
		if(onlyWorkingDays){
			query+=" where stichtag < 6";
		}
		
		query+=" and wegkm_k <> 'NaN' and wegmin_k <> 'NaN' and st_time <> 'NaN' and en_time <>'NaN' and s01=1 order by hhid, pid, wsid";
		
		ResultSet set = statement.executeQuery(query);
		
		int lastWayIdx = 100;
		int counter = 0;
		String lastPersonId = "";
		
		while(set.next()){
			
			String hhId = set.getString(MiDConstants.HOUSEHOLD_ID);
			String personId = set.getString(MiDConstants.PERSON_ID);
			SurveyPerson person = container.getPersons().get(hhId + personId);
			
			if(person != null){
			
			MiDPlan plan = null;
			
			int currentWayIdx = set.getInt(MiDConstants.WAY_ID_SORTED);
			
			//if the index of the current way is lower than the previous index
			//or the currently processed way is a rbw
			//it's probably a new plan...
			if(currentWayIdx < lastWayIdx || currentWayIdx >= 100 ||
					!lastPersonId.equals(person.getId())){
				
				plan = new MiDPlan();
				person.getPlans().add(plan);
				
			} else {
				
				plan = person.getPlans().get(person.getPlans().size() - 1);
				
			}
			
			if(person != null){
				
				//the act type index at the destination
				int purpose = set.getInt(MiDConstants.PURPOSE);
				
				//the main mode of the leg and the mode combination
				String mainMode = handleMainMode(set.getString(MiDConstants.MAIN_MODE));
				Set<String> modes = CollectionUtils.stringToSet(set.getString(MiDConstants
						.MODE_COMBINATION));
				
				double startHour = set.getDouble(MiDConstants.ST_STD);
				double startTime = set.getDouble(MiDConstants.ST_TIME);
				double endTime = set.getDouble(MiDConstants.EN_TIME);
				
				int startDate = set.getInt(MiDConstants.ST_DAT);
				int endDate = set.getInt(MiDConstants.EN_DAT);
				double travelDistance = 1000 * set.getDouble(MiDConstants.WAY_DISTANCE);
				
				if(!container.getModeStatsContainer().containsKey(mainMode)){
					container.getModeStatsContainer().put(mainMode, new RecursiveStatsContainer());
				}
				
				container.getModeStatsContainer().get(mainMode).handleNewEntry(travelDistance);
				
				//if the way ends on the next day, add 24 hrs to the departure / arrival time
				if(startDate != 0){
					continue;
				}
				if(endDate != 0){
					endTime = Time.MIDNIGHT;
				}
				
				double weight = set.getDouble(MiDConstants.WAY_WEIGHT);

				//create a new way and set the main mode, mode combination
				//and departure / arrival time
				MiDWay way = new MiDWay(currentWayIdx);
				way.setMainMode(mainMode);
				way.setModes(modes);
				way.setStartTime(startTime);
				way.setEndTime(endTime);
				way.setWeight(weight);
				way.setTravelDistance(travelDistance);
				
				plan.incrementWeight(weight);
				
				if(plan.getPlanElements().size() < 1){
					
					//add the source activity
					double firstActType = set.getDouble(MiDConstants.START_POINT);
					MiDActivity firstAct = new MiDActivity(handleActTypeAtStart(firstActType));
					if(firstAct.getActType().equals(ActivityTypes.HOME)){
						plan.setHomeIndex(0);
					}
					firstAct.setId(0);
					firstAct.setStartTime(0);
					firstAct.setEndTime(way.getStartTime());
					firstAct.setPriority(this.setActPriority(firstAct.getActType()));
					plan.getPlanElements().add(firstAct);
					
				}
				
				String actType = handleActType(purpose);

				int id = plan.getPlanElements().size() + 1;
				
				if(actType.equals(ActivityTypes.HOME)){
					if(!plan.setHomeIndex){
						plan.setHomeIndex(id);
					} else{
						id = plan.getHomeIndex();
					}
				}

				//if it's a return leg, set the act type according to the act type
				// before the last activity
				if(actType.equals("return")){
				
					if(plan.getPlanElements().size() > 2){
						
						actType = ((MiDActivity)plan.getPlanElements().get(plan.getPlanElements()
								.size() - 3)).getActType();
						id -= 2;
						
					} else{
						
						actType = ActivityTypes.HOME;
						plan.setHomeIndex(id);
						
					}
					
				}
				
				double endPoint = set.getDouble(MiDConstants.END_POINT);
				
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
				
			}
			
			lastWayIdx = currentWayIdx;
			lastPersonId = person.getId();
			
			}
			
		}
		
		set.close();
		statement.close();

		log.info("Created " + counter + " ways from MiD database.");
		
		postprocessData(container);
		
		log.info("Conversion statistics:");
		log.info("#Households in survey: " + container.getHouseholds().size());
		log.info("#Persons in survey:    " + container.getPersons().size());
		
	}
	
	private void addWayAndActivity(MiDPlan plan, MiDWay way, String actType, int id, SurveyDataContainer container) throws SQLException{
		
		MiDActivity activity = new MiDActivity(actType);
		
		//set end time of last activity in plan to
		//the departure time of the current way
		MiDActivity previousAct = ((MiDActivity)plan.getPlanElements().get(plan.getPlanElements().size()-1));
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
					
					if(!this.postprocessPerson(container.getPersons().get(pid))){
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
			
			for(MiDPlan plan : person.getPlans()){
				
				person.incrementPlansWeight(plan.getWeigt());

				MiDActivity firstAct = (MiDActivity) plan.getPlanElements().get(0);
				MiDActivity lastAct = (MiDActivity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
				
				plan.setFirstActEqualsLastAct(firstAct.getActType().equals(lastAct.getActType()));
				
				if(firstAct.getActType().equals(ActivityTypes.HOME) && lastAct.getActType().equals(ActivityTypes.HOME) && plan.getPlanElements().size() == 3){
					
					//remove the last element twice to leave only one home act in the plan
					plan.getPlanElements().removeLast();
					plan.getPlanElements().removeLast();
					
				}
				
				MiDActivity mainAct = null;
				
				for(MiDPlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof MiDActivity){
						
						MiDActivity activity = (MiDActivity)pe;
						
						if(mainAct != null){
							
							mainAct = evaluateActTypes(activity, mainAct);
							
						} else {
							
							mainAct = activity;
							
						}
						
					} else {
						
						MiDWay way = (MiDWay)pe;
						
						if(way.getTravelDistance() > plan.getLongestLeg()){
							plan.setLongestLeg(way.getTravelDistance());
						}
						
						if(way.getMainMode().equals(TransportMode.car) && !person.getCarAvailable()){
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
				
				for(MiDPlanElement pe : plan.getPlanElements()){
					
					if(pe instanceof MiDActivity){
						
						MiDActivity act = (MiDActivity)pe;
						
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
				
				for(MiDPlanElement pe : plan.getPlanElements()){
					if(pe instanceof MiDActivity){
						MiDActivity act = (MiDActivity)pe;
						if(act.getId() == plan.getMainActId()){
							breakpoints.add(plan.getPlanElements().indexOf(pe));
						}
					}
				}
				
				List<Integer> breakpointsList = new ArrayList<>();
				breakpointsList.addAll(breakpoints);
				Collections.sort(breakpointsList);
				
				for(int i = 0; i < breakpointsList.size() - 1; i++){
					
					MiDActivity act1 = (MiDActivity) plan.getPlanElements().get(breakpointsList.get(i));
					MiDActivity act2 = (MiDActivity) plan.getPlanElements().get(breakpointsList.get(i + 1));

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
	
	private MiDActivity evaluateActTypes(MiDActivity activity, MiDActivity currentMainAct){
		
		if(activity.getPriority() < currentMainAct.getPriority()){
			
			return activity;
			
		}
		
		return currentMainAct;
		
	}
	
	private List<Subtour> createSubtours(MiDPlan plan){
		
		List<Subtour> subtours = new ArrayList<>();
		
		if(plan.getPlanElements().size() > 1){
			
			List<Integer> originIds = new ArrayList<>();
			
			Integer destinationId = null;
			
			for(MiDPlanElement pe : plan.getPlanElements()){
				
				if(pe instanceof MiDWay){
					
					MiDActivity from = (MiDActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)-1);
					
					MiDActivity to = (MiDActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(pe)+1);
					
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
	
	private String handleActType(int idx){
		
		switch(idx){
		
			case 1: return ActivityTypes.WORK;
			case 3: return ActivityTypes.EDUCATION;
			case 4: return ActivityTypes.SHOPPING;
			case 7: return ActivityTypes.LEISURE;
			case 8: return ActivityTypes.HOME;
			case 9: return "return";
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
		
		switch(modeIdx){
		
			case "1": return TransportMode.walk;
			case "2": return TransportMode.bike;
//			case "3": return Modes.SCOOTER;
//			case "4": return Modes.MOTORCYCLE;
			case "3": return TransportMode.ride;
			case "4": return TransportMode.car;
			default: return TransportMode.pt;
//			default: return TransportMode.other;
		
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
		
		if(type.equals(ActivityTypes.WORK) || type.equals(ActivityTypes.EDUCATION)){
			return 1;
		} else if(type.equals(ActivityTypes.LEISURE)){
			return 2;
		} else if(type.equals(ActivityTypes.SHOPPING)){
			return 3;
		} else  if(type.equals(ActivityTypes.OTHER)){
			return 4;
		} else{
			return 5;
		}
		
	}
	
}
