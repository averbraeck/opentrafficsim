package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;

/**
 * The egoistic drive changes lane when this yields is personal advantage (totally ignoring any disadvantage to others).
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class DirectedEgoistic extends AbstractDirectedLaneChangeModel
{
    /**
     * @param perception LanePerception; the perception to use
     */
    public DirectedEgoistic(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration applyDriverPersonality(final DualAccelerationStep accelerations)
    {
        // The egoistic driver only looks at the effects on him-/herself.
        return accelerations.getLeaderAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Egoistic";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Egoistic lane change model (as described by Treiber).";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectedEgoistic [name=" + this.getName() + "]";
    }

}
