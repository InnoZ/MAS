package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIdHandler;
import com.innoz.toolbox.io.database.handler.HouseholdIncomeHandler;
import com.innoz.toolbox.io.database.handler.HouseholdWeightHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyHousehold;
import com.innoz.toolbox.utils.data.Tree.Node;

public class ReadHouseholdDatabaseTask extends DatabaseTask {
	
	private Geoinformation geoinformation;
	private Set<String> ids;

	public ReadHouseholdDatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids) {

		super(constants);
		this.geoinformation = geoinformation;
		this.ids = ids;
		
		this.handlers = new HashSet<>();
		this.handlers.add(new HouseholdIdHandler());
		this.handlers.add(new HouseholdIncomeHandler());
		this.handlers.add(new HouseholdWeightHandler());
		
	}
	
	@Override
	public void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException{
		
		Statement statement = connection.createStatement();
		statement.setFetchSize(2000);
	
		String table = surveyType.equals("mid") ? "mid2008.households_raw" : "srv2013.households";
		
		String q = "select * from " + table;
		
		if(surveyType.equals("mid")){
			
			q +=  " where ";
			
			int cntOut = 0;
			Set<Integer> knownRegionTypes = new HashSet<>();

			for(Node<AdministrativeUnit> node: geoinformation.getAdminUnits()){
				
				AdministrativeUnit entry = node.getData();
				
				if(ids.contains(entry.getId().substring(0, 5))&&!knownRegionTypes.contains(entry.getRegionType())){
				
					cntOut++;
					
					q += SurveyConstants.regionType(surveyType) + " = " + entry.getRegionType();
					knownRegionTypes.add(entry.getRegionType());

					if(cntOut < ids.size()){

						q += " or ";
						
					}
					
				}
				
			}
			
//			for(Entry<Integer, Set<Integer>> entry : geoinformation.getRegionTypes().entrySet()){
//
//				cntOut++;
//				
//				q += SurveyConstants.regionType(surveyType) + " = " + entry.getKey();
//
//				if(cntOut < geoinformation.getRegionTypes().size()){
//
//					q += " or ";
//					
//				}
//				
//			}
			
		} else {
			
			q += " where st_code=44"; //Osnabrück 
			
		}
		
		q += ";";
		
		ResultSet resultSet = statement.executeQuery(q);
		
		while(resultSet.next()){
			
			Map<String, String> attributes = new HashMap<>();
			attributes.put(SurveyConstants.householdId(surveyType), resultSet.getString(SurveyConstants.householdId(surveyType)));
			attributes.put(SurveyConstants.householdIncomePerMonth(surveyType),
					resultSet.getString(SurveyConstants.householdIncomePerMonth(surveyType)));
			attributes.put(SurveyConstants.householdWeight(surveyType),
					Double.toString(resultSet.getDouble(SurveyConstants.householdWeight(surveyType))));
			int rtyp = resultSet.getInt(SurveyConstants.regionType(surveyType));
			
			SurveyHousehold hh = new SurveyHousehold();
			
			for(DefaultHandler handler : this.handlers){
				
				handler.handle(hh, attributes, surveyType);
				
			}
			
			container.addHousehold(hh, rtyp);
			
		}
		
		resultSet.close();
		statement.close();
		
	}

}
