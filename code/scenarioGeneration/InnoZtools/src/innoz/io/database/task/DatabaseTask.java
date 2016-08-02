package innoz.io.database.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import innoz.io.SurveyConstants;
import innoz.io.database.handler.DefaultHandler;
import innoz.scenarioGeneration.population.surveys.SurveyDataContainer;

public abstract class DatabaseTask implements Task {

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	SurveyConstants constants;
	Set<DefaultHandler> handlers;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DatabaseTask(SurveyConstants constants){
		
		this.constants = constants;
		
	}
	
	abstract void parse(Connection connection, boolean isUsingHouseholds, boolean onlyWorkingDays,
			SurveyDataContainer container) throws SQLException;
	
}
