package org.opentrafficsim.road.gtu.operational;

import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.gtu.plan.operational.Segments;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.Lane;

/**
 * An operational plan with some extra information about the lanes and lane changes so this information does not have to be
 * recalculated multiple times. Furthermore, it is quite expensive to check whether a lane change is part of the oprtational
 * plan based on geographical data.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 */
public class LaneBasedOperationalPlan extends OperationalPlan
{
    /** Deviative; meaning not along lane center lines. */
    private final boolean deviative;

    /** Lanes to obtain fast center line plan information. */
    private final Map<Lane, Double> lanes;

    /**
     * Construct an operational plan with or without a lane change.
     * @param gtu the GTU for debugging purposes
     * @param path the path to follow from a certain time till a certain time. The path should have &lt;i&gt;at least&lt;/i&gt;
     *            the length
     * @param startTime the absolute start time when we start executing the path
     * @param segments the segments that make up the path with an acceleration, constant speed or deceleration profile
     * @param deviative whether the path is not along lane center lines
     * @param lanes lanes to obtain fast center line plan information
     * @throws OperationalPlanException when the path is too short for the operation
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public LaneBasedOperationalPlan(final LaneBasedGtu gtu, final OtsLine2d path, final Duration startTime,
            final Segments segments, final boolean deviative, final Map<Lane, Double> lanes) throws OperationalPlanException
    {
        super(gtu, path, startTime, segments);
        this.deviative = deviative;
        this.lanes = lanes;
    }

    /**
     * Check if we deviate from the center line.
     * @return whether this maneuver involves deviation from the center line.
     */
    public boolean isDeviative()
    {
        return this.deviative;
    }

    @Override
    public String toString()
    {
        return "LaneBasedOperationalPlan [deviative=" + this.deviative + "]";
    }

    /**
     * This method supports {@link LaneBasedGtu} to perform cheaper determinations of position in the case a path was created
     * along lane center lines. In that case the lane information is stored with the plan and used by this method.
     * @param lane lane for which to derive the position
     * @param relativePosition relative position
     * @param time absolute time
     * @return returns position on lane, if it is contained within the internal information, {@code null} otherwise
     */
    public Length getPosition(final Lane lane, final RelativePosition relativePosition, final Duration time)
    {
        Double laneDistance = this.lanes.get(lane);
        return laneDistance == null ? null
                : Length.ofSI(getTraveledDistance(time).si - laneDistance + relativePosition.dx().si);
    }
}
