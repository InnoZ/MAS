package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

public class PsqlConfigurationGroup extends ConfigurationGroup {

	public static final String GROUP_NAME = "psql";
	
	public static final String DB_PASSWORD = "dbPassword";
	public static final String DB_USER = "dbUser";
	public static final String LOCAL_PORT = "port";
	public static final String IS_WRITING_INTO_DATAHUB = "isWritingIntoDataHub";
	
	private String dbUser = "postgres";
	private String dbPassword = "postgres";
	private int localPort = 5432;
	private boolean writeIntoDataHub = false;
	
	public PsqlConfigurationGroup() {
		
		super(GROUP_NAME);
		
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

	@StringGetter(LOCAL_PORT)
	public int getPsqlPort(){
		
		return this.localPort;
		
	}
	
	@StringSetter(LOCAL_PORT)
	public void setPsqlPort(int port){
		
		this.localPort = port;
		
	}
	
	@StringGetter(DB_PASSWORD)
	public String getPsqlPassword(){
		
		return this.dbPassword;
		
	}
	
	@StringSetter(DB_PASSWORD)
	public void setPsqlPassword(String p){
		
		this.dbPassword = p;
		
	}
	
	@StringGetter(DB_USER)
	public String getPsqlUser(){
		
		return this.dbUser;
		
	}
	
	@StringSetter(DB_USER)
	public void setPsqlUser(String user){
		
		this.dbUser = user;
		
	}
	
	@StringGetter(IS_WRITING_INTO_DATAHUB)
	public boolean isWritingIntoMobilityDatahub(){
		
		return this.writeIntoDataHub;
		
	}
	
	@StringSetter(IS_WRITING_INTO_DATAHUB)
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

		}
		
		@StringGetter(SCHEMA)
		public String getSchemaName(){
			
			return this.schemaName;
			
		}
		
		@StringSetter(SCHEMA)
		public void setSchemaName(String schemaName){
			
			this.schemaName = schemaName;
			
		}
		
		@StringGetter(SCENARIO)
		public String getScenarioName(){
			
			return this.scenarioName;
			
		}
		
		@StringSetter(SCENARIO)
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