package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire for speed, where the slowest vehicle in the current and adjacent lanes are assessed.
 * The larger the speed differences between these vehicles, the larger the desire. Negative speed differences result in
 * negative lane change desire. Only vehicles within a limited anticipation range are considered. The considered speed
 * difference with an adjacent lane is reduced as the slowest leader in the adjacent lane is further ahead. The desire 
 * for speed is reduced as acceleration is larger, preventing over-assertive lane changes as acceleration out of 
 * congestion in the adjacent lane has progressed more.
 * @author Wouter Schakel
 */
public class IncentiveSpeed implements VoluntaryIncentive {

	/** {@inheritDoc} */
	@Override
	public Desire determineDesire(final LaneBasedGTU gtu, final LanePerception perception, Desire mandatory) {
		return new Desire(0, 0);
	}

}
