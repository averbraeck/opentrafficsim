package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;

/**
 * The egoistic drive changes lane when this yields is personal advantage (totally ignoring any disadvantage to others).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class DirectedEgoistic extends AbstractDirectedLaneChangeModel
{
    /**
     * Constructor.
     * @param perception the perception to use
     */
    public DirectedEgoistic(final LanePerception perception)
    {
        super(perception);
    }

    @Override
    public final Acceleration applyDriverPersonality(final DualAccelerationStep accelerations)
    {
        // The egoistic driver only looks at the effects on him-/herself.
        return accelerations.getLeaderAcceleration();
    }

    @Override
    public final String getName()
    {
        return "Egoistic";
    }

    @Override
    public final String getLongName()
    {
        return "Egoistic lane change model (as described by Treiber).";
    }

    @Override
    public final String toString()
    {
        return "DirectedEgoistic [name=" + this.getName() + "]";
    }

}
