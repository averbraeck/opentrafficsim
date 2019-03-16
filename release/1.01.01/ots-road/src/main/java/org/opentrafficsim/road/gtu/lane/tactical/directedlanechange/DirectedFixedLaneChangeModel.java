package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;

/**
 * Dummy lane change model with totally predictable results (used for testing).
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 11 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DirectedFixedLaneChangeModel implements DirectedLaneChangeModel
{
    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final DirectedLaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
            final LateralDirectionality direction, final Collection<Headway> sameLaneTraffic,
            final Collection<Headway> otherLaneTraffic, final Length maxDistance, final Speed speedLimit,
            final Acceleration otherLaneRouteIncentive, final Acceleration laneChangeThreshold, final Duration laneChangeTime)
            throws GTUException
    {
        GTUFollowingModelOld gtuFollowingModel =
                (GTUFollowingModelOld) ((AbstractLaneBasedTacticalPlanner) gtu.getTacticalPlanner()).getCarFollowingModel();
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

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Fixed lane change model";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed lane change model. This model returns a lane change decision that is independent of the actual "
                + "traffic. It is used mostly for testing.";
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectedFixedLaneChangeModel [name=" + getName() + "]";
    }

    /** {@inheritDoc} */
    @Override
    public final LanePerception getPerception()
    {
        return null;
    }
}
