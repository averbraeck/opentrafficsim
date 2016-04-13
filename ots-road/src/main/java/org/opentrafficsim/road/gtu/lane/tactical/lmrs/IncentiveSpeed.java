package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.io.Serializable;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire for speed, where the slowest vehicle in the current and adjacent lanes are assessed. The larger
 * the speed differences between these vehicles, the larger the desire. Negative speed differences result in negative lane
 * change desire. Only vehicles within a limited anticipation range are considered. The considered speed difference with an
 * adjacent lane is reduced as the slowest leader in the adjacent lane is further ahead. The desire for speed is reduced as
 * acceleration is larger, preventing over-assertive lane changes as acceleration out of congestion in the adjacent lane has
 * progressed more.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveSpeed implements VoluntaryIncentive, Serializable
{

    /** */
    private static final long serialVersionUID = 20160413L;

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
        return "IncentiveSpeed []";
    }

}
