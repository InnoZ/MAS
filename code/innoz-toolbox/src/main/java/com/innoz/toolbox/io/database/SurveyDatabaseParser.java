package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.misc.Time;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.Configuration.PopulationType;
import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIdHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIncomeHandler;
import com.innoz.toolbox.io.database.handler.HouseholdWeightHandler;
import com.innoz.toolbox.io.database.handler.PersonAgeHandler;
import com.innoz.toolbox.io.database.handler.PersonCarAvailabilityHandler;
import com.innoz.toolbox.io.database.handler.PersonEmploymentHandler;
import com.innoz.toolbox.io.database.handler.PersonGroupHandler;
import com.innoz.toolbox.io.database.handler.PersonIdHandler;
import com.innoz.toolbox.io.database.handler.PersonLicenseHandler;
import com.innoz.toolbox.io.database.handler.PersonLifephaseHandler;
import com.innoz.toolbox.io.database.handler.PersonSexHandler;
import com.innoz.toolbox.io.database.handler.PersonWeightHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlan;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanActivity;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanElement;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanTrip;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyVehicle;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;
import com.innoz.toolbox.scenarioGeneration.utils.Hydrograph;
import com.innoz.toolbox.utils.matsim.RecursiveStatsContainer;

/**
 * 
 * "Parser" for database tables containing information of traffic surveys (e.g. SrV, MiD).
 * Creates survey data classes from the retrieved information.
 * 
 * @author dhosse
 *
 */
public class SurveyDatabaseParser {

