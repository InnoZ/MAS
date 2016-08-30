package com.innoz.toolbox.io.database.task;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.utils.misc.Time;

import com.innoz.toolbox.io.database.handler.Logbook;
import com.innoz.toolbox.io.database.handler.SurveyStage;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPerson;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlan;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanActivity;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanElement;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyPlanTrip;
import com.innoz.toolbox.scenarioGeneration.utils.ActivityTypes;

public class ConvertToPlansTask implements SurveyDataTask {

	private static final Logger log = Logger.getLogger(ConvertToPlansTask.class);
	
	private int warnCounterLicense = 0;
	
	@Override
	public void run(SurveyDataContainer container) {

		for(SurveyPerson person : container.getPersons().values()){
			
			for(Logbook logbook : person.getLogbook().values()){
				
				int actCounter = 0;
				
				SurveyPlan plan = new SurveyPlan();
				
				for(SurveyStage stage : logbook.getStages()){

					if(Double.parseDouble(stage.getStartTime()) > Time.MIDNIGHT){
						continue;
					}
					
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
						act.setId(actCounter);
						actCounter++;
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
					if(stage.getDistance() != null){
						trip.setTravelDistance(Double.parseDouble(stage.getDistance()));
					}
					plan.getPlanElements().add(trip);
					
					Double distance = trip.getTravelDistance();
					container.handleNewModeEntry(trip.getMainMode(), distance);
					
					String actType = stage.getPurpose();
					
					if(actType.equalsIgnoreCase("return")){
						
						int index = logbook.getStages().indexOf(stage);
						
						if(index >= 1){
							
							actType = ((SurveyPlanActivity)plan.getPlanElements().get(plan.getPlanElements().size()-4)).getActType();
							
						} else {
							
							actType = ActivityTypes.HOME;
							
						}
						
					}
					
					SurveyPlanActivity act = new SurveyPlanActivity(actType);
					act.setStartTime(Double.parseDouble(stage.getEndTime()));
					act.setPriority(setActPriority(actType));
					plan.getPlanElements().add(act);
					
					act.setId(actCounter);
					actCounter++;
					
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
								if(warnCounterLicense < 5){
									log.warn("Person " + person.getId() + " reported that no car was available, but it is driving anyway!");
									log.info("Setting car availability and license ownership to ’true’.");
									warnCounterLicense++;
								} else if(warnCounterLicense == 5){
									log.info("Further occurences of this message are suppressed.");
									warnCounterLicense++;
								}
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
							
							if(plan.getMainActType().equals(ActivityTypes.WORK) || plan.getMainActType().equals(ActivityTypes.EDUCATION)){
								if(plan.getPlanElements().indexOf(act) > plan.getMainActIndex() + 2){
									act.setId(plan.getMainActId());
								}
							} else {
								if(plan.getPlanElements().indexOf(act) > plan.getMainActIndex() + 4){
									act.setId(plan.getMainActId());
								}
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
