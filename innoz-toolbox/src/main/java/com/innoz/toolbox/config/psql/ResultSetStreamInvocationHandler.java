package com.innoz.toolbox.config.psql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ResultSetStreamInvocationHandler<T> implements InvocationHandler {

	private Stream<T> stream;
	private PreparedStatement statement;
	private ResultSet resultSet;
	
	public void setup(PreparedStatement st, Function<ResultSet, T> mappingFunction) throws SQLException {
		
		this.statement = st;
		this.resultSet = st.executeQuery();
		this.stream = Stream.generate(new ResultSetSupplier(this.resultSet, mappingFunction));
		
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		if(method == null){
			
			throw new RuntimeException("Cannot invoke null method");
			
		}
		
		if(method.getName().equals("close") && args == null){
			
			if(this.statement != null){
				
				this.statement.close();
				
			}
			
		}
		
		return method.invoke(this.stream, args);
		
	}
	
	private class ResultSetSupplier implements Supplier<T> {

		private final ResultSet resultSet;
		private final Function<ResultSet, T> mappingFunction;
		
		private ResultSetSupplier(ResultSet rs, Function<ResultSet, T> mappingFunction) {
		
			this.resultSet = rs;
			this.mappingFunction = mappingFunction;
			
		}
		
		@Override
		public T get() {
			
			try {
				
				if(this.resultSet.next()){
				
					return this.mappingFunction.apply(this.resultSet);
				
				}
				
			} catch (SQLException e) {

				e.printStackTrace();
				
			}
			
			return null;
		}

	}
	
}