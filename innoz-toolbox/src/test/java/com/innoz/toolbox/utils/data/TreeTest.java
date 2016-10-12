package com.innoz.toolbox.utils.data;

import org.junit.Test;
import static org.junit.Assert.*;

import com.innoz.toolbox.scenarioGeneration.geoinformation.AdministrativeUnit;

public class TreeTest {
	
	@Test
	public void testTreeGeneration(){
		
		AdministrativeUnit root = new AdministrativeUnit("0");
		Tree<AdministrativeUnit> tree = new Tree<AdministrativeUnit>(root);
		tree.add(new AdministrativeUnit("01"));
		tree.add(new AdministrativeUnit("011"));
		tree.add(new AdministrativeUnit("02"));

		assertEquals(tree.get("0").getChildren().toString(), "[[data: 01], [data: 02]]");
		assertEquals(tree.get("01").getChildren().toString(), "[[data: 011]]");
		assertEquals(tree.get("02").getChildren().toString(), "[]");
		
	}
	
	@Test
	public void testTreeGenerationInsertion(){
		
		AdministrativeUnit root = new AdministrativeUnit("0");
		Tree<AdministrativeUnit> tree = new Tree<AdministrativeUnit>(root);
		tree.add(new AdministrativeUnit("01"));
		tree.add(new AdministrativeUnit("011101"));
		
		assertEquals(tree.get("01").getChildren().toString(), "[[data: 011101]]");
		
		tree.add(new AdministrativeUnit("0111"));
		
		assertEquals(tree.get("01").getChildren().toString(), "[[data: 0111]]");
		assertEquals(tree.get("0111").getChildren().toString(), "[[data: 011101]]");
		
		tree.add(new AdministrativeUnit("0355134"));
		tree.add(new AdministrativeUnit("0355334"));
		
		assertEquals(tree.get("0").getChildren().toString(), "[[data: 01], [data: 0355134], [data: 0355334]]");
		
		tree.add(new AdministrativeUnit("03"));
		
		assertEquals(tree.get("0").getChildren().toString(), "[[data: 01], [data: 03]]");
		assertEquals(tree.get("03").getChildren().toString(), "[[data: 0355334], [data: 0355134]]");
		
		tree.add(new AdministrativeUnit("0355"));
		
		assertEquals(tree.get("03").getChildren().toString(), "[[data: 0355]]");
		assertEquals(tree.get("0355").getChildren().toString(), "[[data: 0355334], [data: 0355134]]");
		
	}

}