package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the predicted ego deceleration for a right lane change.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the anticipated deceleration [m/s²] the ego vehicle would experience if it merged
 * into the right lane, as evaluated by the {@link NeighborsContext}.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataEgoDecelRight extends ExtendedDataAcceleration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataEgoDecelRight INSTANCE = new ExtendedDataEgoDecelRight();

    /**
     * Constructs a new extended data type for logging ego deceleration (right).
     */
    public ExtendedDataEgoDecelRight()
    {
        super("EgoDecelRight", "Resulting deceleration in case of lane change to the right [m/s²]");
    }

    /**
     * Retrieves the predicted ego deceleration (right lane change) for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the predicted deceleration as a float, or NaN if unavailable
     */
    @Override
    public FloatAcceleration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Acceleration decel = p.getContext(NeighborsContext.class)
                        .getCachedValue(NeighborsContext.EGO_DECEL_RIGHT, Acceleration.class);

                if (decel == null)
                {
                    return FloatAcceleration.instantiateSI(Float.NaN);
                }

                return FloatAcceleration.instantiateSI((float) decel.si);
            }
        }
        return FloatAcceleration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Resulting deceleration in case of lane change to the right [m/s²]";
    }
}