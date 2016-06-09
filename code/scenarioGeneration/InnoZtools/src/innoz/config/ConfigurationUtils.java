package innoz.config;

public class ConfigurationUtils {

	public static Configuration loadConfiguration(String file){
		return new Configuration(file);
	}
	
	public static Configuration createConfiguration(){
		return new Configuration();
	}
	
	public static void set(Configuration configuration, String parameter, Object value){
		
		configuration.setParam(parameter, value);
		
	}
	
}
