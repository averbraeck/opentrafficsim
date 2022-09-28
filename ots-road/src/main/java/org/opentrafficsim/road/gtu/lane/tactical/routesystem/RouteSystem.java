package org.opentrafficsim.road.gtu.lane.tactical.routesystem;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * A route system supplies information on the number of lane changes and distance within which this has to be performed.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface RouteSystem
{

    /**
     * Returns lane change information from a position over a given length, according to a route and GTU type. The distance
     * concerns the distance within which the lane change has to be performed. Due to lane markings, the actual split may be
     * beyond the distance.
     * @param position DirectedLanePosition; position
     * @param front Length; distance required for the front (relative to reference position)
     * @param route Route; route, may be {@code null}
     * @param gtuType GtuType; GTU type
     * @param distance Length; distance over which required lane changes are desired to be known
     * @return SortedSet&lt;LaneChangeInfo&gt;; lane change information
     */
    SortedSet<LaneChangeInfo> getLaneChangeInfo(DirectedLanePosition position, Length front, Route route, GtuType gtuType,
            Length distance);

    // public void clearCache();
}
