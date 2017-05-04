package com.innoz.toolbox.utils;

import java.util.Set;

public class PsqlUtils {

	private PsqlUtils(){};
	
	public static enum processes {
		SELECT,
		INSERT
	}
	
	public static String setToString(Set<String> set) {
		
		StringBuilder builder = new StringBuilder();
		
		set.stream().forEach(s -> {
			
			builder.append(",'" + s + "'");
			
		});
		
		return builder.toString().replaceFirst(",", "");
		
	}
	
	public static void main(String args[]) {
		
		System.out.println(new PsqlStringBuilder("SELECT", "public", "table").build());
		
	}
	
	public static class PsqlStringBuilder {
		
		public PsqlStringBuilder(String process, String schemaname, String tablename) {
			
			this.process = process;
			this.schemaname = schemaname;
			this.tablename = tablename;
			
		}
		
		// Defines the process being called (i.e. SELECT, INSERT etc.)
		private String process;
		// Variables (comma-separated string)
		private String variables = "";
		// The names of the schema and the table to process
		private String schemaname;
		private String tablename;
		// Conditions
		private String whereClauses = "";
		//
		private String orderClause = "";
		
		public PsqlStringBuilder variables(String v) {
			
			this.variables = v;
			return this;
			
		}
		
		public PsqlStringBuilder whereClauses(String conditions) {
			
			this.whereClauses = " WHERE " + conditions;
			return this;
			
		}
		
		public PsqlStringBuilder orderClause(String order) {
			
			this.orderClause =  " ORDER BY " + order;
			return this;
			
		}
		
		public String build() {
			
			if(this.variables.isEmpty()) this.variables = "*";
			
			return new StringBuilder(this.process).append(" ").append(this.variables).append(" FROM ").append(this.schemaname).append(".")
					.append(this.tablename).append(this.whereClauses).append(this.orderClause).append(";").toString();
			
		}
		
	}
	
}