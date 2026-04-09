package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vfloat.scalar.FloatAcceleration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current baseline car-following acceleration.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the raw car-following acceleration calculated by the {@link EgoContext} in Layer 1.
 * This represents the acceleration [m/s²] the vehicle *would* execute if no other tactical
 * or cooperative maneuvers intervened.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataCurrentCFAcceleration extends ExtendedDataAcceleration<GtuData>
{
    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataCurrentCFAcceleration INSTANCE = new ExtendedDataCurrentCFAcceleration();

    /**
     * Constructs a new extended data type for logging car-following acceleration.
     */
    public ExtendedDataCurrentCFAcceleration()
    {
        super("CurrentCFAcceleration", "Acceleration according to current car-following model [m/s²]");
    }

    /**
     * Retrieves the current car-following acceleration for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the base car-following acceleration as a float, or NaN if unavailable
     */
    @Override
    public FloatAcceleration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Acceleration acc = p.getContext(EgoContext.class)
                        .getCachedValue(EgoContext.CURRENT_CF_ACCELERATION, Acceleration.class);

                if (acc == null)
                {
                    return FloatAcceleration.instantiateSI(Float.NaN);
                }

                // Casting double to float automatically handles Double.NaN and Double.NEGATIVE_INFINITY
                return FloatAcceleration.instantiateSI((float) acc.si);
            }
        }
        return FloatAcceleration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Acceleration according to current car-following model [m/s²]";
    }
}