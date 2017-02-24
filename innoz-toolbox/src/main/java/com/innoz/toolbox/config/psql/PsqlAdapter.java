package com.innoz.toolbox.config.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.innoz.toolbox.config.Configuration;

public class PsqlAdapter {
	
	static final String PSQL_DRIVER = "org.postgresql.Driver";
	static final String PSQL_URL = "jdbc:postgresql://localhost:";

	public static Connection createConnection(final Configuration configuration, String dbName) throws SQLException,
		InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		Class.forName(PSQL_DRIVER).newInstance();
		return DriverManager.getConnection(PSQL_URL + configuration.psql().getPsqlPort() + "/" + dbName,
				configuration.psql().getPsqlUser(), configuration.psql().getPsqlPassword());
		
	}
	
	public static Connection createConnection(String dbName) throws SQLException,
	InstantiationException, IllegalAccessException, ClassNotFoundException{
		
	Class.forName(PSQL_DRIVER).newInstance();
	return DriverManager.getConnection(PSQL_URL + 5432 + "/" + dbName,
			"postgres", "postgres");
		
	}
		
}