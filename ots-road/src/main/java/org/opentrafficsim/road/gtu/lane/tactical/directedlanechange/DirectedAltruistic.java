package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;

/**
 * The altruistic driver changes lane when that is beneficial for all drivers.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class DirectedAltruistic extends AbstractDirectedLaneChangeModel
{
    /**
     * @param perception LanePerception; the perception to use
     */
    public DirectedAltruistic(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration applyDriverPersonality(final DualAccelerationStep accelerationSteps)
    {
        // The unit of the result is the acceleration unit of the leader acceleration.
        // Discussion. The altruistic driver personality in Treiber adds two accelerations together. This reduces the
        // "sensitivity" for keep lane, keep right and follow route incentives.
        // This implementation returns the average of the two in order to avoid this sensitivity problem.
        AccelerationUnit unit = accelerationSteps.getLeaderAcceleration().getDisplayUnit();
        return new Acceleration((accelerationSteps.getLeaderAcceleration().getInUnit()
                + accelerationSteps.getFollowerAcceleration().getInUnit(unit)) / 2, unit);
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Altruistic";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Altruistic lane change model (as described by Treiber).";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectedAltruistic [name=" + getName() + "]";
    }

}
