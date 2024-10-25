package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;

/**
 * The altruistic driver changes lane when that is beneficial for all drivers.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Altruistic extends AbstractLaneChangeModel
{
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

    @Override
    public final String getName()
    {
        return "Altruistic";
    }

    @Override
    public final String getLongName()
    {
        return "Altruistic lane change model (as described by Treiber).";
    }

    @Override
    public final String toString()
    {
        return "Altruistic []";
    }

}
