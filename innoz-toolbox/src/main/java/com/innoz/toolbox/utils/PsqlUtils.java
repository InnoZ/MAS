package com.innoz.toolbox.utils;

import java.util.Set;

public class PsqlUtils {

	private PsqlUtils(){};
	
	public static enum processes {
		INSERT,
		SELECT
	}
	
	public static String setToString(Set<String> set) {
		
		StringBuilder builder = new StringBuilder();
		
		set.stream().forEach(s -> {
			
			builder.append(",'" + s + "'");
			
		});
		
		return builder.toString().replaceFirst(",", "");
		
	}
	
	public static class PsqlStringBuilder {

		// Defines the process being called (i.e. SELECT, INSERT etc.)
		private String process;
		// Variables (comma-separated string)
		private String variables = "";
		private String schemaname;
		private String tablename;
		private String whereClauses = "";
		private String orderClauses = "";
		
		public PsqlStringBuilder(String process, String schemaname, String tablename) {
			
			this.process = process;
			this.schemaname = schemaname;
			this.tablename = tablename;
			
		}
		
		public PsqlStringBuilder variables(String v) {
			
			this.variables = v;
			return this;
			
		}
		
		public PsqlStringBuilder whereClauses(String conditions) {
			
			this.whereClauses = " " + conditions;
			return this;
			
		}
		
		public PsqlStringBuilder orderClause(String order) {
			
			this.orderClauses = " " + order;
			return this;
			
		}
		
		public String build() {
			
			if(this.variables.isEmpty()) this.variables = "*";
			
			return new StringBuilder(this.process).append(" ").append(this.variables).append(" ").append(" FROM ").append(this.schemaname)
					.append(".").append(this.tablename).append(this.whereClauses).append(this.orderClauses).toString();
			
		}
		
	}
	
}