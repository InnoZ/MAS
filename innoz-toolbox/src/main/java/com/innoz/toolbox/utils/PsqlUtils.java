package com.innoz.toolbox.utils;

import java.util.HashSet;
import java.util.Set;

import org.matsim.core.utils.collections.CollectionUtils;

public class PsqlUtils {

	private PsqlUtils(){};
	
	public static String setToString(Set<String> set) {
		
		StringBuilder builder = new StringBuilder();
		
		set.stream().forEach(s -> {
			
			builder.append(",'" + s + "'");
			
		});
		
		return builder.toString().replaceFirst(",", "");
		
	}
	
	public static String createSelectStatement(Set<String> variables, String tableName) {
		
		return createSelectStatement(CollectionUtils.setToString(variables), tableName);
		
	}
	
	/**
	 * Builds a String object representing a PotsgreSQL SELECT statement.
	 * 
	 * @param variables Comma-separated String of the variables to select from the table. 
	 * @param tableName
	 * @return
	 */
	public static String createSelectStatement(String variables, String tableName) {
		
		return new StringBuilder("SELECT ")
				.append(variables).append(" FROM ").append(tableName).append(";").toString();
		
	}
	
	public static void main(String args[]) {
		
		Set<String> variables = new HashSet<>();
		variables.add("one");
		variables.add("two");
		
		System.out.println(createSelectStatement(variables, "test"));
		
		Set<String> set = new HashSet<>();
		set.add("bla");
		set.add("blu");
		set.add("bli");
		
		System.out.println(setToString(set));
		
	}
	
}