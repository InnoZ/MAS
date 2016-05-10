package playground.dhosse.scenarios.generic.population;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.collections.CollectionUtils;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.households.Household;
import org.matsim.households.HouseholdImpl;
import org.matsim.households.Income.IncomePeriod;
import org.matsim.households.IncomeImpl;

import playground.dhosse.scenarios.generic.Configuration;
import playground.dhosse.scenarios.generic.population.io.mid.MiDActivity;
import playground.dhosse.scenarios.generic.population.io.mid.MiDHousehold;
import playground.dhosse.scenarios.generic.population.io.mid.MiDParser;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPerson;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlan;
import playground.dhosse.scenarios.generic.population.io.mid.MiDPlanElement;
import playground.dhosse.scenarios.generic.population.io.mid.MiDWay;
import playground.dhosse.scenarios.generic.utils.ActivityTypes;
import playground.dhosse.scenarios.generic.utils.AdministrativeUnit;
import playground.dhosse.scenarios.generic.utils.Distribution;
import playground.dhosse.scenarios.generic.utils.Geoinformation;
import playground.dhosse.utils.GeometryUtils;

import com.vividsolutions.jts.geom.Geometry;

public class PlansCreator {

	static Random random = MatsimRandom.getLocalInstance();
	
	private static CoordinateTransformation transformation;

	private static AdministrativeUnit homeCell;
	private static AdministrativeUnit mainActCell;
	private static Coord homeCoord;
	
	private static Distribution distribution;
	private static MiDParser parser;
	
	static Comparator<MiDHousehold> householdComparator = new Comparator<MiDHousehold>() {

		@Override
		public int compare(MiDHousehold o1, MiDHousehold o2) {
			return Double.compare(o1.getWeight(), o2.getWeight());
		
		}
		
	};
	
	static Comparator<OD> odComparator = new Comparator<OD>() {

		@Override
		public int compare(OD o1, OD o2) {
			if(o1.disutility > o2.disutility)
				return -1;
			else if(o1.disutility < o2.disutility)
				return 1;
			else return 0;
//			return Double.compare(o1.disutility, o2.disutility);
		
		}
		
	};
	
	public static void run(Configuration configuration, Scenario scenario){
		
		transformation = TransformationFactory.getCoordinateTransformation(
				TransformationFactory.WGS84, configuration.getCrs());
		
		parser = new MiDParser();
		parser.run(configuration);
		
		distribution = new Distribution(scenario.getNetwork(), parser);
		
		createHouseholds(configuration, scenario, parser);
		
	}
	
