package com.innoz.scenarios.osnabrück;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.carsharing.manager.demand.membership.MembershipContainer;
import org.matsim.contrib.carsharing.manager.demand.membership.PersonMembership;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;

import com.innoz.toolbox.scenarioGeneration.carsharing.CSMembersXmlWriter;

/*
 * This class can be used to adjust car-sharing attributes such as company and type with a given propability for each mobilityAttitudeGroup
 * Define percentage for each group in line 61-68
 * @author bsmoehring
 */

public class RunAdjustCSMemberships {
	
	static final String FILEBASE = "/home/bmoehring/3connect/";

	static final String MOBILITYATTITUDE = "mobilityAttitude";
	
	static final String TRADCAR = "tradCar";
	static final String FLEXCAR = "flexCar";
	static final String URBANPT = "urbanPt";
	static final String CONVBIKE = "convBike";
	static final String ENVTPTBIKE = "envtPtBike";
	static final String MULTIOPT = "multiOpt";
	static final String NONE = "none";
	
	static final String COMPANYMEMBERSHIP = "stadtteilauto";
	static final Set<String> CARSHARINGTYPE = new java.util.HashSet<String>(Arrays.asList(new String[]{"twoway", "freefloating"}));
	
	public static void main(String[] args) {
				
		Config config = ConfigUtils.createConfig();
		
		Scenario scenario = ScenarioUtils.createScenario(config);
		
		PopulationReader pr = new PopulationReader(scenario);
		pr.readFile(FILEBASE + "plans_2025.xml.gz");
		ObjectAttributesXmlReader oa= new ObjectAttributesXmlReader(scenario.getPopulation().getPersonAttributes());
		oa.readFile(FILEBASE + "personAttributes_2025.xml.gz");
		
		ObjectAttributes attributes = scenario.getPopulation().getPersonAttributes();
			
		{
			Map<String, Double> mscDistribution = new HashMap<String, Double>();
			//		You can specify the distribution for each mobilityAttitudeGroup here:
			mscDistribution.put(TRADCAR, 		0.006);
			mscDistribution.put(FLEXCAR, 		0.010);
//			mscDistribution.put(URBANPT, 		0.000);
//			mscDistribution.put(CONVBIKE, 		0.000);
			mscDistribution.put(ENVTPTBIKE, 	0.040);
			mscDistribution.put(MULTIOPT, 		0.044);
//			mscDistribution.put(NONE, 			0.000);
//			mscDistribution.put("null", 		0.000);
			MembershipContainer msc = new MembershipContainer();
			System.out.println(mscDistribution.toString());
			msc = adjustMemberships(scenario.getPopulation(), attributes, msc, mscDistribution);
			
			CSMembersXmlWriter writer = new CSMembersXmlWriter(msc);
			writer.writeFile(FILEBASE + "csMembers.xml.gz");
		}

	}
	
	private static MembershipContainer adjustMemberships(Population population, ObjectAttributes attributes, MembershipContainer msc, Map<String, Double> mscDistribution) {
		
		int csmembers=0;
		int nocsmembers=0;
		
		for (Person p : population.getPersons().values()){
			String id = p.getId().toString();
			PersonMembership pms;
			Map<String, Set<String>> membershipsPerCompany = new HashMap<String, Set<String>>();
			try {
				String ma = attributes.getAttribute(id, MOBILITYATTITUDE).toString();
				Double percentage = mscDistribution.get(ma);
				Double rnd = Math.random();
				if (percentage > rnd){
						
					membershipsPerCompany.put(COMPANYMEMBERSHIP, CARSHARINGTYPE);
					csmembers++;
				
				} else {
					
//					membershipsPerCompany.put(null, new java.util.HashSet<String>());
					nocsmembers++;
					
				}
				
			} catch (Exception e){
				
//				membershipsPerCompany.put(null, new java.util.HashSet<String>());
				nocsmembers++;
			}
			
			pms = new PersonMembership(membershipsPerCompany, null);
			
			msc.addPerson(id, pms);
			
		}
		
		System.out.println("csMembers: " + csmembers);
		System.out.println("NOcsMembers: " + nocsmembers);
		
		return msc;
	}

}
