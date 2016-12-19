package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.utils.geometry.geotools.MGC;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.PsqlAdapter;
import com.innoz.toolbox.config.groups.TracksConfigurationGroup;
import com.innoz.toolbox.scenarioGeneration.population.tracks.Track;
import com.innoz.toolbox.scenarioGeneration.population.tracks.TrackedPerson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class TracksDatabaseReader {

	private final Configuration configuration;
	private Map<String, TrackedPerson> persons;
	
	public TracksDatabaseReader(Configuration configuration) {
		
		this.configuration = configuration;
		this.persons = new HashMap<>();
		
	}
	
	public void parse(){
		
		try {
			
			TracksConfigurationGroup config = this.configuration.tracks();
			
			WKTReader wkt = new WKTReader();
			
			Connection c = PsqlAdapter.createConnection(DatabaseConstants.TRACKS_DB);
			
			String sql = "SELECT user_id,id,started_at,finished_at,length,mode,st_astext(start_point) as start,"
					+ "st_astext(end_point) as end FROM tracks_rsl where started_on='"+ config.getDate() + "';";
			
			Statement s = c.createStatement();
			
			ResultSet result = s.executeQuery(sql);
			
			while(result.next()){
				
				String pid = result.getString("user_id");
				
				if(!this.persons.containsKey(pid)){
					
					this.persons.put(pid, new TrackedPerson(pid));
					
				}
				
				TrackedPerson person = this.persons.get(pid);
				
				String id = Integer.toString(result.getInt("id"));
				String start = result.getString("start");
				String end = result.getString("end");
				if(start == null || end == null){
					continue;
				}
				Geometry startPoint = wkt.read(start);
				Geometry endPoint = wkt.read(end);
				String mode = result.getString("mode").replace("Mode::", "").toLowerCase();
				int length = result.getInt("length");
				String startedAt = result.getString("started_at");
				String finishedAt = result.getString("finished_at");
				
				Track t = new Track(id);
				t.setEnd(MGC.point2Coord((Point) endPoint));
				t.setEndDateAndTime(finishedAt);
				t.setLength(length);
				t.setMode(mode);
				t.setStart(MGC.point2Coord((Point)startPoint));
				t.setStartDateAndTime(startedAt);
				
				person.getTracks().put(startedAt, t);
				
				
			}
			
			result.close();
			s.close();
			c.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | ParseException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	public Map<String, TrackedPerson> getPersons(){
		
		return this.persons;
		
	}

}