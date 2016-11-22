package com.innoz.toolbox.config.groups;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.matsim.core.config.ReflectiveConfigGroup.DoNotConvertNull;
import org.matsim.core.config.ReflectiveConfigGroup.StringGetter;
import org.matsim.core.config.ReflectiveConfigGroup.StringSetter;

import com.innoz.toolbox.config.groups.PsqlConfigurationGroup.OutputTablesParameterSet;
import com.innoz.toolbox.config.groups.ScenarioConfigurationGroup.AreaSet;

public abstract class ConfigurationGroup {

	public final String groupName;
	Map<String, String> params = new HashMap<String, String>();
	Map<String, Map<String, ConfigurationGroup>> parameterSets = new HashMap<>();
	private final Map<String,Method> setters;
	private final Map<String, Method> getters;
	
	public ConfigurationGroup(String name){
		
		this.groupName = name;
		this.setters = getSetters();
		this.getters = getGetters();
		
	}
	
	public Map<String, String> getParams(){
			
		Map<String, String> map = new HashMap<>();
		
		for(String f : setters.keySet()){
			
			addParameterToMap(map, f);
			
		}
		
		return map;
			
	}
	
	public Map<String, Map<String, ConfigurationGroup>> getParameterSets(){
		
		return this.parameterSets;
		
	}
	
	public abstract Map<String, String> getComments();
	
	public ConfigurationGroup createParameterSet(String name){
		
		if("areaSet".equals(name)){
			
			return new AreaSet();
			
		} else if("outputTables".equals(name)){
			
			return new OutputTablesParameterSet();
			
		}
		
		return null;
		
	}
	
	public void addParameterSet(ConfigurationGroup parameterSet){
		
		if(parameterSet instanceof AreaSet){
			
			((ScenarioConfigurationGroup)this).addAreaSet((AreaSet)parameterSet);
			
		} else if(parameterSet instanceof OutputTablesParameterSet){
			
			((PsqlConfigurationGroup)this).addOutputTablesParameterSet((OutputTablesParameterSet)parameterSet);
			
		}
		
	}
	
	public void addParam(String name, String value){
		
		this.params.put(name, value);
		final Method setter = setters.get(name);
		
		if(setter != null){
			try {
				invokeSetter(setter, value);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private Map<String, Method> getSetters(){
		
		final Map<String, Method> ss = new HashMap<String, Method>();
		final Class<? extends ConfigurationGroup> c = getClass();
		
		final Method[] allMethods = c.getDeclaredMethods();
		
		for(Method m : allMethods){
			
			final StringSetter annotation = m.getAnnotation(StringSetter.class);
			if(annotation != null){
				ss.put(annotation.value(), m);
			}
			
		}
		
		return ss;
		
	}
	
	private Map<String, Method> getGetters(){
		
		final Map<String, Method> gs = new HashMap<String, Method>();
		
		final Class<? extends ConfigurationGroup> c = getClass();
		
		final Method[] allMethods = c.getDeclaredMethods();
		
		for(Method m : allMethods){
			
			final StringGetter annotation = m.getAnnotation(StringGetter.class);
			
			if(annotation != null){
				
				gs.put(annotation.value(), m);
				
			}
			
		}
		
		return gs;
		
	}
	
	private void invokeSetter(final Method setter, final String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{

		final boolean accessible = setter.isAccessible();
		setter.setAccessible(true);
		
		final Class<?>[] params = setter.getParameterTypes();
		assert params.length == 1;
		
		final Class<?> type = params[0];
		
		if(value.equals("null") && !setter.isAnnotationPresent(DoNotConvertNull.class)){
			
			setter.invoke(this, value);
			
		} else if(type.equals(String.class)){
			
			setter.invoke(this, value);
			
		} else if(type.equals(Float.class) || type.equals(Float.TYPE)){
			
			setter.invoke(this, Float.parseFloat(value));
			
		} else if(type.equals(Double.class) || type.equals(Double.TYPE)){
			
			setter.invoke(this, Double.parseDouble(value));
			
		} else if(type.equals(Long.class) || type.equals(Long.TYPE)){
			
			setter.invoke(this, Long.parseLong(value));
			
		} else if(type.equals(Integer.class) || type.equals(Integer.TYPE)){
			
			setter.invoke(this, Integer.parseInt(value));
			
		} else if(type.equals(Boolean.class) || type.equals(Boolean.TYPE)){
			
			setter.invoke(this, Boolean.parseBoolean(value));
			
		} else if(type.equals(Character.class) || type.equals(Character.TYPE)){
			
			if ( value.length() != 1 ) throw new IllegalArgumentException( value+" is not a single char!" );
			setter.invoke( this , value.toCharArray()[ 0 ] );
			
		} else if ( type.equals( Byte.class ) || type.equals( Byte.TYPE ) ) {
			
			setter.invoke( this , Byte.parseByte( value ) );
			
		} else if ( type.equals( Short.class ) || type.equals( Short.TYPE ) ) {
			
			setter.invoke( this , Short.parseShort( value ) );
			
		} else if ( type.isEnum() ) {
			
			try {
				setter.invoke(
						this,
						Enum.valueOf(
							type.asSubclass( Enum.class ),
							value ) );
			}
			catch (IllegalArgumentException e) {
				// happens when the string does not correspond to any enum values.
				// Surprisingly, the default error message does not print the possible
				// values: do it here, so that the user gets an idea of what went wrong
				final StringBuilder comment =
					new StringBuilder(
							"Error trying to set value "+value+
							" for type "+type.getName()+
							": possible values are " );

				final Object[] consts = type.getEnumConstants();
				for ( int i = 0; i < consts.length; i++ ) {
					comment.append( consts[ i ].toString() );
					if ( i < consts.length - 1 ) comment.append( ", " );
				}

				throw new IllegalArgumentException( comment.toString() , e );
			}
		}
		else {
			throw new RuntimeException( "no method to handle type "+type );
		}

		setter.setAccessible( accessible );
		
	}
	
	private void addParameterToMap(final Map<String, String> map, final String paramName) {
		
		String value = this.getValue(paramName);
		
		if (!((value == null) || value.equalsIgnoreCase("null"))) {
			
			map.put(paramName, value);
			
		} else {
		
			map.put(paramName, "null");
			
		}
		
	}
	
	public String getValue(final String paramName) {
	
		final Method getter = getters.get(paramName);
		
		try {

			final boolean accessible = getter.isAccessible();
			getter.setAccessible( true );
			
			final Object result = getter.invoke( this );
			getter.setAccessible( accessible );
			
			if ( result == null ) {
				
				return null;
				
			}
			
			final String value = ""+result;

			if ( value.equals( "null" ) && !getter.isAnnotationPresent( DoNotConvertNull.class ) ) {
				throw new RuntimeException( "parameter "+ paramName +" understands null pointers for IO. As a consequence, the \"null\" String is not a valid value for "+ getter.getName() );
			}

			return value;
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();

		}
		
		return this.params.get(paramName);
		
	}
	
}