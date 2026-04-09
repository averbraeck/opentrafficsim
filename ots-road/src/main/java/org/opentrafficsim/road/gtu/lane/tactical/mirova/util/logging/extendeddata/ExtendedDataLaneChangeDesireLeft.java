package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.djunits.value.vfloat.scalar.FloatDuration;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the lane change desire to the left.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the tactical desire to change to the left lane, as computed by <b>Layer 2 (Motivation)</b>.
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
public class ExtendedDataLaneChangeDesireLeft extends ExtendedDataDesire<GtuData>
{

    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataLaneChangeDesireLeft INSTANCE = new ExtendedDataLaneChangeDesireLeft();

    /**
     * Constructs a new extended data type for logging left lane change desire.
     */
    public ExtendedDataLaneChangeDesireLeft()
    {
        super("LaneChangeDesireLeft", "Desire for lane change to the left [-, stored as Duration]");
    }

    /**
     * Retrieves the left lane change desire for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the desire value as a float, or NaN if unavailable
     */
    @Override
    public final FloatDuration getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p)
            {
                return FloatDuration.instantiateSI((float) p.getLaneChangeDesire().getLeft());
            }
        }
        return FloatDuration.instantiateSI(Float.NaN);
    }

    @Override
    public final String toString()
    {
        return "Desire for lane change to the left [-, stored as Duration]";
    }
}