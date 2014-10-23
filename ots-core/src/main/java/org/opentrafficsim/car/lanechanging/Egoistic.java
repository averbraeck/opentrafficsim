package org.opentrafficsim.car.lanechanging;

import java.util.Set;

import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.following.FollowAcceleration;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Egoistic
{
    /**
     * This class should never be instantiated.
     */
    private Egoistic()
    {
        // This class should never be instantiated.
    }

    /**
     * Compute the acceleration of this car and the new follower car after a considered lane change. The lowers of the
     * two computed accelerations is returned. If changing lane is not possible because it would result in dangerous
     * deceleration or collision, the returned value is Double.NEGATIVE_INFINITY.
     * @param referenceCar Car; the car that considers changing lane
     * @param otherCars Set&lt;Car&gt;; the cars in the target lane
     * @param maximumDeceleration DoubleScalar.Abs&lt;AccelerationUnit&gt;; the maximum (deemed safe) deceleration. This
     *            must be a positive value, about 2 m/s/s.
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the lowest acceleration (highest deceleration) incurred if the
     *         lane change is carried out
     */
    public static DoubleScalar.Abs<AccelerationUnit> acceleration(final GTU<?> referenceCar,
            final Set<GTU<?>> otherCars, final DoubleScalar.Rel<AccelerationUnit> maximumDeceleration,
            final DoubleScalar.Abs<SpeedUnit> speedLimit)
    {
        DoubleScalar.Abs<TimeUnit> when = referenceCar.getSimulator().getSimulatorTime().get();
        GTU<?> leader = null;
        DoubleScalar.Rel<LengthUnit> leaderHeadway = null;
        GTU<?> follower = null;
        DoubleScalar.Rel<LengthUnit> followerHeadway = null;
        DoubleScalar.Abs<LengthUnit> referenceCarPosition = referenceCar.getPosition(when);
        // TODO: if otherCars are sorted in some way, scanning the entire list like this is not needed.

        // TODO: the car following model already deals with a Set of leaders; can't we do something similar with the set
        // of followers?
        for (GTU<?> c : otherCars)
        {
            DoubleScalar.Rel<LengthUnit> headway =
                    MutableDoubleScalar.minus(c.getPosition(when), referenceCarPosition).immutable();
            if (headway.getSI() < 0)
            {
                if (null == follower || followerHeadway.getSI() > headway.getSI())
                {
                    follower = c;
                    followerHeadway = headway;
                }
            }
            else
            {
                if (null == leader || leaderHeadway.getSI() < headway.getSI())
                {
                    leader = c;
                    leaderHeadway = headway;
                }
            }
        }
        GTUFollowingModel carFollowingModel = referenceCar.getGTUFollowingModel();
        DoubleScalar.Abs<AccelerationUnit> followerAcceleration =
                FollowAcceleration.acceleration(follower, referenceCar, when, carFollowingModel, speedLimit);
        if (followerAcceleration.getSI() < -maximumDeceleration.getSI())
        {
            return new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2);
        }
        DoubleScalar.Abs<AccelerationUnit> referenceAcceleration =
                FollowAcceleration.acceleration(referenceCar, leader, when, carFollowingModel, speedLimit);
        if (followerAcceleration.getSI() < referenceAcceleration.getSI())
        {
            return followerAcceleration;
        }
        return referenceAcceleration;
    }
}
