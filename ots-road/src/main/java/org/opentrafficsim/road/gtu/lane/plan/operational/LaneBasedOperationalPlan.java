package org.opentrafficsim.road.gtu.lane.plan.operational;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * An operational plan with some extra information about the lanes and lane changes so this information does not have to be
 * recalculated multiple times. Furthermore, it is quite expensive to check whether a lane change is part of the oprtational
 * plan based on geographical data.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class LaneBasedOperationalPlan extends OperationalPlan
{
    /** */
    private static final long serialVersionUID = 20160120L;

    /** Deviative; meaning not along lane center lines. */
    private final boolean deviative;

    /**
     * Construct an operational plan with or without a lane change.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at least&lt;/i&gt;
     *            the length
     * @param startTime the absolute start time when we start executing the path
     * @param segments the segments that make up the path with an acceleration, constant speed or deceleration profile
     * @param deviative whether the path is not along lane center lines
     * @throws OperationalPlanException when the path is too short for the operation
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedOperationalPlan(final LaneBasedGtu gtu, final OtsLine2d path, final Duration startTime,
            final Segments segments, final boolean deviative) throws OperationalPlanException
    {
        super(gtu, path, startTime, segments);
        this.deviative = deviative;
    }

    /**
     * Check if we deviate from the center line.
     * @return whether this maneuver involves deviation from the center line.
     */
    public final boolean isDeviative()
    {
        return this.deviative;
    }

    @Override
    public final String toString()
    {
        return "LaneBasedOperationalPlan [deviative=" + this.deviative + "]";
    }
}
