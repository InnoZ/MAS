package com.innoz.toolbox.config.psql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.innoz.toolbox.config.psql.ResultSetStream;
import com.innoz.toolbox.io.database.DatabaseConstants;
import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;

public class ResultSetStreamTest {

	String[] testData = new String[]{"09180","09190","09172","09160"};
	int timesCalled = -1;
	
	ResultSet set;
	PreparedStatement stmt;
	
	@Test
	public void testResultSet() throws SQLException {

		try(Stream<AdministrativeUnit> stream = new ResultSetStream<AdministrativeUnit>().getStream(stmt,
				(ResultSet rs) -> { try {
					return new AdministrativeUnit(rs.getString(DatabaseConstants.MUN_KEY));
				} catch(Exception e){
					return null;
				}})){
			
			Iterator<AdministrativeUnit> iterator = stream.filter(au -> au != null).limit(4).iterator();
			
			assertTrue(iterator.hasNext());
			assertEquals("09180",iterator.next().getId());
			
			assertTrue(iterator.hasNext());
			assertEquals("09190",iterator.next().getId());
			
			assertTrue(iterator.hasNext());
			assertEquals("09172",iterator.next().getId());
			
			assertTrue(iterator.hasNext());
			assertEquals("09160",iterator.next().getId());
			
			assertTrue(!iterator.hasNext());
			
		}
		
	}
	
	@Before
	public void setup() throws SQLException {
		
		set = mock(ResultSet.class);
		stmt = mock(PreparedStatement.class);
		
		when(set.next()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				if (timesCalled++ >= testData.length)
					return false;
				return true;
			}
			
		});
		
		when(set.getString(DatabaseConstants.MUN_KEY)).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				
				return testData[timesCalled];
				
			}
			
		});
		
		when(stmt.executeQuery()).thenReturn(set);
		
	}
	
}