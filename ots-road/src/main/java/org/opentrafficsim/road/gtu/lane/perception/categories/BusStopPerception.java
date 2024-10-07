package org.opentrafficsim.road.gtu.lane.perception.categories;

import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayBusStop;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Bus stop perception category.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface BusStopPerception extends LaneBasedPerceptionCategory
{

    /**
     * Returns the bus stops.
     * @return bus stops
     */
    PerceptionCollectable<HeadwayBusStop, BusStop> getBusStops();

}
