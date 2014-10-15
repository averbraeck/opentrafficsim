package org.opentrafficsim.car.following;

import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Determine acceleration (deceleration) for a Car that follows another car.
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
     * Compute the acceleration (or deceleration) for a Car following another Car.
     * @param follower Car; the car that is following
     * @param leader Car; the car that is leading
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @param carFollowingModel CarFollowingModel; the car following model that is used to compute the result
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (deceleration) for the following car in order to not
     *         collide with the leader car
     */
    public static DoubleScalar.Abs<AccelerationUnit> acceleration(final Car follower, final Car leader,
            final DoubleScalar.Abs<TimeUnit> when, final CarFollowingModel carFollowingModel,
            final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        if (null != leader)
        {
            MutableDoubleScalar.Rel<LengthUnit> headway =
                    MutableDoubleScalar.minus(leader.positionOfRear(when), follower.positionOfFront(when));
            if (headway.getSI() <= 0)
            { // Immediate collision; return a prohibitive negative value
                return new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2);
            }
        }
        // Wrap the leader in a set, then apply the car following model
        Set<Car> leaders = new HashSet<Car>(1);
        if (null != leader)
        {
            leaders.add(leader);
        }
        return carFollowingModel.computeAcceleration(follower, leaders, speedLimit).acceleration;
    }

}
