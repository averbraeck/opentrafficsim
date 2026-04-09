package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the direction of a planned lane change.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the intended direction ("LEFT", "RIGHT", or "NONE") of the ego vehicle's current
 * operational plan.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataLaneChangePlanDirection extends ExtendedDataString<GtuData>
{
    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataLaneChangePlanDirection INSTANCE = new ExtendedDataLaneChangePlanDirection();

    /**
     * Constructs a new extended data type for logging the lane change plan direction.
     */
    public ExtendedDataLaneChangePlanDirection()
    {
        super("PlanDirection", "Direction of lane change plan (LEFT/RIGHT/NONE)");
    }

    /**
     * Retrieves the lane change plan direction for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return "LEFT" or "RIGHT" if a lane change is planned, "NONE" otherwise
     */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getOperationalPlan() != null)
            {
                return p.getOperationalPlan().getLaneChangeDirection() != null
                        ? p.getOperationalPlan().getLaneChangeDirection().toString()
                        : "NONE";
            }
        }
        return "NONE";
    }

    @Override
    public final String toString()
    {
        return "Direction of lane change plan (LEFT/RIGHT/NONE)";
    }
}