package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Dummy desire disabling lane changes when used as the only incentive.
 * @author Wouter Schakel
 */
public class IncentiveDummy implements MandatoryIncentive {

	/** {@inheritDoc} */
	@Override
	public Desire determineDesire(final LaneBasedGTU gtu, final LanePerception perception) {
		return new Desire(0, 0);
	}

}
