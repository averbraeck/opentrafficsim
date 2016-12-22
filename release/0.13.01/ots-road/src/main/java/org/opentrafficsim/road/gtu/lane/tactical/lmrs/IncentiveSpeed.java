package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Determines lane change desire for speed, where the slowest vehicle in the current and adjacent lanes are assessed. The larger
 * the speed differences between these vehicles, the larger the desire. Negative speed differences result in negative lane
 * change desire. Only vehicles within a limited anticipation range are considered. The considered speed difference with an
 * adjacent lane is reduced as the slowest leader in the adjacent lane is further ahead. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeed implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public final Desire determineDesire(final BehavioralCharacteristics behavioralCharacteristics,
        final LanePerception perception, final CarFollowingModel carFollowingModel, final Desire mandatoryDesire,
        final Desire voluntaryDesire) throws ParameterException
    {
        return new Desire(0, 0);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IncentiveSpeed []";
    }

}
