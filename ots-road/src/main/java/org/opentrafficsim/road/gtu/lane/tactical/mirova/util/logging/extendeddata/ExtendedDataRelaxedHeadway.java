package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current relaxed time headway parameter T.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the currently active time headway [s]. In the MiRoVA framework, this parameter is dynamic
 * during headway relaxation processes (e.g., recovering the gap after a lane change).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataRelaxedHeadway extends ExtendedDataDuration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataRelaxedHeadway INSTANCE = new ExtendedDataRelaxedHeadway();

    /**
     * Constructs a new extended data type for logging the relaxed headway.
     */
    public ExtendedDataRelaxedHeadway()
    {
        super("RelaxedHeadway", "Current relaxed headway T [s]");
    }

    /**
     * Retrieves the current relaxed headway for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the relaxed headway as a float, or NaN if unavailable
     */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                var relaxedHeadway = p.getCurrentRelaxedHeadway();
                if (relaxedHeadway != null)
                {
                    return FloatDuration.instantiateSI((float) relaxedHeadway.si);
                }
            }
        }
        return FloatDuration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Current relaxed headway T [s]";
    }
}