package org.opentrafficsim.animation.data;

import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.road.network.lane.object.BusStop;

/**
 * Animation data of a BusStop.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationBusStopData extends AnimationLaneBasedObjectData<BusStop> implements BusStopData
{
    
    /**
     * Constructor.
     * @param busStop BusStop; bus stop.
     */
    public AnimationBusStopData(final BusStop busStop)
    {
        super(busStop);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Bus stop " + getObject().getFullId();
    }
    
}
