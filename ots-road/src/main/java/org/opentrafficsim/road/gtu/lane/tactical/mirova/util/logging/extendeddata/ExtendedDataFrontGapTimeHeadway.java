package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current front gap time headway.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the current time headway [s] between the ego vehicle and its immediate leader on the
 * same lane. This metric is retrieved from the {@link NeighborsContext}.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataFrontGapTimeHeadway extends ExtendedDataDuration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataFrontGapTimeHeadway INSTANCE = new ExtendedDataFrontGapTimeHeadway();

    /**
     * Constructs a new extended data type for logging the front gap time headway.
     */
    public ExtendedDataFrontGapTimeHeadway()
    {
        super("FrontGapTimeHeadway", "Current time headway to leading vehicle [s]");
    }

    /**
     * Retrieves the current front gap time headway for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the time headway as a float, or NaN if unavailable
     */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Duration timeHeadway = p.getContext(NeighborsContext.class)
                        .getCachedValue(NeighborsContext.FRONT_GAP_TIME_HEADWAY_CURRENT, Duration.class);

                if (timeHeadway == null)
                {
                    return FloatDuration.instantiateSI(Float.NaN);
                }

                return FloatDuration.instantiateSI((float) timeHeadway.si);
            }
        }
        return FloatDuration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Current time headway to leading vehicle [s]";
    }
}