	private static void createHouseholds(Configuration configuration, Scenario scenario, MiDParser parser){
		
		Population population = scenario.getPopulation();
		
		List<MiDHousehold> households = new ArrayList<>();
		households.addAll(parser.getHouseholds().values());
		Collections.sort(households, householdComparator);
		
		for(int i = 0; i < 36531; i++){

			//set home cell
			double r = random.nextDouble() * Geoinformation.getTotalWeightForLanduseKey("residential");
			
			double r2 = 0.;
			
			for(AdministrativeUnit admin : Geoinformation.getAdminUnits().values()){
				
				r2 += admin.getWeightForKey("residential");
				if(r <= r2){
					homeCell = admin;
					break;
				}
				
			}
			
			MiDHousehold template = null;

			double accumulatedWeight = 0.;
			double rand = random.nextDouble() * parser.getSumOfHouseholdWeights();
			
			for(MiDHousehold hh : households){
				
				accumulatedWeight += hh.getWeight();
				if(accumulatedWeight >= rand){
					template = hh;
					break;
				}
				
			}
			
			int nPersons = template.getNPersons();

			Household household = new HouseholdImpl(Id.create(homeCell.getId() + "_" + i, Household.class));
			household.setIncome(new IncomeImpl(template.getIncome(), IncomePeriod.month));
			((HouseholdImpl)household).setMemberIds(new ArrayList<Id<Person>>());
			
			
			double p = random.nextDouble() * homeCell.getWeightForKey("residential");
			accumulatedWeight = 0.;
			
			for(Geometry g : homeCell.getLanduseGeometries().get("residential")){
				
				accumulatedWeight += g.getArea();
				if(p <= accumulatedWeight){
					homeCoord = transformation.transform(GeometryUtils.shoot(g));
					break;
				}
				
			}
			
			for(int j = 0; j < nPersons; j++){
				
				String personId = template.getMemberIds().get(j);
				MiDPerson templatePerson = parser.getPersons().get(personId);
				
				Person person = createPerson(templatePerson, population, random.nextDouble(),
						population.getPersons().size());
				if(person != null){
					population.addPerson(person);
					household.getMemberIds().add(person.getId());
				}
				
			}
			
			scenario.getHouseholds().getHouseholds().put(household.getId(), household);
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	private static Person createPerson(MiDPerson personTemplate, Population population, double personalRandom, int i) {
		
		Person person = population.getFactory().createPerson(Id.createPersonId(homeCell.getId() + "_" + personTemplate.getId() + "_" + i));
		Plan plan = population.getFactory().createPlan();
		
		person.addPlan(plan);
		person.setSelectedPlan(plan);
		
		playground.dhosse.utils.PersonUtils.setAge(person, personTemplate.getAge());
		playground.dhosse.utils.PersonUtils.setEmployed(person, personTemplate.isEmployed());
		String carAvail = personTemplate.getCarAvailable() ? "always" : "never";
		playground.dhosse.utils.PersonUtils.setCarAvail(person, carAvail);
		String hasLicense = personTemplate.hasLicense() ? "yes" : "no";
		playground.dhosse.utils.PersonUtils.setLicence(person, hasLicense);
		
		if(personTemplate.getPlans().size() > 0){
			
			//select a template plan from the mid survey to create a matsim plan
			MiDPlan planTemplate = null;
			
			//if there is only one plan, make it the template
			if(personTemplate.getPlans().size() < 2){
				
				planTemplate = personTemplate.getPlans().get(0);
				
			} else {

				//otherwise, randomly draw a plan from the collection
				double planRandom = personalRandom * personTemplate.getWeightOfAllPlans();
				double accumulatedWeight = 0.;
				
				for(MiDPlan p : personTemplate.getPlans()){
					
					accumulatedWeight += p.getWeigt();
					
					if(planRandom <= accumulatedWeight){
					
						planTemplate = p;
						break;
					
					}
					
				}
				
			}
			
			if(planTemplate.getPlanElements().size() > 1){
			
				String mainActType = planTemplate.getMainActType();
				
				Set<String> availableModes = CollectionUtils.stringToSet(TransportMode.bike + "," + TransportMode.walk + "," + TransportMode.pt);
				if(personTemplate.getCarAvailable()){
					availableModes.add(TransportMode.ride);
					if(personTemplate.hasLicense()){
						availableModes.add(TransportMode.car);
					}
				}
				
				String usedMode = null;
				double totalWeight = 0d;
				
				for(String mode : availableModes){
					
					totalWeight += parser.modeStats.get(mode).getNumberOfEntries();
					
				}
				
				double modeRandom = MatsimRandom.getLocalInstance().nextDouble() * totalWeight;
				double accumulated = 0d;
				for(String mode : availableModes){
					accumulated += parser.modeStats.get(mode).getNumberOfEntries();
					if(modeRandom <= accumulated){
						usedMode = mode;
						break;
					}
				}
				
				List<OD> ods = new ArrayList<>();
				double sumOfDisutilities = 0d;
				
				for(AdministrativeUnit au : Geoinformation.getAdminUnits().values()){
					
					double disutility = distribution.getDisutilityForActTypeAndMode(homeCell.getId(), au.getId(), mainActType, usedMode);
					if(Double.isFinite(disutility)){
						ods.add(new OD(usedMode, disutility,homeCell.getId(), au.getId()));
						sumOfDisutilities += disutility;
					}
					
				}
				
				if(ods.size() < 1)return null;
				
				Collections.sort(ods, odComparator);
				
				double accumulatedWeight = 0d;
				double r = random.nextDouble() * sumOfDisutilities;
				
				for(OD entry : ods){
					
					accumulatedWeight += entry.disutility;
					
					if(r <= accumulatedWeight){
						
						mainActCell = Geoinformation.getAdminUnits().get(entry.toId);
						break;
						
					}
					
				}
				
				Coord mainActCoord = shootLocationForActType(mainActCell, mainActType);
				
				if(mainActType.equals(ActivityTypes.OTHER)){
				}
				
				double departureTime = parser.activityTypeHydrographs.get(mainActType).getDepartureTime();
	
				for(MiDPlanElement pe : planTemplate.getPlanElements()){
					
					if(pe instanceof MiDActivity){
					
						MiDActivity activity = (MiDActivity)pe;
						plan.addActivity(population.getFactory().createActivityFromCoord(activity.getActType(), new CoordImpl(0, 0)));
						
					} else {
						
						MiDWay way = (MiDWay)pe;
						plan.addLeg(population.getFactory().createLeg(way.getMainMode()));
						
					}
					
				}
				
//				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
//				home.setStartTime(0d);
//				home.setEndTime(departureTime);
//				plan.addActivity(home);
//				
//				plan.addLeg(population.getFactory().createLeg(usedMode));
//
//				Activity main = population.getFactory().createActivityFromCoord(mainActType, mainActCoord);
//				main.setMaximumDuration(parser.activityTypeHydrographs.get(mainActType).getDurationInSec());
//				plan.addActivity(main);
//				
//				plan.addLeg(population.getFactory().createLeg(usedMode));
//				
//				Activity home2 = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
//				home2.setEndTime(Time.MIDNIGHT);
//				plan.addActivity(home2);
				
			} else {
			
				Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
				home.setMaximumDuration(24 * 3600);
				home.setStartTime(0);
				home.setEndTime(24 * 3600);
				plan.addActivity(home);
				
			}
			
		} else {
			
			//create a 24hrs home activity
			Activity home = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, homeCoord);
			home.setMaximumDuration(24 * 3600);
			home.setStartTime(0);
			home.setEndTime(24 * 3600);
			plan.addActivity(home);
			
		}
		
		return person;
		
	}
	
	private static Coord shootLocationForActType(AdministrativeUnit au, String actType){
		
		List<Geometry> geometries = au.getLanduseGeometries().get(actType);
		
		int weight = random.nextInt(geometries.size());
		
		return transformation.transform(GeometryUtils.shoot(geometries.get(weight)));
		
	}
	
	static class OD{
		private String mode;
		private double disutility;
		private String fromId;
		private String toId;
		OD(String mode, double disutility, String fromId, String toId){
			this.mode = mode;
			this.disutility = disutility;
			this.fromId = fromId;
			this.toId = toId;
		}
		@Override
		public String toString(){
			return this.fromId + "," + this.toId + "," + this.mode + "," + this.disutility;
		}
	}
	
}
