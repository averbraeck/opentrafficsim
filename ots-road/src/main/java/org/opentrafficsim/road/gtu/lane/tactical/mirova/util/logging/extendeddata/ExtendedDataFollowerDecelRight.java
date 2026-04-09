package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the predicted deceleration of the right follower.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the anticipated deceleration [m/s²] the follower on the right adjacent lane would
 * experience if the ego vehicle merged into that lane, as evaluated by the
 * {@link NeighborsContext}.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataFollowerDecelRight extends ExtendedDataAcceleration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataFollowerDecelRight INSTANCE = new ExtendedDataFollowerDecelRight();

    /**
     * Constructs a new extended data type for logging follower deceleration (right).
     */
    public ExtendedDataFollowerDecelRight()
    {
        super("FollowerDecelRight", "Resulting deceleration for follower on adjacent lane in case of lane change to the right [m/s²]");
    }

    /**
     * Retrieves the predicted deceleration of the right follower for a specific GTU.
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
                        .getCachedValue(NeighborsContext.FOLLOWER_DECEL_RIGHT, Acceleration.class);

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
        return "Resulting deceleration for follower on adjacent lane in case of lane change to the right [m/s²]";
    }
}