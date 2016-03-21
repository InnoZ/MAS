package innoz.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class TracksDatabaseHandler extends DatabaseHandler {

	private TracksDatabaseHandler(){};
	
	public static void main(String args[]){

		if(args.length <= 0){
			
			throw new RuntimeException("No properties file given! Aborting...");
			
		}
		
		DatabaseHandler dbAccessor = new TracksDatabaseHandler();
		
		try {

			Class.forName("org.postgresql.Driver");
			Properties properties = new Properties();
			properties.load(new FileInputStream(new File(args[0])));
			Connection connection = DriverManager.getConnection(properties.getProperty(DatabaseHandler.url),
					properties.getProperty(DatabaseHandler.username),
					properties.getProperty(DatabaseHandler.password));
			
			if(connection != null){
				
				System.out.println("Database connected");
	
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery(properties.getProperty(DatabaseHandler.query));
	
				while(result.next()){
					
					dbAccessor.handleResult(result);
	
				}
				
				result.close();
				statement.close();
				connection.close();
				
			}
			
		} catch (SQLException | ClassNotFoundException | IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void handleResult(ResultSet result) throws SQLException {
		
		String key = result.getString("id");
		String g = result.getString("st_astext");
		System.out.println(key+ "\t" + g);
		
	}
	
}
