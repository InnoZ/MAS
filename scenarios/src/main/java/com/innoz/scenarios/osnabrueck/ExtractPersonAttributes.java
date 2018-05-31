package com.innoz.scenarios.osnabrueck;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.SynchronousQueue;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.AttributeConverter;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlReader;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;
import org.matsim.utils.objectattributes.attributable.AttributesUtils;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;

/**
 * fuel types of private fleet in scenarios according to Öko-Institut 2015 (AMS = Negative, KS80 = Trend, KS95 = Positiv), values
 * between 2020 and 2030 are interpolated
 * 
 * 				gasoline	diesel	gas		hybrid	electric
 * NEGATIVE:	0.49		0.375	0.01	0.1		0.025
 * TREND:		0.49		0.305	0.04	0.115	0.05
 * POSITIVE		0.51		0.26	0.01	0.145	0.075
 *  
 * @author dhosse
 *
 */
public class ExtractPersonAttributes {

	public static void main(String[] args) {
		
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile("/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/input_trend/plans_2025.xml.gz");
		new ObjectAttributesXmlReader(scenario.getPopulation().getPersonAttributes()).readFile("/home/bmoehring/3connect/3connect_trend/Trendszenario_DLR_allAgents/input_trend/"
				+ "personAttributes_2025.xml.gz");

		createScenarioFleet(scenario);
		
		new ObjectAttributesXmlWriter(scenario.getPopulation().getPersonAttributes()).writeFile("/home/bmoehring/3connect/testAgentAttributes/personAttributes_2025_Positiv.xml.gz");
		PopulationWriter pw = new PopulationWriter(scenario.getPopulation());
		pw.writeV6("/home/bmoehring/3connect/testAgentAttributes/plans_2025_routed.xml.gz");
		
	}
	
	private static void createScenarioFleet(final Scenario scenario) {
		
		double pGasoline = 0.51;
		double pDiesel = 0.77;
		double pGas = 0.78;
		double pHybrid = 0.925;
		
		Random random = MatsimRandom.getRandom();
		
		List<String> carAvailTypes= new ArrayList<String>();
		int always = 0;
		int never = 0;
		int other = 0;
		int verbrenner = 0;
		int electric = 0;
		
		for( Person p : scenario.getPopulation().getPersons().values()){
			String carAvail = p.getAttributes().getAttribute("carAvail").toString();
			
			if (!carAvailTypes.contains(carAvail)){
				carAvailTypes.add(carAvail);
			}
			if (carAvail.equalsIgnoreCase("always")){
				always ++;
			} else if (carAvail.equalsIgnoreCase("never")){
				never++;
				continue;
			} else {
				other++;
				System.out.println(carAvail);
			}
			
//			if(random.nextDouble() <= pGasoline) {
//				
//				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", "verbrenner");
//				
//			} else if(random.nextDouble() <= pDiesel) {
//				
//				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", "verbrenner");
//				
//			} else if(random.nextDouble() <= pGas) {
//				
//				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", "verbrenner");
//				
//			} else 
			if(random.nextDouble() <= pHybrid) {
				
				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", "verbrenner");
				p.getAttributes().putAttribute("vehicleType", "verbrenner");
				verbrenner ++;
				
			} else {
				
				scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", "electric");
				p.getAttributes().putAttribute("vehicleType", "electric");

				electric ++;
			}
			
		};
		
		System.out.println("always " + always);
		System.out.println("never " + never);
		System.out.println("other " + other);
		System.out.println(carAvailTypes.toString());
		System.out.println(carAvailTypes);
		System.out.println("electric " + electric);
		System.out.println("verbrenner " + verbrenner);
		
	}
	
	private static void createCurrentPrivateFleet(final Scenario scenario) {
		try {
			
			Connection c = PsqlAdapter.createConnection(DatabaseConstants.SURVEYS_DB);
			
			Statement statement = c.createStatement();
			
			ResultSet result = statement.executeQuery("SELECT hhid, h048 FROM mid2008.cars_raw WHERE rtypd7=3;");
			
			while(result.next()) {
				
				String hhId = result.getString("hhid");
				String vehType = resolveVehicleType(result.getInt("h048"));
				
				scenario.getPopulation().getPersons().values().stream().filter(p -> p.getId().toString().split("_")[1].startsWith(hhId))
					.forEach(p -> {
					
						scenario.getPopulation().getPersonAttributes().putAttribute(p.getId().toString(), "vehicleType", vehType);
						
				});
				
			}
			
			result.close();
			statement.close();
			c.close();
			
		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static String resolveVehicleType(int type) {
		
		switch(type) {
			case 1: return "gasoline";
			case 2: return "diesel";
			case 3: return "gas";
			case 4: return "hybrid";
			case 5: return "electric";
			default: return "other";
		}
		
	}
	
	private static void atts() {
		Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
		new PopulationReader(scenario).readFile("/home/dhosse/scenarios/3connect/scenarios2025/plans_2025.xml.gz");
		
		final ObjectAttributes personAttributes = scenario.getPopulation().getPersonAttributes();
		
		scenario.getPopulation().getPersons().values().stream().forEach(p -> {
			
			String carAvail = (String)p.getAttributes().getAttribute("carAvail");
			personAttributes.putAttribute(p.getId().toString(), "carAvail", carAvail);
			
			if(p.getAttributes().getAttribute("sex") != null) {
				String sex = (String)p.getAttributes().getAttribute("sex");
				String hasLicense = (String)p.getAttributes().getAttribute("hasLicense");
				boolean isEmployed = (boolean)p.getAttributes().getAttribute("employed");
				int age = (int)p.getAttributes().getAttribute("age");
				
				personAttributes.putAttribute(p.getId().toString(), "sex", sex);
				personAttributes.putAttribute(p.getId().toString(), "hasLicense", hasLicense);
				personAttributes.putAttribute(p.getId().toString(), "employed", isEmployed);
				personAttributes.putAttribute(p.getId().toString(), "age", age);
			}
			
		});
		
		new ObjectAttributesXmlWriter(personAttributes).writeFile("/home/dhosse/scenarios/3connect/scenarios2025/personAttributes_2025.xml.gz");
	}

}