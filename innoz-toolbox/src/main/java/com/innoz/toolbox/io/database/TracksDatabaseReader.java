package com.innoz.toolbox.io.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.misc.Time;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.innoz.toolbox.config.Configuration;
import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.config.groups.TracksConfigurationGroup;
import com.innoz.toolbox.scenarioGeneration.geoinformation.Geoinformation;
import com.innoz.toolbox.scenarioGeneration.population.tracks.Track;
import com.innoz.toolbox.scenarioGeneration.population.tracks.TrackedPerson;
import com.innoz.toolbox.utils.GlobalNames;
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
	
	public void parse() {
		
		try {
			
			TracksConfigurationGroup config = this.configuration.tracks();
			
			WKTReader wkt = new WKTReader();
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = sdf.parse(config.getDate());
			
			Connection c = PsqlAdapter.createConnection(this.configuration, DatabaseConstants.TRACKS_DB);
			
			String sql = "SELECT user_id,id,started_at,finished_at,length,mode,st_astext(start_point) as start,"
					+ "st_astext(end_point) as end FROM tracks_natur where started_on='"+ config.getDate() + "';";
			
			Statement s = c.createStatement();
			
			ResultSet result = s.executeQuery(sql);
			
			CoordinateReferenceSystem crsFrom = CRS.decode(GlobalNames.ETRS89LAEA, true);
			CoordinateReferenceSystem crsTo = CRS.decode(GlobalNames.WGS84, true);
			CoordinateTransformation transform = TransformationFactory.getCoordinateTransformation(crsFrom.toString(), crsTo.toString());
			
			while(result.next()){
				
				String pid = result.getString("user_id");
				
				if(!this.persons.containsKey(pid)){
					
					this.persons.put(pid, new TrackedPerson(pid));
					
				}
				
				TrackedPerson person = this.persons.get(pid);
				
				String id = Integer.toString(result.getInt("id"));
				String start = result.getString("start");
				String end = result.getString("end");
				
				if(start != null && end != null){
					
					Geometry startPoint = MGC.coord2Point(transform.transform(MGC.point2Coord((Point) wkt.read(start))));
					Geometry endPoint = MGC.coord2Point(transform.transform(MGC.point2Coord((Point) wkt.read(end))));
					
					if(Geoinformation.getInstance().getCompleteGeometry().contains(startPoint) &&
							Geoinformation.getInstance().getCompleteGeometry().contains(endPoint)){

						String mode = result.getString("mode").replace("Mode::", "").toLowerCase();
						int length = result.getInt("length");
						String startedAt = result.getString("started_at");
						String finishedAt = result.getString("finished_at");

						Date startDate = sdf.parse(startedAt.substring(0, 11));
						Date endDate = sdf.parse(finishedAt.substring(0, 11));
						double startTime = Time.parseTime(startedAt.substring(11,19)) +
								(startDate.getTime() - currentDate.getTime()) / 1000;
						double endTime = Time.parseTime(finishedAt.substring(11,19)) +
								(endDate.getTime() - currentDate.getTime()) / 1000;
						
						Track t = new Track(id);
						t.setEnd(MGC.point2Coord((Point) endPoint));
						t.setLength(length);
						t.setMode(mode);
						t.setStart(MGC.point2Coord((Point)startPoint));
						t.setStartTime(startTime);
						t.setEndTime(endTime);
						
						person.getTracks().put(startedAt, t);
						
					}
					
				}
				
			}
			
			result.close();
			s.close();
			c.close();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | ParseException | java.text.ParseException e) {
			
			e.printStackTrace();
			
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public Map<String, TrackedPerson> getPersons(){
		
		return this.persons;
		
	}

}