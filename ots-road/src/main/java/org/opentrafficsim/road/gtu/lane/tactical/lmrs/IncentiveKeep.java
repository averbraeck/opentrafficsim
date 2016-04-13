package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.io.Serializable;

import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;

/**
 * Determines lane change desire in order to adhere to keeping right or left. Such desire only exists if the route and speed
 * (considered within an anticpation distance) are not affected on the adjacent lane. The level of lane change desire is only
 * sufficient to overcome the lowest threshold for free lane changes.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveKeep implements VoluntaryIncentive, Serializable
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
        return "IncentiveKeep []";
    }

}
