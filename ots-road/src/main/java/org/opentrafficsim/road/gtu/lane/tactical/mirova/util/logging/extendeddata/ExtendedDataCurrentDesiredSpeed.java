package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vfloat.scalar.FloatSpeed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataSpeed;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current desired speed.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the desired speed calculated by the {@link EgoContext} in Layer 1. This represents
 * the target speed [m/s] the vehicle aims to achieve based on the speed limit and
 * its personal speed adherence factor.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataCurrentDesiredSpeed extends ExtendedDataSpeed<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataCurrentDesiredSpeed INSTANCE = new ExtendedDataCurrentDesiredSpeed();

    /**
     * Constructs a new extended data type for logging desired speed.
     */
    public ExtendedDataCurrentDesiredSpeed()
    {
        super("CurrentDesiredSpeed", "Current desired speed according to car-following model [m/s]");
    }

    /**
     * Retrieves the current desired speed for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the desired speed as a float, or NaN if unavailable
     */
    @Override
    public FloatSpeed getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Speed desiredSpeed = Speed.NaN;
                try
                {
                    desiredSpeed = p.getContext(EgoContext.class).getCurrentDesiredSpeed();
                }
                catch (ParameterException | GtuException | NetworkException exception)
                {
                    exception.printStackTrace();
                }

                if (desiredSpeed == null || Double.isNaN(desiredSpeed.si))
                {
                    return FloatSpeed.instantiateSI(Float.NaN);
                }

                return FloatSpeed.instantiateSI((float) desiredSpeed.si);
            }
        }
        return FloatSpeed.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Current desired speed according to car-following model [m/s]";
    }
}