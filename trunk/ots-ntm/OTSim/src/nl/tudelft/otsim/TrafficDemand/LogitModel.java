package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;

/**
 * Compute probabilities based on costs using a Logit model
 * 
 * @author Yufei Yuan
 */
public class LogitModel extends CostsToProbabilities {
	final double miu;
	
	LogitModel (double miu) {
		if (miu >= 0)
			throw new Error ("miu must be < 0");
		this.miu = miu;
	}
	
	@Override
	public ArrayList<Double> probabilities(ArrayList<Double> costs) {
		ArrayList<Double> result = new ArrayList<Double> (costs.size());
		double denominator = 0;
		for (int i = 0; i < costs.size(); i++)
			denominator += Math.pow(Math.E,  miu * costs.get(i));
		for (int i = 0; i < costs.size(); i++)
			result.add(Math.pow(Math.E,  miu * costs.get(i)) / denominator);
		return result;
	}

}
