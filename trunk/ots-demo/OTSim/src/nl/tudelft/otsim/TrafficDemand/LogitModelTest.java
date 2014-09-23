package nl.tudelft.otsim.TrafficDemand;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

/**
 * 
 * Test the LogitModel class.
 * 
 * @author Peter Knoppers
 *
 */
public class LogitModelTest {

	/**
	 * Test the creator method.
	 */
	@SuppressWarnings({ "unused", "static-method" })
	@Test
	public void testLogitModel() {
		new LogitModel(-0.2);
		try {
			new LogitModel(0.2);
			fail("Non-negative miu should have thrown an Error");
		} catch (Error e) {
			;
		}
		try {
			new LogitModel(0.0);
			fail("Zero miu should have thrown an Error");
		} catch (Error e) {
			;
		}
	}

	/**
	 * Test the probabilities method.
	 */
	@SuppressWarnings("static-method")
	@Test
	public void testProbabilities() {
		for (int i = 1; i < 50; i++) {
			double miu = - Math.log(1 + i / 100d); 
			LogitModel lm = new LogitModel(miu);
			ArrayList<Double> costs = new ArrayList<Double>();
			costs.add(10d);
			ArrayList<Double> probabilities = lm.probabilities(costs);
			assertEquals("One in -> one out", 1, probabilities.size());
			assertEquals("One in -> probability 100% out", 1.0, probabilities.get(0), 0.0000001);
			costs.add(20d);
			probabilities = lm.probabilities(costs);
			//System.out.format(Locale.US, "miu %.3f: p(10): %.5f, p(20): %.5f\r\n", miu, probabilities.get(0), probabilities.get(1));
			assertEquals("Two in -> two out", 2, probabilities.size());
			assertTrue("Higher cost -> lower probability", probabilities.get(1) < probabilities.get(0));
			double sum = 0;
			for (Double p : probabilities)
				sum += p;
			assertEquals("Total probability is 1.0", 1.0, sum, 0.0000001);
			costs.add(10d);
			costs.add(20d);
			costs.add(10d);
			probabilities = lm.probabilities(costs);
			assertEquals("5 in -> 5 out", 5, probabilities.size());
			sum = 0;
			for (Double p : probabilities)
				sum += p;
			assertEquals("Total probability is 1.0", 1.0, sum, 0.0000001);
			assertEquals("Equal cost should result in equals probability", probabilities.get(0), probabilities.get(2), 0.00000001);
			assertEquals("Equal cost should result in equals probability", probabilities.get(0), probabilities.get(4), 0.00000001);
			assertEquals("Equal cost should result in equals probability", probabilities.get(1), probabilities.get(3), 0.00000001);
		}
	}

}
