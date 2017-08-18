package com.innoz.toolbox.scenarioGeneration.geoinformation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.innoz.toolbox.config.psql.PsqlAdapter;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.utils.GlobalNames;

public class ZensusGrid {

	private final Logger log = Logger.getLogger(ZensusGrid.class);
	
	private List<ZensusGridNode> nodes;
	private int allInhabitants = 0;
	
	private static final ZensusGrid instance = new ZensusGrid();
	
	private ZensusGrid() {
	
		log.info("Loading population density grid from Zensus 2011 data...");
		
		try {

			CoordinateReferenceSystem from = CRS.decode(GlobalNames.ETRS89LAEA, true);
			CoordinateReferenceSystem to = CRS.decode(GlobalNames.WGS84, true);
			CoordinateTransformation trafo = TransformationFactory.getCoordinateTransformation(from.toString(), to.toString());

			this.nodes = new ArrayList<>();
			
			Connection c = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
			
			Statement st = c.createStatement();
			st.setFetchSize(100000);
			
			String query = "SELECT * from zensus2011.gitter_100m where einwohner > - 1 and st_contains(st_geomfromtext('" +
					Geoinformation.getInstance().getCompleteGeometry().toString() +
					"',4326), st_transform(st_setsrid(st_makepoint(x_mp_100m,y_mp_100m),3035),4326));";
			
			ResultSet result = st.executeQuery(query);
			
			while(result.next()) {
				
				int x = result.getInt("x_mp_100m");
				int y = result.getInt("y_mp_100m");
				
				Coord coord = trafo.transform(new Coord(x, y));
				
				int inhabitants = result.getInt("einwohner");
				
				this.nodes.add(new ZensusGridNode(coord, inhabitants));
				this.allInhabitants += inhabitants;
				
			}
			
			result.close();
			st.close();
			c.close();
		
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException | FactoryException e) {

			e.printStackTrace();
			
		}
		
	}
	
	public static ZensusGrid getInstance() {
		
		return ZensusGrid.instance;
		
	}
	
	public static class ZensusGridNode {
		
		private Coord coord;
		private int nInhabitants;
		
		ZensusGridNode(Coord coord, int inhabitants){
			
			this.coord = coord;
			this.nInhabitants = inhabitants;
			
		}
		
		public Coord getCoord(){
			
			return this.coord;
			
		}
		
		public int getNumberOfInhabitants(){
			
			return this.nInhabitants;
			
		}
		
	}
	
	public int getNumberOfInhabitants() {
		
		return this.allInhabitants;
		
	}
	
	public List<ZensusGridNode> getNodes(){
		
		return this.nodes;
		
	}
	
}