package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire for speed. The anticipation speed in the current and adjacent lanes are compared. The larger
 * the difference, the larger the lane change desire. For negative differences, negative desire results. Anticipation speed
 * involves the the most critical vehicle considered to be in a lane. Vehicles are more critical if their speed is lower, and if
 * they are closer. The set of vehicles considered to be on a lane includes drivers on adjacent lanes of the considered lane,
 * with a lane change desire towards the considered lane above a certain certain threshold. If such vehicles have low speeds
 * (i.e. vehicle accelerating to merge), this may result in a courtesy lane change, or in not changing lane out of courtesy from
 * the 2nd lane of the mainline. Vehicle on the current lane of the driver, are not considered on adjacent lanes. This would
 * maintain a large speed difference between the lanes where all drivers do not change lane as they consider leading vehicles to
 * be on the adjacent lane, lowering the anticipation speed on the adjacent lane. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.<br>
 * <br>
 * <b>Note:</b> This incentive includes speed, and a form of courtesy. It should therefore not be combined with incentives
 * solely for speed, or solely for courtesy.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeedWithCourtesy implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final LaneBasedGTU gtu, final LanePerception perception, Desire mandatory)
    {
        return new Desire(0, 0);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSpeedWithCourtesy []";
    }

}
