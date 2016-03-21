package innoz.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DatabaseHandler {

	static final String url = "url";
	static final String username = "username";
	static final String password = "password";
	static final String query = "query";
	
	abstract void handleResult(ResultSet result) throws SQLException;
	
	protected String readSqlQuery(String filename) throws IOException{
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
		
		StringBuilder builder = new StringBuilder();
		String line = null;
		
		while((line = reader.readLine()) != null){
			
			builder.append(line);
			
		}
		
		reader.close();
		
		return builder.toString();
		
	}
	
}
