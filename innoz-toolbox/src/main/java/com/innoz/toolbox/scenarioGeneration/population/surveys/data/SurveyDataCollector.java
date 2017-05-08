package com.innoz.toolbox.scenarioGeneration.population.surveys.data;

public class SurveyDataCollector {

//	private Map<Integer, PersonGroupStats> perPersonGroupStats = new HashMap<Integer, PersonGroupStats>(12);
//	
//	private SurveyDataCollector(){};
//	
//	enum OD_GROUP {
//
//		// home based trips
//		home_work(ActivityTypes.HOME, ActivityTypes.WORK),
//		home_education(ActivityTypes.HOME, ActivityTypes.EDUCATION),
//		home_other(ActivityTypes.HOME, ActivityTypes.OTHER),
//		
//		// non home based trips
//		education_home(ActivityTypes.EDUCATION, ActivityTypes.HOME),
//		work_home(ActivityTypes.WORK, ActivityTypes.HOME),
//		other_home(ActivityTypes.OTHER, ActivityTypes.HOME);
//		
//		String act1;
//		String act2;
//		
//		OD_GROUP(String originActType, String destinationActType) {
//			this.act1 = originActType;
//			this.act2 = destinationActType;
//		}
//		
//		static OD_GROUP getEnumForValues(String first, String second) {
//			
//			for(OD_GROUP v : values()) {
//				
//				if(v.act1.equals(first) && v.act2.equals(second)) {
//					
//					return v;
//					
//				}
//				
//			}
//			
//			return null;
//			
//		}
//		
//	}
//	
//	public void process(final SurveyDataContainer container) {
//		
//		for(SurveyPerson person : container.getPersons().values()) {
//
//			int personGroup = person.getPersonGroup();
//			
//			if(personGroup < 95) {
//				
//				if(!perPersonGroupStats.containsKey(personGroup)) {
//					
//					this.perPersonGroupStats.put(personGroup, new PersonGroupStats(personGroup));
//
//				}
//				
//				PersonGroupStats pgStats = this.perPersonGroupStats.get(personGroup);
//				
//				pgStats.nPersons++;
//				pgStats.ageStats.handleNewEntry(person.getAge());
//				int hasLicense = person.hasLicense() ? 1 : 0;
//				pgStats.licenseStats.handleNewEntry(hasLicense);
//				int carAvail = person.hasCarAvailable() ? 1 : 0;
//				pgStats.carAvailStats.handleNewEntry(carAvail);
//				
//				if(person.isMobile()) {
//				
//					pgStats.mobilePersons++;
//					
//					for(SurveyPlan plan : person.getPlans()) {
//						
//						pgStats.nActsStats.handleNewEntry((int)(plan.getPlanElements().size()/2+1));
//						
//						for(SurveyPlanElement element : plan.getPlanElements()) {
//							
//							if(element instanceof SurveyPlanActivity) {
//								
//								handleActivity((SurveyPlanActivity) element, pgStats.activityStats);
//								
//							} else {
//								
//								String act1 = ((SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(element) - 1)).getActType();
//								String act2 = ((SurveyPlanActivity) plan.getPlanElements().get(plan.getPlanElements().indexOf(element) + 1)).getActType();
//								handleTrip((SurveyPlanTrip) element, pgStats.modeStats, act1, act2);
//								
//							}
//							
//						}
//						
//					}
//					
//				}
//				
//			}
//
//		}
//		
//	}
//	
//	private void handleActivity(SurveyPlanActivity activity, Map<String, ActivityStats> stats) {
//		
//		String actType = activity.getActType();
//		
//		if(!actType.equals(ActivityTypes.HOME)) {
//
//			double start = activity.getStartTime();
//			double end = activity.getEndTime();
//			if(end < start) end += Time.MIDNIGHT;
//			double duration = end - start;
//
//			if(!stats.containsKey(actType)) {
//				
//				stats.put(actType, new ActivityStats(actType));
//				
//			}
//			
//			stats.get(actType).durationStats.handleNewEntry(duration);
//			stats.get(actType).nActs++;
//			
//		}
//		
//	}
//	
//	private void handleTrip(SurveyPlanTrip trip, Map<String, ModeStats> stats, String act1, String act2) {
//		
//		String mode = trip.getMainMode();
//		double distance = trip.getTravelDistance();
//		
//		if(!stats.containsKey(mode)) {
//			
//			stats.put(mode, new ModeStats(mode));
//			
//		}
//		
//		stats.get(mode).modeDistanceStats.handleNewEntry(distance);
//		stats.get(mode).nTrips++;
//		
//	}
//	
//	static class PersonGroupStats {
//		
//		final int personGroup;
//		
//		int nPersons = 0;
//		int mobilePersons = 0;
//		
//		Map<String, ActivityStats> activityStats = new HashMap<>();
//		Map<String, ModeStats> modeStats = new HashMap<>();
//		
//		RecursiveStatsContainer nActsStats = new RecursiveStatsContainer();
//		RecursiveStatsContainer sexStats = new RecursiveStatsContainer();
//		RecursiveStatsContainer ageStats = new RecursiveStatsContainer();
//		RecursiveStatsContainer licenseStats = new RecursiveStatsContainer();
//		RecursiveStatsContainer carAvailStats = new RecursiveStatsContainer();
//		
//		private PersonGroupStats(final int personGroup) {
//			this.personGroup = personGroup;
//		}
//		
//		@Override
//		public String toString() {
//			
//			return "n persons:" + nPersons + "; mobility rate:" + ((double)mobilePersons/(double)nPersons) + "; sex;" + sexStats.getMean() + "; age min:" + ageStats.getMin() + ", max:" +
//					ageStats.getMax() + ", avg:" + ageStats.getMean() + "; license:" + licenseStats.getMean() + "; car avail:" + carAvailStats.getMean();
//			
//		}
//		
//	}
//	
//	static class ActivityStats {
//		
//		final String actType;
//		RecursiveStatsContainer durationStats = new RecursiveStatsContainer();
//		double nActs;
//		
//		private ActivityStats(String actType) {
//			this.actType = actType;
//		}
//		
//		@Override
//		public String toString() {
//
//			return "n acts:" + nActs + "; duration min:" + Time.writeTime(durationStats.getMin()) + ", max:" +
//					Time.writeTime(durationStats.getMax()) + ", avg:" + Time.writeTime(durationStats.getMean());
//			
//		}
//		
//	}
//	
//	static class TripPurposeStats {
//		
//		final OD_GROUP od;
//		
//		private TripPurposeStats(final OD_GROUP od) {
//			this.od = od;
//		}
//		
//	}
//	
//	static class ModeStats {
//	
//		final String mode;
//		RecursiveStatsContainer modeDistanceStats = new RecursiveStatsContainer();
//		double nTrips;
//		
//		private ModeStats(final String mode) {
//			this.mode = mode;
//		}
//		
//		@Override
//		public String toString() {
//			
//			return "n trips:" + nTrips + "distance min:" + modeDistanceStats.getMin() + ", max:" + modeDistanceStats.getMax() + ", avg:" + modeDistanceStats.getMean();
//			
//		}
//		
//	}
//	
//	public static void main(String args[]) throws NoSuchAuthorityCodeException, FactoryException, InstantiationException,
//		IllegalAccessException, ClassNotFoundException, SQLException, ParseException, IOException {
//		
//		Configuration configuration = ConfigurationUtils.createConfiguration();
//		
//		configuration.psql().setPsqlPort(5432);
//		configuration.misc().setNumberOfThreads(2);
//		
//		configuration.surveyPopulation().setDayTypes(DayTypes.weekday);
//		
//		String id = "15001";
//		
//		AreaSet areaSet = new AreaSet();
//		areaSet.setIds(id);
//		areaSet.setIsSurveyArea(true);
//		areaSet.setNetworkLevel(6);
//		areaSet.setPopulationSource(PopulationSource.SURVEY);
//		configuration.scenario().addAreaSet(areaSet);
//		
//		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
//		
//		// Container for geoinformation (admin borders, landuse)
//		Geoinformation geoinformation = new Geoinformation(configuration.scenario().getActivityLocationsType());
//
//		// A class that reads data from database tables into local containers
//		DatabaseReader dbReader = new DatabaseReader(configuration, geoinformation);
//		dbReader.readGeodataFromDatabase(configuration, scenario);
//		dbReader.readPopulationFromDatabase(configuration, scenario);
//		new BbsrDataReader().read(geoinformation, "/home/dhosse/workspace/MAS/innoz-toolbox/src/main/resources/regionstypen.csv");
//		
//		// Create a MATSim network from OpenStreetMap data
//		NetworkCreatorFromPsql nc = new NetworkCreatorFromPsql(scenario.getNetwork(),
//					geoinformation,	configuration);
//		nc.create(dbReader);
//		
//		SurveyDatabaseParserV2 parser = new SurveyDatabaseParserV2();
//		SurveyDataContainer container = new SurveyDataContainer(configuration);
//		parser.run(configuration, container, geoinformation, CollectionUtils.stringToSet(id));
//		
//		SurveyDataCollector collector = new SurveyDataCollector();
//		collector.process(container);
//		
//		RecursiveStatsContainer ageStats = new RecursiveStatsContainer();
//
//		for(PersonGroupStats stats : collector.perPersonGroupStats.values()) {
//			System.out.println(stats.personGroup + "\t" + stats.nPersons + "\t" + stats.ageStats.getMean());
//			for(Double d : stats.ageStats.getEntries()) {
//				ageStats.handleNewEntry(d);
//			}
//		}
//		
//		Population population = scenario.getPopulation();
//		
//		BufferedWriter out = IOUtils.getBufferedWriter("/home/dhosse/scenarios/dessau/persongroupTest.csv");
//		out.write("person group;age");
//		out.newLine();
//		
//		Random random = MatsimRandom.getRandom();
//		
//		for(Node<AdministrativeUnit> node : geoinformation.getAdminUnits()) {
//			
//			AdministrativeUnit unit = node.getData();
//			
//			if(unit.getPopulationMap() != null) {
//				
//				for(Entry<String, Integer> entry : unit.getPopulationMap().entrySet()) {
//					
//					String y = entry.getKey().substring(entry.getKey().length()-1);
//					if (y.equals("w")) {
//						y="f";
//					}
//					String sex = y;
//					
//					int ageFrom = Integer.parseInt(entry.getKey().substring(8, 10));	
//					int x = Integer.parseInt(entry.getKey().substring(12, 14));	
////					for ageGroup85to101 the String consists of 16 characters
//					if (entry.getKey().length()==16){
//						x = Integer.parseInt(entry.getKey().substring(12, 15));
//					}	
//					int ageTo = x;
//					
//					int totalPersons = 0;
//					Map<Integer,Integer> potentialPersonGroups = new HashMap<>();
//
//					for(PersonGroupStats stats : collector.perPersonGroupStats.values()) {
//						
//						if(stats.ageStats.getMin() <= ageTo || stats.ageStats.getMax() >= ageFrom) {
//							
//							totalPersons += stats.nPersons;
//							potentialPersonGroups.put(stats.personGroup, stats.nPersons);
//							
//						}
//						
//					}
//					
//					for(int ii = 0; ii < entry.getValue(); ii++) {
//
//						int r = random.nextInt(totalPersons);
//						int accumulated = 0;
//						
//						for(Entry<Integer, Integer> entry2 : potentialPersonGroups.entrySet()) {
//							
//							// TODO: some notes about variables' distribution
//							// age, travel distance: normal distribution w/ lower and upper bound
//							// license, car availability, transport mode: threshold
//							// sex: forecast data
//							
//							accumulated += entry2.getValue();
//							if(r <= accumulated) {
//								
//								PersonGroupStats rStats = collector.perPersonGroupStats.get(entry2.getKey());
//								double age = Math.round(rStats.ageStats.getStdDev() * random.nextGaussian() + rStats.ageStats.getMean());
//								if(age < rStats.ageStats.getMin()) age = rStats.ageStats.getMin();
//								if(age > rStats.ageStats.getMax()) age = rStats.ageStats.getMax();
//								
//								double personalRandom = random.nextDouble();
//								
//								boolean hasLicense = false;
//								if(personalRandom <= rStats.licenseStats.getMean()) {
//									hasLicense = true;
//								}
//								boolean carAvail = false;
//								if(personalRandom <= rStats.carAvailStats.getMean()) {
//									carAvail = true;
//								}
//								
//								out.write(Integer.toString(entry2.getKey()) + ";" + age);
//								out.newLine();
//								
//								Person person = population.getFactory().createPerson(Id.createPersonId(population.getPersons().size()));
//								
//								Plan plan = population.getFactory().createPlan();
//								
//								Activity act = population.getFactory().createActivityFromCoord(ActivityTypes.HOME, new Coord(0d,0d));
//								plan.addActivity(act);
//								
//								double mobilityRate = (double)(rStats.mobilePersons) / (double)(rStats.nPersons);
//								double nActs = 0;
//								if(personalRandom <= mobilityRate) {
//
//									nActs = Math.round(rStats.nActsStats.getMean() + rStats.nActsStats.getStdDev() * random.nextGaussian());
//									
//									if(nActs < rStats.nActsStats.getMin()) {
//										nActs = rStats.nActsStats.getMin();
//									}
//									
//								}
//								
//								int totalNumberOfActs = 0;
//								Map<String, Double> actType2Number = new HashMap<>();
//								for(Entry<String,ActivityStats> actStats : rStats.activityStats.entrySet()) {
//									totalNumberOfActs += actStats.getValue().nActs;
//									actType2Number.put(actStats.getKey(), actStats.getValue().nActs);
//								}
//								
//								String mode = chooseTransportMode(rStats, carAvail, hasLicense, personalRandom);
//								Leg leg = population.getFactory().createLeg(mode);
//								plan.addLeg(leg);
//								
//								double accumulated2 = 0d;
//								double threshold = personalRandom * totalNumberOfActs;
//								
//								for(Entry<String,Double> ee : actType2Number.entrySet()) {
//									
//									accumulated2 += ee.getValue();
//									if(accumulated2 > threshold) {
//										
//										Activity act2 = population.getFactory().createActivityFromCoord(ee.getKey(), new Coord(0d, 0d));
//										plan.addActivity(act2);
//										break;
//									}
//									
//								}
//								
//								person.addPlan(plan);
//								person.setSelectedPlan(plan);
//								
//								population.addPerson(person);
//								
//								population.getPersonAttributes().putAttribute(person.getId().toString(), "age", age);
//								population.getPersonAttributes().putAttribute(person.getId().toString(), "sex", sex);
//								population.getPersonAttributes().putAttribute(person.getId().toString(), "persongroup", entry2.getKey());
//								population.getPersonAttributes().putAttribute(person.getId().toString(), "carAvail", carAvail);
//								population.getPersonAttributes().putAttribute(person.getId().toString(), "hasLicense", hasLicense);
//								
//								break;
//								
//							}
//							
//						}
//						
//					}
//					
//					out.flush();
//					
//				}
//				
//			}
//			
//		}
//		
//		out.close();
//		
//		new PopulationWriter(population).write("/home/dhosse/scenarios/dessau/plans.xml.gz");
//		new ObjectAttributesXmlWriter(population.getPersonAttributes()).writeFile("/home/dhosse/scenarios/dessau/personAttributes.xml.gz");
//		
//	}
//	
//	private static String chooseTransportMode(PersonGroupStats stats, boolean carAvail, boolean hasLicense, double personalRandom) {
//		
//		double allTrips = 0;
//		
//		for(ModeStats s : stats.modeStats.values()) {
//
//			allTrips += s.nTrips;
//			
//			if(s.mode.equals("car") && (!hasLicense || !carAvail)) {
//				
//				allTrips -= s.nTrips;
//				
//			}
//			
//		}
//		
//		double accumulated = 0;
//		double threshold = personalRandom * allTrips;
//		
//		for(ModeStats s : stats.modeStats.values()) {
//			
//			accumulated += s.nTrips;
//			if(threshold <= accumulated) return s.mode;
//			
//		}
//		
//		return null;
//		
//	}
	
}