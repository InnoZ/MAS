package innoz.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;

import innoz.config.Configuration;
import innoz.io.SurveyConstants;
import innoz.io.database.handler.Logbook;
import innoz.io.database.handler.SurveyStage;
import innoz.io.database.task.ReadHouseholdDatabaseTask;
import innoz.io.database.task.ReadPersonDatabaseTask;
import innoz.io.database.task.ReadWayDatabaseTask;
import innoz.io.database.task.SortStagesTask;
import innoz.io.database.validation.ValidateMissingTravelTimes;
import innoz.io.database.validation.ValidateNegativeTravelTimes;
import innoz.io.database.validation.ValidateOverlappingStages;
import innoz.scenarioGeneration.geoinformation.Geoinformation;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;
import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyPerson;
import innoz.scenarioGeneration.population.surveys.SurveyPlan;
import innoz.scenarioGeneration.population.surveys.SurveyPlanActivity;
import innoz.scenarioGeneration.population.surveys.SurveyPlanElement;
import innoz.scenarioGeneration.population.surveys.SurveyPlanTrip;
import innoz.scenarioGeneration.utils.ActivityTypes;

public class SurveyDatabaseParserV2 {

	private static final Logger log = Logger.getLogger(SurveyDatabaseParser.class);
	
	private SurveyConstants constants;
	
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
				
				if(configuration.isUsingHouseholds()){
					
					log.info("Creating survey households...");
					
					new ReadHouseholdDatabaseTask(constants, geoinformation).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);;
					
				}
				
				log.info("Creating survey persons...");
				
				new ReadPersonDatabaseTask(constants).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				log.info("Read " + container.getPersons().size() + " persons...");
				
				log.info("Creating survey ways...");

				new ReadWayDatabaseTask(constants).parse(connection, configuration.isUsingHouseholds(),
						configuration.isOnlyUsingWorkingDays(), container);
				
				if(configuration.isUsingVehicles() && configuration.getDatasource().equals("mid")){
				
					log.info("Creating survey cars...");
					
//					parseVehiclesDatabase(connection, container);
					
				}
	
				process(container);
				
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
	
	private void process(SurveyDataContainer container){
		
		Set<Logbook> toRemove;
		
		SortStagesTask task1 = new SortStagesTask();
		
		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
			
				task1.apply(logbook);
			
			}
			
		}
		
		ValidateMissingTravelTimes vmtt = new ValidateMissingTravelTimes();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vmtt.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
			
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
		
		}
		
		ValidateNegativeTravelTimes vntt = new ValidateNegativeTravelTimes();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vntt.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
			
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
		
		}
		
		ValidateOverlappingStages vos = new ValidateOverlappingStages();
		
		for(SurveyPerson person : container.getPersons().values()){
		
			toRemove = new HashSet<>();
			
			for(Logbook logbook : person.getLogbook().values()){
			
				vos.validate(logbook);
				if(logbook.isDelete()) toRemove.add(logbook);
		
			}
			
			for(Logbook log : toRemove){
				person.getLogbook().remove(log);
			}
			
		}
		
		Set<String> personsToRemove = new HashSet<>();
		for(SurveyPerson person : container.getPersons().values()){
			if(person.getLogbook().size() < 1){
				personsToRemove.add(person.getId());
			}
			if(person.getId() == null){
				personsToRemove.add(person.getId());
			}
		}
		
		for(String id : personsToRemove){
			container.removePerson(id);
		}
		
		Set<String> hhToRemove = new HashSet<>();
		for(SurveyHousehold hh : container.getHouseholds().values()){
			if(hh.getMemberIds().isEmpty()){
				hhToRemove.add(hh.getId());
				continue;
			}
			int size = hh.getMemberIds().size();
			for(String id : hh.getMemberIds()){
				if(container.getPersons().get(id) == null){
					size--;
				}
			}
			if(size <= 0){
				hhToRemove.add(hh.getId());
			}
		}
		for(String id : hhToRemove){
			container.removeHousehold(id);
		}
		
		resolveRoundTrips(container);
		
		convertToPlans(container);
		
	}
	
	void resolveRoundTrips(SurveyDataContainer container){
		
		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
				
				for(SurveyStage stage : logbook.getStages()){
					
					if(stage.getDestination() != null){

						if(stage.getDestination().equals("5")){
							
							int start = Integer.parseInt(stage.getStartTime());
							int end = Integer.parseInt(stage.getEndTime());
							int duration = end - start;
							double distance = Double.parseDouble(stage.getDistance());
							
							SurveyStage st2 = new SurveyStage();
							st2.setDestination("HOME");
							st2.setPurpose(ActivityTypes.HOME);
							st2.setDistance(Double.toString(distance / 2));
							st2.setEndTime(Integer.toString(end));
							st2.setMode(stage.getMode());
							st2.setStartTime(Integer.toString(start + duration / 2));
							
							logbook.getStages().add(logbook.getStages().indexOf(stage)+1, st2);
							
							stage.setDistance(Double.toString(distance / 2));
							stage.setEndTime(Integer.toString(start + duration / 2 - 1));
							stage.setPurpose(ActivityTypes.OTHER);
							
						}
						
					}
					
				}
				
			}
			
		}
		
	}
	
	void convertToPlans(SurveyDataContainer container){
		
		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
				
				SurveyPlan plan = new SurveyPlan();
				
				for(SurveyStage stage : logbook.getStages()){
			
					if(logbook.getStages().indexOf(stage)==0){
						
						String actType = "";
						
						if(stage.getOrigin() != null){
							if(stage.getOrigin().equals("HOME")){
								actType = ActivityTypes.HOME;
							} else if(stage.getOrigin().equals("WORK")){
								actType = ActivityTypes.WORK;
							} else {
								actType = ActivityTypes.OTHER;
							}
						} else{
							actType = ActivityTypes.OTHER;
						}
						
						SurveyPlanActivity act = new SurveyPlanActivity(actType);
						act.setEndTime(Double.parseDouble(stage.getStartTime()));
						act.setPriority(setActPriority(actType));
						plan.getPlanElements().add(act);
						
					} else {
						
						SurveyPlanActivity act = (SurveyPlanActivity)plan.getPlanElements().get(plan.getPlanElements().size()-1);
						act.setEndTime(Double.parseDouble(stage.getStartTime()));
						
					}
					
					SurveyPlanTrip trip = new SurveyPlanTrip(Integer.parseInt(stage.getIndex()));
					trip.setMainMode(stage.getMode());
					trip.setStartTime(Double.parseDouble(stage.getStartTime()));
					trip.setEndTime(Double.parseDouble(stage.getEndTime()));
					plan.getPlanElements().add(trip);
					
					String actType = stage.getPurpose();
					SurveyPlanActivity act = new SurveyPlanActivity(actType);
					act.setStartTime(Double.parseDouble(stage.getEndTime()));
					act.setPriority(setActPriority(actType));
					plan.getPlanElements().add(act);
					
				}
				
				person.getPlans().add(plan);
			
			}
			
		}
		
		for(SurveyPerson person : container.getPersons().values()){
			
			boolean licenseAndCarAvailabilitySet = false;
			
			for(SurveyPlan plan : person.getPlans()){
				
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
	
	private SurveyPlanActivity evaluateActTypes(SurveyPlanActivity activity, SurveyPlanActivity currentMainAct){
		
		if(activity.getPriority() < currentMainAct.getPriority()){
			
			return activity;
			
		}
		
		return currentMainAct;
		
	}
	
}