	//CONSTANTS//////////////////////////////////////////////////////////////////////////////
	private static final Logger log = Logger.getLogger(SurveyDatabaseParser.class);
	/////////////////////////////////////////////////////////////////////////////////////////
	
	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	private SurveyConstants constants;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * Executes the data retrieval process. Depending on the specifications in the configuration household, person,
	 * travel and vehicle data is read from the survey database tables.
	 * 
	 * @param configuration The scenario generation configuration.
	 * @param container The class containing all survey information needed for demand generation.
	 * @param geoinformation
	 */
	public void run(Configuration configuration, SurveyDataContainer container, Geoinformation geoinformation){
		
		// Initialize the survey constants according to what datasource was specified.
		this.constants = new SurveyConstants(configuration.getDatasource());
		
		try {
			
			log.info("Parsing surveys database to create a synthetic population");
			
			// Instantiate a new postgreSQL driver and establish a connection to the mobility database
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL +
					configuration.getLocalPort() + "/" + DatabaseConstants.SURVEYS_DB, 
					configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				if(configuration.getPopulationType().equals(PopulationType.households)){
					
					log.info("Creating survey households...");
					
					parseHouseholdsDatabase(connection, geoinformation, container);
					
				}
				
				log.info("Creating survey persons...");
				
				parsePersonsDatabase(connection, configuration.getPopulationType().equals(PopulationType.households),
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
		statement.setFetchSize(2000);
	
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
			
			q += " where st_code=44"; //Osnabrück 
			
		}
		
		q += ";";
		
		ResultSet set = statement.executeQuery(q);
		
		List<DefaultHandler> householdHandlers = new ArrayList<>();
		householdHandlers.add(new HouseholdIdHandler());
		householdHandlers.add(new HouseholdIncomeHandler());
		householdHandlers.add(new HouseholdWeightHandler());
		
		while(set.next()){
			
			Map<String, String> attributes = new HashMap<>();
			attributes.put(this.constants.householdId(), set.getString(this.constants.householdId()));
			attributes.put(this.constants.householdIncomePerMonth(), Double.toString(set.getDouble(this.constants
					.householdIncomePerMonth())));
			
			SurveyHousehold hh = new SurveyHousehold();
			
			for(DefaultHandler handler : householdHandlers){
				
				handler.handle(hh, attributes);
				
			}
			
			int rtyp = this.constants.getNamespace().equals("mid") ? set.getInt(this.constants.regionType()) : 3;
			container.addHousehold(hh, rtyp);
			
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
		statement.setFetchSize(2000);
		
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
		
		List<DefaultHandler> personHandlers = new ArrayList<>();
		personHandlers.add(new PersonIdHandler());
		personHandlers.add(new PersonWeightHandler());
		personHandlers.add(new PersonCarAvailabilityHandler());
		personHandlers.add(new PersonLicenseHandler());
		personHandlers.add(new PersonSexHandler());
		personHandlers.add(new PersonAgeHandler());
		personHandlers.add(new PersonEmploymentHandler());
		personHandlers.add(new PersonGroupHandler());
		personHandlers.add(new PersonLifephaseHandler());
		
		while(set.next()){
			
			String hhId = set.getString(this.constants.householdId());
			
			Map<String, String> attributes = new HashMap<>();
			attributes.put(this.constants.personId(), hhId + set.getString(this.constants.personId()));
			attributes.put(this.constants.personWeight(), Double.toString(set.getDouble(this.constants.personWeight())));
			attributes.put(this.constants.personCarAvailability(), set.getString(this.constants.personCarAvailability()));
			attributes.put(this.constants.personDrivingLicense(), set.getString(this.constants.personDrivingLicense()));
			attributes.put(this.constants.personSex(), set.getString(this.constants.personSex()));
			attributes.put(this.constants.personAge(), set.getString(this.constants.personAge()));
			attributes.put(this.constants.personEmployment(), set.getString(this.constants.personEmployment()));
			attributes.put(this.constants.personGroup(), Integer.toString(set.getInt(this.constants.personGroup())));
			attributes.put(this.constants.personLifephase(), Integer.toString(set.getInt(this.constants.personLifephase())));
			
//			String carshare = "2";
//			if(this.constants.getNamespace().equalsIgnoreCase("srv")){
//				carshare = set.getString(this.constants.personIsCarsharingUser());
//			}
			
			SurveyPerson person = new SurveyPerson();
			
			for(DefaultHandler handler : personHandlers){
				
				handler.handle(person, attributes);
				
			}
			
			if(isUsingHouseholds){
				
				if(!container.getHouseholds().containsKey(hhId)){
					
					continue;
					
				} else {
					
					container.getHouseholds().get(hhId).getMemberIds().add(person.getId());
					
				}
				
			}
			
			container.addPerson(person);
			
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
		statement.setFetchSize(2000);
		
		String table = this.constants.getNamespace().equals("mid") ? "mid2008.ways_raw" : "srv2013.ways";

		String query = "select * from " + table;
		
		if(onlyWorkingDays){
			query+=" where " + this.constants.dayOfTheWeek() + " < 6";
		}
		
		query+= " and " + this.constants.wayTravelDistance() + " <> 'NaN' and " + this.constants.wayTravelTime() + " <> 'NaN' and "
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
				
//				if(actType != null){

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
						
//						addRoundBasedWayAndActivity(plan, way, currentWayIdx, actType, id);
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
						boolean inHomeCell = endPoint > 2;
						addWayAndActivity(plan, way, actType, id, container, inHomeCell);
						if(endPoint == 5){
							id += 2;
						}
						
//					}
					
					counter++;
						
				}
					
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
	
	private void addWayAndActivity(SurveyPlan plan, SurveyPlanTrip way, String actType, int id, SurveyDataContainer container,
			boolean inHomeCell) throws SQLException{
		
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
		activity.setInHomeCell(inHomeCell);
		
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
						personsToRemove.add(pid);
						it.remove();
					}
					
				}
				
				if(household.getMemberIds().size() < 1)
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
			
			if(person.getPlans().isEmpty()) return false;
			
			for(SurveyPlan plan : person.getPlans()){
				
				double w = plan.getWeigt() > 0 ? plan.getWeigt() : 0.00001;
				person.incrementPlansWeight(w);

				if(plan.getPlanElements().size() < 1) continue;
				
				SurveyPlanActivity firstAct = (SurveyPlanActivity) plan.getPlanElements().get(0);
				SurveyPlanActivity lastAct = (SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().size()-1);
				
				plan.setFirstActEqualsLastAct(firstAct.getActType().equals(lastAct.getActType()));
				
//				if(firstAct.getActType().equals(ActivityTypes.HOME) && lastAct.getActType().equals(ActivityTypes.HOME) && plan.getPlanElements().size() == 3){
//					
//					//remove the last element twice to leave only one home act in the plan
//					plan.getPlanElements().removeLast();
//					plan.getPlanElements().removeLast();
//					
//				}
				
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
						
						if(way.getStartTime() > way.getEndTime()) return false;
						
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
				
				if(plan.getMainActType().equals(ActivityTypes.HOME) && plan.getPlanElements().size() > 1){
					
					boolean first = true;
					
					for(Iterator<SurveyPlanElement> it = plan.getPlanElements().iterator(); it.hasNext();){
						
						it.next();
						
						if(!first){
							it.remove();
						}
						
						first = false;
						
					}
					
				}
				
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
	
	private String handleActType(int idx, int idxD){
		
		if(this.constants.getNamespace().equalsIgnoreCase("mid")){
			
			return handleMiDActType(idx, idxD);
			
		} else {
			
			return handleSrVActType(idx);
			
		}

	}
	
	private String handleMiDActType(int idx, int idxD){
	
		String actType = null;
		
		if(idx == 1){
			
			actType = ActivityTypes.WORK;
			
		} else if(idx == 3){
			
			actType = ActivityTypes.EDUCATION;
			
		} else if(idx == 4){
			
			actType = ActivityTypes.SHOPPING;
			
			if(idxD == 501){
				
				actType = ActivityTypes.SUPPLY;
				
			} else if(idxD == 504){
				
				actType = ActivityTypes.SERVICE;
				
			}
			
		} else if(idx == 5){
			
			actType = ActivityTypes.ERRAND;
			
		} else if(idx == 7){
			
			actType = ActivityTypes.LEISURE;
			
			if(idxD == 702){
				
				actType = ActivityTypes.CULTURE;
				
			} else if(idxD == 703){
				
				actType = ActivityTypes.EVENT;
				
			} else if(idxD == 704){
				
				actType = ActivityTypes.SPORTS;
				
			} else if(idxD == 705){
				
				actType = ActivityTypes.FURTHER;
				
			} else if(idxD == 706){
				
				actType = ActivityTypes.EATING;
				
			}
			
		} else if(idx == 8){
			
			actType = ActivityTypes.HOME;
			
		} else if(idx == 9){
			
			actType = "return";
			
		} else if(idx == 31){
			
			actType = ActivityTypes.EDUCATION;
			
		} else if(idx == 32){
			
			actType = ActivityTypes.KINDERGARTEN;
			
		} else {
			
			actType = ActivityTypes.OTHER;
			
			if(idxD == 601){
				
				actType = ActivityTypes.HEALTH;
				
			}
			
		}
		
		return actType;
		
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
			case "5": return TransportMode.pt;
			default: return TransportMode.other;
	
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