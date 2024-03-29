package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.matsim.matrices.Matrix;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.config.psql.ResultSetStream;
import com.innoz.toolbox.run.controller.Controller;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.commuters.CommuterDataElement;
import com.innoz.toolbox.utils.PsqlUtils;

public class CommuterDatabaseParser {

	private static final Logger log = Logger.getLogger(CommuterDatabaseParser.class);
	
	Set<CommuterDataElement> commuterData;
	Matrix od;
	
	public void run(){
		
		try {
		
			log.info("Parsing commuter data");
			
			this.commuterData = new HashSet<CommuterDataElement> ();
			this.od = new Matrix("", "");
			
			Connection connection = PsqlAdapter.createConnection(DatabaseConstants.SURVEYS_DB);
		
			if(connection != null){
				
				// Select the entries that contain the administrative areas we defined in the
				// configuration.
				String surveyAreaId = Controller.configuration().scenario().getSurveyAreaId();
				
				Set<String> vicinityIds = Geoinformation.getInstance().getVicinityIds();

				if(!vicinityIds.isEmpty()){
				
					this.execute(connection, "2015_reverse", "home_id = '" + surveyAreaId + "'", 
							createChainedStatementFromSet(vicinityIds, "work_id"));
					this.execute(connection, "2015_commuters",
							createChainedStatementFromSet(vicinityIds, "home_id"),
							"work_id = '" + surveyAreaId + "'");
					
				}

				this.execute(connection, "2015_internal",
						"home_id = '" + surveyAreaId + "'",
						createChainedStatementFromSet(vicinityIds, "work_id"));
				
				
			}
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
	private void execute(Connection connection, String table, String homeString, String workString) throws SQLException{
		
		String q = null;
		
		if(!table.equals("2015_internal")){
			
			q = "select * from commuters.\"" + table + "\" where (" + homeString + ") and (" + workString + ");";
			
		} else {
			
			q = "select * from commuters.\"" + table + "\" where (" + homeString + ");";
			
		}
		
		PreparedStatement statement = connection.prepareStatement(q);
		
		try(Stream<CommuterDataElement> stream = new ResultSetStream<CommuterDataElement>().getStream(statement,
				(ResultSet results) -> { try {
					
					String fromId = results.getString("home_id");
					String fromName = results.getString("home_name");
					String toId = table.equals("2015_internal") ? fromId : results.getString("work_id");
					String toName = table.equals("2015_internal") ? fromName : results.getString("work_name");
					
					return new CommuterDataElement(fromId, fromName, toId, toName, results.getInt("amount"));
					
				} catch (Exception e){
					
					return null;
					
				}
					
				})){
			
			Iterator<CommuterDataElement> elements = stream.iterator();
			
			CommuterDataElement next = null;
			while((next = elements.next()) != null){
				
				this.commuterData.add(next);
				this.od.createEntry(next.getFromId(), next.getToId(), next.getNumberOfCommuters());
				
			}
			
		}
		
	}
	
	private String createChainedStatementFromSet(Set<String> set, String var){
		
		return new StringBuilder().append(var + " in (").append(PsqlUtils.setToString(set)).append(")").toString();
		
	}
	
	public Set<CommuterDataElement> getCommuterRelations(){
		return this.commuterData;
	}
	
	public Matrix getOD(){
		
		return this.od;
		
	}
	
}