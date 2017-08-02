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

/**
 * 
 * Tests the functionality of the {@code ResultSetStream} element, a hybrid of the classic jdbc ResultSet and the Java 8
 * streaming interface.
 * 
 * @author dhosse
 *
 */
public class ResultSetStreamTest {

	// mock data
	String[] testData = new String[]{"09180","09190","09172","09160"};
	int timesCalled = -1;
	
	ResultSet set;
	PreparedStatement stmt;
	
	@Test
	public void testResultSet() throws SQLException {

		// Create a new streaming result set of administrative units
		try(Stream<AdministrativeUnit> stream = new ResultSetStream<AdministrativeUnit>().getStream(stmt,
				(ResultSet rs) -> { try {
					
					// If possible, return a new administrative unit
					return new AdministrativeUnit(rs.getString(DatabaseConstants.MUN_KEY));
					
				} catch(Exception e){
					
					return null;
					
				}})) {

			// The actual test: check if all the mocked data could be received
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
		
		// Create new mocked ResultSet and Statement
		this.set = mock(ResultSet.class);
		this.stmt = mock(PreparedStatement.class);
		
		
		// Define the response of the mocked objects on specific requests (queries)
		// Return true if the index does not exceed to number of testData objects
		when(this.set.next()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				if (timesCalled++ >= testData.length)
					return false;
				return true;
			}
			
		});
		
		// Return the next element of testData if the next element is queried
		when(set.getString(DatabaseConstants.MUN_KEY)).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				
				return testData[timesCalled];
				
			}
			
		});
		
		// Return the mocked ResultSet if the mocked Statement is asked to executeQuery()
		when(stmt.executeQuery()).thenReturn(set);
		
	}
	
}