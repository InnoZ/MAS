package innoz.scenarioGeneration.population.mobilityAttitude;

import java.util.Random;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ScoringParameterSet;
import org.matsim.core.population.PersonUtils;
import org.matsim.utils.objectattributes.ObjectAttributes;

import innoz.scenarioGeneration.utils.ActivityTypes;
import innoz.scenarioGeneration.utils.Modes;

public class MobilityAttitudeGroups {
	
	final static String[] subpops = new String[]{"none", "tradCar", "flexCar", "urbanPt",
			"convBike", "envtPtBike", "multiOpt",null};
	
	final static double[] pWomen = new double[]{
			0.554, 0.505, 0.732, 0.607, 0.467, 0.358
	};
	
	final static double[] pCSmembers = new double[]{
		0.006, 0.01, 0.0, 0.0, 0.04, 0.044	
	};
	
	//THE FOLLOWING IS ONLY VALID FOR OSNABRÜCK!!!
	final static double[][] ageIndices = new double[][]{
		//18-20
		{0.0812307343, 0.1599753649, 0.1377107523, 0.0439406019, 0.2226323829,
			0.3545101638},
		//21-30
		{0.1190968235, 0.1940200101, 0.0801682681, 0.1335844789, 0.1952080832,
			0.2779223363},
		//31-40
		{0.1800128878, 0.1808033635, 0.1052858883, 0.1850132458, 0.1754199664,
			0.1734646483},
		//41-50
		{0.1570834778, 0.169260133, 0.0834929082, 0.1964108196, 0.2258492261,
			0.1679034354},
		//51-60
		{0.2021641433, 0.1835690906, 0.1264166521, 0.1694149475, 0.1544155995,
			0.164019567},
		//>60
		{0.1813054323, 0.1400781414, 0.3032301005, 0.1708760881, 0.1146718119,
			0.0898384259}
	};
	
	final static double[][] incomeIndices = new double[][]{
		//<=800
		{0.1044379926, 0.0985457623, 0.3014908089, 0.1644495321, 0.1672981429,
			0.1637777611},
		//800-1400
		{0.1815373054, 0.1520997717, 0.1209370438, 0.2399544519, 0.1780462033,
			0.1274252239},
		//1400-2000
		{0.1810156449, 0.1607686169, 0.1892008026, 0.1428659121, 0.1622986553,
			0.1638503682},
		//2000-2600
		{0.157034931, 0.1586131716, 0.1540720078, 0.1523789088, 0.1746793053,
			0.2032216755},
		//>2600
		{0.1695004811, 0.2840296039, 0.0836431982, 0.1182953803, 0.1456942337,
			0.1988371028}
	};
	
	public static void addScoringParameterSets(final Config config){

		String[] modes = new String[]{TransportMode.bike, TransportMode.car,
				TransportMode.other, TransportMode.pt, TransportMode.ride,
				TransportMode.walk, Modes.OW, Modes.TW, Modes.FF};
		
		for(String subpop : subpops){
			
			ScoringParameterSet set = config.planCalcScore().getOrCreateScoringParameters(subpop);
			
			ActivityParams education = new ActivityParams(ActivityTypes.EDUCATION);
			education.setTypicalDuration(6 * 3600);
			set.addActivityParams(education);
			
			ActivityParams home = new ActivityParams(ActivityTypes.HOME);
			home.setTypicalDuration(12 * 3600);
			set.addActivityParams(home);
			
			ActivityParams leisure = new ActivityParams(ActivityTypes.LEISURE);
			leisure.setTypicalDuration(4 * 3600);
			set.addActivityParams(leisure);
			
			ActivityParams other = new ActivityParams(ActivityTypes.OTHER);
			other.setTypicalDuration(2 * 3600);
			set.addActivityParams(other);
			
			ActivityParams shopping = new ActivityParams(ActivityTypes.SHOPPING);
			shopping.setTypicalDuration(1 * 3600);
			set.addActivityParams(shopping);
			
			ActivityParams work = new ActivityParams(ActivityTypes.WORK);
			work.setTypicalDuration(8 * 3600);
			set.addActivityParams(work);
			
			ActivityParams kindergarten = new ActivityParams(ActivityTypes.KINDERGARTEN);
			kindergarten.setTypicalDuration(3 * 3600);
			set.addActivityParams(kindergarten);
			
			for(String mode : modes){
				
				ModeParams params = new ModeParams(mode);
				params.setConstant(0.0);
				params.setMarginalUtilityOfDistance(-0.0);
				params.setMarginalUtilityOfTraveling(-6.0);
				params.setMonetaryDistanceRate(0.0);
				set.addModeParams(params);
				
			}
			
		}
		
	}
	
	public static String assignPersonToGroup(Person person, Random random, double hhIncome, ObjectAttributes personAttributes){
		
		int age = PersonUtils.getAge(person);
		String sex = PersonUtils.getSex(person);
		
		int ageIndex = 0;
		int incomeIndex = 0;
		
		if(age < 18){
			
			return "none";
			
		} else if(age > 17 && age < 21){
			
			ageIndex = 0;
			
		} else if(age > 20 && age < 31){
			
			ageIndex = 1;
			
		} else if(age > 30 && age < 41){
			
			ageIndex = 2;
			
		} else if(age > 40 && age < 51){
			
			ageIndex = 3;
			
		} else if(age > 50 && age < 61){
			
			ageIndex = 4;
			
		} else {
			
			ageIndex = 5;
			
		}
		
		if(hhIncome <= 800){
			
			incomeIndex = 0;
			
		} else if(hhIncome > 800 && hhIncome <= 1400){
			
			incomeIndex = 1;
			
		} else if(hhIncome > 1400 && hhIncome <= 2000){
			
			incomeIndex = 2;
			
		} else if(hhIncome > 2000 && hhIncome <= 2600){
			
			incomeIndex = 3;
			
		} else {
			
			incomeIndex = 4;
			
		}
		
		return getMobilityAttitudeGroupForAgeAndIncome(person, ageIndex,
				incomeIndex, sex, personAttributes, random);
	
	}
	
	private static String getMobilityAttitudeGroupForAgeAndIncome(Person person,
			int ageIndex, int incomeIndex, String sex, ObjectAttributes atts,  Random random){
		
		double[] indices = new double[6];
		double sum = 0.0d;
		
		for(int i = 0; i < 6; i++){
			
			double pSex = sex.equals("m") ? 1- pWomen[i] : pWomen[i];
			
			indices[i] = ageIndices[ageIndex][i] * incomeIndices[incomeIndex][i] * pSex;
			sum += indices[i];
			
		}
		
		double p = random.nextDouble() * sum;
		double accumulatedWeight = 0.0d;
		
		String result = null;
		
		for(int i = 0; i < 6; i++){
			
			accumulatedWeight += indices[i];
			
			if(p <= accumulatedWeight){
				result = subpops[i+1];
				
				double pCS = random.nextDouble();
				
				if(pCS <= pCSmembers[i]){
					
					atts.putAttribute(person.getId().toString(), "OW_CARD", "true");
					atts.putAttribute(person.getId().toString(), "RT_CARD", "true");
					atts.putAttribute(person.getId().toString(), "FF_CARD", "true");
					
				}
				
				break;
				
			}
			
		}
		
		return result;
		
	}
	
}