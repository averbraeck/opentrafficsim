package org.opentrafficsim.road.gtu.tactical.util;

import org.opentrafficsim.road.gtu.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;

/**
 * This class overrules the lane change decision in a {@link SimpleOperationalPlan} if the GTU does not allow a lane change.
 * This is can be the case for a short while after vehicle generation to prevent generation artifacts and exceptions.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class LaneChangeNotAllowedUtil
{

    /**
     * Constructor.
     */
    private LaneChangeNotAllowedUtil()
    {
        //
    }

    /**
     * Adjusts the simple operational plan to deal with dead-end situations.
     * @param context tactical information such as parameters and car-following model
     * @param plan simple operational plan
     * @return adjusted simple operational plan to deal with dead-end situations
     */
    public static SimpleOperationalPlan preventLaneChange(final TacticalContextEgo context, final SimpleOperationalPlan plan)
    {
        if (!plan.isLaneChange() || context.getUnsafeGtu().laneChangeAllowed())
        {
            return plan;
        }
        return new SimpleOperationalPlan(plan.getAcceleration(), plan.getDuration());
    }

}
