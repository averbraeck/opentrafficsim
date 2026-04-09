package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging whether the vehicle is currently changing lanes.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * a boolean-like string ("true" or "false") indicating if the ego vehicle is actively
 * performing a lane change maneuver.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataIsChangingLane extends ExtendedDataString<GtuData>
{
    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataIsChangingLane INSTANCE = new ExtendedDataIsChangingLane();

    /**
     * Constructs a new extended data type for logging the lane change status.
     */
    public ExtendedDataIsChangingLane()
    {
        super("IsChangingLane", "true/false indicator for active lane change");
    }

    /**
     * Retrieves the lane change status for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return "true" if the vehicle is currently changing lanes, "false" otherwise
     */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                return Boolean.toString(p.getLaneChange().isChangingLane());
            }
        }
        return "false";
    }

    @Override
    public String toString()
    {
        return "true/false indicator for active lane change";
    }
}