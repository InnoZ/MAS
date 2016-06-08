package innoz.config;

public class ConfigurationParameterSetter {

	public static void set(Configuration configuration, String parameter, Object value){
		
		configuration.setParam(parameter, value);
		
	}
	
}
