package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the socio-speed pressure from tailgating.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the social pressure exerted on the ego vehicle by faster-following vehicles (tailgating).
 * As a workaround for output framework limitations, this dimensionless metric is stored
 * using a Duration container.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataSocioSpeedPressure extends ExtendedDataDesire<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataSocioSpeedPressure INSTANCE = new ExtendedDataSocioSpeedPressure();

    /**
     * Constructs a new extended data type for logging socio-speed pressure.
     */
    public ExtendedDataSocioSpeedPressure()
    {
        super("SocioSpeedPressure", "Socio-speed pressure from tailgating [-, stored as Duration]");
    }

    /**
     * Retrieves the socio-speed pressure for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the socio-speed pressure as a float, or NaN if unavailable
     */
    @Override
    public final FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Double pressure = p.getSocioSpeedPressure();

                if (pressure == null)
                {
                    return FloatDuration.instantiateSI(Float.NaN);
                }

                return FloatDuration.instantiateSI(pressure.floatValue());
            }
        }
        return FloatDuration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Socio-speed pressure from tailgating [-, stored as Duration]";
    }
}