package com.innoz.toolbox.utils;

import java.util.HashSet;
import java.util.Set;

public class PsqlUtils {

	public static String setToString(Set<String> set) {
		
		StringBuilder builder = new StringBuilder();
		
		set.stream().forEach(s -> {
			
			builder.append(",'" + s + "'");
			
		});
		
		return builder.toString().replaceFirst(",", "");
		
	}
	
	public static void main(String args[]) {
		
		Set<String> set = new HashSet<>();
		set.add("bla");
		set.add("blu");
		set.add("bli");
		
		System.out.println(setToString(set));
		
	}
	
}