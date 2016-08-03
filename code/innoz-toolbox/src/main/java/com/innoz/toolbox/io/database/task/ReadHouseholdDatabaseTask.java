package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIdHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIncomeHandler;
import com.innoz.toolbox.io.database.handler.HouseholdWeightHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;

public class ReadHouseholdDatabaseTask extends DatabaseTask {
	
	private Geoinformation geoinformation;

	public ReadHouseholdDatabaseTask(SurveyConstants constants, Geoinformation geoinformation) {

		super(constants);
		this.geoinformation = geoinformation;
		
		this.handlers = new HashSet<>();
		this.handlers.add(new HouseholdIdHandler());
		this.handlers.add(new HouseholdIncomeHandler());
		this.handlers.add(new HouseholdWeightHandler());
		
	}
	
	@Override
	public void parse(Connection connection, boolean isUsingHouseholds, boolean onlyWorkingDays,
			SurveyDataContainer container) throws SQLException{
		
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
		
		ResultSet resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			Map<String, String> attributes = new HashMap<>();
			attributes.put(this.constants.householdId(), resultSet.getString(this.constants.householdId()));
			attributes.put(this.constants.householdIncomePerMonth(), resultSet.getString(this.constants.householdIncomePerMonth()));
			attributes.put(this.constants.householdWeight(), Double.toString(resultSet.getDouble(this.constants.householdWeight())));
			int rtyp = resultSet.getInt(this.constants.regionType());
			
			SurveyHousehold hh = new SurveyHousehold();
			
			for(DefaultHandler handler : this.handlers){
				
				handler.handle(hh, attributes);
				
			}
			
			container.addHousehold(hh, rtyp);
			
		}
		
		resultSet.close();
		statement.close();
		
	}

}