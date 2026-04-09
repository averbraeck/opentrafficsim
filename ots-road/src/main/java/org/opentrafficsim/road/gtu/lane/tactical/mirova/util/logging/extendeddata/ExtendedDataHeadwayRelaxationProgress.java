package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataDuration;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the progress of the current headway relaxation.
 * <p>
 * This metric indicates the state of the relaxation process (typically a dimensionless
 * value between 0 and 1). Similar to the lane change desire metric, it is stored
 * using a Duration container to bypass limitations in the trajectory output framework
 * regarding dimensionless units.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataHeadwayRelaxationProgress extends ExtendedDataDuration<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataHeadwayRelaxationProgress INSTANCE = new ExtendedDataHeadwayRelaxationProgress();

    /**
     * Constructs a new extended data type for logging headway relaxation progress.
     */
    public ExtendedDataHeadwayRelaxationProgress()
    {
        super("HeadwayRelaxationProgress", "Progress of the current headway relaxation process [-, stored as Duration]");
    }

    /**
     * Retrieves the headway relaxation progress for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the progress as a float, or NaN if unavailable
     */
    @Override
    public FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                Double progress = p.getRelaxProgress();

                if (progress == null)
                {
                    return FloatDuration.instantiateSI(Float.NaN);
                }

                return FloatDuration.instantiateSI(progress.floatValue());
            }
        }
        return FloatDuration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Progress of the current headway relaxation process [-, stored as Duration]";
    }
}