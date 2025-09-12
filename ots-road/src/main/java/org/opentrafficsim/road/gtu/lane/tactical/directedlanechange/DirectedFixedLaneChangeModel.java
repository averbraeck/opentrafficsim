package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;

/**
 * Dummy lane change model with totally predictable results (used for testing).
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class DirectedFixedLaneChangeModel implements DirectedLaneChangeModel
{

    /**
     * Constructor.
     */
    public DirectedFixedLaneChangeModel()
    {
        //
    }

    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final DirectedLaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGtu gtu,
            final LateralDirectionality direction, final Collection<PerceivedObject> sameLaneTraffic,
            final Collection<PerceivedObject> otherLaneTraffic, final Length maxDistance, final Speed speedLimit,
            final Acceleration otherLaneRouteIncentive, final Acceleration laneChangeThreshold, final Duration laneChangeTime)
            throws GtuException
    {
        GtuFollowingModelOld gtuFollowingModel =
                (GtuFollowingModelOld) ((AbstractLaneBasedTacticalPlanner) gtu.getTacticalPlanner()).getCarFollowingModel();
        if (null == direction)
        {
            return new DirectedLaneMovementStep(gtuFollowingModel
                    .computeDualAccelerationStep(gtu, sameLaneTraffic, maxDistance, speedLimit).getLeaderAccelerationStep(),
                    null);
        }
        else
        {
            return new DirectedLaneMovementStep(gtuFollowingModel
                    .computeDualAccelerationStep(gtu, otherLaneTraffic, maxDistance, speedLimit).getLeaderAccelerationStep(),
                    direction);
        }
    }

    @Override
    public final String getName()
    {
        return "Fixed lane change model";
    }

    @Override
    public final String getLongName()
    {
        return "Fixed lane change model. This model returns a lane change decision that is independent of the actual "
                + "traffic. It is used mostly for testing.";
    }

    @Override
    public final String toString()
    {
        return "DirectedFixedLaneChangeModel [name=" + getName() + "]";
    }

    @Override
    public final LanePerception getPerception()
    {
        return null;
    }
}
