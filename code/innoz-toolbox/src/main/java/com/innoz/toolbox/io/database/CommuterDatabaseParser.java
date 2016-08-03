package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.scenarioGeneration.population.commuters.CommuterDataElement;

public class CommuterDatabaseParser {

	private static final Logger log = Logger.getLogger(CommuterDatabaseParser.class);
	
	Set<CommuterDataElement> commuterData;
	
	public void run(Configuration configuration){
		
		try {
		
			log.info("Parsing commuter data");
			
			this.commuterData = new HashSet<CommuterDataElement> ();
			
			Class.forName(DatabaseConstants.PSQL_DRIVER).newInstance();
			Connection connection = DriverManager.getConnection(DatabaseConstants.PSQL_URL + configuration.getLocalPort() +
					"/" + DatabaseConstants.SURVEYS_DB, configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				// Select the entries that contain the administrative areas we defined in the
				// configuration.
				
				Set<String> allAdminUnits = new HashSet<String>();
				
				for(String key : configuration.getAdminUnitEntries().keySet()){
					
					allAdminUnits.add(key);
					
				}
				
				String homeString = createChainedStatementFromSet(allAdminUnits, "home_id");
				String workString = createChainedStatementFromSet(allAdminUnits, "work_id");
				
				this.execute(connection, "2015_commuters", homeString, workString);
//				this.execute(connection, "2015_reverse", homeString, workString);
//				this.execute(connection, "2015_internal", homeString, workString);
				
				
			}
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private void execute(Connection connection, String table, String homeString, String workString) throws SQLException{
		
		String q = null;
		
		if(!table.equals("2015_internal")){
			q = "select * from commuters.\"" + table + "\" where (" + homeString + ") or (" + workString + ");";
		} else {
			q = "select * from commuters.\"" + table + "\" where (" + homeString + ");";
		}
		
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery(q);
		
		while(results.next()){
			
			String fromKey = results.getString("home_id");
			String fromName = results.getString("home_name");
			String toKey = table.equals("2015_internal") ? fromKey : results.getString("work_id");
			String toName = table.equals("2015_internal") ? fromName : results.getString("work_name");
			String amount = results.getString("amount");
			String nMale = table.equals("2015_internal") ? "0" : results.getString("men");
			String nAzubis = table.equals("2015_internal") ? "0" : results.getString("azubis");
			
			int n = 0;
			int nTrainees = 0;
			double pMale = 0.0d;
			
			if(amount != null){
				
				n = Integer.parseInt(amount);
				
				if(nMale != null){
					
					pMale = (double)(Double.parseDouble(nMale)/n);
					
				}
				
			}
			
			if(nAzubis != null){
				
				nTrainees = Integer.parseInt(nAzubis);
				
			}
			
			this.commuterData.add(new CommuterDataElement(fromKey, fromName, toKey, toName, n, pMale, nTrainees));
			
		}
		
	}
	
	private String createChainedStatementFromSet(Set<String> set, String var){
		
		boolean isFirst = true;
		StringBuilder result = new StringBuilder();
		
		for(String s : set){
			
			if(!isFirst){
			
				result.append(" or ");
			
			}
			
			result.append(var + " = '" + s + "'");
			isFirst = false;
			
		}
		
		return result.toString();
		
	}
	
	public Set<CommuterDataElement> getCommuterRelations(){
		return this.commuterData;
	}
	
}
