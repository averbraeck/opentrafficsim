package org.opentrafficsim.road.gtu.lane.tactical.mirova.util.logging.extendeddata;

import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataString;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Extended data type for logging the current active ActionState of the MiRoVA tactical planner.
 * <p>
 * This utility class integrates with the OpenTrafficSim KPI sampling framework to record
 * the specific Finite State Machine (FSM) state of the currently executing maneuver in
 * <b>Layer 4 (Procedure & Action)</b>.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class ExtendedDataActionState extends ExtendedDataString<GtuData>
{
    /** Singleton instance for convenient sampler registration. */
    public static final ExtendedDataActionState INSTANCE = new ExtendedDataActionState();

    /**
     * Constructs a new extended data type for logging ActionStates.
     */
    public ExtendedDataActionState()
    {
        super("ActionState", "Current MiRoVA ActionState");
    }

    /**
     * Retrieves the string representation of the current ActionState for a specific GTU.
     *
     * @param gtu the GTU data from the sampler
     * @return the string representation of the current ActionState, or "none" if unavailable
     */
    @Override
    public String getValue(final GtuData gtu)
    {
        if (gtu instanceof GtuDataRoad road)
        {
            LaneBasedGtu lgtu = road.getGtu();
            if (lgtu.getTacticalPlanner() instanceof MirovaTacticalPlanner p && p.getCurrentActionState() != null)
            {
                return p.getCurrentActionState().toString();
            }
        }
        return "none";
    }

    @Override
    public String toString()
    {
        return "Current MiRoVA ActionState";
    }
}