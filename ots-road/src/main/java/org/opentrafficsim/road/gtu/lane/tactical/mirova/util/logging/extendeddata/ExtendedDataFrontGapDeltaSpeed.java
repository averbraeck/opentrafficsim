package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current speed delta to the leading vehicle.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the speed difference [m/s] between the ego vehicle and its immediate leader
 * (calculated as: ego speed - leader speed). This metric is retrieved from the
 * {@link NeighborsContext}.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataFrontGapDeltaSpeed extends ExtendedDataSpeed<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataFrontGapDeltaSpeed INSTANCE = new ExtendedDataFrontGapDeltaSpeed();

    /**
     * Constructs a new extended data type for logging the front gap delta speed.
     */
    public ExtendedDataFrontGapDeltaSpeed()
    {
        super("FrontGapDeltaSpeed", "Current speed delta to leading vehicle [m/s]: ego speed minus leader speed");
    }

    /**
     * Retrieves the front gap delta speed for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the delta speed as a float, or NaN if unavailable
     */
    @Override
    public FloatSpeed getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Speed deltaSpeed = p.getContext(NeighborsContext.class)
                        .getCachedValue(NeighborsContext.FRONT_GAP_DELTA_SPEED_CURRENT, Speed.class);

                if (deltaSpeed == null)
                {
                    return FloatSpeed.instantiateSI(Float.NaN);
                }

                return FloatSpeed.instantiateSI((float) deltaSpeed.si);
            }
        }
        return FloatSpeed.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Current speed delta to leading vehicle [m/s]: ego speed minus leader speed";
    }
}