package innoz.io.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class DataRetrieval implements Runnable {

	private final Connection connection;
	private final String query;
	
	private static List<LanduseDataset> data;
	private WKTReader wktReader;
	
	public DataRetrieval(Connection connection, String query, List<LanduseDataset> data){
		
		this.connection = connection;
		this.query = query;
		DataRetrieval.data = data;
		this.wktReader = new WKTReader();
		
	}
	
	@Override
	public void run() {
		
		try {
			
			Statement statement = this.connection.createStatement();
			ResultSet resultSet = statement.executeQuery(this.query);
			
			while(resultSet.next()){
				
				Geometry geometry = this.wktReader.read(resultSet.getString(DatabaseConstants.functions.st_astext
						.name()));
				String landuse = resultSet.getString(DatabaseConstants.ATT_LANDUSE);
				String amenity = resultSet.getString(DatabaseConstants.ATT_AMENITY);
				String leisure = resultSet.getString(DatabaseConstants.ATT_LEISURE);
				String shop = resultSet.getString(DatabaseConstants.ATT_SHOP);
				
				data.add(new LanduseDataset(geometry, landuse, amenity, leisure, shop));
				
			}
			
			// Close everything in the end
			resultSet.close();
			statement.close();
			
		} catch (SQLException | ParseException e) {

			e.printStackTrace();
			
		}
		
	}

}
