package com.innoz.toolbox.utils;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class PsqlUtilsTest {

	Set<String> set;
	
	@Before
	public void setup() {
		
		set = new HashSet<String>(Arrays.asList("la","le","lu"));
		
	}
	
	@Test
	public void testSetToStringConversion() {
		
		String s = PsqlUtils.setToString(set);
		
		assertEquals("String conversion failed!", "'la','le','lu'", s);
		
	}
	
}