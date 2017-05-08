package com.innoz.toolbox.io.pgsql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.matsim.api.core.v01.events.Event;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.core.events.algorithms.EventWriter;
import org.matsim.core.events.handler.BasicEventHandler;

import com.innoz.toolbox.config.psql.PsqlAdapter;

public class EventsWriterPsql implements EventWriter, BasicEventHandler, ShutdownListener {

	PreparedStatement statement;
	
	public EventsWriterPsql(String schemaname, String tablename) {
		
		try {
			
			Connection connection = PsqlAdapter.createConnection("simulated_mobility");
			
			Statement stmt = connection.createStatement();
			stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schemaname + ";");
			stmt.executeUpdate("DROP TABLE IF EXISTS " + schemaname + "." + tablename + ";");
			stmt.executeUpdate("CREATE TABLE " + schemaname + "." + tablename + "(time double precision, type varchar);");
			
			this.statement = connection.prepareStatement("INSERT INTO " + schemaname + "." + tablename +
					"(time, type) VALUES (?, ?);");

		} catch (InstantiationException | IllegalAccessException
		        | ClassNotFoundException | SQLException e) {
		
			e.printStackTrace();
			
		}
		
	}
	
	@Override
	public void reset(int iteration) {
		
		try {
		
			this.statement.clearBatch();
		
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override
	public void handleEvent(Event event) {

		try {
		
			Map<String, String> attributes = event.getAttributes();
			
			Double time = Double.parseDouble(attributes.get("time"));
			String type = attributes.get("type");
		
			this.statement.setDouble(1, time);
			this.statement.setString(2, type);
			this.statement.addBatch();
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}
		
	}

	@Override
	public void closeFile() {
		
		if(this.statement != null) {

			try {
		
				this.statement.executeBatch();
				this.statement.close();
				
			} catch (SQLException e) {

				e.printStackTrace();
				
			}
			
		}
		
	}

	@Override
	public void notifyShutdown(ShutdownEvent event) {
		
		this.closeFile();
		
	}	
	
}