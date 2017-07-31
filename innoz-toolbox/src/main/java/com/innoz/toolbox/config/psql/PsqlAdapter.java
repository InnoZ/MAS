package com.innoz.toolbox.config.psql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.innoz.toolbox.run.controller.Controller;

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
	private static final Logger log = Logger.getLogger(PsqlAdapter.class);
	///////////////////////////////////////////////////////
	
	// No instantiation!
	private PsqlAdapter() {};
	
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
	
	log.info("Connecting to database " + dbName + " as user " + Controller.configuration().psql().getPsqlUser() + "...");
	
	return DriverManager.getConnection(PSQL_URL + 5432 + "/" + dbName,
			Controller.configuration().psql().getPsqlUser(), Controller.configuration().psql().getPsqlPassword());
		
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