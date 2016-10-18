package com.innoz.toolbox.config.groups;

import java.util.HashMap;
import java.util.Map;

public class PsqlConfigurationGroup extends ConfigurationGroup {

	public static final String GROUP_NAME = "psql";
	
	public static final String DB_PASSWORD = "dbPassword";
	public static final String DB_USER = "dbUser";
	public static final String LOCAL_PORT = "port";
	
	private String dbUser = "postgres";
	private String dbPassword = "postgres";
	private int localPort = 3200;
	
	public PsqlConfigurationGroup() {
		
		super(GROUP_NAME);
		this.params.put(DB_USER, this.dbUser);
		this.params.put(LOCAL_PORT, this.localPort);
		
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

}
