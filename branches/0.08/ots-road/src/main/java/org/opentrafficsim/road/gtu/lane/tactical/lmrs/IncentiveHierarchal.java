package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines desire out of hierarchal courtesy. For right-hand driving this is towards the right if the follower has a  
 * higher desired velocity. If the left follower has a higher desired velocity, a negative desire towards the left 
 * exists. For left-hand driving it is the other way around. Hierarchal desire depends on the level of hierarchal 
 * courtesy. 
 * @author Wouter Schakel
 */
public class IncentiveHierarchal implements VoluntaryIncentive {

	/** {@inheritDoc} */
	@Override
	public Desire determineDesire(final LaneBasedGTU gtu, final LanePerception perception, Desire mandatory) {
		return new Desire(0, 0);
	}

}
