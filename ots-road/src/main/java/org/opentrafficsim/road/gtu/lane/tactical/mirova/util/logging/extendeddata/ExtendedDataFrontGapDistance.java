package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vfloat.scalar.FloatLength;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataLength;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current front gap distance to the leading vehicle.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the physical distance [m] between the ego vehicle and its immediate leader on the same lane.
 * This metric is retrieved from the {@link NeighborsContext}.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataFrontGapDistance extends ExtendedDataLength<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataFrontGapDistance INSTANCE = new ExtendedDataFrontGapDistance();

    /**
     * Constructs a new extended data type for logging the front gap distance.
     */
    public ExtendedDataFrontGapDistance()
    {
        super("FrontGapDistance", "Current front gap distance headway to leading vehicle [m]");
    }

    /**
     * Retrieves the front gap distance for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the front gap distance as a float, or NaN if unavailable
     */
    @Override
    public FloatLength getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Length distanceHeadway = p.getContext(NeighborsContext.class)
                        .getCachedValue(NeighborsContext.FRONT_GAP_DISTANCE_CURRENT, Length.class);

                if (distanceHeadway == null)
                {
                    return FloatLength.instantiateSI(Float.NaN);
                }

                return FloatLength.instantiateSI((float) distanceHeadway.si);
            }
        }
        return FloatLength.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Current front gap distance headway to leading vehicle [m]";
    }
}