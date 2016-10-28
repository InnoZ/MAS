package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

public class PsqlConfigurationGroup extends ConfigurationGroup {

	public static final String GROUP_NAME = "psql";
	
	public static final String DB_PASSWORD = "dbPassword";
	public static final String DB_USER = "dbUser";
	public static final String LOCAL_PORT = "port";
	public static final String IS_WRITING_INTO_DATAHUB = "isWritingIntoDataHub";
	
	private String dbUser = "postgres";
	private String dbPassword = "postgres";
	private int localPort = 3200;
	private boolean writeIntoDataHub = false;
	
	public PsqlConfigurationGroup() {
		
		super(GROUP_NAME);
		this.params.put(DB_USER, this.dbUser);
		this.params.put(DB_PASSWORD, this.dbPassword);
		this.params.put(LOCAL_PORT, this.localPort);
		this.params.put(IS_WRITING_INTO_DATAHUB, Boolean.toString(this.writeIntoDataHub));
		
	}
	
	public final void addOutputTablesParameterSet(OutputTablesParameterSet set){
		
		if(this.parameterSets.isEmpty()){
			
			this.parameterSets.put(null, new HashMap<>());
			this.parameterSets.get(null).put(set.groupName, set);
			
		}
		
	}
	
	public ConfigurationGroup getOutputTablesParameterSet(){
		
		return this.parameterSets.get(null).get(OutputTablesParameterSet.SET_TYPE);
		
	}

	@Override
	public Map<String, String> getComments() {
		
		Map<String, String> map = new HashMap<>();
		
		return map;
		
	}
	
	public int getPsqlPort(){
		
		return this.localPort;
		
	}
	
	public void setPsqlPort(int port){
		
		this.localPort = port;
		
	}
	
	public String getPsqlPassword(){
		
		return this.dbPassword;
		
	}
	
	public void setPsqlPassword(String p){
		
		this.dbPassword = p;
		
	}
	
	public String getPsqlUser(){
		
		return this.dbUser;
		
	}
	
	public void setPsqlUser(String user){
		
		this.dbUser = user;
		
	}
	
	public boolean isWritingIntoMobilityDatahub(){
		
		return this.writeIntoDataHub;
		
	}
	
	public void setWriteIntoMobilityDatahub(boolean b){
		
		this.writeIntoDataHub = b;
		
	}
	
	public static class OutputTablesParameterSet extends ConfigurationGroup {

		public static final String SET_TYPE = "outputTables";
		
		public static final String SCENARIO = "scenario";
		public static final String SCHEMA = "schemaName";
		
		private String scenarioName;
		private String schemaName;
		
		public OutputTablesParameterSet() {
		
			super(SET_TYPE);
			this.params.put(SCENARIO, this.scenarioName);
			this.params.put(SCHEMA, this.schemaName);

		}
		
		public String getSchemaName(){
			
			return this.schemaName;
			
		}
		
		public void setSchemaName(String schemaName){
			
			this.schemaName = schemaName;
			
		}
		
		public String getScenarioName(){
			
			return this.scenarioName;
			
		}
		
		public void setScenarioName(String scenarioName){
			
			this.scenarioName = scenarioName;
			
		}

		@Override
		public Map<String, String> getComments() {

			Map<String, String> map = new HashMap<>();
			
			return map;
		
		}
		
	}

}