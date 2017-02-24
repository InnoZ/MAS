package com.innoz.toolbox.config.psql;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;

public class ResultSetStream<T> {

	@SuppressWarnings("unchecked")
	public Stream<T> getStream(PreparedStatement st, Function<ResultSet, T> mappingFunction) throws SQLException {
		
		final ResultSetStreamInvocationHandler<T> handler = new ResultSetStreamInvocationHandler<T>();
		handler.setup(st, mappingFunction);
		Stream<T> proxy = (Stream<T>)Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?> [] {Stream.class}, handler);
		return proxy;
		
	}
	
	public static void main(String args[]) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		long t0 = System.currentTimeMillis();
		readStreaming();
		long d1 = System.currentTimeMillis() - t0;

		System.out.println("ResultSetStream -> " + d1 + " ms");
		
		t0 = System.currentTimeMillis();
		read();
		long d2 = System.currentTimeMillis() - t0;
		
		System.out.println("ResultSet       -> " + d2 + " ms");
		
	}
	
	private static void readStreaming() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		Connection c = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
		
		PreparedStatement st = c.prepareStatement("SELECT * FROM gadm.districts where cca_4 is not null limit 10000;");
		
		Map<String, AdministrativeUnit> map = new HashMap<>();
		
		try(Stream<AdministrativeUnit> stream = new ResultSetStream<AdministrativeUnit>().getStream(st,
				(ResultSet rs) -> { try {
					return new AdministrativeUnit(rs.getString(DatabaseConstants.MUN_KEY));
				} catch(Exception e){
					return null;
				}})){
			
			stream.filter(p -> p != null).limit(10000).forEach(p -> map.put(p.getId(), p));

		}
		
	}
	
	private static void read() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		Connection c = PsqlAdapter.createConnection(DatabaseConstants.GEODATA_DB);
		
		PreparedStatement st = c.prepareStatement("SELECT * FROM gadm.districts where cca_4 is not null limit 10000;");
		
		ResultSet result = st.executeQuery();
		
		while(result.next()){
			
			Map<String, AdministrativeUnit> map = new HashMap<>();
			AdministrativeUnit u = new AdministrativeUnit(result.getString(DatabaseConstants.MUN_KEY));
			if(u != null) {
				map.put(u.getId(), u);
			}
			
		}
		
	}
	
}