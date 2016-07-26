package innoz.io.database.handler;

import java.util.Map;

import innoz.scenarioGeneration.population.surveys.SurveyHousehold;
import innoz.scenarioGeneration.population.surveys.SurveyObject;

public class HouseholdIncomeHandler implements DefaultHandler {

	@Override
	public void handle(SurveyObject obj, Map<String, String> attributes) {

		SurveyHousehold hh = (SurveyHousehold)obj;
		
		String inc = attributes.get("hheink");
		
		if(inc.equals("1")){
			
			hh.setIncome(250);
			
		} else if(inc.equals("2")){
			
			hh.setIncome(700);
			
		} else if(inc.equals("3")){
			
			hh.setIncome(1200);
			
		} else if(inc.equals("4")){
			
			hh.setIncome(1750);
			
		} else if(inc.equals("5")){
			
			hh.setIncome(2300);
			
		} else if(inc.equals("6")){
			
			hh.setIncome(2800);
			
		} else if(inc.equals("7")){
			
			hh.setIncome(3300);
			
		} else if(inc.equals("8")){
			
			hh.setIncome(3800);
			
		} else if(inc.equals("9")){
			
			hh.setIncome(4300);
			
		} else if(inc.equals("10")){
			
			hh.setIncome(4800);
			
		} else if(inc.equals("11")){
			
			hh.setIncome(5300);
			
		} else if(inc.equals("12")){
			
			hh.setIncome(5800);
			
		} else if(inc.equals("13")){
			
			hh.setIncome(6300);
			
		} else if(inc.equals("14")){
			
			hh.setIncome(6800);
			
		} else if(inc.equals("15")){
			
			hh.setIncome(7300);
			
		}
		
	}

}