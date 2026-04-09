package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging whether a lane change is currently planned.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * a boolean-like string ("true" or "false") indicating if the ego vehicle's current
 * operational plan involves a lane change maneuver.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataLaneChangePlan extends ExtendedDataString<GtuData>
{
    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataLaneChangePlan INSTANCE = new ExtendedDataLaneChangePlan();

    /**
     * Constructs a new extended data type for logging the lane change plan status.
     */
    public ExtendedDataLaneChangePlan()
    {
        super("PlanIsLaneChange", "Whether current operational plan is a lane change");
    }

    /**
     * Retrieves the lane change plan status for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return "true" if a lane change is planned, "false" otherwise
     */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getOperationalPlan() != null)
            {
                return "true";
            }
        }
        return "false";
    }

    @Override
    public final String toString()
    {
        return "Whether current operational plan is a lane change";
    }
}