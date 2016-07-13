package innoz.run.parallelization;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import innoz.io.database.DatabaseConstants;
import innoz.io.database.DatabaseReader;
import innoz.io.database.datasets.OsmPolygonDataset;

public class DataRetrievalThread extends AlgoThread {

	private ResultSet resultSet;
	private DatabaseReader reader;
	
	@Override
	public void run() {
		
		try {
			
			if(resultSet.next()){
				
				Geometry geometry = new WKTReader().read(resultSet.getString(DatabaseConstants.functions.st_astext
						.name()));
				String landuse = resultSet.getString(DatabaseConstants.ATT_LANDUSE);
				String amenity = resultSet.getString(DatabaseConstants.ATT_AMENITY);
				String leisure = resultSet.getString(DatabaseConstants.ATT_LEISURE);
				String shop = resultSet.getString(DatabaseConstants.ATT_SHOP);
				String building = resultSet.getString(DatabaseConstants.ATT_BUILDING);
				
				String type = null;
				
				if(building != null){
				
					type = "buildings";
					
				} else {
					
					type = "landuse";
					
				}
				
				this.reader.getPolygonData().get(type).add(new OsmPolygonDataset(geometry, landuse, amenity, shop, leisure, building));
				
			}
			
		} catch (SQLException | ParseException e) {

			e.printStackTrace();
			
		}

	}

	@Override
	public void init(Object... args) {
		
		this.reader = (DatabaseReader)args[0];
		this.resultSet = (ResultSet)args[1];

	}
	
	@Override
	void addToThread(Object obj) {
		
	}

}
