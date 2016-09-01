package com.innoz.toolbox.io.database.task;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import com.innoz.toolbox.io.SurveyConstants;
import com.innoz.toolbox.io.database.handler.DefaultHandler;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.surveys.SurveyDataContainer;

public abstract class DatabaseTask implements Task {

	//MEMBERS////////////////////////////////////////////////////////////////////////////////
	SurveyConstants constants;
	Set<DefaultHandler> handlers;
	Geoinformation geoinformation;
	Set<String> ids;
	/////////////////////////////////////////////////////////////////////////////////////////
	
	public DatabaseTask(SurveyConstants constants, Geoinformation geoinformation, Set<String> ids){
		
		this.constants = constants;
		this.geoinformation = geoinformation;
		this.ids = ids;
		
	}
	
	abstract void parse(Connection connection, SurveyDataContainer container, String surveyType) throws SQLException;
	
}
