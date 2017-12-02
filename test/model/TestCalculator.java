package model;


import static org.junit.Assert.*;

import org.junit.Test;

public class TestCalculator {


	
	@Test
	public void testCalculateAttractiveness(){
		assertEquals(1d, Calculator.calculateAttractiveness(10, 50, 10), 0.001);
		assertEquals(0d, Calculator.calculateAttractiveness(10, 100, 10), 0.001);
		assertEquals(0d, Calculator.calculateAttractiveness(10, 0, 10), 0.001);
		assertEquals(0.5, Calculator.calculateAttractiveness(10, 25, 10), 0.001);
		assertEquals(0.5, Calculator.calculateAttractiveness(10, 75, 10), 0.001);
		
	}

	
	
	
}
