package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.gtu.LaneBasedGTU;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Determine acceleration (deceleration) for a GTU that follows another GTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FollowAcceleration
{
    /**
     * This class should never be instantiated.
     */
    private FollowAcceleration()
    {
        // This class should never be instantiated
    }

    /**
     * Compute the acceleration (or deceleration) for a GTU following another GTU.
     * @param follower GTU; the GTU that is following
     * @param leader GTU; the GTU that is leading
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @param gtuFollowingModel GTUFollowingModel; the GTU following model that is used to compute the result
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (deceleration) for the following GTU in order to not
     *         collide with the leader GTU
     */
    public static DoubleScalar.Abs<AccelerationUnit> acceleration(final LaneBasedGTU<?> follower,
            final LaneBasedGTU<?> leader, final DoubleScalar.Abs<TimeUnit> when, final GTUFollowingModel gtuFollowingModel,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException
    {
        if (null != leader)
        {
            // find a lane where follower and leader are jointly
            Set<Lane> lanes = leader.getLongitudinalPositions().keySet();
            lanes.retainAll(follower.getLongitudinalPositions().keySet());
            // TODO expand to lanes for next links as well, to a certain distance (which is...?)
            if (lanes.size() > 0)
            {
                Lane lane = lanes.iterator().next();
                try
                {
                    MutableDoubleScalar.Rel<LengthUnit> headway =
                            DoubleScalar.minus(leader.positionOfRear(lane, when), follower.positionOfFront(lane, when));
                    if (headway.getSI() <= 0)
                    {
                        // Immediate collision; return a prohibitive negative value
                        return new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY,
                                AccelerationUnit.METER_PER_SECOND_2);
                    }
                }
                catch (NetworkException ne)
                {
                    // not possible -- both vehicles are on these lanes
                }
            }
        }
        // Wrap the leader in a set, then apply the GTU following model
        Set<LaneBasedGTU<?>> leaders = new HashSet<>(1);
        if (null != leader)
        {
            leaders.add(leader);
        }
        return gtuFollowingModel.computeAcceleration(follower, leaders, speedLimit).getAcceleration();
    }

}
