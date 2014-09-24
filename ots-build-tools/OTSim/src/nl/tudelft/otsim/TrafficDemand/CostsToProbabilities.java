package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;

/**
 * Assign probabilities given costs for the alternative choices.
 * 
 * @author Peter Knoppers
 */
public abstract class CostsToProbabilities {
	/**
	 * Given a set of costs, compute probabilities of selecting each option.
	 * <br />The returned probabilities shall add up to 1.0 (with high precision).
	 * <br />The set of costs may not be empty.
	 * <br />The returned probabilities correspond one to one with the set of costs 
	 * @param costs ArrayList&lt;Double&gt;; the set of costs
	 * @return ArrayList&lt;Double&gt;; the set of probabilities
	 */
	public abstract ArrayList<Double> probabilities (ArrayList<Double> costs);
}
