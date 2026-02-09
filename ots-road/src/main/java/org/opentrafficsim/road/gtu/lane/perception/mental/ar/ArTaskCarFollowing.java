package org.opentrafficsim.road.gtu.lane.perception.mental.ar;

import java.util.NoSuchElementException;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.TaskHeadwayCollector;

/**
 * Task demand for car-following.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ArTaskCarFollowing extends ArTaskHeadwayBased implements Stateless<ArTaskCarFollowing>
{

    /** Singleton instance. */
    public static final ArTaskCarFollowing SINGLETON = new ArTaskCarFollowing();

    /** Constructor. */
    private ArTaskCarFollowing()
    {
        super("car-following");
    }

    @Override
    protected Duration getHeadway(final LanePerception perception, final LaneBasedGtu gtu, final Parameters parameters)
    {
        NeighborsPerception neighbors = perception.getPerceptionCategoryOptional(NeighborsPerception.class)
                .orElseThrow(() -> new NoSuchElementException("NeighborsPerception not available."));
        return neighbors.getLeaders(RelativeLane.CURRENT).collect(new TaskHeadwayCollector(getSpeed()));
    }

    @Override
    public ArTaskCarFollowing get()
    {
        return SINGLETON;
    }

}
