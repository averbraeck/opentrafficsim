package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire in order to adhere to keeping right or left. Such desire only exists if the route and
 * speed (considered within an anticpation distance) are not affected on the adjacent lane. The level of lane change 
 * desire is only sufficient to overcome the lowest threshold for free lane changes.
 * @author Wouter Schakel
 */
public class IncentiveKeep implements VoluntaryIncentive {

	/** {@inheritDoc} */
	@Override
	public Desire determineDesire(final LaneBasedGTU gtu, final LanePerception perception, Desire mandatory) {
		return new Desire(0, 0);
	}

}
