package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the target desired time headway during relaxation.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the target time headway [s] that the ego vehicle is attempting to reach during a
 * headway relaxation process (e.g., after a lane change).
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataRelaxationTargetHeadway extends ExtendedDataDuration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataRelaxationTargetHeadway INSTANCE = new ExtendedDataRelaxationTargetHeadway();

    /**
     * Constructs a new extended data type for logging the relaxation target headway.
     */
    public ExtendedDataRelaxationTargetHeadway()
    {
        super("RelaxationTargetHeadway", "Target desired time headway of the current headway relaxation process [s]");
    }

    /**
     * Retrieves the relaxation target headway for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the target time headway as a float, or NaN if unavailable
     */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Duration timeHeadway = p.getTargetDesiredHeadway();

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
        return "Target desired time headway of the current headway relaxation process [s]";
    }
}