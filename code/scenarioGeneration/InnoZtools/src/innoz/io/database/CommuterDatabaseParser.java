package innoz.io.database;

import innoz.config.Configuration;
import innoz.scenarioGeneration.population.commuters.CommuterDataElement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

public class CommuterDatabaseParser {

	private static final Logger log = Logger.getLogger(CommuterDatabaseParser.class);
	
	public void run(Configuration configuration){
		
		try {
		
			log.info("Parsing commuter data");
			
			Map<Tuple<String, String>, CommuterDataElement> commuterData = new HashMap<Tuple<String,String>, CommuterDataElement>();
			
			Class.forName("org.postgresql.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + configuration.getLocalPort() +
					"/surveyed_mobility", configuration.getDatabaseUsername(), configuration.getDatabasePassword());
		
			if(connection != null){
				
				// Select the entries that contain the administrative areas we defined in the
				// configuration.
				String q = "select * from commuters where from_key = '' or to_key = '';";
				
				Statement statement = connection.createStatement();
				ResultSet results = statement.executeQuery(q);
				
				while(results.next()){
					
					String fromKey = results.getString("from_key");
					String fromName = results.getString("from_name");
					String toKey = results.getString("to_key");
					String toName = results.getString("to_name");
					String amount = results.getString("amount");
					String nMale = results.getString("n_male");
					String nAzubis = results.getString("n_azubis");
					
					int n = 0;
					int nTrainees = 0;
					double pMale = 0.0d;
					
					if(amount != null){
						
						n = Integer.parseInt(amount);
						
						if(nMale != null){
							
							pMale = (double)(Double.parseDouble(nMale)/n);
							
						}
						
					}
					
					if(nAzubis != null){
						
						nTrainees = Integer.parseInt(nAzubis);
						
					}
					
					commuterData.put(new Tuple<String, String>(fromKey, toKey), new CommuterDataElement(fromKey,
							fromName, toKey, toName, n, pMale, nTrainees));
					
				}
				
			}
			
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {

			e.printStackTrace();
			
		}
		
	}
	
}
