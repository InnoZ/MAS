package com.innoz.toolbox.config.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.innoz.toolbox.config.Configuration;

/**
 * 
 * Class that provides some static convenience functions for the creation of postgreSQL database connections.
 * 
 * @author dhosse
 *
 */
public class PsqlAdapter {
	
	// FIELDS /////////////////////////////////////////////
	static final String PSQL_DRIVER = "org.postgresql.Driver";
	static final String PSQL_URL = "jdbc:postgresql://localhost:";
	///////////////////////////////////////////////////////
	
	// No instantiation!
	private PsqlAdapter(){};

	/**
	 * 
	 * Creates a connection object to interact with a postgreSQL database. Information about the database user's name and password
	 * have to be provided as parameters (given in configuration).
	 * 
	 * @param configuration The scenario generation configuration containing such information as database user and password.
	 * @param dbName The name of the database you want to connect to. Must not be null!
	 * @return A {@link java.sql.Connection} object.
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Connection createConnection(final Configuration configuration, String dbName) throws SQLException,
		InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		Class.forName(PSQL_DRIVER).newInstance();
		return DriverManager.getConnection(PSQL_URL + configuration.psql().getPsqlPort() + "/" + dbName,
				configuration.psql().getPsqlUser(), configuration.psql().getPsqlPassword());
		
	}
	
	/**
	 * 
	 * Creates a connection object to interact with a postgreSQL database. The user is set to 'potgres' (super user), so this method should
	 * eventually not be public!
	 * 
	 * @param dbName The name of the database you want to connect to. Must not be null!
	 * @return A {@link java.sql.Connection} object.
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Connection createConnection(String dbName) throws SQLException,
	InstantiationException, IllegalAccessException, ClassNotFoundException {
		
	Class.forName(PSQL_DRIVER).newInstance();
	return DriverManager.getConnection(PSQL_URL + 5432 + "/" + dbName,
			"postgres", "postgres");
		
	}
	
	/**
	 * 
	 * Creates a connection object to interact with a postgreSQL database. This method must only be used inside a Ruby on Rails app
	 * since the user is set to 'apprunner'.
	 * 
	 * @param dbName The name of the database you want to connect to. Must not be null!
	 * @return A {@link java.sql.Connection} object with full functionality to insert into the Ruby on Rails database.
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public static Connection createConnectionROR(String dbName) throws SQLException,
	InstantiationException, IllegalAccessException, ClassNotFoundException {
	
		Class.forName(PSQL_DRIVER).newInstance();
		return DriverManager.getConnection(PSQL_URL + 5432 + "/" + dbName,
				"apprunner", "");
		
	}
		
}