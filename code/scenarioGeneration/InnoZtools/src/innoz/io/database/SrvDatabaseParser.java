package innoz.io.database;

import innoz.config.Configuration;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlan;
import innoz.scenarioGeneration.population.surveys.SurveyPlanActivity;
import innoz.scenarioGeneration.population.surveys.SurveyPlanElement;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;
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
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.collections.Tuple;

public class SrvDatabaseParser implements DemandDatabaseParser {

	private static final Logger log = Logger.getLogger(SrvDatabaseParser.class);
	
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation){
	
		log.info("Parsing SrV database to create a synthetic population");
		
		try {
		
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_PREFIX + configuration.getLocalPort() + "/" +
					DatabaseConstants.SURVEYS_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating SrV households...");
					parseHouseholdsDatabase(connection, geoinformation, container);
					
				}
				
				log.info("Creating SrV persons...");
				parsePersonsDatabase(connection, configuration.isUsingHouseholds(), configuration.isOnlyUsingWorkingDays(), container);
				
				log.info("Creating SrV ways...");
				parseWaysDatabase(connection, configuration.isOnlyUsingWorkingDays(), container);
				
				connection.close();
			
			} else {
				
				throw new RuntimeException("Database connection could not be established! Aborting...");
				
			}
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private void parseHouseholdsDatabase(Connection connection, Geoinformation geoinformation,
			SurveyDataContainer container) throws RuntimeException, SQLException{

		Statement statement = connection.createStatement();
		
		String q = "select * from srv2013.households;";
		
		ResultSet set = statement.executeQuery(q);
		
		while(set.next()){
			
			String hhId = set.getString(SrVConstants.HH_NR);
			SurveyHousehold hh = new SurveyHousehold(hhId);
			
			hh.setWeight(set.getDouble(SrVConstants.HH_WEIGHT));
			
			double income = set.getDouble(SrVConstants.HH_INCOME);
			hh.setIncome(handleHouseholdIncome(income));
			
			container.getHouseholds().put(hhId, hh);
			container.incrementSumOfHouseholdWeigtsBy(hh.getWeight());
			
			int bland = 3;
			int rtyp = 3;
			
			if(container.getHouseholdsForState(bland, rtyp) == null){
				container.getStateId2Households().put(new Tuple<Integer, Integer>(bland, rtyp), new HashSet<String>());
			}
			
			container.getStateId2Households().get(new Tuple<Integer,Integer>(bland,rtyp)).add(hhId);
			
		}
		
	}
	
	private void parsePersonsDatabase(Connection connection, boolean isUsingHouseholds, boolean onlyWorkingDays,
			SurveyDataContainer container) throws SQLException{
		
		Statement statement = connection.createStatement();

		ResultSet set = null;
		String q = null;
		
		if(isUsingHouseholds){
			
			q = "select * from srv2013.persons where " + SrVConstants.STICHTAG + " < 6;";
			set = statement.executeQuery(q);
			
		}
		
		while(set.next()){
			
			String hhId = set.getString(SrVConstants.HH_NR);
			String personId = set.getString(SrVConstants.P_NR);
			double personWeight = set.getDouble(SrVConstants.P_WEIGHT);
			String carAvail = set.getString(SrVConstants.P_CARAVAIL);
			String license = set.getString(SrVConstants.P_LICENSE_CAR);
			String sex = set.getString(SrVConstants.P_SEX);
			String age = set.getString(SrVConstants.P_AGE);
			String employed = set.getString(SrVConstants.P_EMPL);
			
			SurveyPerson person = new SurveyPerson(hhId + personId, sex, age, carAvail, license, employed);
			person.setWeight(personWeight);
			
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
			
			container.incrementSumOfPersonWeightsBy(set.getDouble(SrVConstants.P_WEIGHT));
			
		}
		
		set.close();
		statement.close();
		
	}
	
	private void parseWaysDatabase(Connection connection, boolean onlyWorkingDays, SurveyDataContainer container) throws SQLException {
		
		Statement statement = connection.createStatement();

		String query = "select * from srv2013.ways where stichtag_wtag < 6 and v_laenge <> 'NaN';";
		
		ResultSet set = statement.executeQuery(query);
		
		int lastWayIdx = 100;
		String lastPersonId = "";
		
		while(set.next()){
			
			String hhId = set.getString(SrVConstants.HH_NR);
			String personId = set.getString(SrVConstants.P_NR);
			SurveyPerson person = container.getPersons().get(hhId + personId);
			
			if(person != null){
			
			SurveyPlan plan = null;
			
			int currentWayIdx = set.getInt(SrVConstants.W_NR);
			
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
			
			if(person != null){
				
				//the act type index at the destination
				double purpose = set.getDouble(SrVConstants.W_PURPOSE);
				if(Double.isNaN(purpose)){
					purpose = 99;
				}
				
				//the main mode of the leg and the mode combination
				String mainMode = handleMainMode(set.getString(SrVConstants.W_HVM));
				
				double startTime = set.getDouble(SrVConstants.W_DEP_H) * 3600 + set.getDouble(SrVConstants.W_DEP_MIN) * 60;
				double endTime = set.getDouble(SrVConstants.W_ARR_H) * 3600 + set.getDouble(SrVConstants.W_ARR_MIN) * 60;
				
				double weight = set.getDouble(SrVConstants.W_WEIGHT);
				double travelDistance = 1000 * set.getDouble("v_laenge");

				//create a new way and set the main mode, mode combination
				//and departure / arrival time
				SurveyPlanTrip way = new SurveyPlanTrip(currentWayIdx);
				way.setMainMode(mainMode);
//				way.setModes(modes);
				way.setStartTime(startTime);
				way.setEndTime(endTime);
				way.setWeight(weight);
				way.setTravelDistance(travelDistance);
				
				if(!container.getModeStatsContainer().containsKey(mainMode)){
					container.getModeStatsContainer().put(mainMode, new RecursiveStatsContainer());
				}
				
				container.getModeStatsContainer().get(mainMode).handleNewEntry(travelDistance);
				
				plan.incrementWeight(weight);
				
				if(plan.getPlanElements().size() < 1){
					
					//add the source activity
					double firstActType = set.getDouble(SrVConstants.W_START);
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
				
//				int dp = Double.isNaN(detailedPurpose) ? 0 : (int)detailedPurpose;
				String actType = handleActType((int)purpose, 0);

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
				
				double endPoint = actType.equalsIgnoreCase(ActivityTypes.HOME) ? 1 : 0;
				
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
					
					addWayAndActivity(plan, way, actType, id, container);
					if(endPoint == 5){
						id += 2;
					}
					
				}
				
			}
			
			lastWayIdx = currentWayIdx;
			lastPersonId = person.getId();
			
			}
			
		}
		
		set.close();
		statement.close();
		
		postprocessData(container);
		
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
	
	private String handleActType(int idx, int idxD){
		
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
						
						if(act.getActType().equals(plan.getMainActType()) && act.getId() != plan.getMainActId() &&
								(act.getActType().equals(ActivityTypes.WORK)||(act.getActType().equals(ActivityTypes.EDUCATION)))){
							
							if(plan.getPlanElements().indexOf(act) >= plan.getMainActIndex() + 2){
								
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
				
//				for(int i = 0; i < breakpointsList.size() - 1; i++){
//					
//					SurveyPlanActivity act1 = (SurveyPlanActivity) plan.getPlanElements().get(breakpointsList.get(i));
//					SurveyPlanActivity act2 = (SurveyPlanActivity) plan.getPlanElements().get(breakpointsList.get(i + 1));
//
//					Subtour subtour = new Subtour(breakpointsList.get(i), breakpointsList.get(i+1));
//					
//					if(act1.getActType().equals(act2.getActType())){
//						
//						subtour.type = subtourType.inter;
//						plan.getSubtours().add(subtour);
//						
//					} else if(act2.getActType().equals(plan.getMainActType())){
//						
//						subtour.type = subtourType.forth;
//						plan.getSubtours().add(subtour);
//
//					} else if(act1.getActType().equals(plan.getMainActType())){
//
//						subtour.type = subtourType.back;
//						plan.getSubtours().add(subtour);
//
//					}
//					
//				}
				
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
	
}
