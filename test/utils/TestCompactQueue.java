package utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestCompactQueue {

	@Test
	public void test() {
		
		CompactQueue cq;
		
		/* A queue with zero items and zero packets should always return zero. */
		cq = new CompactQueue(0, 0);
		for(int i = 0; i < 10; i++){
			assertEquals(0, cq.next());
		}

		/* A queue with five packets and two items should return a value of 
		 * one twice and zeroes afterward */
		cq = new CompactQueue(2, 5);
		for(int i = 0; i < 6; i++){
			if(i < 2) assertEquals(1, cq.next());
			else assertEquals(0, cq.next());
		}
		
		/* A queue with four packets and seven items should return 
		 * a value of 2 three times, then a value of 1, and zero thereafter. */
		cq = new CompactQueue(7, 4);
		for(int i = 0; i < 15; i++){
			if(i < 3) assertEquals(2, cq.next());
			else if(i == 3) assertEquals(1, cq.next());
			else assertEquals(0, cq.next());
		}
		
		/* A queue with four packets and eight items should return
		 * a value of two four times, and zeroes thereafter. */
		cq = new CompactQueue(8, 4);
		for(int i = 0; i < 15; i++){
			if(i < 4) assertEquals(2, cq.next());
			else assertEquals(0, cq.next());
		}
	}

}
