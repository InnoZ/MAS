package com.innoz.toolbox.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PsqlAdapter {
	
	static final String PSQL_DRIVER = "org.postgresql.Driver";
	static final String PSQL_URL = "jdbc:postgresql://localhost:";

	public static Connection createConnection(final Configuration configuration, String dbName) throws SQLException,
		InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		Class.forName(PSQL_DRIVER).newInstance();
		return DriverManager.getConnection(PSQL_URL + configuration.getLocalPort() + "/" + dbName,
				configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
	}
		
}